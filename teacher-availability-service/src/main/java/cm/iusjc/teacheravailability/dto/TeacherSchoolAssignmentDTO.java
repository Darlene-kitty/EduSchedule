package cm.iusjc.teacheravailability.dto;

import cm.iusjc.teacheravailability.entity.ContractType;
import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;

public class TeacherSchoolAssignmentDTO {
    
    private Long id;
    
    @NotNull(message = "Teacher ID is required")
    private Long teacherId;
    
    @NotNull(message = "School ID is required")
    private Long schoolId;
    
    private Set<DayOfWeek> workingDays;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer travelTimeMinutes = 0;
    private Boolean isPrimarySchool = false;
    private ContractType contractType = ContractType.PART_TIME;
    private Integer maxHoursPerWeek;
    private Boolean isActive = true;
    
    // Additional fields for display
    private String teacherName;
    private String schoolName;
    
    // Constructors
    public TeacherSchoolAssignmentDTO() {}
    
    public TeacherSchoolAssignmentDTO(Long teacherId, Long schoolId, Set<DayOfWeek> workingDays) {
        this.teacherId = teacherId;
        this.schoolId = schoolId;
        this.workingDays = workingDays;
    }
    
    // Business methods
    public boolean isWorkingDay(DayOfWeek dayOfWeek) {
        return workingDays != null && workingDays.contains(dayOfWeek);
    }
    
    public int getWorkingDaysCount() {
        return workingDays != null ? workingDays.size() : 0;
    }
    
    public boolean hasTimeRestrictions() {
        return startTime != null && endTime != null;
    }
    
    public int getDailyWorkingHours() {
        if (!hasTimeRestrictions()) return 8; // Default 8 hours
        return (int) java.time.Duration.between(startTime, endTime).toHours();
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
    
    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }
    
    public String getSchoolName() { return schoolName; }
    public void setSchoolName(String schoolName) { this.schoolName = schoolName; }
}