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
    
    @Builder.Default
    private Boolean emailEnabled = true;
    
    @Builder.Default
    private Boolean smsEnabled = false;
    
    @Builder.Default
    private Boolean pushEnabled = true;
    
    @Builder.Default
    private Boolean scheduleChanges = true;
    
    @Builder.Default
    private Boolean reservationUpdates = true;
    
    @Builder.Default
    private Boolean maintenanceAlerts = true;
    
    @Builder.Default
    private Boolean conflictAlerts = true;
    
    @Builder.Default
    private Boolean reminderNotifications = true;
    
    @Builder.Default
    private Integer reminderMinutesBefore = 30;
    
    private List<String> quietHours; // Format: "22:00-08:00"
    
    private List<String> preferredChannels; // EMAIL, SMS, PUSH
    
    @Builder.Default
    private String language = "fr";
    
    @Builder.Default
    private String timezone = "Europe/Paris";
    
    @Builder.Default
    private Boolean digestMode = false;
    
    @Builder.Default
    private String digestFrequency = "DAILY"; // DAILY, WEEKLY
    
    @Builder.Default
    private String digestTime = "08:00";
}