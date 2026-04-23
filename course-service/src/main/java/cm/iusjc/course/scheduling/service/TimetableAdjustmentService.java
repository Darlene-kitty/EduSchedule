package cm.iusjc.course.scheduling.service;

import cm.iusjc.course.dto.CourseDTO;
import cm.iusjc.course.scheduling.config.RabbitMQConfig;
import cm.iusjc.course.scheduling.entity.GeneratedSchedule;
import cm.iusjc.course.scheduling.event.AvailabilityChangedEvent;
import cm.iusjc.course.scheduling.repository.GeneratedScheduleRepository;
import cm.iusjc.course.service.CourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Gère l'ajustement incrémental du planning quand une disponibilité change.
 *
 * Stratégie :
 *  1. Écoute les événements RabbitMQ "availability.changed"
 *  2. Identifie les créneaux du planning qui violent la nouvelle contrainte
 *  3. Tente une réassignation sur un créneau libre compatible
 *  4. Si impossible → relaxation des contraintes (RELAXED) avec notification
 *  5. Publie un événement "schedule.changed" pour la sync calendrier
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TimetableAdjustmentService {

    private final GeneratedScheduleRepository scheduleRepo;
    private final TeacherAvailabilityClient availabilityClient;
    private final CourseService courseService;
    private final RabbitTemplate rabbitTemplate;
    private final RestTemplate restTemplate;

    /** URL du notification-service (configurable via application.properties) */
    @Value("${app.notification-service.url:http://localhost:8087}")
    private String notificationServiceUrl;

    // ─────────────────────────────────────────────────────────────────────────
    // 1. ÉCOUTE RABBITMQ — déclenchement automatique
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Point d'entrée automatique : reçoit un événement de changement de disponibilité
     * publié par le user-service quand un enseignant modifie ses disponibilités.
     */
    @RabbitListener(queues = RabbitMQConfig.AVAILABILITY_QUEUE)
    public void onAvailabilityChanged(AvailabilityChangedEvent event) {
        log.info("Received availability change event: teacher={} type={} day={} {}-{}",
                event.getTeacherId(), event.getChangeType(),
                event.getDayOfWeek(), event.getStartTime(), event.getEndTime());
        try {
            AdjustmentReport report = processAvailabilityChange(event);
            log.info("Adjustment complete: {} conflicts found, {} reassigned, {} relaxed",
                    report.conflictsFound, report.reassigned, report.relaxed);
            // Publier le rapport pour notification
            if (report.conflictsFound > 0) {
                publishScheduleChangedEvent(event.getTeacherId(), report);
                // ── NOUVEAU : alertes push WebSocket + email via notification-service ──
                sendConflictAlerts(event.getTeacherId(), report);
            }
        } catch (Exception e) {
            log.error("Failed to process availability change for teacher {}: {}",
                    event.getTeacherId(), e.getMessage(), e);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. TRAITEMENT PRINCIPAL
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public AdjustmentReport processAvailabilityChange(AvailabilityChangedEvent event) {
        AdjustmentReport report = new AdjustmentReport(event.getTeacherId());

        // Récupérer tous les créneaux actifs de cet enseignant
        List<GeneratedSchedule> teacherSlots = scheduleRepo.findByTeacherId(event.getTeacherId())
                .stream()
                .filter(s -> "ACTIVE".equals(s.getStatus()) || "RELAXED".equals(s.getStatus()))
                .collect(Collectors.toList());

        if (teacherSlots.isEmpty()) {
            log.debug("No active slots for teacher {}", event.getTeacherId());
            return report;
        }

        // Recharger les disponibilités à jour
        Set<String> allowedKeys = availabilityClient.getAllowedSlotKeys(event.getTeacherId());

        // Identifier les créneaux qui violent maintenant les disponibilités
        List<GeneratedSchedule> violated = teacherSlots.stream()
                .filter(slot -> !isSlotStillValid(slot, allowedKeys))
                .collect(Collectors.toList());

        report.conflictsFound = violated.size();
        log.info("Teacher {}: {} slots now violate updated availability", event.getTeacherId(), violated.size());

        for (GeneratedSchedule slot : violated) {
            boolean reassigned = tryReassign(slot, allowedKeys, teacherSlots);
            if (reassigned) {
                report.reassigned++;
                report.reassignedSlots.add(slot.getCourseCode() + " → nouveau créneau");
            } else {
                // Relaxation : on maintient le créneau mais on le marque RELAXED
                applyRelaxation(slot, "Disponibilité enseignant modifiée — créneau maintenu par relaxation");
                report.relaxed++;
                report.relaxedSlots.add(slot.getCourseCode() + " " + slot.getDayOfWeek() + " " + slot.getStartTime());
            }
        }

        return report;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. RÉASSIGNATION INCRÉMENTALE
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Tente de déplacer un créneau vers un slot compatible avec les nouvelles disponibilités.
     * Cherche un créneau libre (pas de conflit salle, pas de conflit enseignant).
     */
    private boolean tryReassign(GeneratedSchedule slot, Set<String> allowedKeys,
                                 List<GeneratedSchedule> existingSlots) {
        List<String> candidates = buildCandidateSlots(allowedKeys);

        // Créneaux déjà occupés par cet enseignant ou cette salle
        Set<String> occupiedByTeacher = existingSlots.stream()
                .filter(s -> !s.getId().equals(slot.getId()))
                .map(s -> s.getDayOfWeek() + "_" + s.getStartTime())
                .collect(Collectors.toSet());

        Set<String> occupiedByRoom = scheduleRepo
                .findBySchoolIdAndSemesterAndLevel(slot.getSchoolId(), slot.getSemester(), slot.getLevel())
                .stream()
                .filter(s -> slot.getRoomId().equals(s.getRoomId()) && !s.getId().equals(slot.getId()))
                .map(s -> s.getDayOfWeek() + "_" + s.getStartTime())
                .collect(Collectors.toSet());

        for (String candidate : candidates) {
            String[] parts = candidate.split("_");
            if (parts.length < 3) continue;
            String day = parts[0], start = parts[1];
            String key = day + "_" + start;

            if (!occupiedByTeacher.contains(key) && !occupiedByRoom.contains(key)) {
                // Créneau libre trouvé — réassigner
                String oldSlot = slot.getDayOfWeek() + " " + slot.getStartTime();
                slot.setDayOfWeek(day);
                slot.setStartTime(start);
                slot.setEndTime(parts[2]);
                slot.setStatus("ACTIVE");
                slot.setConflictReason(null);
                scheduleRepo.save(slot);
                log.info("Reassigned slot {} (course={}) from {} to {} {}",
                        slot.getId(), slot.getCourseCode(), oldSlot, day, start);
                return true;
            }
        }
        return false;
    }

    /**
     * Construit la liste des créneaux autorisés par les nouvelles disponibilités.
     * Si allowedKeys est vide (pas de contrainte), retourne les créneaux par défaut.
     */
    private List<String> buildCandidateSlots(Set<String> allowedKeys) {
        if (allowedKeys.isEmpty()) {
            return defaultSlots();
        }
        // Dériver les créneaux de 2h à partir des plages de disponibilité
        List<String> candidates = new ArrayList<>();
        String[] slotTimes = {"08:00_10:00", "10:00_12:00", "14:00_16:00", "16:00_18:00"};
        String[] days = {"LUNDI", "MARDI", "MERCREDI", "JEUDI", "VENDREDI"};
        for (String day : days) {
            for (String time : slotTimes) {
                String[] t = time.split("_");
                if (availabilityClient.isSlotAllowed(allowedKeys, day, t[0], t[1])) {
                    candidates.add(day + "_" + t[0] + "_" + t[1]);
                }
            }
        }
        return candidates;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. RELAXATION DES CONTRAINTES
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Relaxation : quand aucun créneau alternatif n'est disponible,
     * on maintient le créneau existant mais on le marque RELAXED avec la raison.
     * Un admin peut ensuite décider manuellement.
     */
    @Transactional
    public void applyRelaxation(GeneratedSchedule slot, String reason) {
        slot.setStatus("RELAXED");
        slot.setConflictReason(reason);
        scheduleRepo.save(slot);
        log.warn("Relaxation applied to slot {} (course={} {} {}): {}",
                slot.getId(), slot.getCourseCode(), slot.getDayOfWeek(), slot.getStartTime(), reason);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 5. SYNCHRONISATION LIVE — vérification des conflits avec les réservations
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Vérifie la cohérence entre les créneaux générés et les réservations réelles.
     * Appelable manuellement via l'API ou périodiquement.
     * Retourne la liste des créneaux en conflit avec une réservation existante.
     */
    @Transactional
    public List<ConflictInfo> syncWithReservations(Long schoolId, String semester, String level) {
        List<GeneratedSchedule> slots = scheduleRepo
                .findActiveBySchoolSemesterLevel(schoolId, semester, level);

        List<ConflictInfo> conflicts = new ArrayList<>();

        // Détecter les conflits salle : même salle, même jour, même créneau
        Map<String, List<GeneratedSchedule>> byRoomSlot = slots.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getRoomId() + "_" + s.getDayOfWeek() + "_" + s.getStartTime()));

        for (Map.Entry<String, List<GeneratedSchedule>> entry : byRoomSlot.entrySet()) {
            if (entry.getValue().size() > 1) {
                conflicts.add(new ConflictInfo(
                        "ROOM_CONFLICT",
                        "Salle " + entry.getValue().get(0).getRoomName() + " réservée plusieurs fois sur " + entry.getKey(),
                        entry.getValue().stream().map(GeneratedSchedule::getId).collect(Collectors.toList())
                ));
            }
        }

        // Détecter les conflits enseignant : même enseignant, même créneau
        Map<String, List<GeneratedSchedule>> byTeacherSlot = slots.stream()
                .filter(s -> s.getTeacherId() != null)
                .collect(Collectors.groupingBy(
                        s -> s.getTeacherId() + "_" + s.getDayOfWeek() + "_" + s.getStartTime()));

        for (Map.Entry<String, List<GeneratedSchedule>> entry : byTeacherSlot.entrySet()) {
            if (entry.getValue().size() > 1) {
                conflicts.add(new ConflictInfo(
                        "TEACHER_CONFLICT",
                        "Enseignant assigné à plusieurs cours sur " + entry.getKey(),
                        entry.getValue().stream().map(GeneratedSchedule::getId).collect(Collectors.toList())
                ));
            }
        }

        log.info("Sync check for school={} {}/{}: {} conflicts found", schoolId, semester, level, conflicts.size());
        return conflicts;
    }

    /**
     * Résout manuellement un conflit en appliquant le créneau alternatif choisi.
     * Met à jour le GeneratedSchedule avec le nouveau jour/heure et repasse en ACTIVE.
     *
     * @param slotId  ID du créneau en conflit
     * @param slotKey clé du créneau choisi, ex: "LUNDI_08:00"
     */
    @Transactional
    public void resolveConflict(Long slotId, String slotKey) {
        GeneratedSchedule slot = scheduleRepo.findById(slotId)
                .orElseThrow(() -> new IllegalArgumentException("Créneau introuvable : " + slotId));

        String[] parts = slotKey.split("_");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Format slotKey invalide : " + slotKey);
        }
        String day   = parts[0];
        String start = parts[1];
        // Calculer l'heure de fin (+2h par défaut)
        String end   = computeEndTime(start, 2);

        slot.setDayOfWeek(day);
        slot.setStartTime(start);
        slot.setEndTime(end);
        slot.setStatus("ACTIVE");
        slot.setConflictReason(null);
        scheduleRepo.save(slot);

        log.info("Conflict resolved for slot {} (course={}): moved to {} {}–{}",
                slotId, slot.getCourseCode(), day, start, end);
    }

    private String computeEndTime(String startTime, int durationHours) {
        try {
            String[] parts = startTime.split(":");
            int hour = Integer.parseInt(parts[0]) + durationHours;
            int min  = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
            return String.format("%02d:%02d", hour, min);
        } catch (Exception e) {
            return startTime; // fallback
        }
    }

    /**
     * Retourne tous les créneaux en état CONFLICT ou RELAXED pour un enseignant.
     * Utilisé par le frontend pour afficher les alertes.
     */
    public List<GeneratedSchedule> getPendingAdjustments(Long teacherId) {
        List<GeneratedSchedule> conflicts = scheduleRepo.findByTeacherIdAndStatus(teacherId, "CONFLICT");
        List<GeneratedSchedule> relaxed   = scheduleRepo.findByTeacherIdAndStatus(teacherId, "RELAXED");
        List<GeneratedSchedule> all = new ArrayList<>(conflicts);
        all.addAll(relaxed);
        return all;
    }

    /**
     * Retourne tous les créneaux nécessitant une attention (toutes écoles).
     */
    public List<GeneratedSchedule> getAllPendingAdjustments() {
        List<GeneratedSchedule> conflicts = scheduleRepo.findByStatus("CONFLICT");
        List<GeneratedSchedule> relaxed   = scheduleRepo.findByStatus("RELAXED");
        List<GeneratedSchedule> all = new ArrayList<>(conflicts);
        all.addAll(relaxed);
        return all;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 5b. ALERTES CONFLIT — WebSocket + Email via notification-service
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Envoie une alerte de conflit via le notification-service :
     *  - WebSocket push sur /topic/notifications (broadcastConflict)
     *  - Email de notification de changement d'emploi du temps
     *
     * Appel HTTP vers POST /api/notifications/advanced/schedule-change
     * (le notification-service gère ensuite WebSocket + email en interne).
     */
    private void sendConflictAlerts(Long teacherId, AdjustmentReport report) {
        try {
            // ── 1. Alerte WebSocket via notification-service ──────────────────
            String conflictType = report.relaxed > 0
                    ? "INTER_SCHOOL_CONFLICT_RELAXED"
                    : "INTER_SCHOOL_CONFLICT_REASSIGNED";

            String description = String.format(
                    "Enseignant %d : %d conflit(s) détecté(s) suite à un changement de disponibilité. " +
                    "%d créneau(x) réassigné(s), %d maintenu(s) par relaxation.",
                    teacherId, report.conflictsFound, report.reassigned, report.relaxed);

            Map<String, Object> wsPayload = new HashMap<>();
            wsPayload.put("conflictType", conflictType);
            wsPayload.put("description", description);
            wsPayload.put("teacherId", teacherId);
            wsPayload.put("conflictsFound", report.conflictsFound);
            wsPayload.put("reassigned", report.reassigned);
            wsPayload.put("relaxed", report.relaxed);
            wsPayload.put("relaxedSlots", report.relaxedSlots);
            wsPayload.put("timestamp", LocalDateTime.now().toString());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Appel au notification-service pour broadcast WebSocket
            String wsUrl = notificationServiceUrl + "/api/notifications/advanced/conflict-alert";
            try {
                restTemplate.postForObject(wsUrl, new HttpEntity<>(wsPayload, headers), Map.class);
                log.info("Conflict alert sent via WebSocket for teacher {}", teacherId);
            } catch (Exception wsEx) {
                log.warn("Could not send WebSocket conflict alert: {}", wsEx.getMessage());
            }

            // ── 2. Email d'alerte via notification-service ────────────────────
            // Construit un ScheduleChangeEvent pour déclencher l'envoi email
            Map<String, Object> emailPayload = new HashMap<>();
            emailPayload.put("scheduleId", teacherId); // utilisé comme référence
            emailPayload.put("eventType", conflictType);
            emailPayload.put("changeType", conflictType);
            emailPayload.put("eventTimestamp", LocalDateTime.now().toString());
            emailPayload.put("description", description);
            // Créneaux relaxés → listés dans le corps de l'email
            if (!report.relaxedSlots.isEmpty()) {
                emailPayload.put("relaxedSlots", String.join(", ", report.relaxedSlots));
            }

            String emailUrl = notificationServiceUrl + "/api/notifications/advanced/schedule-change";
            try {
                restTemplate.postForObject(emailUrl, new HttpEntity<>(emailPayload, headers), Map.class);
                log.info("Conflict email alert sent for teacher {}", teacherId);
            } catch (Exception emailEx) {
                log.warn("Could not send email conflict alert: {}", emailEx.getMessage());
            }

        } catch (Exception e) {
            log.error("Failed to send conflict alerts for teacher {}: {}", teacherId, e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 5c. SUGGESTIONS ALTERNATIVES — API publique pour le frontend
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Calcule des suggestions de créneaux alternatifs pour un créneau en conflit.
     * Retourne jusqu'à 5 alternatives triées par score de compatibilité.
     *
     * @param slotId     ID du créneau GeneratedSchedule en conflit
     * @return liste de suggestions avec label, score et disponibilité
     */
    public List<AlternativeSuggestion> getAlternativeSuggestions(Long slotId) {
        GeneratedSchedule slot = scheduleRepo.findById(slotId).orElse(null);
        if (slot == null) return List.of();

        Set<String> allowedKeys = availabilityClient.getAllowedSlotKeys(slot.getTeacherId());

        // Créneaux déjà occupés par cet enseignant
        Set<String> occupiedByTeacher = scheduleRepo.findByTeacherId(slot.getTeacherId())
                .stream()
                .filter(s -> !s.getId().equals(slotId) && !"RELAXED".equals(s.getStatus()))
                .map(s -> s.getDayOfWeek() + "_" + s.getStartTime())
                .collect(Collectors.toSet());

        // Créneaux déjà occupés par cette salle
        Set<String> occupiedByRoom = scheduleRepo
                .findBySchoolIdAndSemesterAndLevel(slot.getSchoolId(), slot.getSemester(), slot.getLevel())
                .stream()
                .filter(s -> slot.getRoomId() != null && slot.getRoomId().equals(s.getRoomId())
                        && !s.getId().equals(slotId))
                .map(s -> s.getDayOfWeek() + "_" + s.getStartTime())
                .collect(Collectors.toSet());

        String[] days  = {"LUNDI", "MARDI", "MERCREDI", "JEUDI", "VENDREDI"};
        String[][] times = {{"08:00", "10:00"}, {"10:00", "12:00"}, {"14:00", "16:00"}, {"16:00", "18:00"}};

        List<AlternativeSuggestion> suggestions = new ArrayList<>();

        for (String day : days) {
            for (String[] time : times) {
                String start = time[0], end = time[1];
                String key = day + "_" + start;

                // Ignorer le créneau actuel
                if (day.equals(slot.getDayOfWeek()) && start.equals(slot.getStartTime())) continue;

                boolean teacherFree = !occupiedByTeacher.contains(key);
                boolean roomFree    = !occupiedByRoom.contains(key);
                boolean teacherAvail = availabilityClient.isSlotAllowed(allowedKeys, day, start, end);

                if (!teacherFree) continue; // enseignant déjà occupé → pas une alternative valide

                // Score : 100 si tout est libre + dispo, -10 si salle occupée, -20 si hors dispo
                int score = 100;
                if (!roomFree)    score -= 15;
                if (!teacherAvail && !allowedKeys.isEmpty()) score -= 25;

                String dayFr = switch (day) {
                    case "LUNDI"    -> "Lundi";
                    case "MARDI"    -> "Mardi";
                    case "MERCREDI" -> "Mercredi";
                    case "JEUDI"    -> "Jeudi";
                    case "VENDREDI" -> "Vendredi";
                    default         -> day;
                };

                String label = String.format("%s %s–%s — %s%s",
                        dayFr, start, end,
                        slot.getRoomName() != null ? slot.getRoomName() : "Salle " + slot.getRoomId(),
                        roomFree ? " (disponible)" : " (salle occupée — autre salle requise)");

                suggestions.add(new AlternativeSuggestion(key, label, score, teacherAvail && roomFree));
            }
        }

        // Trier par score décroissant, limiter à 5
        suggestions.sort(Comparator.comparingInt(AlternativeSuggestion::getScore).reversed());
        return suggestions.stream().limit(5).collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 6. PUBLICATION D'ÉVÉNEMENTS
    // ─────────────────────────────────────────────────────────────────────────

    private void publishScheduleChangedEvent(Long teacherId, AdjustmentReport report) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("teacherId", teacherId);
            event.put("conflictsFound", report.conflictsFound);
            event.put("reassigned", report.reassigned);
            event.put("relaxed", report.relaxed);
            event.put("relaxedSlots", report.relaxedSlots);
            event.put("changedAt", LocalDateTime.now().toString());
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.SCHEDULE_EXCHANGE,
                    RabbitMQConfig.SCHEDULE_CHANGE_ROUTING_KEY,
                    event);
            log.info("Published schedule.changed event for teacher {}", teacherId);
        } catch (Exception e) {
            log.warn("Failed to publish schedule.changed event: {}", e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 7. HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    private boolean isSlotStillValid(GeneratedSchedule slot, Set<String> allowedKeys) {
        if (allowedKeys.isEmpty()) return true; // pas de contrainte = tout autorisé
        return availabilityClient.isSlotAllowed(allowedKeys, slot.getDayOfWeek(),
                slot.getStartTime(), slot.getEndTime());
    }

    private List<String> defaultSlots() {
        String[] days  = {"LUNDI", "MARDI", "MERCREDI", "JEUDI", "VENDREDI"};
        String[] times = {"08:00_10:00", "10:00_12:00", "14:00_16:00", "16:00_18:00"};
        List<String> slots = new ArrayList<>();
        for (String day : days)
            for (String time : times)
                slots.add(day + "_" + time);
        return slots;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 8. DTOs INTERNES
    // ─────────────────────────────────────────────────────────────────────────

    public static class AdjustmentReport {
        public final Long teacherId;
        public int conflictsFound = 0;
        public int reassigned     = 0;
        public int relaxed        = 0;
        public final List<String> reassignedSlots = new ArrayList<>();
        public final List<String> relaxedSlots    = new ArrayList<>();

        public AdjustmentReport(Long teacherId) { this.teacherId = teacherId; }
    }

    public static class ConflictInfo {
        public final String type;
        public final String description;
        public final List<Long> affectedSlotIds;

        public ConflictInfo(String type, String description, List<Long> affectedSlotIds) {
            this.type = type;
            this.description = description;
            this.affectedSlotIds = affectedSlotIds;
        }
    }

    /** Suggestion de créneau alternatif pour résoudre un conflit. */
    public static class AlternativeSuggestion {
        private final String slotKey;   // ex: "LUNDI_08:00"
        private final String label;     // texte affiché dans le frontend
        private final int score;        // 0–100
        private final boolean fullyAvailable;

        public AlternativeSuggestion(String slotKey, String label, int score, boolean fullyAvailable) {
            this.slotKey = slotKey;
            this.label = label;
            this.score = score;
            this.fullyAvailable = fullyAvailable;
        }

        public String getSlotKey()         { return slotKey; }
        public String getLabel()           { return label; }
        public int getScore()              { return score; }
        public boolean isFullyAvailable()  { return fullyAvailable; }
    }
}
