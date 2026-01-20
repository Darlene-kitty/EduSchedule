package cm.iusjc.eventservice.dto;

import cm.iusjc.eventservice.entity.ExamStatus;
import cm.iusjc.eventservice.entity.ExamType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamDTO {
    
    private Long id;
    private String title;
    private String description;
    private Long courseId;
    private ExamType type;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private Long resourceId;
    private Long teacherId;
    private Integer maxStudents;
    private Integer durationMinutes;
    private ExamStatus status;
    private String instructions;
    private String materialsAllowed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Informations enrichies
    private String courseName;
    private String resourceName;
    private String teacherName;
    private List<SupervisorDTO> supervisors;
}