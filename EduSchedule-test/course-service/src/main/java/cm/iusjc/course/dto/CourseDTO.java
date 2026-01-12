package cm.iusjc.course.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseDTO {
    private Long id;
    private String name;
    private String code;
    private String description;
    private Integer credits;
    private Integer duration;
    private String department;
    private String level;
    private String semester;
    private Long teacherId;
    private String teacherName; // Nom de l'enseignant (récupéré via API)
    private Integer maxStudents;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<CourseGroupDTO> groups; // Groupes associés
}