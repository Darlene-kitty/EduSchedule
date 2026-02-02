package cm.iusjc.teacheravailabilityservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "teacher_availability")
public class TeacherAvailability {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    @Column(name = "teacher_id")
    private Long teacherId;
    
    @Column(name = "teacher_name")
    private String teacherName; // Cache du nom pour éviter les appels
    
    @NotNull
    @Column(name = "effective_date")
    private LocalDate effectiveDate;
    
    @Column(name = "end_date")
    private LocalDate endDate; // Null = indéfini
    
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "availability_id")
    private List<TimeSlot> availableSlots = new ArrayList<>();
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private AvailabilityStatus status = AvailabilityStatus.ACTIVE;
    
    @Column(name = "notes")
    private String notes;
    
    @Column(name = "max_hours_per_day")
    private Integer maxHoursPerDay = 8;
    
    @Column(name = "max_hours_per_week")
    private Integer maxHoursPerWeek = 40;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "created_by")
    private Long createdBy;
    
    // Constructors
    public TeacherAvailability() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public TeacherAvailability(Long teacherId, LocalDate effectiveDate) {
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
    
    public void addTimeSlot(TimeSlot slot) {
        if (availableSlots == null) {
            availableSlots = new ArrayList<>();
        }
        availableSlots.add(slot);
    }
    
    public void removeTimeSlot(TimeSlot slot) {
        if (availableSlots != null) {
            availableSlots.remove(slot);
        }
    }
    
    public boolean hasConflictWith(TimeSlot newSlot) {
        return availableSlots.stream()
                .anyMatch(slot -> slot.overlaps(newSlot));
    }
    
    public int getTotalWeeklyHours() {
        return availableSlots.stream()
                .mapToInt(slot -> (int) slot.getDurationMinutes())
                .sum() / 60;
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Enums
    public enum AvailabilityStatus {
        ACTIVE,
        INACTIVE,
        SUSPENDED,
        ARCHIVED
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
    
    public List<TimeSlot> getAvailableSlots() { return availableSlots; }
    public void setAvailableSlots(List<TimeSlot> availableSlots) { this.availableSlots = availableSlots; }
    
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
    
    @Override
    public String toString() {
        return String.format("TeacherAvailability{id=%d, teacherId=%d, effectiveDate=%s, status=%s, slots=%d}", 
                           id, teacherId, effectiveDate, status, availableSlots != null ? availableSlots.size() : 0);
    }
}