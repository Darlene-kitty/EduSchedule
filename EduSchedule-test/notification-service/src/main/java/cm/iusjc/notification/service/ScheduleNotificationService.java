package cm.iusjc.notification.service;

import cm.iusjc.notification.dto.BulkNotificationRequestDTO;
import cm.iusjc.notification.dto.NotificationDTO;
import cm.iusjc.notification.dto.NotificationPreferencesDTO;
import cm.iusjc.notification.dto.NotificationTemplateDTO;
import cm.iusjc.notification.dto.ScheduleChangeEventDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleNotificationService {
    
    private final NotificationService notificationService;
    private final EmailService emailService;
    
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm");
    
    public void processScheduleChangeEvent(ScheduleChangeEventDTO event) {
        log.info("Processing schedule change event: {} for schedule {}", event.getEventType(), event.getScheduleId());
        
        try {
            List<String> affectedEmails = event.getAllAffectedEmails();
            NotificationTemplateDTO template = createNotificationTemplate(event);
            
            for (String email : affectedEmails) {
                sendScheduleNotification(email, event, template);
            }
            
            log.info("Successfully processed schedule change event for {} users", affectedEmails.size());
            
        } catch (Exception e) {
            log.error("Error processing schedule change event: {}", e.getMessage(), e);
        }
    }
    
    private void sendScheduleNotification(String email, ScheduleChangeEventDTO event, NotificationTemplateDTO template) {
        try {
            Map<String, Object> userVariables = createUserVariables(event, email);
            template.setVariables(userVariables);
            
            if (event.getNotificationChannels().contains("EMAIL")) {
                sendEmailNotification(email, event, template);
            }
            
            createNotificationRecord(email, event, template);
            
        } catch (Exception e) {
            log.error("Error sending notification to {}: {}", email, e.getMessage());
        }
    }
    
    private void sendEmailNotification(String email, ScheduleChangeEventDTO event, NotificationTemplateDTO template) {
        try {
            String subject = template.getProcessedEmailSubject();
            String body = template.getProcessedEmailBody();
            
            emailService.sendHtmlEmail(email, subject, body);
            log.debug("Email notification sent to {} for event {}", email, event.getEventType());
            
        } catch (Exception e) {
            log.error("Error sending email to {}: {}", email, e.getMessage());
        }
    }
    
    private void createNotificationRecord(String email, ScheduleChangeEventDTO event, NotificationTemplateDTO template) {
        try {
            NotificationDTO notification = new NotificationDTO();
            notification.setRecipientEmail(email);
            notification.setSubject(template.getProcessedEmailSubject());
            notification.setMessage(template.getProcessedEmailBody());
            notification.setType("SCHEDULE_CHANGE");
            notification.setChannel("EMAIL");
            
            notificationService.createNotification(notification);
            
        } catch (Exception e) {
            log.error("Error creating notification record: {}", e.getMessage());
        }
    }
    
    private NotificationTemplateDTO createNotificationTemplate(ScheduleChangeEventDTO event) {
        NotificationTemplateDTO template = new NotificationTemplateDTO();
        template.setTemplateType(event.getEventType());
        template.setChannel("EMAIL");
        
        switch (event.getEventType()) {
            case "CREATED":
                createScheduleCreatedTemplate(template, event);
                break;
            case "UPDATED":
                createScheduleUpdatedTemplate(template, event);
                break;
            case "DELETED":
            case "CANCELLED":
                createScheduleCancelledTemplate(template, event);
                break;
            case "ROOM_CHANGED":
                createRoomChangedTemplate(template, event);
                break;
            default:
                createGenericTemplate(template, event);
        }
        
        return template;
    }
    
    private void createScheduleCreatedTemplate(NotificationTemplateDTO template, ScheduleChangeEventDTO event) {
        template.setEmailSubject("📅 Nouveau cours programmé - {{courseName}}");
        template.setEmailBodyHtml(createScheduleCreatedHtml());
        template.setSmsMessage("📅 Nouveau cours: {{courseName}} le {{startDateTime}} en salle {{room}}");
    }
    
    private void createScheduleUpdatedTemplate(NotificationTemplateDTO template, ScheduleChangeEventDTO event) {
        template.setEmailSubject("🔄 Modification d'emploi du temps - {{courseName}}");
        template.setEmailBodyHtml(createScheduleUpdatedHtml());
        template.setSmsMessage("🔄 MODIFIÉ: {{courseName}} maintenant le {{startDateTime}} en salle {{room}}");
    }
    
    private void createScheduleCancelledTemplate(NotificationTemplateDTO template, ScheduleChangeEventDTO event) {
        template.setEmailSubject("❌ Cours annulé - {{courseName}}");
        template.setEmailBodyHtml(createScheduleCancelledHtml());
        template.setSmsMessage("❌ ANNULÉ: {{courseName}} du {{startDateTime}}");
    }
    
    private void createRoomChangedTemplate(NotificationTemplateDTO template, ScheduleChangeEventDTO event) {
        template.setEmailSubject("🏢 Changement de salle - {{courseName}}");
        template.setEmailBodyHtml(createRoomChangedHtml());
        template.setSmsMessage("🏢 SALLE CHANGÉE: {{courseName}} maintenant en salle {{room}}");
    }
    
    private void createGenericTemplate(NotificationTemplateDTO template, ScheduleChangeEventDTO event) {
        template.setEmailSubject("📋 Modification d'emploi du temps - {{courseName}}");
        template.setEmailBodyHtml("Une modification a été apportée à votre emploi du temps pour le cours {{courseName}}.");
        template.setSmsMessage("Modification emploi du temps: {{courseName}} le {{startDateTime}}");
    }
    
    private Map<String, Object> createUserVariables(ScheduleChangeEventDTO event, String userEmail) {
        Map<String, Object> variables = new HashMap<>();
        
        variables.put("courseName", event.getCourseName());
        variables.put("courseCode", event.getCourseCode());
        variables.put("teacherName", event.getTeacherName());
        variables.put("groupName", event.getGroupName());
        variables.put("room", event.getRoom());
        variables.put("previousRoom", event.getPreviousRoom());
        variables.put("schoolName", event.getSchoolName());
        variables.put("changeReason", event.getChangeReason());
        variables.put("changeDescription", event.getChangeDescription());
        variables.put("userEmail", userEmail);
        
        if (event.getStartTime() != null) {
            variables.put("startDateTime", event.getStartTime().format(DATETIME_FORMATTER));
        }
        
        if (event.getEndTime() != null) {
            variables.put("endDateTime", event.getEndTime().format(DATETIME_FORMATTER));
        }
        
        return variables;
    }
    
    private String createScheduleCreatedHtml() {
        return """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                <div style="background-color: #4CAF50; color: white; padding: 20px; text-align: center;">
                    <h1>📅 Nouveau Cours Programmé</h1>
                </div>
                <div style="padding: 20px; background-color: #f9f9f9;">
                    <h2 style="color: #333;">Détails du cours</h2>
                    <div style="background-color: white; padding: 15px; border-radius: 5px;">
                        <p><strong>📚 Cours :</strong> {{courseName}} ({{courseCode}})</p>
                        <p><strong>👨‍🏫 Enseignant :</strong> {{teacherName}}</p>
                        <p><strong>👥 Groupe :</strong> {{groupName}}</p>
                        <p><strong>📅 Date et heure :</strong> {{startDateTime}}</p>
                        <p><strong>🏢 Salle :</strong> {{room}}</p>
                        <p><strong>🏫 École :</strong> {{schoolName}}</p>
                    </div>
                </div>
            </div>
            """;
    }
    
    private String createScheduleUpdatedHtml() {
        return """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                <div style="background-color: #FF9800; color: white; padding: 20px; text-align: center;">
                    <h1>🔄 Emploi du Temps Modifié</h1>
                </div>
                <div style="padding: 20px; background-color: #f9f9f9;">
                    <div style="background-color: white; padding: 15px; border-radius: 5px;">
                        <p><strong>📚 Cours :</strong> {{courseName}}</p>
                        <p><strong>📅 Nouvelle date :</strong> {{startDateTime}}</p>
                        <p><strong>🏢 Salle :</strong> {{room}}</p>
                        <p><strong>📝 Raison :</strong> {{changeDescription}}</p>
                    </div>
                </div>
            </div>
            """;
    }
    
    private String createScheduleCancelledHtml() {
        return """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                <div style="background-color: #f44336; color: white; padding: 20px; text-align: center;">
                    <h1>❌ Cours Annulé</h1>
                </div>
                <div style="padding: 20px; background-color: #f9f9f9;">
                    <div style="background-color: white; padding: 15px; border-radius: 5px;">
                        <p><strong>📚 Cours :</strong> {{courseName}}</p>
                        <p><strong>📅 Date prévue :</strong> {{startDateTime}}</p>
                        <p><strong>📝 Raison :</strong> {{changeReason}}</p>
                    </div>
                </div>
            </div>
            """;
    }
    
    private String createRoomChangedHtml() {
        return """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                <div style="background-color: #2196F3; color: white; padding: 20px; text-align: center;">
                    <h1>🏢 Changement de Salle</h1>
                </div>
                <div style="padding: 20px; background-color: #f9f9f9;">
                    <div style="background-color: white; padding: 15px; border-radius: 5px;">
                        <p><strong>📚 Cours :</strong> {{courseName}}</p>
                        <p><strong>📅 Date :</strong> {{startDateTime}}</p>
                        <p><strong>🏢 Ancienne salle :</strong> {{previousRoom}}</p>
                        <p><strong>🏢 Nouvelle salle :</strong> {{room}}</p>
                    </div>
                </div>
            </div>
            """;
    }
    
    public void markAsRead(Long notificationId) {
        notificationService.markAsRead(notificationId);
    }
    
    public int sendBulkNotifications(BulkNotificationRequestDTO request) {
        int sentCount = 0;
        
        for (Long userId : request.getRecipientIds()) {
            try {
                NotificationDTO notification = new NotificationDTO();
                notification.setUserId(userId);
                notification.setTitle(request.getTitle());
                notification.setMessage(request.getMessage());
                notification.setType(request.getType());
                notification.setPriority(request.getPriority());
                notification.setScheduledFor(request.getScheduledFor());
                
                if (request.getChannels() != null && !request.getChannels().isEmpty()) {
                    notification.setChannel(String.join(",", request.getChannels()));
                }
                
                notificationService.createNotification(notification);
                sentCount++;
                
            } catch (Exception e) {
                log.error("Error sending notification to user {}: {}", userId, e.getMessage());
            }
        }
        
        return sentCount;
    }
    
    public void handleScheduleChange(ScheduleChangeEventDTO event) {
        processScheduleChangeEvent(event);
    }
    
    private int lastNotificationCount = 0;
    
    public int getLastNotificationCount() {
        return lastNotificationCount;
    }
    
    public void setUserNotificationPreferences(Long userId, NotificationPreferencesDTO preferences) {
        log.info("Setting notification preferences for user: {}", userId);
        // Implementation would store preferences in database
    }
    
    public NotificationPreferencesDTO getUserNotificationPreferences(Long userId) {
        log.info("Getting notification preferences for user: {}", userId);
        // Implementation would retrieve from database
        return new NotificationPreferencesDTO();
    }
    
    public Map<String, Object> getNotificationStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalSent", notificationService.getAllNotifications().size());
        stats.put("pending", 0);
        stats.put("failed", 0);
        return stats;
    }
    
    public List<Map<String, Object>> getNotificationHistory(Long userId, int page, int size) {
        log.info("Getting notification history for user: {} (page: {}, size: {})", userId, page, size);
        List<NotificationDTO> notifications = notificationService.getNotificationsByUserId(userId);
        
        // Convert to Map format
        return notifications.stream()
                .map(notification -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", notification.getId());
                    map.put("title", notification.getTitle());
                    map.put("message", notification.getMessage());
                    map.put("type", notification.getType());
                    map.put("priority", notification.getPriority());
                    map.put("read", notification.isRead());
                    map.put("createdAt", notification.getCreatedAt());
                    return map;
                })
                .collect(java.util.stream.Collectors.toList());
    }
}
