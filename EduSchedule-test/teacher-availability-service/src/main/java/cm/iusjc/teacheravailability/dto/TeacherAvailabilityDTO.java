package cm.iusjc.teacheravailability.dto;

import cm.iusjc.teacheravailability.entity.AvailabilityType;
import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

public class TeacherAvailabilityDTO {
    
    private Long id;
    
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
    private AvailabilityType availabilityType;
    
    private LocalDate specificDate;
    private Boolean isRecurring = true;
    private Integer priorityLevel = 1;
    private String notes;
    private Boolean isActive = true;
    
    // Constructors
    public TeacherAvailabilityDTO() {}
    
    public TeacherAvailabilityDTO(Long teacherId, DayOfWeek dayOfWeek, LocalTime startTime, 
                                 LocalTime endTime, AvailabilityType availabilityType) {
        this.teacherId = teacherId;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.availabilityType = availabilityType;
    }
    
    // Validation methods
    public boolean isValidTimeRange() {
        return startTime != null && endTime != null && startTime.isBefore(endTime);
    }
    
    public int getDurationInMinutes() {
        if (!isValidTimeRange()) return 0;
        return (int) java.time.Duration.between(startTime, endTime).toMinutes();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    
    public Long getSchoolId() { return schoolId; }
    public void setSchoolId(Long schoolId) { this.schoolId = schoolId; }
    
    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(DayOfWeek dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    
    public AvailabilityType getAvailabilityType() { return availabilityType; }
    public void setAvailabilityType(AvailabilityType availabilityType) { this.availabilityType = availabilityType; }
    
    public LocalDate getSpecificDate() { return specificDate; }
    public void setSpecificDate(LocalDate specificDate) { this.specificDate = specificDate; }
    
    public Boolean getIsRecurring() { return isRecurring; }
    public void setIsRecurring(Boolean isRecurring) { this.isRecurring = isRecurring; }
    
    public Integer getPriorityLevel() { return priorityLevel; }
    public void setPriorityLevel(Integer priorityLevel) { this.priorityLevel = priorityLevel; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}