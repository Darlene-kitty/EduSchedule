package cm.iusjc.reservation.dto;

import lombok.Data;
import java.util.List;

@Data
public class OptimizationResult {
    private boolean optimized;
    private String message;
    private List<OptimizationSuggestion> suggestions;
    private Double efficiencyScore;
    private Integer totalConflicts;
    private Integer resolvedConflicts;
}