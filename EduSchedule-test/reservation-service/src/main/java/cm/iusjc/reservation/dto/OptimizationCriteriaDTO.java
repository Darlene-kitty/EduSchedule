package cm.iusjc.reservation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OptimizationCriteriaDTO {
    
    @NotNull
    private LocalDateTime startTime;
    
    @NotNull
    private LocalDateTime endTime;
    
    @NotNull
    private Integer minCapacity;
    
    private Integer maxCapacity;
    
    private String courseType; // COURS, TD, TP, EXAMEN
    
    private List<String> requiredEquipment;
    
    private Long schoolId;
    
    private Long buildingId;
    
    private String preferredBuilding;
    
    private String preferredFloor;
    
    private Boolean accessibilityRequired;
    
    private Boolean airConditioningRequired;
    
    @Builder.Default
    private Integer maxSuggestions = 5;
    
    @Builder.Default
    private Boolean emergencyMode = false;
    
    @Builder.Default
    private Double minOptimizationScore = 0.0;
    
    @Builder.Default
    private String sortBy = "OPTIMIZATION_SCORE"; // OPTIMIZATION_SCORE, CAPACITY, DISTANCE
    
    @Builder.Default
    private String sortOrder = "DESC"; // ASC, DESC
    
    @Builder.Default
    private Boolean includeAlternatives = true;
    
    private Integer maxDistance; // Distance maximale en mètres
    
    private List<Long> excludeRoomIds;
    
    @Builder.Default
    private String priority = "NORMAL"; // LOW, NORMAL, HIGH, URGENT
    
    @Builder.Default
    private Boolean considerUsageHistory = true;
    
    @Builder.Default
    private Boolean considerMaintenanceSchedule = true;
    
    @Builder.Default
    private Double energyEfficiencyWeight = 0.1;
    
    @Builder.Default
    private Double capacityMatchWeight = 0.3;
    
    @Builder.Default
    private Double equipmentMatchWeight = 0.2;
    
    @Builder.Default
    private Double locationWeight = 0.2;
    
    @Builder.Default
    private Double availabilityWeight = 0.2;
}