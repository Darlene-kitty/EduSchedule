package cm.iusjc.notification.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleChangeEventDTO {
    
    private String eventType; // CREATED, UPDATED, DELETED, CANCELLED, ROOM_CHANGED
    private Long scheduleId;
    private String title;
    private String description;
    
    // Teacher information
    private Long teacherId;
    private String teacherName;
    private String teacherEmail;
    
    // Course information
    private Long courseId;
    private String courseName;
    private String courseCode;
    
    // Group information
    private String groupName;
    private List<String> studentEmails;
    
    // Schedule details
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
    
    private String room;
    private String previousRoom; // For room changes
    
    // School information
    private Long schoolId;
    private String schoolName;
    
    // Change details
    private String changeReason;
    private String changeDescription;
    private Map<String, Object> previousValues; // For tracking what changed
    private Map<String, Object> newValues;
    
    // Notification preferences
    private List<String> notificationChannels; // EMAIL, SMS, PUSH
    @Builder.Default
    private Boolean sendImmediately = true;
    private Integer reminderMinutesBefore;
    
    // Additional metadata
    private String createdBy;
    private LocalDateTime eventTimestamp;
    @Builder.Default
    private String priority = "NORMAL"; // LOW, NORMAL, HIGH, URGENT
    
    // Helper methods
    public String getChangeType() {
        return eventType;
    }
    
    public boolean isRoomChange() {
        return "ROOM_CHANGED".equals(eventType) && previousRoom != null && !previousRoom.equals(room);
    }
    
    public boolean isTimeChange() {
        return previousValues != null && 
               (previousValues.containsKey("startTime") || previousValues.containsKey("endTime"));
    }
    
    public boolean isCancellation() {
        return "CANCELLED".equals(eventType) || "DELETED".equals(eventType);
    }
    
    public boolean isUrgent() {
        return "URGENT".equals(priority) || isCancellation() || isRoomChange();
    }
    
    public List<String> getAllAffectedEmails() {
        List<String> allEmails = new java.util.ArrayList<>();
        
        if (teacherEmail != null) {
            allEmails.add(teacherEmail);
        }
        
        if (studentEmails != null) {
            allEmails.addAll(studentEmails);
        }
        
        // Add admin emails for urgent changes
        if (isUrgent()) {
            allEmails.add("admin@iusjc.cm");
            allEmails.add("planning@iusjc.cm");
        }
        
        return allEmails.stream().distinct().collect(java.util.stream.Collectors.toList());
    }
}