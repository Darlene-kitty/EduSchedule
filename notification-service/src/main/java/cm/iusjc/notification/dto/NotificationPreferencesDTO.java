package cm.iusjc.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPreferencesDTO {
    
    private Long userId;
    
    private Boolean emailEnabled = true;
    
    private Boolean smsEnabled = false;
    
    private Boolean pushEnabled = true;
    
    private Boolean scheduleChanges = true;
    
    private Boolean reservationUpdates = true;
    
    private Boolean maintenanceAlerts = true;
    
    private Boolean conflictAlerts = true;
    
    private Boolean reminderNotifications = true;
    
    private Integer reminderMinutesBefore = 30;
    
    private List<String> quietHours; // Format: "22:00-08:00"
    
    private List<String> preferredChannels; // EMAIL, SMS, PUSH
    
    private String language = "fr";
    
    private String timezone = "Europe/Paris";
    
    private Boolean digestMode = false;
    
    private String digestFrequency = "DAILY"; // DAILY, WEEKLY
    
    private String digestTime = "08:00";
}