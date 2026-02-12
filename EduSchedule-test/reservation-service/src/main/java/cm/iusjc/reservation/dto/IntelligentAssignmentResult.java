package cm.iusjc.reservation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntelligentAssignmentResult {
    
    private boolean success;
    private String message;
    private ScoredRoom recommendedRoom;
    private List<ScoredRoom> alternatives;
    private double confidenceScore;
    private String reasoning;
    private List<String> optimizationTips;
    private List<AlternativeRecommendation> alternativeRecommendations;
    
    // Métriques d'analyse
    private Map<String, Object> analysisMetrics;
    private LocalDateTime analysisTimestamp;
    private String algorithmVersion;
    
    // Feedback pour amélioration continue
    private String feedbackId;
    private boolean requiresUserFeedback;
}
