package cm.iusjc.course.scheduling.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Un créneau généré dans l'emploi du temps.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeneratedSlotDTO {

    private Long courseId;
    private String courseName;
    private String courseCode;
    private Long teacherId;
    private String teacherName;
    private Long roomId;
    private String roomName;
    private String dayOfWeek;
    private String startTime;
    private String endTime;
    private String level;
    private String semester;
    private Long schoolId;
}
