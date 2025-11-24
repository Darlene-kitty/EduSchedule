package cm.iusjc.notification.controller;

import cm.iusjc.notification.dto.NotificationDTO;
import cm.iusjc.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
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
    
    @PostMapping
    public ResponseEntity<NotificationDTO> createNotification(@RequestBody Map<String, String> request) {
        String recipient = request.get("recipient");
        String subject = request.get("subject");
        String message = request.get("message");
        String type = request.getOrDefault("type", "EMAIL");
        
        NotificationDTO notification = notificationService.createNotification(recipient, subject, message, type);
        return ResponseEntity.status(HttpStatus.CREATED).body(notification);
    }
    
    @PostMapping("/{id}/send")
    public ResponseEntity<Void> sendNotification(@PathVariable Long id) {
        notificationService.sendNotification(id);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/test-email")
    public ResponseEntity<Map<String, String>> testEmail(@RequestBody Map<String, String> request) {
        try {
            String recipient = request.getOrDefault("recipient", "test@example.com");
            String subject = request.getOrDefault("subject", "Test Email - EduSchedule");
            String message = request.getOrDefault("message", "Ceci est un email de test depuis EduSchedule. Si vous recevez cet email, la configuration SMTP fonctionne correctement!");
            
            NotificationDTO notification = notificationService.createNotification(recipient, subject, message, "EMAIL");
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Email de test envoyé avec succès",
                "notificationId", notification.getId().toString(),
                "recipient", recipient
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "status", "error",
                "message", "Échec de l'envoi de l'email: " + e.getMessage()
            ));
        }
    }
}
