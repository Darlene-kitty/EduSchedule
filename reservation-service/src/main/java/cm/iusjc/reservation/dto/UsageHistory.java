package cm.iusjc.reservation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsageHistory {
    private Double satisfactionRate;
    private Integer problemCount;
    private Integer totalReservations;
    private Integer cancelledReservations;
    /** Taux d'annulation calculé (0.0 – 1.0) */
    private Double cancellationRate;
    /** Durée moyenne des réservations en minutes */
    private Double avgDurationMinutes;
    /** Nombre de réservations confirmées */
    private Integer confirmedReservations;
    /** Score de fiabilité calculé (0.0 – 1.0) */
    private Double reliabilityScore;
}
