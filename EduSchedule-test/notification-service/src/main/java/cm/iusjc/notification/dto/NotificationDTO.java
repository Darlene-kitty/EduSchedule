package cm.iusjc.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private Long id;
    private Long userId;
    private String recipient;
    private String recipientEmail;
    private String subject;
    private String title;
    private String message;
    private String type;
    private String status;
    private String priority;
    private String channel;
    private String relatedEntityType;
    private Long relatedEntityId;
    private boolean read;
    private LocalDateTime readAt;
    private boolean sent;
    private LocalDateTime sentAt;
    private LocalDateTime scheduledFor;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
