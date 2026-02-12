package cm.iusjc.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkNotificationRequestDTO {
    
    @NotEmpty
    private List<Long> recipientIds;
    
    @NotNull
    private String title;
    
    @NotNull
    private String message;
    
    private String type = "GENERAL"; // GENERAL, SCHEDULE_CHANGE, MAINTENANCE, ALERT
    
    private String priority = "NORMAL"; // LOW, NORMAL, HIGH, URGENT
    
    private List<String> channels; // EMAIL, SMS, PUSH
    
    private LocalDateTime scheduledFor;
    
    private Map<String, Object> templateVariables;
    
    private String templateId;
    
    private Boolean requiresAcknowledgment = false;
    
    private LocalDateTime expiresAt;
    
    private String category;
    
    private Map<String, String> metadata;
}