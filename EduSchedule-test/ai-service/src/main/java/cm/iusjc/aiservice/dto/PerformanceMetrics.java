package cm.iusjc.aiservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceMetrics {
    private double averageResponseTime;
    private double errorRate;
    private double peakHandlingCapacity;
    private double errorRecoveryRate;
    private double systemUptime;
}
