package cm.iusjc.teacheravailabilityservice.service;

import cm.iusjc.teacheravailabilityservice.entity.TeacherAvailability;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class NotificationService {
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    @Autowired
    private UserServiceClient userServiceClient;
    
    @Value("${teacher-availability.notifications.queue-name:availability.notifications}")
    private String notificationQueue;
    
    @Value("${teacher-availability.notifications.enabled:true}")
    private boolean notificationsEnabled;
    
    public void notifyAvailabilityCreated(TeacherAvailability availability) {
        if (!notificationsEnabled) {
            return;
        }
        
        try {
            Map<String, Object> notification = createNotificationPayload(
                    availability,
                    "AVAILABILITY_CREATED",
                    "Nouvelle disponibilité créée",
                    String.format("Disponibilité créée pour %s du %s au %s", 
                            availability.getTeacherName(),
                            availability.getEffectiveDate(),
                            availability.getEndDate() != null ? availability.getEndDate() : "indéfini")
            );
            
            rabbitTemplate.convertAndSend(notificationQueue, notification);
        } catch (Exception e) {
            // Log l'erreur mais ne pas faire échouer l'opération principale
            System.err.println("Erreur lors de l'envoi de notification: " + e.getMessage());
        }
    }
    
    public void notifyAvailabilityUpdated(TeacherAvailability availability) {
        if (!notificationsEnabled) {
            return;
        }
        
        try {
            Map<String, Object> notification = createNotificationPayload(
                    availability,
                    "AVAILABILITY_UPDATED",
                    "Disponibilité mise à jour",
                    String.format("Disponibilité mise à jour pour %s", availability.getTeacherName())
            );
            
            rabbitTemplate.convertAndSend(notificationQueue, notification);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi de notification: " + e.getMessage());
        }
    }
    
    public void notifyAvailabilityDeleted(TeacherAvailability availability) {
        if (!notificationsEnabled) {
            return;
        }
        
        try {
            Map<String, Object> notification = createNotificationPayload(
                    availability,
                    "AVAILABILITY_DELETED",
                    "Disponibilité supprimée",
                    String.format("Disponibilité supprimée pour %s", availability.getTeacherName())
            );
            
            rabbitTemplate.convertAndSend(notificationQueue, notification);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi de notification: " + e.getMessage());
        }
    }
    
    public void notifyConflictDetected(Long teacherId, String conflictMessage) {
        if (!notificationsEnabled) {
            return;
        }
        
        try {
            String teacherName = userServiceClient.getUserName(teacherId);
            String teacherEmail = userServiceClient.getUserEmail(teacherId);
            
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "CONFLICT_DETECTED");
            notification.put("title", "Conflit de disponibilité détecté");
            notification.put("message", conflictMessage);
            notification.put("teacherId", teacherId);
            notification.put("teacherName", teacherName);
            notification.put("teacherEmail", teacherEmail);
            notification.put("timestamp", LocalDateTime.now());
            notification.put("priority", "HIGH");
            notification.put("category", "AVAILABILITY");
            
            rabbitTemplate.convertAndSend(notificationQueue, notification);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi de notification: " + e.getMessage());
        }
    }
    
    public void notifyScheduleConflict(Long teacherId, LocalDateTime startTime, LocalDateTime endTime, String details) {
        if (!notificationsEnabled) {
            return;
        }
        
        try {
            String teacherName = userServiceClient.getUserName(teacherId);
            String teacherEmail = userServiceClient.getUserEmail(teacherId);
            
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "SCHEDULE_CONFLICT");
            notification.put("title", "Conflit d'emploi du temps");
            notification.put("message", String.format("Conflit détecté pour le créneau du %s de %s à %s: %s", 
                    startTime.toLocalDate(), startTime.toLocalTime(), endTime.toLocalTime(), details));
            notification.put("teacherId", teacherId);
            notification.put("teacherName", teacherName);
            notification.put("teacherEmail", teacherEmail);
            notification.put("startTime", startTime);
            notification.put("endTime", endTime);
            notification.put("details", details);
            notification.put("timestamp", LocalDateTime.now());
            notification.put("priority", "HIGH");
            notification.put("category", "SCHEDULE");
            
            rabbitTemplate.convertAndSend(notificationQueue, notification);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi de notification: " + e.getMessage());
        }
    }
    
    private Map<String, Object> createNotificationPayload(
            TeacherAvailability availability, 
            String type, 
            String title, 
            String message) {
        
        String teacherEmail = userServiceClient.getUserEmail(availability.getTeacherId());
        
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", type);
        notification.put("title", title);
        notification.put("message", message);
        notification.put("teacherId", availability.getTeacherId());
        notification.put("teacherName", availability.getTeacherName());
        notification.put("teacherEmail", teacherEmail);
        notification.put("availabilityId", availability.getId());
        notification.put("effectiveDate", availability.getEffectiveDate());
        notification.put("endDate", availability.getEndDate());
        notification.put("status", availability.getStatus());
        notification.put("timestamp", LocalDateTime.now());
        notification.put("priority", "MEDIUM");
        notification.put("category", "AVAILABILITY");
        
        return notification;
    }
}