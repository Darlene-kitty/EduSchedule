package cm.iusjc.notification.listener;

import cm.iusjc.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduleEventListener {
    
    private final NotificationService notificationService;
    
    @RabbitListener(queues = "schedule-notifications")
    public void handleScheduleEvent(Map<String, Object> message) {
        String event = (String) message.get("event");
        log.info("Received schedule event: {}", event);
        
        try {
            switch (event) {
                case "schedule.created":
                    handleScheduleCreated(message);
                    break;
                case "schedule.updated":
                    handleScheduleUpdated(message);
                    break;
                case "schedule.deleted":
                    handleScheduleDeleted(message);
                    break;
                default:
                    log.warn("Unknown event type: {}", event);
            }
        } catch (Exception e) {
            log.error("Error processing schedule event", e);
        }
    }
    
    private void handleScheduleCreated(Map<String, Object> message) {
        String title = (String) message.get("title");
        String teacher = (String) message.get("teacher");
        String groupName = (String) message.get("groupName");
        String startTime = (String) message.get("startTime");
        
        String notificationMessage = String.format(
                "Nouvel emploi du temps créé: %s\nEnseignant: %s\nGroupe: %s\nDébut: %s",
                title, teacher, groupName, startTime
        );
        
        // Envoyer notification (exemple avec email fictif)
        notificationService.createNotification(
                "admin@iusjc.cm",
                "Nouvel emploi du temps",
                notificationMessage,
                "EMAIL"
        );
        
        log.info("Notification created for schedule.created event");
    }
    
    private void handleScheduleUpdated(Map<String, Object> message) {
        String title = (String) message.get("title");
        Long scheduleId = ((Number) message.get("scheduleId")).longValue();
        
        String notificationMessage = String.format(
                "Emploi du temps modifié: %s (ID: %d)",
                title, scheduleId
        );
        
        notificationService.createNotification(
                "admin@iusjc.cm",
                "Emploi du temps modifié",
                notificationMessage,
                "EMAIL"
        );
        
        log.info("Notification created for schedule.updated event");
    }
    
    private void handleScheduleDeleted(Map<String, Object> message) {
        Long scheduleId = ((Number) message.get("scheduleId")).longValue();
        
        String notificationMessage = String.format(
                "Emploi du temps supprimé (ID: %d)",
                scheduleId
        );
        
        notificationService.createNotification(
                "admin@iusjc.cm",
                "Emploi du temps supprimé",
                notificationMessage,
                "EMAIL"
        );
        
        log.info("Notification created for schedule.deleted event");
    }
}
