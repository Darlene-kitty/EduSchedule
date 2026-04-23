package cm.iusjc.notification.controller;

import cm.iusjc.notification.dto.NotificationDTO;
import cm.iusjc.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {
    
    private final NotificationService notificationService;
    
    /**
     * Crée une nouvelle notification
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> createNotification(@Valid @RequestBody NotificationDTO notificationDTO) {
        try {
            log.info("Creating notification for user: {}", notificationDTO.getUserId());
            NotificationDTO createdNotification = notificationService.createNotification(notificationDTO);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "message", "Notification created successfully",
                "data", createdNotification
            ));
        } catch (Exception e) {
            log.error("Error creating notification: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Crée des notifications en masse
     */
    @PostMapping("/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createBulkNotifications(
            @RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Long> userIds = (List<Long>) request.get("userIds");
            NotificationDTO template = new NotificationDTO();
            template.setTitle((String) request.get("title"));
            template.setMessage((String) request.get("message"));
            template.setType((String) request.get("type"));
            template.setPriority((String) request.get("priority"));
            template.setChannel((String) request.get("channel"));
            
            log.info("Creating bulk notifications for {} users", userIds.size());
            List<NotificationDTO> createdNotifications = notificationService.createBulkNotifications(userIds, template);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "message", "Bulk notifications created successfully",
                "data", createdNotifications,
                "total", createdNotifications.size()
            ));
        } catch (Exception e) {
            log.error("Error creating bulk notifications: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Récupère toutes les notifications
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAllNotifications() {
        try {
            List<NotificationDTO> notifications = notificationService.getAllNotifications();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", notifications,
                "total", notifications.size()
            ));
        } catch (Exception e) {
            log.error("Error fetching notifications: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Récupère les notifications avec pagination
     */
    @GetMapping("/paginated")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAllNotificationsPaginated(Pageable pageable) {
        try {
            Page<NotificationDTO> notificationsPage = notificationService.getAllNotifications(pageable);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", notificationsPage.getContent(),
                "page", notificationsPage.getNumber(),
                "size", notificationsPage.getSize(),
                "totalElements", notificationsPage.getTotalElements(),
                "totalPages", notificationsPage.getTotalPages()
            ));
        } catch (Exception e) {
            log.error("Error fetching paginated notifications: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Récupère une notification par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getNotificationById(@PathVariable Long id) {
        try {
            return notificationService.getNotificationById(id)
                    .map(notification -> ResponseEntity.ok(Map.of(
                        "success", true,
                        "data", notification
                    )))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error fetching notification by ID {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Récupère les notifications d'un utilisateur
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getNotificationsByUser(@PathVariable Long userId) {
        try {
            List<NotificationDTO> notifications = notificationService.getNotificationsByUser(userId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", notifications,
                "total", notifications.size()
            ));
        } catch (Exception e) {
            log.error("Error fetching notifications for user {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Récupère les notifications d'un utilisateur avec pagination
     */
    @GetMapping("/user/{userId}/paginated")
    public ResponseEntity<Map<String, Object>> getNotificationsByUserPaginated(
            @PathVariable Long userId, Pageable pageable) {
        try {
            Page<NotificationDTO> notificationsPage = notificationService.getNotificationsByUser(userId, pageable);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", notificationsPage.getContent(),
                "page", notificationsPage.getNumber(),
                "size", notificationsPage.getSize(),
                "totalElements", notificationsPage.getTotalElements(),
                "totalPages", notificationsPage.getTotalPages()
            ));
        } catch (Exception e) {
            log.error("Error fetching paginated notifications for user {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Récupère les notifications non lues d'un utilisateur
     */
    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<Map<String, Object>> getUnreadNotificationsByUser(@PathVariable Long userId) {
        try {
            List<NotificationDTO> unreadNotifications = notificationService.getUnreadNotificationsByUser(userId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", unreadNotifications,
                "total", unreadNotifications.size()
            ));
        } catch (Exception e) {
            log.error("Error fetching unread notifications for user {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Récupère les notifications par type
     */
    @GetMapping("/type/{type}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getNotificationsByType(@PathVariable String type) {
        try {
            List<NotificationDTO> notifications = notificationService.getNotificationsByType(type);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", notifications,
                "total", notifications.size()
            ));
        } catch (Exception e) {
            log.error("Error fetching notifications by type {}: {}", type, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Récupère les notifications en attente
     */
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getPendingNotifications() {
        try {
            List<NotificationDTO> pendingNotifications = notificationService.getPendingNotifications();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", pendingNotifications,
                "total", pendingNotifications.size()
            ));
        } catch (Exception e) {
            log.error("Error fetching pending notifications: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Met à jour une notification
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> updateNotification(
            @PathVariable Long id, 
            @Valid @RequestBody NotificationDTO notificationDTO) {
        try {
            NotificationDTO updatedNotification = notificationService.updateNotification(id, notificationDTO);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Notification updated successfully",
                "data", updatedNotification
            ));
        } catch (Exception e) {
            log.error("Error updating notification {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Marque une notification comme lue
     */
    @PatchMapping("/{id}/mark-read")
    public ResponseEntity<Map<String, Object>> markAsRead(@PathVariable Long id) {
        try {
            NotificationDTO updatedNotification = notificationService.markAsRead(id);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Notification marked as read",
                "data", updatedNotification
            ));
        } catch (Exception e) {
            log.error("Error marking notification as read {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Marque toutes les notifications d'un utilisateur comme lues
     */
    @PatchMapping("/user/{userId}/mark-all-read")
    public ResponseEntity<Map<String, Object>> markAllAsReadForUser(@PathVariable Long userId) {
        try {
            notificationService.markAllAsReadForUser(userId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "All notifications marked as read for user"
            ));
        } catch (Exception e) {
            log.error("Error marking all notifications as read for user {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Marque une notification comme envoyée
     */
    @PatchMapping("/{id}/mark-sent")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> markAsSent(@PathVariable Long id) {
        try {
            NotificationDTO updatedNotification = notificationService.markAsSent(id);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Notification marked as sent",
                "data", updatedNotification
            ));
        } catch (Exception e) {
            log.error("Error marking notification as sent {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Supprime une notification
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteNotification(@PathVariable Long id) {
        try {
            notificationService.deleteNotification(id);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Notification deleted successfully"
            ));
        } catch (Exception e) {
            log.error("Error deleting notification {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Supprime toutes les notifications lues d'un utilisateur
     */
    @DeleteMapping("/user/{userId}/read")
    public ResponseEntity<Map<String, Object>> deleteReadNotificationsForUser(@PathVariable Long userId) {
        try {
            notificationService.deleteReadNotificationsForUser(userId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Read notifications deleted for user"
            ));
        } catch (Exception e) {
            log.error("Error deleting read notifications for user {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Recherche des notifications
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> searchNotifications(@RequestParam String searchTerm) {
        try {
            List<NotificationDTO> notifications = notificationService.searchNotifications(searchTerm);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", notifications,
                "total", notifications.size()
            ));
        } catch (Exception e) {
            log.error("Error searching notifications: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Obtient les statistiques des notifications
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getNotificationStatistics() {
        try {
            NotificationService.NotificationStatistics stats = notificationService.getNotificationStatistics();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", stats
            ));
        } catch (Exception e) {
            log.error("Error fetching notification statistics: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Compte les notifications non lues d'un utilisateur
     */
    @GetMapping("/user/{userId}/unread-count")
    public ResponseEntity<Map<String, Object>> getUnreadCount(@PathVariable Long userId) {
        try {
            long unreadCount = notificationService.countUnreadNotificationsForUser(userId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", Map.of("unreadCount", unreadCount)
            ));
        } catch (Exception e) {
            log.error("Error fetching unread count for user {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
}