package cm.iusjc.notification.controller;

import cm.iusjc.notification.dto.NotificationDTO;
import cm.iusjc.notification.entity.Notification;
import cm.iusjc.notification.service.NotificationService;
import cm.iusjc.notification.service.ReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class NotificationController {
    
    private final NotificationService notificationService;
    private final ReminderService reminderService;
    
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
    
    @PostMapping("/send")
    public ResponseEntity<Map<String, String>> sendDirectEmail(@RequestBody Map<String, String> request) {
        try {
            String to = request.get("to");
            String subject = request.get("subject");
            String message = request.get("message");
            String type = request.getOrDefault("type", "EMAIL");
            
            if (to == null || subject == null || message == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Les champs 'to', 'subject' et 'message' sont requis"
                ));
            }
            
            // Créer et envoyer immédiatement la notification
            NotificationDTO notification = notificationService.createNotification(to, subject, message, type);
            notificationService.sendNotification(notification.getId());
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Email envoyé avec succès",
                "notificationId", notification.getId().toString(),
                "recipient", to
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "status", "error",
                "message", "Échec de l'envoi de l'email: " + e.getMessage()
            ));
        }
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
    
    // Nouveaux endpoints pour les rappels
    
    @PostMapping("/reminders")
    public ResponseEntity<Map<String, Object>> createReminder(@RequestBody Map<String, Object> request) {
        try {
            String recipient = (String) request.get("recipient");
            String subject = (String) request.get("subject");
            String message = (String) request.get("message");
            String scheduledForStr = (String) request.get("scheduledFor");
            String eventType = (String) request.get("eventType");
            Long eventId = request.containsKey("eventId") ? ((Number) request.get("eventId")).longValue() : null;
            String priority = (String) request.getOrDefault("priority", "NORMAL");
            String templateName = (String) request.get("templateName");
            
            @SuppressWarnings("unchecked")
            Map<String, Object> metadata = (Map<String, Object>) request.get("metadata");
            
            LocalDateTime scheduledFor = LocalDateTime.parse(scheduledForStr);
            
            Long reminderId = reminderService.createReminder(
                recipient, subject, message, scheduledFor, eventType, eventId, priority, templateName, metadata
            );
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Reminder created successfully",
                "reminderId", reminderId
            ));
        } catch (Exception e) {
            log.error("Error creating reminder", e);
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }
    
    @GetMapping("/reminders/user/{userEmail}")
    public ResponseEntity<List<Notification>> getScheduledRemindersForUser(@PathVariable String userEmail) {
        List<Notification> reminders = reminderService.getScheduledRemindersForUser(userEmail);
        return ResponseEntity.ok(reminders);
    }
    
    @DeleteMapping("/reminders/{reminderId}")
    public ResponseEntity<Map<String, String>> cancelReminder(@PathVariable Long reminderId) {
        try {
            reminderService.cancelReminder(reminderId);
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Reminder cancelled successfully"
            ));
        } catch (Exception e) {
            log.error("Error cancelling reminder", e);
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }
    
    @PostMapping("/schedule-change")
    public ResponseEntity<Map<String, String>> notifyScheduleChange(@RequestBody Map<String, Object> request) {
        try {
            Long scheduleId = ((Number) request.get("scheduleId")).longValue();
            String changeType = (String) request.get("changeType");
            @SuppressWarnings("unchecked")
            List<String> affectedUsers = (List<String>) request.get("affectedUsers");
            @SuppressWarnings("unchecked")
            Map<String, Object> scheduleData = (Map<String, Object>) request.get("scheduleData");
            
            reminderService.createScheduleChangeReminders(scheduleId, changeType, affectedUsers, scheduleData);
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Schedule change notifications created successfully"
            ));
        } catch (Exception e) {
            log.error("Error creating schedule change notifications", e);
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }
    
    @PostMapping("/room-change")
    public ResponseEntity<Map<String, String>> notifyRoomChange(@RequestBody Map<String, Object> request) {
        try {
            Long reservationId = ((Number) request.get("reservationId")).longValue();
            String oldRoom = (String) request.get("oldRoom");
            String newRoom = (String) request.get("newRoom");
            @SuppressWarnings("unchecked")
            List<String> affectedUsers = (List<String>) request.get("affectedUsers");
            @SuppressWarnings("unchecked")
            Map<String, Object> reservationData = (Map<String, Object>) request.get("reservationData");
            
            reminderService.createRoomChangeReminders(reservationId, oldRoom, newRoom, affectedUsers, reservationData);
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Room change notifications created successfully"
            ));
        } catch (Exception e) {
            log.error("Error creating room change notifications", e);
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }
    
    @PostMapping("/cancellation")
    public ResponseEntity<Map<String, String>> notifyCancellation(@RequestBody Map<String, Object> request) {
        try {
            Long scheduleId = ((Number) request.get("scheduleId")).longValue();
            @SuppressWarnings("unchecked")
            List<String> affectedUsers = (List<String>) request.get("affectedUsers");
            @SuppressWarnings("unchecked")
            Map<String, Object> scheduleData = (Map<String, Object>) request.get("scheduleData");
            String reason = (String) request.getOrDefault("reason", "Non spécifiée");
            
            reminderService.createCancellationReminders(scheduleId, affectedUsers, scheduleData, reason);
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Cancellation notifications created successfully"
            ));
        } catch (Exception e) {
            log.error("Error creating cancellation notifications", e);
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }
}