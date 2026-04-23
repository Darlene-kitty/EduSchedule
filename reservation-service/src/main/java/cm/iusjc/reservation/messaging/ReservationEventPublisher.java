package cm.iusjc.reservation.messaging;

import cm.iusjc.reservation.entity.Reservation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Publie les événements de réservation vers RabbitMQ.
 * Le notification-service écoute sur "reservation-notifications" (routing key reservation.#).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationEventPublisher {

    private static final String EXCHANGE    = "notification-exchange";
    private static final String KEY_CREATED  = "reservation.created";
    private static final String KEY_APPROVED = "reservation.approved";
    private static final String KEY_REJECTED = "reservation.rejected";
    private static final String KEY_CANCELLED = "reservation.cancelled";
    private static final String KEY_UPDATED  = "reservation.updated";

    private final RabbitTemplate rabbitTemplate;

    public void publishCreated(Reservation r) {
        publish(KEY_CREATED, buildPayload(r, "reservation.created"));
    }

    public void publishApproved(Reservation r) {
        publish(KEY_APPROVED, buildPayload(r, "reservation.approved"));
    }

    public void publishRejected(Reservation r, String reason) {
        Map<String, Object> payload = buildPayload(r, "reservation.rejected");
        payload.put("reason", reason);
        publish(KEY_REJECTED, payload);
    }

    public void publishCancelled(Reservation r, String reason) {
        Map<String, Object> payload = buildPayload(r, "reservation.cancelled");
        payload.put("reason", reason);
        publish(KEY_CANCELLED, payload);
    }

    public void publishUpdated(Reservation r) {
        publish(KEY_UPDATED, buildPayload(r, "reservation.updated"));
    }

    // ── private ──────────────────────────────────────────────────────────────

    private Map<String, Object> buildPayload(Reservation r, String event) {
        Map<String, Object> m = new HashMap<>();
        m.put("event",         event);
        m.put("reservationId", r.getId());
        m.put("userId",        r.getUserId());
        m.put("resourceId",    r.getResourceId());
        m.put("title",         r.getTitle());
        m.put("startTime",     r.getStartTime() != null ? r.getStartTime().toString() : null);
        m.put("endTime",       r.getEndTime()   != null ? r.getEndTime().toString()   : null);
        m.put("status",        r.getStatus()    != null ? r.getStatus().toString()    : null);
        m.put("type",          r.getType()      != null ? r.getType().toString()      : null);
        m.put("notificationChannels", java.util.List.of("EMAIL", "APP"));
        return m;
    }

    private void publish(String routingKey, Map<String, Object> payload) {
        try {
            rabbitTemplate.convertAndSend(EXCHANGE, routingKey, payload);
            log.info("Published reservation event [{}] for reservation {}",
                    routingKey, payload.get("reservationId"));
        } catch (Exception e) {
            log.error("Failed to publish reservation event [{}]: {}", routingKey, e.getMessage());
        }
    }
}
