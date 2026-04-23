package cm.iusjc.teacheravailability.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

@Entity
@Table(name = "teacher_school_assignment")
@EntityListeners(AuditingEntityListener.class)
public class TeacherSchoolAssignment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    @Column(name = "teacher_id", nullable = false)
    private Long teacherId;
    
    @NotNull
    @Column(name = "school_id", nullable = false)
    private Long schoolId;
    
    @ElementCollection(targetClass = DayOfWeek.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "teacher_working_days", joinColumns = @JoinColumn(name = "assignment_id"))
    @Column(name = "day_of_week")
    private Set<DayOfWeek> workingDays;
    
    @Column(name = "start_time")
    private LocalTime startTime;
    
    @Column(name = "end_time")
    private LocalTime endTime;
    
    @Column(name = "travel_time_minutes")
    private Integer travelTimeMinutes = 0; // Temps de déplacement vers cette école
    
    @Column(name = "is_primary_school")
    private Boolean isPrimarySchool = false;
    
    @Column(name = "contract_type")
    @Enumerated(EnumType.STRING)
    private ContractType contractType = ContractType.PART_TIME;
    
    @Column(name = "max_hours_per_week")
    private Integer maxHoursPerWeek;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public TeacherSchoolAssignment() {}
    
    public TeacherSchoolAssignment(Long teacherId, Long schoolId, Set<DayOfWeek> workingDays) {
        this.teacherId = teacherId;
        this.schoolId = schoolId;
        this.workingDays = workingDays;
    }
    
    // Business methods
    public boolean isWorkingDay(DayOfWeek dayOfWeek) {
        return workingDays != null && workingDays.contains(dayOfWeek);
    }
    
    public boolean isAvailableAt(LocalTime time) {
        if (startTime == null || endTime == null) {
            return true; // No time restrictions
        }
        return !time.isBefore(startTime) && !time.isAfter(endTime);
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    
    public Long getSchoolId() { return schoolId; }
    public void setSchoolId(Long schoolId) { this.schoolId = schoolId; }
    
    public Set<DayOfWeek> getWorkingDays() { return workingDays; }
    public void setWorkingDays(Set<DayOfWeek> workingDays) { this.workingDays = workingDays; }
    
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    
    public Integer getTravelTimeMinutes() { return travelTimeMinutes; }
    public void setTravelTimeMinutes(Integer travelTimeMinutes) { this.travelTimeMinutes = travelTimeMinutes; }
    
    public Boolean getIsPrimarySchool() { return isPrimarySchool; }
    public void setIsPrimarySchool(Boolean isPrimarySchool) { this.isPrimarySchool = isPrimarySchool; }
    
    public ContractType getContractType() { return contractType; }
    public void setContractType(ContractType contractType) { this.contractType = contractType; }
    
    public Integer getMaxHoursPerWeek() { return maxHoursPerWeek; }
    public void setMaxHoursPerWeek(Integer maxHoursPerWeek) { this.maxHoursPerWeek = maxHoursPerWeek; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}