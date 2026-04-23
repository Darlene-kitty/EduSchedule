package cm.iusjc.course.scheduling.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityChangedEvent {
    private String changeType;   // CREATED | UPDATED | DELETED
    private Long teacherId;
    private Long schoolId;
    private String dayOfWeek;
    private String startTime;
    private String endTime;
    private LocalDateTime changedAt;
}
