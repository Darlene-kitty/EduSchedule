package cm.iusjc.teacheravailabilityservice.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import java.time.DayOfWeek;

public class TimeSlotDTO {
    
    private Long id;
    
    @NotNull(message = "L'heure de début est obligatoire")
    private LocalTime startTime;
    
    @NotNull(message = "L'heure de fin est obligatoire")
    private LocalTime endTime;
    
    @NotNull(message = "Le jour de la semaine est obligatoire")
    private DayOfWeek dayOfWeek;
    
    private Boolean isRecurring = false;
    
    // Constructors
    public TimeSlotDTO() {}
    
    public TimeSlotDTO(LocalTime startTime, LocalTime endTime, DayOfWeek dayOfWeek) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.dayOfWeek = dayOfWeek;
    }
    
    // Validation methods
    public boolean isValid() {
        if (startTime == null || endTime == null || dayOfWeek == null) {
            return false;
        }
        return startTime.isBefore(endTime);
    }
    
    public long getDurationMinutes() {
        if (startTime == null || endTime == null) {
            return 0;
        }
        return java.time.Duration.between(startTime, endTime).toMinutes();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    
    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(DayOfWeek dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    
    public Boolean getIsRecurring() { return isRecurring; }
    public void setIsRecurring(Boolean isRecurring) { this.isRecurring = isRecurring; }
    
    @Override
    public String toString() {
        return String.format("TimeSlotDTO{%s %s-%s, recurring=%s}", 
                           dayOfWeek, startTime, endTime, isRecurring);
    }
}