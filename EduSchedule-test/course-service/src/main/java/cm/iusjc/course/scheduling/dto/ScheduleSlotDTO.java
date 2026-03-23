package cm.iusjc.course.scheduling.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Un créneau affecté dans l'emploi du temps généré.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleSlotDTO {

    private Long courseId;
    private String courseCode;
    private String courseName;
    private Long teacherId;
    private String teacherName;
    private Long roomId;
    private String roomName;
    private String dayOfWeek;   // ex: LUNDI
    private String startTime;   // ex: 08:00
    private String endTime;     // ex: 10:00
    private String level;
    private String semester;
}
