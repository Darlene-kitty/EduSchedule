package cm.iusjc.course.scheduling.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Emploi du temps généré et validé par l'utilisateur.
 * Un enregistrement = un créneau (cours + salle + horaire).
 */
@Entity
@Table(name = "generated_schedules",
       indexes = {
           @Index(name = "idx_gs_school",   columnList = "school_id"),
           @Index(name = "idx_gs_semester", columnList = "semester, level"),
           @Index(name = "idx_gs_teacher",  columnList = "teacher_id")
       })
@Data
@NoArgsConstructor
public class GeneratedSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_id", nullable = false, length = 36)
    private String jobId;

    @Column(name = "school_id", nullable = false)
    private Long schoolId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "course_code", nullable = false, length = 20)
    private String courseCode;

    @Column(name = "course_name", nullable = false, length = 100)
    private String courseName;

    @Column(name = "teacher_id")
    private Long teacherId;

    @Column(name = "room_id")
    private Long roomId;

    @Column(name = "room_name", length = 100)
    private String roomName;

    @Column(name = "day_of_week", nullable = false, length = 10)
    private String dayOfWeek;   // LUNDI, MARDI…

    @Column(name = "start_time", nullable = false, length = 5)
    private String startTime;   // 08:00

    @Column(name = "end_time", nullable = false, length = 5)
    private String endTime;     // 10:00

    @Column(name = "level", length = 10)
    private String level;

    @Column(name = "semester", length = 5)
    private String semester;

    @Column(name = "calendar_event_id", length = 100)
    private String calendarEventId; // ID retourné par le calendar-service après sync

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { this.createdAt = LocalDateTime.now(); }
}
