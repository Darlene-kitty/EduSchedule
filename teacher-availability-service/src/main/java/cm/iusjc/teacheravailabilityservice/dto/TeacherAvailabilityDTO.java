package cm.iusjc.teacheravailabilityservice.dto;

import cm.iusjc.teacheravailabilityservice.entity.TeacherAvailability.AvailabilityStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

public class TeacherAvailabilityDTO {
    
    private Long id;
    
    @NotNull(message = "L'ID de l'enseignant est obligatoire")
    @Positive(message = "L'ID de l'enseignant doit être positif")
    private Long teacherId;
    
    private String teacherName;
    
    @NotNull(message = "La date d'effet est obligatoire")
    private LocalDate effectiveDate;
    
    private LocalDate endDate;
    
    @Valid
    private List<TimeSlotDTO> availableSlots = new ArrayList<>();
    
    private AvailabilityStatus status = AvailabilityStatus.ACTIVE;
    
    private String notes;
    
    @Positive(message = "Le nombre maximum d'heures par jour doit être positif")
    private Integer maxHoursPerDay = 8;
    
    @Positive(message = "Le nombre maximum d'heures par semaine doit être positif")
    private Integer maxHoursPerWeek = 40;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    
    // Champs calculés
    private Integer totalWeeklyHours;
    private Boolean hasConflicts;
    private List<String> conflictMessages;
    
    // Constructors
    public TeacherAvailabilityDTO() {
        this.conflictMessages = new ArrayList<>();
    }
    
    public TeacherAvailabilityDTO(Long teacherId, LocalDate effectiveDate) {
        this();
        this.teacherId = teacherId;
        this.effectiveDate = effectiveDate;
    }
    
    // Business methods
    public boolean isActiveOn(LocalDate date) {
        if (status != AvailabilityStatus.ACTIVE) {
            return false;
        }
        
        if (date.isBefore(effectiveDate)) {
            return false;
        }
        
        return endDate == null || !date.isAfter(endDate);
    }
    
    public void addTimeSlot(TimeSlotDTO slot) {
        if (availableSlots == null) {
            availableSlots = new ArrayList<>();
        }
        availableSlots.add(slot);
    }
    
    public void calculateTotalWeeklyHours() {
        if (availableSlots == null) {
            totalWeeklyHours = 0;
            return;
        }
        
        totalWeeklyHours = availableSlots.stream()
                .mapToInt(slot -> (int) slot.getDurationMinutes())
                .sum() / 60;
    }
    
    public boolean isValid() {
        if (teacherId == null || effectiveDate == null) {
            return false;
        }
        
        if (endDate != null && endDate.isBefore(effectiveDate)) {
            return false;
        }
        
        if (maxHoursPerDay != null && maxHoursPerDay <= 0) {
            return false;
        }
        
        if (maxHoursPerWeek != null && maxHoursPerWeek <= 0) {
            return false;
        }
        
        return availableSlots == null || availableSlots.stream().allMatch(TimeSlotDTO::isValid);
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    
    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }
    
    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }
    
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    
    public List<TimeSlotDTO> getAvailableSlots() { return availableSlots; }
    public void setAvailableSlots(List<TimeSlotDTO> availableSlots) { this.availableSlots = availableSlots; }
    
    public AvailabilityStatus getStatus() { return status; }
    public void setStatus(AvailabilityStatus status) { this.status = status; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public Integer getMaxHoursPerDay() { return maxHoursPerDay; }
    public void setMaxHoursPerDay(Integer maxHoursPerDay) { this.maxHoursPerDay = maxHoursPerDay; }
    
    public Integer getMaxHoursPerWeek() { return maxHoursPerWeek; }
    public void setMaxHoursPerWeek(Integer maxHoursPerWeek) { this.maxHoursPerWeek = maxHoursPerWeek; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    
    public Integer getTotalWeeklyHours() { return totalWeeklyHours; }
    public void setTotalWeeklyHours(Integer totalWeeklyHours) { this.totalWeeklyHours = totalWeeklyHours; }
    
    public Boolean getHasConflicts() { return hasConflicts; }
    public void setHasConflicts(Boolean hasConflicts) { this.hasConflicts = hasConflicts; }
    
    public List<String> getConflictMessages() { return conflictMessages; }
    public void setConflictMessages(List<String> conflictMessages) { this.conflictMessages = conflictMessages; }
    
    @Override
    public String toString() {
        return String.format("TeacherAvailabilityDTO{id=%d, teacherId=%d, effectiveDate=%s, status=%s, slots=%d}", 
                           id, teacherId, effectiveDate, status, availableSlots != null ? availableSlots.size() : 0);
    }
}