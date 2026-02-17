package cm.iusjc.aiservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Recommendation {
    private String category;
    private String priority;
    private String title;
    private String description;
    private String expectedImpact;
    private String implementationEffort;
    private String timeframe;
    private List<String> steps;
    private double confidenceScore;
}
