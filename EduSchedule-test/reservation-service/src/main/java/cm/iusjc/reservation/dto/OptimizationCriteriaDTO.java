package cm.iusjc.reservation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OptimizationCriteriaDTO {
    private String roomType;
    private Integer minCapacity;
    private Long schoolId;
    private Long preferredBuilding;
    private String preferredFloor;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean requiresProjector;
    private boolean requiresComputer;
    private boolean requiresWhiteboard;
    private boolean requiresAirConditioning;
    private boolean requiresAccessibility;
    private String requiredEquipment;
    private String courseType; // COURS, TD, TP, EXAMEN
    private Integer priority; // 1=Urgent, 2=Normal, 3=Flexible
}