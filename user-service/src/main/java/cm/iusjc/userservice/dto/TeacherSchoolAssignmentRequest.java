package cm.iusjc.userservice.dto;

import cm.iusjc.userservice.entity.ContractType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeacherSchoolAssignmentRequest {
    
    @NotNull(message = "Teacher ID is required")
    private Long teacherId;
    
    @NotNull(message = "School ID is required")
    private Long schoolId;
    
    @NotNull(message = "Working days are required")
    private List<DayOfWeek> workingDays;
    
    private LocalTime startTime;
    
    private LocalTime endTime;
    
    private Integer travelTimeMinutes = 30;
    
    private Integer maxHoursPerDay = 8;
    
    private Integer maxHoursPerWeek = 20;
    
    private Integer priority = 1;
    
    private ContractType contractType = ContractType.PERMANENT;
    
    @NotNull(message = "Effective from date is required")
    private LocalDateTime effectiveFrom;
    
    private LocalDateTime effectiveTo;
    
    private String notes;
}