package cm.iusjc.reservation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleDTO {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long roomId;
    private Long teacherId;
    private Long courseId;
    private Long groupId;
    private String status;
    private Integer expectedAttendees;
}