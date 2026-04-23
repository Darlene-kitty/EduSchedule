package cm.iusjc.aiservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaseMetrics {
    private ReservationMetrics reservationMetrics;
    private OccupancyMetrics occupancyMetrics;
    private ResourceMetrics resourceMetrics;
    private UserMetrics userMetrics;
    private PerformanceMetrics performanceMetrics;
}
