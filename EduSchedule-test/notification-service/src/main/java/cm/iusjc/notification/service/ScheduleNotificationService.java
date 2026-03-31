package cm.iusjc.notification.service;

import cm.iusjc.notification.dto.BulkNotificationRequestDTO;
import cm.iusjc.notification.dto.NotificationDTO;
import cm.iusjc.notification.dto.NotificationPreferencesDTO;
import cm.iusjc.notification.dto.NotificationTemplateDTO;
import cm.iusjc.notification.dto.ScheduleChangeEventDTO;
import cm.iusjc.notification.entity.Notification;
import cm.iusjc.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleNotificationService {

    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm");
    private static final int MAX_RETRIES = 3;

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
        // Créer l'enregistrement en statut PENDING avant l'envoi
        Notification record = createPendingRecord(email, event, template);

        try {
            Map<String, Object> userVariables = createUserVariables(event, email);
            template.setVariables(userVariables);

            List<String> channels = event.getNotificationChannels();
            if (channels != null && channels.contains("EMAIL")) {
                emailService.sendHtmlEmail(email, template.getProcessedEmailSubject(), template.getProcessedEmailBody());
            }

            // Marquer comme SENT
            markDelivered(record);

        } catch (Exception e) {
            log.error("Error sending notification to {}: {}", email, e.getMessage());
            markFailed(record, e.getMessage());
        }
    }

    @Transactional
    private Notification createPendingRecord(String email, ScheduleChangeEventDTO event, NotificationTemplateDTO template) {
        Notification n = new Notification();
        n.setRecipient(email);
        n.setSubject(template.getEmailSubject());
        n.setTitle(template.getEmailSubject());
        n.setMessage(template.getEmailBodyHtml() != null ? template.getEmailBodyHtml() : "");
        n.setType("SCHEDULE_CHANGE");
        n.setChannel("EMAIL");
        n.setStatus("PENDING");
        n.setEventType(event.getEventType());
        n.setEventId(event.getScheduleId());
        n.setPriority(event.getPriority() != null ? event.getPriority() : "NORMAL");
        n.setRetryCount(0);
        n.setMaxRetries(MAX_RETRIES);
        n.setCreatedAt(LocalDateTime.now());
        n.setUpdatedAt(LocalDateTime.now());
        return notificationRepository.save(n);
    }

    @Transactional
    private void markDelivered(Notification n) {
        n.setStatus("SENT");
        n.setSent(true);
        n.setSentAt(LocalDateTime.now());
        n.setUpdatedAt(LocalDateTime.now());
        notificationRepository.save(n);
        log.debug("Notification {} marked as SENT", n.getId());
    }

    @Transactional
    private void markFailed(Notification n, String reason) {
        n.setRetryCount(n.getRetryCount() + 1);
        n.setStatus(n.getRetryCount() >= MAX_RETRIES ? "FAILED" : "PENDING");
        n.setMetadata("lastError=" + reason);
        n.setUpdatedAt(LocalDateTime.now());
        notificationRepository.save(n);
        log.warn("Notification {} failed (attempt {}/{}): {}", n.getId(), n.getRetryCount(), MAX_RETRIES, reason);
    }

    /**
     * Retry automatique toutes les 5 minutes pour les notifications PENDING en échec.
     */
    @Scheduled(fixedDelay = 300_000)
    @Transactional
    public void retryFailedNotifications() {
        List<Notification> pending = notificationRepository
                .findByStatusAndRetryCountLessThan("PENDING", MAX_RETRIES);

        if (pending.isEmpty()) return;

        log.info("Retrying {} pending notification(s)", pending.size());

        for (Notification n : pending) {
            try {
                emailService.sendEmail(n.getRecipient(), n.getSubject(), n.getMessage());
                markDelivered(n);
            } catch (Exception e) {
                markFailed(n, e.getMessage());
            }
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
