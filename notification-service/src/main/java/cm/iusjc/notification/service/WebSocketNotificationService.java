package cm.iusjc.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Pousse les événements en temps réel vers les clients connectés via STOMP/WebSocket.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    /** Diffuse une notification à tous les abonnés du topic global. */
    public void broadcastNotification(String type, String title, String message, Object data) {
        Map<String, Object> payload = buildPayload(type, title, message, data);
        messagingTemplate.convertAndSend("/topic/notifications", payload);
        log.debug("WS broadcast [{}]: {}", type, title);
    }

    /** Envoie une notification à un utilisateur spécifique. */
    public void sendToUser(Long userId, String type, String title, String message, Object data) {
        Map<String, Object> payload = buildPayload(type, title, message, data);
        messagingTemplate.convertAndSend("/topic/notifications/" + userId, payload);
        log.debug("WS → user {} [{}]: {}", userId, type, title);
    }

    /** Diffuse un événement de réservation (création, approbation, rejet, annulation). */
    public void broadcastReservationEvent(String event, Long reservationId, String title,
                                           Long userId, String status) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("event",         event);
        payload.put("reservationId", reservationId);
        payload.put("title",         title);
        payload.put("status",        status);
        payload.put("timestamp",     LocalDateTime.now().toString());

        messagingTemplate.convertAndSend("/topic/reservations", payload);
        if (userId != null) {
            messagingTemplate.convertAndSend("/topic/notifications/" + userId, buildPayload(
                    "RESERVATION_" + event.toUpperCase(), title,
                    "Réservation " + status.toLowerCase(), payload));
        }
        log.debug("WS reservation event [{}] id={}", event, reservationId);
    }

    /** Diffuse un changement d'emploi du temps. */
    public void broadcastScheduleChange(String eventType, Long scheduleId, String courseName,
                                         String room, String changeDescription) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("eventType",         eventType);
        payload.put("scheduleId",        scheduleId);
        payload.put("courseName",        courseName);
        payload.put("room",              room);
        payload.put("changeDescription", changeDescription);
        payload.put("timestamp",         LocalDateTime.now().toString());

        messagingTemplate.convertAndSend("/topic/schedule", payload);
        log.debug("WS schedule change [{}] course={}", eventType, courseName);
    }

    /** Diffuse une alerte de conflit. */
    public void broadcastConflict(String conflictType, String description, Object details) {
        Map<String, Object> payload = buildPayload("CONFLICT", conflictType, description, details);
        messagingTemplate.convertAndSend("/topic/notifications", payload);
        log.debug("WS conflict alert: {}", conflictType);
    }

    // ── private ───────────────────────────────────────────────────────────────

    private Map<String, Object> buildPayload(String type, String title, String message, Object data) {
        Map<String, Object> p = new HashMap<>();
        p.put("type",      type);
        p.put("title",     title);
        p.put("message",   message);
        p.put("data",      data);
        p.put("timestamp", LocalDateTime.now().toString());
        return p;
    }
}
