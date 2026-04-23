package cm.iusjc.reservation.dto;

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
public class AlternativeRecommendation {
    
    private String type; // TIME_ALTERNATIVE, CAPACITY_ALTERNATIVE, EQUIPMENT_ALTERNATIVE
    private String description;
    private LocalDateTime suggestedStartTime;
    private LocalDateTime suggestedEndTime;
    private Integer suggestedCapacity;
    private List<String> suggestedEquipments;
    private String suggestedLocation;
    private Double improvementScore;
}
