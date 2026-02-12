package cm.iusjc.userservice.dto;

import cm.iusjc.userservice.entity.ContractType;
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
public class TeacherSchoolAssignmentDTO {
    private Long id;
    private Long teacherId;
    private Long schoolId;
    private String schoolName;
    private String schoolAddress;
    private List<DayOfWeek> workingDays;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer travelTimeMinutes;
    private Integer maxHoursPerDay;
    private Integer maxHoursPerWeek;
    private Integer priority;
    private ContractType contractType;
    private LocalDateTime effectiveFrom;
    private LocalDateTime effectiveTo;
    private String notes;
    private Boolean active;
    private Boolean isPrimarySchool;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}