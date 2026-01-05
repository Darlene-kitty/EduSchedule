package cm.iusjc.scheduling.dto;

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
    private String room;
    private String teacher;
    private String course;
    private String groupName;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
