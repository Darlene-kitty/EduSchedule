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
    private String recipient;
    private String subject;
    private String message;
    private String type;
    private String status;
    private LocalDateTime sentAt;
    private LocalDateTime createdAt;
}
