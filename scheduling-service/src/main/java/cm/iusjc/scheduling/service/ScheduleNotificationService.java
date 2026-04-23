package cm.iusjc.scheduling.service;

import cm.iusjc.scheduling.entity.Schedule;
import cm.iusjc.scheduling.dto.ScheduleChangedEvent;
import cm.iusjc.scheduling.dto.NotificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleNotificationService {
    
    private final RabbitTemplate rabbitTemplate;
    private final RestTemplate restTemplate;
    private final NotificationPublisher notificationPublisher;
    
    private static final String USER_SERVICE_URL = "http://user-service";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    @EventListener
    public void onScheduleCreated(ScheduleChangedEvent event) {
        if (event.getChangeType() == ScheduleChangedEvent.ChangeType.CREATED) {
            log.info("Processing schedule created event: {}", event.getScheduleId());
            sendScheduleCreatedNotifications(event.getSchedule());
        }
    }
    
    @EventListener
    public void onScheduleUpdated(ScheduleChangedEvent event) {
        if (event.getChangeType() == ScheduleChangedEvent.ChangeType.UPDATED) {
            log.info("Processing schedule updated event: {}", event.getScheduleId());
            sendScheduleUpdatedNotifications(event.getSchedule(), event.getOldSchedule());
        }
    }
    
    @EventListener
    public void onScheduleCancelled(ScheduleChangedEvent event) {
        if (event.getChangeType() == ScheduleChangedEvent.ChangeType.CANCELLED) {
            log.info("Processing schedule cancelled event: {}", event.getScheduleId());
            sendScheduleCancelledNotifications(event.getSchedule());
        }
    }
    
    private void sendScheduleCreatedNotifications(Schedule schedule) {
        try {
            List<Long> affectedUsers = getAffectedUsers(schedule);
            
            for (Long userId : affectedUsers) {
                // Email notification
                String emailContent = buildScheduleCreatedEmailTemplate(schedule);
                sendEmailNotification(userId, "Nouveau cours programmé", emailContent);
                
                // SMS notification
                String smsContent = buildScheduleCreatedSMSTemplate(schedule);
                sendSMSNotification(userId, smsContent);
                
                // Push notification
                String pushContent = buildScheduleCreatedPushTemplate(schedule);
                sendPushNotification(userId, "Nouveau cours", pushContent);
            }
            
            log.info("Schedule created notifications sent for schedule: {}", schedule.getId());
            
        } catch (Exception e) {
            log.error("Failed to send schedule created notifications: {}", e.getMessage());
        }
    }
    
    private void sendScheduleUpdatedNotifications(Schedule newSchedule, Schedule oldSchedule) {
        try {
            List<Long> affectedUsers = getAffectedUsers(newSchedule);
            
            for (Long userId : affectedUsers) {
                // Email notification
                String emailContent = buildScheduleUpdatedEmailTemplate(newSchedule, oldSchedule);
                sendEmailNotification(userId, "Modification d'emploi du temps", emailContent);
                
                // SMS notification
                String smsContent = buildScheduleUpdatedSMSTemplate(newSchedule, oldSchedule);
                sendSMSNotification(userId, smsContent);
                
                // Push notification
                String pushContent = buildScheduleUpdatedPushTemplate(newSchedule, oldSchedule);
                sendPushNotification(userId, "Cours modifié", pushContent);
            }
            
            log.info("Schedule updated notifications sent for schedule: {}", newSchedule.getId());
            
        } catch (Exception e) {
            log.error("Failed to send schedule updated notifications: {}", e.getMessage());
        }
    }
    
    private void sendScheduleCancelledNotifications(Schedule schedule) {
        try {
            List<Long> affectedUsers = getAffectedUsers(schedule);
            
            for (Long userId : affectedUsers) {
                // Email notification
                String emailContent = buildScheduleCancelledEmailTemplate(schedule);
                sendEmailNotification(userId, "Cours annulé", emailContent);
                
                // SMS notification
                String smsContent = buildScheduleCancelledSMSTemplate(schedule);
                sendSMSNotification(userId, smsContent);
                
                // Push notification
                String pushContent = buildScheduleCancelledPushTemplate(schedule);
                sendPushNotification(userId, "Cours annulé", pushContent);
            }
            
            log.info("Schedule cancelled notifications sent for schedule: {}", schedule.getId());
            
        } catch (Exception e) {
            log.error("Failed to send schedule cancelled notifications: {}", e.getMessage());
        }
    }
    
    private List<Long> getAffectedUsers(Schedule schedule) {
        try {
            // Récupérer les utilisateurs concernés (enseignant + étudiants du groupe)
            String url = USER_SERVICE_URL + "/api/users/affected-by-schedule?teacher=" + schedule.getTeacher() + 
                        "&groupName=" + schedule.getGroupName() + "&course=" + schedule.getCourse();
            
            Long[] userIds = restTemplate.getForObject(url, Long[].class);
            return userIds != null ? List.of(userIds) : List.of();
            
        } catch (Exception e) {
            log.warn("Failed to get affected users for schedule {}: {}", schedule.getId(), e.getMessage());
            return List.of();
        }
    }
    
    private void sendEmailNotification(Long userId, String subject, String content) {
        try {
            NotificationRequest request = new NotificationRequest();
            request.setUserId(userId);
            request.setType("EMAIL");
            request.setSubject(subject);
            request.setContent(content);
            request.setPriority("NORMAL");
            
            notificationPublisher.publishNotification("schedule-notifications", request);
            
        } catch (Exception e) {
            log.warn("Failed to send email notification to user {}: {}", userId, e.getMessage());
        }
    }
    
    private void sendSMSNotification(Long userId, String content) {
        try {
            NotificationRequest request = new NotificationRequest();
            request.setUserId(userId);
            request.setType("SMS");
            request.setContent(content);
            request.setPriority("HIGH");
            
            notificationPublisher.publishNotification("schedule-notifications", request);
            
        } catch (Exception e) {
            log.warn("Failed to send SMS notification to user {}: {}", userId, e.getMessage());
        }
    }
    
    private void sendPushNotification(Long userId, String title, String content) {
        try {
            NotificationRequest request = new NotificationRequest();
            request.setUserId(userId);
            request.setType("PUSH");
            request.setSubject(title);
            request.setContent(content);
            request.setPriority("NORMAL");
            
            notificationPublisher.publishNotification("schedule-notifications", request);
            
        } catch (Exception e) {
            log.warn("Failed to send push notification to user {}: {}", userId, e.getMessage());
        }
    }
    
    // Templates Email
    private String buildScheduleCreatedEmailTemplate(Schedule schedule) {
        return String.format("""
            <html>
            <body>
                <h2>Nouveau cours programmé</h2>
                <p>Un nouveau cours a été ajouté à votre emploi du temps :</p>
                <ul>
                    <li><strong>Cours :</strong> %s</li>
                    <li><strong>Enseignant :</strong> %s</li>
                    <li><strong>Groupe :</strong> %s</li>
                    <li><strong>Salle :</strong> %s</li>
                    <li><strong>Date et heure :</strong> %s - %s</li>
                </ul>
                <p>Merci de noter cette information dans votre agenda.</p>
                <p><em>EduSchedule - Système de gestion des emplois du temps</em></p>
            </body>
            </html>
            """,
            schedule.getCourse() != null ? schedule.getCourse() : schedule.getTitle(),
            schedule.getTeacher() != null ? schedule.getTeacher() : "Non défini",
            schedule.getGroupName() != null ? schedule.getGroupName() : "Tous",
            schedule.getRoom() != null ? schedule.getRoom() : "À définir",
            schedule.getStartTime().format(FORMATTER),
            schedule.getEndTime().format(FORMATTER)
        );
    }
    
    private String buildScheduleUpdatedEmailTemplate(Schedule newSchedule, Schedule oldSchedule) {
        return String.format("""
            <html>
            <body>
                <h2>Modification d'emploi du temps</h2>
                <p>Un cours de votre emploi du temps a été modifié :</p>
                
                <h3>Anciennes informations :</h3>
                <ul>
                    <li><strong>Cours :</strong> %s</li>
                    <li><strong>Salle :</strong> %s</li>
                    <li><strong>Date et heure :</strong> %s - %s</li>
                </ul>
                
                <h3>Nouvelles informations :</h3>
                <ul>
                    <li><strong>Cours :</strong> %s</li>
                    <li><strong>Salle :</strong> %s</li>
                    <li><strong>Date et heure :</strong> %s - %s</li>
                </ul>
                
                <p>Merci de mettre à jour votre agenda en conséquence.</p>
                <p><em>EduSchedule - Système de gestion des emplois du temps</em></p>
            </body>
            </html>
            """,
            oldSchedule.getCourse() != null ? oldSchedule.getCourse() : oldSchedule.getTitle(),
            oldSchedule.getRoom() != null ? oldSchedule.getRoom() : "À définir",
            oldSchedule.getStartTime().format(FORMATTER),
            oldSchedule.getEndTime().format(FORMATTER),
            newSchedule.getCourse() != null ? newSchedule.getCourse() : newSchedule.getTitle(),
            newSchedule.getRoom() != null ? newSchedule.getRoom() : "À définir",
            newSchedule.getStartTime().format(FORMATTER),
            newSchedule.getEndTime().format(FORMATTER)
        );
    }
    
    private String buildScheduleCancelledEmailTemplate(Schedule schedule) {
        return String.format("""
            <html>
            <body>
                <h2>Cours annulé</h2>
                <p>Le cours suivant a été annulé :</p>
                <ul>
                    <li><strong>Cours :</strong> %s</li>
                    <li><strong>Enseignant :</strong> %s</li>
                    <li><strong>Groupe :</strong> %s</li>
                    <li><strong>Salle :</strong> %s</li>
                    <li><strong>Date et heure :</strong> %s - %s</li>
                </ul>
                <p>Merci de noter cette annulation dans votre agenda.</p>
                <p><em>EduSchedule - Système de gestion des emplois du temps</em></p>
            </body>
            </html>
            """,
            schedule.getCourse() != null ? schedule.getCourse() : schedule.getTitle(),
            schedule.getTeacher() != null ? schedule.getTeacher() : "Non défini",
            schedule.getGroupName() != null ? schedule.getGroupName() : "Tous",
            schedule.getRoom() != null ? schedule.getRoom() : "À définir",
            schedule.getStartTime().format(FORMATTER),
            schedule.getEndTime().format(FORMATTER)
        );
    }
    
    // Templates SMS
    private String buildScheduleCreatedSMSTemplate(Schedule schedule) {
        return String.format("Nouveau cours: %s le %s à %s en salle %s. EduSchedule",
            schedule.getCourse() != null ? schedule.getCourse() : schedule.getTitle(),
            schedule.getStartTime().format(DateTimeFormatter.ofPattern("dd/MM")),
            schedule.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")),
            schedule.getRoom() != null ? schedule.getRoom() : "TBD"
        );
    }
    
    private String buildScheduleUpdatedSMSTemplate(Schedule newSchedule, Schedule oldSchedule) {
        return String.format("Cours modifié: %s maintenant le %s à %s en salle %s. EduSchedule",
            newSchedule.getCourse() != null ? newSchedule.getCourse() : newSchedule.getTitle(),
            newSchedule.getStartTime().format(DateTimeFormatter.ofPattern("dd/MM")),
            newSchedule.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")),
            newSchedule.getRoom() != null ? newSchedule.getRoom() : "TBD"
        );
    }
    
    private String buildScheduleCancelledSMSTemplate(Schedule schedule) {
        return String.format("Cours annulé: %s du %s à %s. EduSchedule",
            schedule.getCourse() != null ? schedule.getCourse() : schedule.getTitle(),
            schedule.getStartTime().format(DateTimeFormatter.ofPattern("dd/MM")),
            schedule.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm"))
        );
    }
    
    // Templates Push
    private String buildScheduleCreatedPushTemplate(Schedule schedule) {
        return String.format("Nouveau cours %s programmé le %s à %s",
            schedule.getCourse() != null ? schedule.getCourse() : schedule.getTitle(),
            schedule.getStartTime().format(DateTimeFormatter.ofPattern("dd/MM")),
            schedule.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm"))
        );
    }
    
    private String buildScheduleUpdatedPushTemplate(Schedule newSchedule, Schedule oldSchedule) {
        return String.format("Cours %s modifié: maintenant le %s à %s",
            newSchedule.getCourse() != null ? newSchedule.getCourse() : newSchedule.getTitle(),
            newSchedule.getStartTime().format(DateTimeFormatter.ofPattern("dd/MM")),
            newSchedule.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm"))
        );
    }
    
    private String buildScheduleCancelledPushTemplate(Schedule schedule) {
        return String.format("Cours %s du %s annulé",
            schedule.getCourse() != null ? schedule.getCourse() : schedule.getTitle(),
            schedule.getStartTime().format(DateTimeFormatter.ofPattern("dd/MM à HH:mm"))
        );
    }
}