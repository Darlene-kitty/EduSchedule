package cm.iusjc.course.scheduling.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Événement publié sur RabbitMQ (schedule-exchange / schedule.changed)
 * quand un créneau d'emploi du temps est ajusté ou supprimé.
 * Consommé par le notification-service.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleChangedEvent implements Serializable {

    /** UPDATED | DELETED | CANCELLED */
    private String eventType;

    private Long scheduleId;
    private Long schoolId;
    private Long courseId;
    private String courseName;
    private String courseCode;

    private Long teacherId;
    private String teacherName;
    private String teacherEmail;

    private String groupName;
    private List<String> studentEmails;

    private String dayOfWeek;
    private String startTime;
    private String endTime;
    private String room;
    private String previousRoom;

    private String changeReason;
    private LocalDateTime eventTimestamp;
}
