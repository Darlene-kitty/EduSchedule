package cm.iusjc.course.scheduling.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamSlotDTO {
    private Long courseId;
    private String courseName;
    private String courseCode;
    private String level;
    private String semester;
    private Long teacherId;
    private String teacherName;
    private Long roomId;
    private String roomName;
    private Integer roomCapacity;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer durationMinutes;
    private String examType; // MIDTERM, FINAL, PRACTICAL
    private String status;   // SCHEDULED, CONFLICT
    private String conflictReason;
}
