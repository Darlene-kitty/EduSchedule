package cm.iusjc.course.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseDTO {
    
    private Long id;
    
    @NotBlank(message = "Course name is required")
    @Size(min = 2, max = 100, message = "Course name must be between 2 and 100 characters")
    private String name;
    
    @NotBlank(message = "Course code is required")
    @Size(min = 2, max = 20, message = "Course code must be between 2 and 20 characters")
    private String code;
    
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
    
    @Min(value = 1, message = "Credits must be at least 1")
    private Integer credits;
    
    @Min(value = 1, message = "Hours per week must be at least 1")
    private Integer hoursPerWeek;
    
    @Min(value = 1, message = "Duration must be at least 1")
    private Integer duration; // en semaines
    
    @Size(max = 50, message = "Level cannot exceed 50 characters")
    private String level; // L1, L2, L3, M1, M2, etc.
    
    @Size(max = 100, message = "Department cannot exceed 100 characters")
    private String department;

    @Size(max = 10, message = "Semester cannot exceed 10 characters")
    private String semester; // S1, S2
    
    @NotNull(message = "School ID is required")
    private Long schoolId;
    
    private Long teacherId; // Peut être null si pas encore assigné
    
    private boolean active = true;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Champs calculés (non persistés)
    private String schoolName;
    private String teacherName;
    private Integer totalHours; // hoursPerWeek * duration
    private Integer groupCount; // Nombre de groupes pour ce cours
}