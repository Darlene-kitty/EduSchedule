package cm.iusjc.scheduling.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleDTO {
    
    private Long id;
    
    @NotBlank(message = "Title is required")
    @Size(min = 2, max = 200, message = "Title must be between 2 and 200 characters")
    private String title;
    
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;
    
    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;
    
    @NotNull(message = "End time is required")
    private LocalDateTime endTime;
    
    @Size(max = 100, message = "Room name cannot exceed 100 characters")
    private String room;
    
    @Size(max = 100, message = "Teacher name cannot exceed 100 characters")
    private String teacher;
    
    @Size(max = 100, message = "Course name cannot exceed 100 characters")
    private String course;
    
    @Size(max = 100, message = "Group name cannot exceed 100 characters")
    private String groupName;
    
    @Size(max = 20, message = "Status cannot exceed 20 characters")
    private String status = "ACTIVE";
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Champs calculés
    private Long durationMinutes;
    private String formattedDuration;
    private boolean isActive;
    private boolean isCompleted;
    private boolean isCancelled;
    
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
    
    public boolean isActive() {
        return "ACTIVE".equals(status);
    }
    
    public boolean isCompleted() {
        return "COMPLETED".equals(status);
    }
    
    public boolean isCancelled() {
        return "CANCELLED".equals(status);
    }
}