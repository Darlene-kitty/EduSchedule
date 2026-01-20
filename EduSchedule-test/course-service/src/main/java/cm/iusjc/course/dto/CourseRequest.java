package cm.iusjc.course.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseRequest {
    
    @NotBlank(message = "Course name is required")
    @Size(max = 100, message = "Course name must not exceed 100 characters")
    private String name;
    
    @NotBlank(message = "Course code is required")
    @Size(max = 20, message = "Course code must not exceed 20 characters")
    @Pattern(regexp = "^[A-Z]{2,4}[0-9]{2,3}$", message = "Course code format: 2-4 letters + 2-3 numbers (ex: INF101)")
    private String code;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    
    @NotNull(message = "Credits is required")
    @Min(value = 1, message = "Credits must be at least 1")
    @Max(value = 10, message = "Credits must not exceed 10")
    private Integer credits;
    
    @NotNull(message = "Duration is required")
    @Min(value = 30, message = "Duration must be at least 30 minutes")
    @Max(value = 480, message = "Duration must not exceed 8 hours")
    private Integer duration;
    
    @NotBlank(message = "Department is required")
    @Size(max = 100, message = "Department must not exceed 100 characters")
    private String department;
    
    @NotBlank(message = "Level is required")
    @Pattern(regexp = "^(L1|L2|L3|M1|M2|DOCTORAT)$", message = "Level must be L1, L2, L3, M1, M2, or DOCTORAT")
    private String level;
    
    @NotBlank(message = "Semester is required")
    @Pattern(regexp = "^(S1|S2)$", message = "Semester must be S1 or S2")
    private String semester;
    
    private Long teacherId;
    
    @Min(value = 1, message = "Max students must be at least 1")
    @Max(value = 500, message = "Max students must not exceed 500")
    private Integer maxStudents;
}