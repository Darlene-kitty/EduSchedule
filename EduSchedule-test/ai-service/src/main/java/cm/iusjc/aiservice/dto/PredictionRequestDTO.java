package cm.iusjc.aiservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PredictionRequestDTO {
    
    private Long resourceId;
    
    @NotNull
    private LocalDateTime startDate;
    
    @NotNull
    private LocalDateTime endDate;
    
    private Integer daysHistory = 30;
    
    private String predictionType = "OCCUPANCY"; // OCCUPANCY, CONFLICTS, MAINTENANCE
    
    private Double confidenceThreshold = 0.7;
    
    private Boolean includeRecommendations = true;
    
    private String granularity = "HOURLY"; // HOURLY, DAILY, WEEKLY
}