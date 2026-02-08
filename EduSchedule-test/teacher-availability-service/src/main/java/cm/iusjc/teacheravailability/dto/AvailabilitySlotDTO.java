package cm.iusjc.teacheravailability.dto;

import cm.iusjc.teacheravailability.entity.AvailabilityType;

import java.time.DayOfWeek;
import java.time.LocalTime;

public class AvailabilitySlotDTO {
    
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private AvailabilityType availabilityType;
    private Integer priorityLevel;
    private String notes;
    private Long schoolId;
    private String schoolName;
    private boolean hasConflict;
    private String conflictReason;
    
    // Constructors
    public AvailabilitySlotDTO() {}
    
    public AvailabilitySlotDTO(DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime, 
                              AvailabilityType availabilityType) {
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.availabilityType = availabilityType;
    }
    
    // Business methods
    public boolean isAvailable() {
        return availabilityType != null && availabilityType.isAvailable() && !hasConflict;
    }
    
    public boolean isPreferred() {
        return availabilityType != null && availabilityType.isPreferred();
    }
    
    public int getDurationInMinutes() {
        if (startTime == null || endTime == null) return 0;
        return (int) java.time.Duration.between(startTime, endTime).toMinutes();
    }
    
    public boolean overlaps(AvailabilitySlotDTO other) {
        if (!this.dayOfWeek.equals(other.dayOfWeek)) {
            return false;
        }
        return !(this.endTime.isBefore(other.startTime) || 
                this.startTime.isAfter(other.endTime));
    }
    
    // Getters and Setters
    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(DayOfWeek dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    
    public AvailabilityType getAvailabilityType() { return availabilityType; }
    public void setAvailabilityType(AvailabilityType availabilityType) { this.availabilityType = availabilityType; }
    
    public Integer getPriorityLevel() { return priorityLevel; }
    public void setPriorityLevel(Integer priorityLevel) { this.priorityLevel = priorityLevel; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public Long getSchoolId() { return schoolId; }
    public void setSchoolId(Long schoolId) { this.schoolId = schoolId; }
    
    public String getSchoolName() { return schoolName; }
    public void setSchoolName(String schoolName) { this.schoolName = schoolName; }
    
    public boolean isHasConflict() { return hasConflict; }
    public void setHasConflict(boolean hasConflict) { this.hasConflict = hasConflict; }
    
    public String getConflictReason() { return conflictReason; }
    public void setConflictReason(String conflictReason) { this.conflictReason = conflictReason; }
}