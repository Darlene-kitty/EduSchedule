package cm.iusjc.aiservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OccupancyMetrics {
    private double averageOccupancyRate;
    private double peakOccupancyRate;
    private double patternConsistency;
    private double utilizationEfficiency;
    private int totalOccupiedHours;
}
