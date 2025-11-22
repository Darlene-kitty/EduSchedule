package cm.iusjc.notification.controller;

import cm.iusjc.notification.dto.NotificationDTO;
import cm.iusjc.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    
    private final NotificationService notificationService;
    
    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getAllNotifications() {
        return ResponseEntity.ok(notificationService.getAllNotifications());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<NotificationDTO> getNotificationById(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.getNotificationById(id));
    }
    
    @GetMapping("/recipient/{recipient}")
    public ResponseEntity<List<NotificationDTO>> getNotificationsByRecipient(@PathVariable String recipient) {
        return ResponseEntity.ok(notificationService.getNotificationsByRecipient(recipient));
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<NotificationDTO>> getNotificationsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(notificationService.getNotificationsByStatus(status));
    }
    
    @PostMapping("/{id}/send")
    public ResponseEntity<Void> sendNotification(@PathVariable Long id) {
        notificationService.sendNotification(id);
        return ResponseEntity.ok().build();
    }
}
