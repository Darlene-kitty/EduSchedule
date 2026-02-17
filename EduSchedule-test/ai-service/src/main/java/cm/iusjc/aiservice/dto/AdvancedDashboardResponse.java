package cm.iusjc.aiservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdvancedDashboardResponse {

    private boolean success;
    private String message;
    private String period;
    private LocalDateTime generatedAt;
    private BaseMetrics baseMetrics;
    private AdvancedMetrics advancedMetrics;
    private List<Alert> alerts;
    private List<Recommendation> recommendations;
    private double performanceScore;
}
