package cm.iusjc.course.scheduling.service;

import cm.iusjc.course.scheduling.entity.GeneratedSchedule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Client REST vers le calendar-service.
 * Exporte chaque créneau validé comme CalendarEvent via POST /api/calendar/events/export.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CalendarServiceClient {

    private final RestTemplate restTemplate;

    @Value("${app.calendar-service.url:http://calendar-service:8089}")
    private String calendarServiceUrl;

    /**
     * Exporte une liste de créneaux vers le calendar-service.
     * Chaque créneau devient un CalendarEvent avec récurrence hebdomadaire.
     *
     * @param slots    créneaux à exporter
     * @param userId   ID de l'utilisateur qui valide (pour l'intégration calendrier)
     * @param weekStart date de début de la semaine de référence
     */
    public void exportSlots(List<GeneratedSchedule> slots, String userId, LocalDate weekStart) {
        String url = calendarServiceUrl + "/api/calendar/events/export";

        for (GeneratedSchedule slot : slots) {
            try {
                Map<String, Object> payload = buildEventPayload(slot, userId, weekStart);
                restTemplate.postForObject(url, payload, String.class);
                log.debug("Exported slot {} {} {} to calendar-service", slot.getCourseCode(), slot.getDayOfWeek(), slot.getStartTime());
            } catch (Exception e) {
                // On log mais on ne bloque pas la validation si la sync échoue
                log.warn("Failed to export slot {} to calendar-service: {}", slot.getId(), e.getMessage());
            }
        }
    }

    /**
     * Déclenche la sync complète des calendriers d'un utilisateur.
     */
    public void triggerSync(String userId) {
        try {
            String url = calendarServiceUrl + "/api/calendar/sync/" + userId;
            restTemplate.postForObject(url, null, String.class);
            log.info("Calendar sync triggered for user {}", userId);
        } catch (Exception e) {
            log.warn("Failed to trigger calendar sync for user {}: {}", userId, e.getMessage());
        }
    }

    private Map<String, Object> buildEventPayload(GeneratedSchedule slot, String userId, LocalDate weekStart) {
        // Calcule la date réelle du créneau à partir du jour de la semaine
        LocalDate slotDate = resolveDate(weekStart, slot.getDayOfWeek());
        LocalDateTime start = LocalDateTime.of(slotDate, parseTime(slot.getStartTime()));
        LocalDateTime end   = LocalDateTime.of(slotDate, parseTime(slot.getEndTime()));

        Map<String, Object> payload = new HashMap<>();
        payload.put("userId",      userId);
        payload.put("title",       slot.getCourseCode() + " — " + slot.getCourseName());
        payload.put("description", "Niveau: " + slot.getLevel() + " | Semestre: " + slot.getSemester());
        payload.put("location",    slot.getRoomName());
        payload.put("startTime",   start.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        payload.put("endTime",     end.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        payload.put("isAllDay",    false);
        // Récurrence hebdomadaire (RRULE iCal)
        payload.put("recurrenceRule", "FREQ=WEEKLY;COUNT=16");
        return payload;
    }

    private LocalDate resolveDate(LocalDate weekStart, String dayOfWeek) {
        return switch (dayOfWeek.toUpperCase()) {
            case "LUNDI"    -> weekStart;
            case "MARDI"    -> weekStart.plusDays(1);
            case "MERCREDI" -> weekStart.plusDays(2);
            case "JEUDI"    -> weekStart.plusDays(3);
            case "VENDREDI" -> weekStart.plusDays(4);
            default         -> weekStart;
        };
    }

    private java.time.LocalTime parseTime(String time) {
        return java.time.LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"));
    }
}
