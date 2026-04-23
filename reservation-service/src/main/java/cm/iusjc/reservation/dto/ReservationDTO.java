package cm.iusjc.reservation.dto;

import cm.iusjc.reservation.entity.ReservationStatus;
import cm.iusjc.reservation.entity.ReservationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationDTO {
    
    private Long id;
    
    @NotNull(message = "Resource ID is required")
    private Long resourceId;
    
    private Long courseId;
    private Long courseGroupId;
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotBlank(message = "Title is required")
    @Size(min = 2, max = 100, message = "Title must be between 2 and 100 characters")
    private String title;
    
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
    
    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;
    
    @NotNull(message = "End time is required")
    private LocalDateTime endTime;
    
    @NotNull(message = "Status is required")
    private ReservationStatus status = ReservationStatus.PENDING;
    
    @NotNull(message = "Type is required")
    private ReservationType type;
    
    private String recurringPattern;
    private Long parentReservationId;
    private Long scheduleId;
    
    @Positive(message = "Expected attendees must be positive")
    private Integer expectedAttendees;
    
    private Integer setupTime = 0;
    private Integer cleanupTime = 0;
    
    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;
    
    private Long approvedBy;
    private LocalDateTime approvedAt;
    private Long cancelledBy;
    private LocalDateTime cancelledAt;
    private String cancellationReason;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Champs calculés
    private Long durationMinutes;
    private String formattedDuration;
    private LocalDateTime effectiveStartTime;
    private LocalDateTime effectiveEndTime;
    private boolean isActive;
    private boolean isRecurring;
    private boolean isPending;
    private boolean isConfirmed;
    private boolean isCancelled;
    private boolean isRejected;
    
    // Getters pour les champs calculés
    public Long getDurationMinutes() {
        if (startTime != null && endTime != null) {
            return java.time.Duration.between(startTime, endTime).toMinutes();
        }
        return 0L;
    }
    
    public String getFormattedDuration() {
        Long minutes = getDurationMinutes();
        if (minutes > 0) {
            long hours = minutes / 60;
            long mins = minutes % 60;
            if (hours > 0) {
                return String.format("%dh %02dmin", hours, mins);
            } else {
                return String.format("%dmin", mins);
            }
        }
        return "0min";
    }
    
    public LocalDateTime getEffectiveStartTime() {
        if (startTime != null && setupTime != null) {
            return startTime.minusMinutes(setupTime);
        }
        return startTime;
    }
    
    public LocalDateTime getEffectiveEndTime() {
        if (endTime != null && cleanupTime != null) {
            return endTime.plusMinutes(cleanupTime);
        }
        return endTime;
    }
    
    public boolean isActive() {
        return status == ReservationStatus.CONFIRMED || status == ReservationStatus.PENDING;
    }
    
    public boolean isRecurring() {
        return recurringPattern != null && !recurringPattern.trim().isEmpty();
    }
    
    public boolean isPending() {
        return status == ReservationStatus.PENDING;
    }
    
    public boolean isConfirmed() {
        return status == ReservationStatus.CONFIRMED;
    }
    
    public boolean isCancelled() {
        return status == ReservationStatus.CANCELLED;
    }
    
    public boolean isRejected() {
        return status == ReservationStatus.REJECTED;
    }
}