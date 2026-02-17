package cm.iusjc.aiservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdvancedMetrics {
    private double systemEfficiency;
    private double userSatisfactionScore;
    private double resourceOptimizationRate;
    private double predictabilityIndex;
    private double systemResilienceScore;
}
