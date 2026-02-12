package cm.iusjc.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeSlotDTO {
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer priority;
    
    public TimeSlotDTO(LocalTime startTime, LocalTime endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.priority = 2; // Défaut
    }
    
    public boolean overlaps(TimeSlotDTO other) {
        return startTime.isBefore(other.endTime) && endTime.isAfter(other.startTime);
    }
    
    public long getDurationMinutes() {
        return java.time.Duration.between(startTime, endTime).toMinutes();
    }
}