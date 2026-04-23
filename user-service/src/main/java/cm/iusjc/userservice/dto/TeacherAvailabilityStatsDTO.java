package cm.iusjc.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeacherAvailabilityStatsDTO {
    private Long teacherId;
    private String teacherName;
    private Integer activeAvailabilities;
    private Integer totalAvailableMinutesPerWeek;
    private Double totalAvailableHoursPerWeek;
    private Integer averageAvailabilityDuration;
    private Integer totalSchools;
}