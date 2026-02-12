package cm.iusjc.scheduling.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeSlotDTO {
    
    private Long id;
    
    @NotBlank(message = "Day of week is required")
    @Pattern(regexp = "^(MONDAY|TUESDAY|WEDNESDAY|THURSDAY|FRIDAY|SATURDAY|SUNDAY)$", 
             message = "Day of week must be a valid day (MONDAY, TUESDAY, etc.)")
    private String dayOfWeek;
    
    @NotNull(message = "Start time is required")
    private LocalTime startTime;
    
    @NotNull(message = "End time is required")
    private LocalTime endTime;
    
    private Long scheduleId;
    
    // Champs calculés
    private Long durationMinutes;
    private String formattedDuration;
    private String formattedTimeRange;
    private boolean isValid;
    
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
    
    public String getFormattedTimeRange() {
        if (startTime != null && endTime != null) {
            return String.format("%s - %s", startTime.toString(), endTime.toString());
        }
        return "";
    }
    
    public boolean isValid() {
        return startTime != null && endTime != null && startTime.isBefore(endTime);
    }
}