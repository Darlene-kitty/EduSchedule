package cm.iusjc.notification.service;

import cm.iusjc.notification.entity.Notification;
import cm.iusjc.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReminderService {
    
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;
    
    /**
     * Créer un rappel programmé
     */
    @Transactional
    public Long createReminder(String recipient, String subject, String message, 
                              LocalDateTime scheduledFor, String eventType, Long eventId, 
                              String priority, String templateName, Map<String, Object> metadata) {
        
        Notification reminder = new Notification();
        reminder.setRecipient(recipient);
        reminder.setSubject(subject);
        reminder.setMessage(message);
        reminder.setType("REMINDER");
        reminder.setStatus("SCHEDULED");
        reminder.setScheduledFor(scheduledFor);
        reminder.setEventType(eventType);
        reminder.setEventId(eventId);
        reminder.setPriority(priority != null ? priority : "NORMAL");
        reminder.setTemplateName(templateName);
        
        if (metadata != null && !metadata.isEmpty()) {
            // Convertir la map en JSON string (simple implementation)
            reminder.setMetadata(metadata.toString());
        }
        
        Notification savedReminder = notificationRepository.save(reminder);
        log.info("Reminder created with ID: {} for {}", savedReminder.getId(), scheduledFor);
        
        return savedReminder.getId();
    }
    
    /**
     * Créer des rappels automatiques pour les changements d'emploi du temps
     */
    @Transactional
    public void createScheduleChangeReminders(Long scheduleId, String changeType, 
                                            List<String> affectedUsers, Map<String, Object> scheduleData) {
        
        LocalDateTime now = LocalDateTime.now();
        
        for (String userEmail : affectedUsers) {
            String subject = getSubjectForChangeType(changeType);
            String message = buildScheduleChangeMessage(changeType, scheduleData);
            
            // Notification immédiate
            createReminder(userEmail, subject, message, now, 
                         "SCHEDULE_CHANGE", scheduleId, "HIGH", 
                         "schedule_change_template", scheduleData);
            
            // Rappel 24h avant si c'est un changement futur
            if (scheduleData.containsKey("startTime")) {
                LocalDateTime scheduleStart = (LocalDateTime) scheduleData.get("startTime");
                if (scheduleStart.isAfter(now.plusHours(24))) {
                    LocalDateTime reminderTime = scheduleStart.minusHours(24);
                    createReminder(userEmail, "Rappel: " + subject, 
                                 "Rappel: " + message, reminderTime, 
                                 "REMINDER", scheduleId, "NORMAL", 
                                 "schedule_reminder_template", scheduleData);
                }
            }
        }
    }
    
    /**
     * Créer des rappels pour les changements de salle
     */
    @Transactional
    public void createRoomChangeReminders(Long reservationId, String oldRoom, String newRoom, 
                                        List<String> affectedUsers, Map<String, Object> reservationData) {
        
        String subject = "Changement de salle - " + reservationData.get("title");
        String message = String.format(
            "La salle pour votre cours '%s' a été modifiée.\n" +
            "Ancienne salle: %s\n" +
            "Nouvelle salle: %s\n" +
            "Date et heure: %s\n" +
            "Veuillez noter ce changement.",
            reservationData.get("title"), oldRoom, newRoom, reservationData.get("startTime")
        );
        
        LocalDateTime now = LocalDateTime.now();
        
        for (String userEmail : affectedUsers) {
            createReminder(userEmail, subject, message, now, 
                         "ROOM_CHANGE", reservationId, "HIGH", 
                         "room_change_template", reservationData);
        }
    }
    
    /**
     * Créer des rappels pour les annulations de cours
     */
    @Transactional
    public void createCancellationReminders(Long scheduleId, List<String> affectedUsers, 
                                           Map<String, Object> scheduleData, String reason) {
        
        String subject = "Annulation de cours - " + scheduleData.get("title");
        String message = String.format(
            "Le cours '%s' prévu le %s a été annulé.\n" +
            "Enseignant: %s\n" +
            "Groupe: %s\n" +
            "Raison: %s\n" +
            "Veuillez consulter votre emploi du temps mis à jour.",
            scheduleData.get("title"), scheduleData.get("startTime"),
            scheduleData.get("teacher"), scheduleData.get("groupName"), reason
        );
        
        LocalDateTime now = LocalDateTime.now();
        
        for (String userEmail : affectedUsers) {
            createReminder(userEmail, subject, message, now, 
                         "COURSE_CANCELLATION", scheduleId, "URGENT", 
                         "cancellation_template", scheduleData);
        }
    }
    
    /**
     * Traitement automatique des rappels programmés (exécuté toutes les minutes)
     */
    @Scheduled(fixedRate = 60000) // Toutes les minutes
    @Transactional
    public void processScheduledReminders() {
        LocalDateTime now = LocalDateTime.now();
        
        List<Notification> dueReminders = notificationRepository
            .findByStatusAndScheduledForLessThanEqual("SCHEDULED", now);
        
        log.info("Processing {} scheduled reminders", dueReminders.size());
        
        for (Notification reminder : dueReminders) {
            try {
                sendReminder(reminder);
            } catch (Exception e) {
                log.error("Failed to send reminder {}: {}", reminder.getId(), e.getMessage());
                handleReminderFailure(reminder);
            }
        }
    }
    
    /**
     * Envoyer un rappel
     */
    private void sendReminder(Notification reminder) {
        try {
            if ("EMAIL".equals(reminder.getType()) || "REMINDER".equals(reminder.getType())) {
                emailService.sendEmail(
                    reminder.getRecipient(),
                    reminder.getSubject(),
                    reminder.getMessage()
                );
            }
            
            reminder.setStatus("SENT");
            reminder.setSentAt(LocalDateTime.now());
            notificationRepository.save(reminder);
            
            log.info("Reminder sent successfully: {}", reminder.getId());
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to send reminder", e);
        }
    }
    
    /**
     * Gérer les échecs d'envoi de rappels
     */
    private void handleReminderFailure(Notification reminder) {
        reminder.setRetryCount(reminder.getRetryCount() + 1);
        
        if (reminder.getRetryCount() >= reminder.getMaxRetries()) {
            reminder.setStatus("FAILED");
            log.error("Reminder {} failed after {} retries", reminder.getId(), reminder.getMaxRetries());
        } else {
            // Reprogrammer pour dans 5 minutes
            reminder.setScheduledFor(LocalDateTime.now().plusMinutes(5));
            log.warn("Retrying reminder {} in 5 minutes (attempt {}/{})", 
                    reminder.getId(), reminder.getRetryCount(), reminder.getMaxRetries());
        }
        
        notificationRepository.save(reminder);
    }
    
    /**
     * Obtenir le sujet selon le type de changement
     */
    private String getSubjectForChangeType(String changeType) {
        switch (changeType) {
            case "SCHEDULE_UPDATED":
                return "Modification d'emploi du temps";
            case "SCHEDULE_CREATED":
                return "Nouvel emploi du temps";
            case "SCHEDULE_DELETED":
                return "Suppression d'emploi du temps";
            default:
                return "Changement d'emploi du temps";
        }
    }
    
    /**
     * Construire le message pour les changements d'emploi du temps
     */
    private String buildScheduleChangeMessage(String changeType, Map<String, Object> scheduleData) {
        StringBuilder message = new StringBuilder();
        
        switch (changeType) {
            case "SCHEDULE_UPDATED":
                message.append("Votre emploi du temps a été modifié.\n\n");
                break;
            case "SCHEDULE_CREATED":
                message.append("Un nouvel emploi du temps a été créé.\n\n");
                break;
            case "SCHEDULE_DELETED":
                message.append("Un emploi du temps a été supprimé.\n\n");
                break;
        }
        
        message.append("Détails:\n");
        message.append("Cours: ").append(scheduleData.get("title")).append("\n");
        message.append("Enseignant: ").append(scheduleData.get("teacher")).append("\n");
        message.append("Groupe: ").append(scheduleData.get("groupName")).append("\n");
        message.append("Date et heure: ").append(scheduleData.get("startTime")).append("\n");
        
        if (scheduleData.containsKey("room")) {
            message.append("Salle: ").append(scheduleData.get("room")).append("\n");
        }
        
        message.append("\nVeuillez consulter votre emploi du temps mis à jour.");
        
        return message.toString();
    }
    
    /**
     * Annuler un rappel programmé
     */
    @Transactional
    public void cancelReminder(Long reminderId) {
        Notification reminder = notificationRepository.findById(reminderId)
            .orElseThrow(() -> new RuntimeException("Reminder not found"));
        
        if ("SCHEDULED".equals(reminder.getStatus())) {
            reminder.setStatus("CANCELLED");
            notificationRepository.save(reminder);
            log.info("Reminder {} cancelled", reminderId);
        }
    }
    
    /**
     * Obtenir les rappels programmés pour un utilisateur
     */
    public List<Notification> getScheduledRemindersForUser(String userEmail) {
        return notificationRepository.findByRecipientAndStatusOrderByScheduledForAsc(userEmail, "SCHEDULED");
    }
}