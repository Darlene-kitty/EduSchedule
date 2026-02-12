package cm.iusjc.notification.controller;

import cm.iusjc.notification.dto.*;
import cm.iusjc.notification.service.ScheduleNotificationService;
import cm.iusjc.notification.service.NotificationTemplateService;
import cm.iusjc.notification.service.SMSService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications/advanced")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AdvancedNotificationController {
    
    private final ScheduleNotificationService scheduleNotificationService;
    private final NotificationTemplateService templateService;
    private final SMSService smsService;
    
    /**
     * Envoie une notification de changement d'emploi du temps
     */
    @PostMapping("/schedule-change")
    public ResponseEntity<Map<String, Object>> sendScheduleChangeNotification(
            @Valid @RequestBody ScheduleChangeEventDTO event) {
        try {
            log.info("Sending schedule change notification for schedule: {}", event.getScheduleId());
            scheduleNotificationService.handleScheduleChange(event);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Schedule change notification sent successfully",
                    "scheduleId", event.getScheduleId(),
                    "changeType", event.getChangeType()
            ));
        } catch (Exception e) {
            log.error("Error sending schedule change notification: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Envoie des notifications automatiques pour les changements d'emploi du temps
     */
    @PostMapping("/auto-schedule-notifications")
    public ResponseEntity<Map<String, Object>> sendAutoScheduleNotifications(
            @RequestParam Long scheduleId,
            @RequestParam String changeType) {
        try {
            log.info("Sending auto notifications for schedule {} with change type {}", scheduleId, changeType);
            
            ScheduleChangeEventDTO event = ScheduleChangeEventDTO.builder()
                    .scheduleId(scheduleId)
                    .eventType(changeType)
                    .eventTimestamp(java.time.LocalDateTime.now())
                    .build();
                    
            scheduleNotificationService.handleScheduleChange(event);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Auto notifications sent successfully",
                    "scheduleId", scheduleId,
                    "notificationsSent", scheduleNotificationService.getLastNotificationCount()
            ));
        } catch (Exception e) {
            log.error("Error sending auto schedule notifications: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Récupère les templates de notification disponibles
     */
    @GetMapping("/templates")
    public ResponseEntity<List<NotificationTemplateDTO>> getNotificationTemplates() {
        try {
            List<NotificationTemplateDTO> templates = templateService.getAllTemplates();
            return ResponseEntity.ok(templates);
        } catch (Exception e) {
            log.error("Error getting notification templates: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Crée un nouveau template de notification
     */
    @PostMapping("/templates")
    public ResponseEntity<Map<String, Object>> createNotificationTemplate(
            @Valid @RequestBody NotificationTemplateDTO template) {
        try {
            NotificationTemplateDTO created = templateService.createTemplate(template);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Template created successfully",
                    "template", created
            ));
        } catch (Exception e) {
            log.error("Error creating notification template: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Met à jour un template de notification
     */
    @PutMapping("/templates/{id}")
    public ResponseEntity<Map<String, Object>> updateNotificationTemplate(
            @PathVariable Long id,
            @Valid @RequestBody NotificationTemplateDTO template) {
        try {
            NotificationTemplateDTO updated = templateService.updateTemplate(id, template);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Template updated successfully",
                    "template", updated
            ));
        } catch (Exception e) {
            log.error("Error updating notification template: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Supprime un template de notification
     */
    @DeleteMapping("/templates/{id}")
    public ResponseEntity<Map<String, Object>> deleteNotificationTemplate(@PathVariable Long id) {
        try {
            templateService.deleteTemplate(id);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Template deleted successfully"
            ));
        } catch (Exception e) {
            log.error("Error deleting notification template: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Envoie un SMS de test
     */
    @PostMapping("/sms/test")
    public ResponseEntity<Map<String, Object>> sendTestSMS(
            @RequestParam String phoneNumber,
            @RequestParam String message) {
        try {
            log.info("Sending test SMS to: {}", phoneNumber);
            smsService.sendSMS(phoneNumber, message);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Test SMS sent successfully",
                    "phoneNumber", phoneNumber
            ));
        } catch (Exception e) {
            log.error("Error sending test SMS: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Envoie un SMS de changement d'emploi du temps
     */
    @PostMapping("/sms/schedule-change")
    public ResponseEntity<Map<String, Object>> sendScheduleChangeSMS(
            @RequestParam Long userId,
            @RequestParam Long scheduleId,
            @RequestParam String changeType) {
        try {
            log.info("Sending schedule change SMS to user: {}", userId);
            smsService.sendScheduleChangeSMS(userId, scheduleId, changeType);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Schedule change SMS sent successfully",
                    "userId", userId,
                    "scheduleId", scheduleId
            ));
        } catch (Exception e) {
            log.error("Error sending schedule change SMS: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Configure les préférences de notification pour un utilisateur
     */
    @PostMapping("/preferences/{userId}")
    public ResponseEntity<Map<String, Object>> setNotificationPreferences(
            @PathVariable Long userId,
            @Valid @RequestBody NotificationPreferencesDTO preferences) {
        try {
            log.info("Setting notification preferences for user: {}", userId);
            scheduleNotificationService.setUserNotificationPreferences(userId, preferences);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Notification preferences updated successfully",
                    "userId", userId,
                    "preferences", preferences
            ));
        } catch (Exception e) {
            log.error("Error setting notification preferences: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Récupère les préférences de notification d'un utilisateur
     */
    @GetMapping("/preferences/{userId}")
    public ResponseEntity<NotificationPreferencesDTO> getNotificationPreferences(@PathVariable Long userId) {
        try {
            NotificationPreferencesDTO preferences = scheduleNotificationService.getUserNotificationPreferences(userId);
            return ResponseEntity.ok(preferences);
        } catch (Exception e) {
            log.error("Error getting notification preferences: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Récupère les statistiques des notifications
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getNotificationStatistics() {
        try {
            Map<String, Object> stats = scheduleNotificationService.getNotificationStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting notification statistics: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Récupère l'historique des notifications pour un utilisateur
     */
    @GetMapping("/history/{userId}")
    public ResponseEntity<List<Map<String, Object>>> getNotificationHistory(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            List<Map<String, Object>> history = scheduleNotificationService.getNotificationHistory(userId, page, size);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("Error getting notification history: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Marque une notification comme lue
     */
    @PatchMapping("/mark-read/{notificationId}")
    public ResponseEntity<Map<String, Object>> markNotificationAsRead(@PathVariable Long notificationId) {
        try {
            scheduleNotificationService.markAsRead(notificationId);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Notification marked as read",
                    "notificationId", notificationId
            ));
        } catch (Exception e) {
            log.error("Error marking notification as read: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Envoie des notifications en masse
     */
    @PostMapping("/bulk-send")
    public ResponseEntity<Map<String, Object>> sendBulkNotifications(
            @Valid @RequestBody BulkNotificationRequestDTO request) {
        try {
            log.info("Sending bulk notifications to {} recipients", request.getRecipientIds().size());
            int sentCount = scheduleNotificationService.sendBulkNotifications(request);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Bulk notifications sent successfully",
                    "totalRecipients", request.getRecipientIds().size(),
                    "sentCount", sentCount
            ));
        } catch (Exception e) {
            log.error("Error sending bulk notifications: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
}