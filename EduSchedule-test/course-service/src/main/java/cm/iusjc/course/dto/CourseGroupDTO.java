package cm.iusjc.course.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseGroupDTO {
    private Long id;
    private Long courseId;
    private String groupName;
    private String type;
    private Integer maxStudents;
    private Integer currentStudents;
    private Long teacherId;
    private String teacherName; // Nom de l'enseignant
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}