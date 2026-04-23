package cm.iusjc.userservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Event publié sur RabbitMQ quand la disponibilité d'un enseignant change.
 * Consommé par le course-service pour déclencher un recalcul incrémental.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityChangedEvent implements Serializable {

    /** CREATED | UPDATED | DELETED */
    private String changeType;

    private Long teacherId;
    private Long schoolId;

    /** Créneau concerné (peut être null si suppression globale) */
    private String dayOfWeek;
    private String startTime;
    private String endTime;

    private LocalDateTime changedAt;
}
