package cm.iusjc.course.scheduling.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;

/**
 * Client REST vers le user-service pour récupérer les disponibilités des enseignants.
 * Endpoint : GET /api/teacher-availability/teacher/{teacherId}
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TeacherAvailabilityClient {

    private final RestTemplate restTemplate;

    @Value("${app.user-service.url:http://localhost:8096}")
    private String userServiceUrl;

    // Types de disponibilité qui autorisent un cours
    private static final Set<String> ALLOWED_TYPES = Set.of("AVAILABLE", "PREFERRED");

    // Mapping jour interne (français) → DayOfWeek Java (anglais)
    private static final Map<String, DayOfWeek> DAY_MAP = Map.of(
            "LUNDI",    DayOfWeek.MONDAY,
            "MARDI",    DayOfWeek.TUESDAY,
            "MERCREDI", DayOfWeek.WEDNESDAY,
            "JEUDI",    DayOfWeek.THURSDAY,
            "VENDREDI", DayOfWeek.FRIDAY,
            "SAMEDI",   DayOfWeek.SATURDAY,
            "DIMANCHE", DayOfWeek.SUNDAY
    );

    /**
     * Construit un Set de clés "LUNDI_08:00" représentant les créneaux autorisés
     * pour un enseignant donné. Si aucune disponibilité n'est enregistrée, retourne
     * un Set vide (le caller doit alors autoriser par défaut).
     */
    public Set<String> getAllowedSlotKeys(Long teacherId) {
        try {
            String url = userServiceUrl + "/api/teacher-availability/teacher/" + teacherId;
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null,
                    new ParameterizedTypeReference<>() {}
            );

            List<Map<String, Object>> availabilities = response.getBody();
            if (availabilities == null || availabilities.isEmpty()) {
                log.debug("No availability records for teacher {}, allowing all slots", teacherId);
                return Collections.emptySet(); // fallback : tout autorisé
            }

            Set<String> allowed = new HashSet<>();
            for (Map<String, Object> avail : availabilities) {
                Boolean active = (Boolean) avail.get("active");
                if (Boolean.FALSE.equals(active)) continue;

                String type = (String) avail.get("availabilityType");
                if (!ALLOWED_TYPES.contains(type)) continue;

                String dayStr   = (String) avail.get("dayOfWeek");   // ex: "MONDAY"
                String startStr = (String) avail.get("startTime");   // ex: "08:00:00" ou "08:00"
                String endStr   = (String) avail.get("endTime");

                if (dayStr == null || startStr == null || endStr == null) continue;

                // Convertir DayOfWeek anglais → clé française
                String frDay = toFrenchDay(dayStr);
                if (frDay == null) continue;

                LocalTime availStart = parseTime(startStr);
                LocalTime availEnd   = parseTime(endStr);

                // Pour chaque créneau interne qui tombe dans cette plage, on l'autorise
                // On stocke la clé "LUNDI_08:00" (début du créneau)
                allowed.add(frDay + "_" + formatTime(availStart) + "_" + formatTime(availEnd));
            }

            log.debug("Teacher {} allowed slot keys: {}", teacherId, allowed);
            return allowed;

        } catch (Exception e) {
            log.warn("Could not fetch availability for teacher {}: {} — allowing all slots", teacherId, e.getMessage());
            return Collections.emptySet(); // fallback : tout autorisé
        }
    }

    /**
     * Vérifie si un enseignant est disponible pour un créneau donné.
     * @param allowedKeys  résultat de getAllowedSlotKeys() — vide = tout autorisé
     * @param frDay        ex: "LUNDI"
     * @param startTime    ex: "08:00"
     * @param endTime      ex: "10:00"
     */
    public boolean isSlotAllowed(Set<String> allowedKeys, String frDay, String startTime, String endTime) {
        if (allowedKeys.isEmpty()) return true; // pas de contrainte enregistrée

        LocalTime slotStart = parseTime(startTime);
        LocalTime slotEnd   = parseTime(endTime);

        // Cherche une plage de disponibilité qui couvre entièrement le créneau
        for (String key : allowedKeys) {
            String[] parts = key.split("_");
            if (parts.length < 3) continue;
            if (!parts[0].equals(frDay)) continue;

            LocalTime availStart = parseTime(parts[1]);
            LocalTime availEnd   = parseTime(parts[2]);

            // Le créneau est couvert si availStart <= slotStart && slotEnd <= availEnd
            if (!availStart.isAfter(slotStart) && !availEnd.isBefore(slotEnd)) {
                return true;
            }
        }
        return false;
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private String toFrenchDay(String englishDay) {
        return switch (englishDay.toUpperCase()) {
            case "MONDAY"    -> "LUNDI";
            case "TUESDAY"   -> "MARDI";
            case "WEDNESDAY" -> "MERCREDI";
            case "THURSDAY"  -> "JEUDI";
            case "FRIDAY"    -> "VENDREDI";
            case "SATURDAY"  -> "SAMEDI";
            case "SUNDAY"    -> "DIMANCHE";
            default          -> null;
        };
    }

    private LocalTime parseTime(String t) {
        if (t == null) return LocalTime.MIDNIGHT;
        // Supporte "08:00", "08:00:00", "8:00"
        String[] parts = t.split(":");
        int h = Integer.parseInt(parts[0]);
        int m = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
        return LocalTime.of(h, m);
    }

    private String formatTime(LocalTime t) {
        return String.format("%02d:%02d", t.getHour(), t.getMinute());
    }
}
