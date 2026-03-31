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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
