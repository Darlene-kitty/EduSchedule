package cm.iusjc.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InterSchoolConflictDTO {
    private Long scheduleId;
    private Long teacherId;
    private Long schoolId;
    private String schoolName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String courseName;
    private String roomName;
    private Integer travelTimeMinutes;
    private LocalDateTime requiredArrivalTime;
    private String conflictType;
    private String conflictDescription;
}