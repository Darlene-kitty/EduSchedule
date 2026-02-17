package cm.iusjc.aiservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationMetrics {
    private int totalReservations;
    private double successRate;
    private double conflictRate;
    private double demandVariability;
    private double averageBookingTime;
    private int cancelledReservations;
}
