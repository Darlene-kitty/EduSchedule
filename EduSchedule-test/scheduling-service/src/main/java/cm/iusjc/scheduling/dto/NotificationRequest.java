package cm.iusjc.scheduling.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {
    private Long userId;
    private String type; // EMAIL, SMS, PUSH
    private String subject;
    private String content;
    private String priority; // HIGH, NORMAL, LOW
    private LocalDateTime scheduledFor; // Pour les notifications programmées
    private Map<String, Object> metadata; // Données supplémentaires
    
    public NotificationRequest(Long userId, String type, String subject, String content) {
        this.userId = userId;
        this.type = type;
        this.subject = subject;
        this.content = content;
        this.priority = "NORMAL";
    }
}