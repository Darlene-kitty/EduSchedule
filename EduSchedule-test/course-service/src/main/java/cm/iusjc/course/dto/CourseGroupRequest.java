package cm.iusjc.course.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseGroupRequest {
    
    @NotNull(message = "Course ID is required")
    private Long courseId;
    
    @NotBlank(message = "Group name is required")
    @Size(max = 50, message = "Group name must not exceed 50 characters")
    private String groupName;
    
    @NotBlank(message = "Type is required")
    @Pattern(regexp = "^(COURS|TD|TP|EXAMEN)$", message = "Type must be COURS, TD, TP, or EXAMEN")
    private String type;
    
    @Min(value = 1, message = "Max students must be at least 1")
    @Max(value = 100, message = "Max students must not exceed 100")
    private Integer maxStudents;
    
    private Long teacherId;
}