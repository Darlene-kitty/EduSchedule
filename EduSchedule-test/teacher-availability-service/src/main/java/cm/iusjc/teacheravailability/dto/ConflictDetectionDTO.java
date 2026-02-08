package cm.iusjc.teacheravailability.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

public class ConflictDetectionDTO {
    
    private Long teacherId;
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private Long schoolId;
    private boolean hasConflicts;
    private List<ConflictDTO> conflicts;
    private String message;
    
    // Constructors
    public ConflictDetectionDTO() {}
    
    public ConflictDetectionDTO(Long teacherId, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
        this.teacherId = teacherId;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
    }
    
    // Inner class for conflict details
    public static class ConflictDTO {
        private Long conflictingAvailabilityId;
        private LocalTime conflictStartTime;
        private LocalTime conflictEndTime;
        private String conflictType;
        private String description;
        private Long conflictingSchoolId;
        private String conflictingSchoolName;
        
        // Constructors
        public ConflictDTO() {}
        
        public ConflictDTO(Long conflictingAvailabilityId, LocalTime conflictStartTime, 
                          LocalTime conflictEndTime, String conflictType, String description) {
            this.conflictingAvailabilityId = conflictingAvailabilityId;
            this.conflictStartTime = conflictStartTime;
            this.conflictEndTime = conflictEndTime;
            this.conflictType = conflictType;
            this.description = description;
        }
        
        // Getters and Setters
        public Long getConflictingAvailabilityId() { return conflictingAvailabilityId; }
        public void setConflictingAvailabilityId(Long conflictingAvailabilityId) { this.conflictingAvailabilityId = conflictingAvailabilityId; }
        
        public LocalTime getConflictStartTime() { return conflictStartTime; }
        public void setConflictStartTime(LocalTime conflictStartTime) { this.conflictStartTime = conflictStartTime; }
        
        public LocalTime getConflictEndTime() { return conflictEndTime; }
        public void setConflictEndTime(LocalTime conflictEndTime) { this.conflictEndTime = conflictEndTime; }
        
        public String getConflictType() { return conflictType; }
        public void setConflictType(String conflictType) { this.conflictType = conflictType; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public Long getConflictingSchoolId() { return conflictingSchoolId; }
        public void setConflictingSchoolId(Long conflictingSchoolId) { this.conflictingSchoolId = conflictingSchoolId; }
        
        public String getConflictingSchoolName() { return conflictingSchoolName; }
        public void setConflictingSchoolName(String conflictingSchoolName) { this.conflictingSchoolName = conflictingSchoolName; }
    }
    
    // Business methods
    public void addConflict(ConflictDTO conflict) {
        if (conflicts == null) {
            conflicts = new java.util.ArrayList<>();
        }
        conflicts.add(conflict);
        this.hasConflicts = true;
    }
    
    public int getConflictCount() {
        return conflicts != null ? conflicts.size() : 0;
    }
    
    // Getters and Setters
    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    
    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(DayOfWeek dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    
    public Long getSchoolId() { return schoolId; }
    public void setSchoolId(Long schoolId) { this.schoolId = schoolId; }
    
    public boolean isHasConflicts() { return hasConflicts; }
    public void setHasConflicts(boolean hasConflicts) { this.hasConflicts = hasConflicts; }
    
    public List<ConflictDTO> getConflicts() { return conflicts; }
    public void setConflicts(List<ConflictDTO> conflicts) { this.conflicts = conflicts; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}