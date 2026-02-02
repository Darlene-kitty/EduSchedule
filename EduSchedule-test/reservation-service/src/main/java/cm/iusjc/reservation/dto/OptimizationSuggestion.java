package cm.iusjc.reservation.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OptimizationSuggestion {
    private Long reservationId;
    private Long originalResourceId;
    private Long suggestedResourceId;
    private LocalDateTime originalStartTime;
    private LocalDateTime suggestedStartTime;
    private LocalDateTime originalEndTime;
    private LocalDateTime suggestedEndTime;
    private String reason;
    private Double improvementScore;
    private String type; // RESOURCE_CHANGE, TIME_CHANGE, BOTH
}