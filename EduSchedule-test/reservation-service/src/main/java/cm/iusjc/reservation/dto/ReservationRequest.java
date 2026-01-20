package cm.iusjc.reservation.dto;

import cm.iusjc.reservation.entity.ReservationType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationRequest {
    
    @NotNull(message = "Resource ID is required")
    private Long resourceId;
    
    private Long courseId; // Optionnel
    
    private Long courseGroupId; // Optionnel
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title must not exceed 100 characters")
    private String title;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    
    @NotNull(message = "Start time is required")
    @Future(message = "Start time must be in the future")
    private LocalDateTime startTime;
    
    @NotNull(message = "End time is required")
    private LocalDateTime endTime;
    
    @NotNull(message = "Reservation type is required")
    private ReservationType type;
    
    private String recurringPattern; // JSON pour les récurrences
    
    @Min(value = 1, message = "Expected attendees must be at least 1")
    private Integer expectedAttendees;
    
    @Min(value = 0, message = "Setup time cannot be negative")
    @Max(value = 120, message = "Setup time cannot exceed 120 minutes")
    private Integer setupTime = 0;
    
    @Min(value = 0, message = "Cleanup time cannot be negative")
    @Max(value = 120, message = "Cleanup time cannot exceed 120 minutes")
    private Integer cleanupTime = 0;
    
    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;
    
    // Validation personnalisée
    @AssertTrue(message = "End time must be after start time")
    public boolean isEndTimeAfterStartTime() {
        if (startTime == null || endTime == null) {
            return true; // Laisse les autres validations gérer les nulls
        }
        return endTime.isAfter(startTime);
    }
    
    @AssertTrue(message = "Reservation duration cannot exceed 8 hours")
    public boolean isReasonableDuration() {
        if (startTime == null || endTime == null) {
            return true;
        }
        return startTime.plusHours(8).isAfter(endTime);
    }
}