package cm.iusjc.userservice.dto;

import cm.iusjc.userservice.entity.AvailabilityType;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TeacherAvailabilityRequest {
    
    @NotNull(message = "Teacher ID is required")
    private Long teacherId;
    
    private Long schoolId;
    
    @NotNull(message = "Day of week is required")
    private DayOfWeek dayOfWeek;
    
    @NotNull(message = "Start time is required")
    private LocalTime startTime;
    
    @NotNull(message = "End time is required")
    private LocalTime endTime;
    
    @NotNull(message = "Availability type is required")
    private AvailabilityType availabilityType = AvailabilityType.AVAILABLE;
    
    private Boolean recurring = true;
    
    private LocalDateTime specificDate;
    
    private Integer priority = 2;
    
    private String notes;
}