package cm.iusjc.userservice.dto;

import cm.iusjc.userservice.entity.AvailabilityType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeacherAvailabilityDTO {
    private Long id;
    private Long teacherId;
    private Long schoolId;
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private AvailabilityType availabilityType;
    private Boolean recurring;
    private LocalDateTime specificDate;
    private Integer priority;
    private String notes;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}