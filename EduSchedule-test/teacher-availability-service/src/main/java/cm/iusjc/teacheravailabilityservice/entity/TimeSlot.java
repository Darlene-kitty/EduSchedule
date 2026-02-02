package cm.iusjc.teacheravailabilityservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.DayOfWeek;

@Entity
@Table(name = "time_slots")
public class TimeSlot {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    @Column(name = "start_time")
    private LocalTime startTime;
    
    @NotNull
    @Column(name = "end_time")
    private LocalTime endTime;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week")
    private DayOfWeek dayOfWeek;
    
    @Column(name = "is_recurring")
    private Boolean isRecurring = false;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public TimeSlot() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public TimeSlot(LocalTime startTime, LocalTime endTime, DayOfWeek dayOfWeek) {
        this();
        this.startTime = startTime;
        this.endTime = endTime;
        this.dayOfWeek = dayOfWeek;
    }
    
    // Business methods
    public boolean overlaps(TimeSlot other) {
        if (!this.dayOfWeek.equals(other.dayOfWeek)) {
            return false;
        }
        
        return this.startTime.isBefore(other.endTime) && 
               this.endTime.isAfter(other.startTime);
    }
    
    public boolean contains(LocalTime time) {
        return !time.isBefore(startTime) && !time.isAfter(endTime);
    }
    
    public long getDurationMinutes() {
        return java.time.Duration.between(startTime, endTime).toMinutes();
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
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
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    @Override
    public String toString() {
        return String.format("TimeSlot{id=%d, %s %s-%s, recurring=%s}", 
                           id, dayOfWeek, startTime, endTime, isRecurring);
    }
}