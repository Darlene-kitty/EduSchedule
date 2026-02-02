package cm.iusjc.notification.listener;

import cm.iusjc.notification.service.NotificationService;
import cm.iusjc.notification.service.ReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduleEventListener {
    
    private final NotificationService notificationService;
    private final ReminderService reminderService;
    
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
                case "room.changed":
                    handleRoomChanged(message);
                    break;
                case "course.cancelled":
                    handleCourseCancelled(message);
                    break;
                default:
                    log.warn("Unknown event type: {}", event);
            }
        } catch (Exception e) {
            log.error("Error processing schedule event", e);
        }
    }
    
    @RabbitListener(queues = "reservation-notifications")
    public void handleReservationEvent(Map<String, Object> message) {
        String event = (String) message.get("event");
        log.info("Received reservation event: {}", event);
        
        try {
            switch (event) {
                case "reservation.created":
                    handleReservationCreated(message);
                    break;
                case "reservation.updated":
                    handleReservationUpdated(message);
                    break;
                case "reservation.cancelled":
                    handleReservationCancelled(message);
                    break;
                case "room.conflict":
                    handleRoomConflict(message);
                    break;
                default:
                    log.warn("Unknown reservation event type: {}", event);
            }
        } catch (Exception e) {
            log.error("Error processing reservation event", e);
        }
    }
    
    private void handleScheduleCreated(Map<String, Object> message) {
        String title = (String) message.get("title");
        String teacher = (String) message.get("teacher");
        String groupName = (String) message.get("groupName");
        String startTime = (String) message.get("startTime");
        Long scheduleId = ((Number) message.get("scheduleId")).longValue();
        
        // Obtenir les utilisateurs affectés
        List<String> affectedUsers = getAffectedUsers(message);
        
        Map<String, Object> scheduleData = new HashMap<>();
        scheduleData.put("title", title);
        scheduleData.put("teacher", teacher);
        scheduleData.put("groupName", groupName);
        scheduleData.put("startTime", startTime);
        
        // Créer des rappels pour tous les utilisateurs affectés
        reminderService.createScheduleChangeReminders(scheduleId, "SCHEDULE_CREATED", affectedUsers, scheduleData);
        
        log.info("Reminders created for schedule.created event: {}", scheduleId);
    }
    
    private void handleScheduleUpdated(Map<String, Object> message) {
        String title = (String) message.get("title");
        Long scheduleId = ((Number) message.get("scheduleId")).longValue();
        String teacher = (String) message.get("teacher");
        String groupName = (String) message.get("groupName");
        String startTime = (String) message.get("startTime");
        
        List<String> affectedUsers = getAffectedUsers(message);
        
        Map<String, Object> scheduleData = new HashMap<>();
        scheduleData.put("title", title);
        scheduleData.put("teacher", teacher);
        scheduleData.put("groupName", groupName);
        scheduleData.put("startTime", startTime);
        
        // Créer des rappels pour la modification
        reminderService.createScheduleChangeReminders(scheduleId, "SCHEDULE_UPDATED", affectedUsers, scheduleData);
        
        log.info("Reminders created for schedule.updated event: {}", scheduleId);
    }
    
    private void handleScheduleDeleted(Map<String, Object> message) {
        Long scheduleId = ((Number) message.get("scheduleId")).longValue();
        String title = (String) message.get("title");
        String teacher = (String) message.get("teacher");
        String groupName = (String) message.get("groupName");
        String startTime = (String) message.get("startTime");
        
        List<String> affectedUsers = getAffectedUsers(message);
        
        Map<String, Object> scheduleData = new HashMap<>();
        scheduleData.put("title", title);
        scheduleData.put("teacher", teacher);
        scheduleData.put("groupName", groupName);
        scheduleData.put("startTime", startTime);
        
        reminderService.createScheduleChangeReminders(scheduleId, "SCHEDULE_DELETED", affectedUsers, scheduleData);
        
        log.info("Reminders created for schedule.deleted event: {}", scheduleId);
    }
    
    private void handleRoomChanged(Map<String, Object> message) {
        Long reservationId = ((Number) message.get("reservationId")).longValue();
        String oldRoom = (String) message.get("oldRoom");
        String newRoom = (String) message.get("newRoom");
        String title = (String) message.get("title");
        String startTime = (String) message.get("startTime");
        
        List<String> affectedUsers = getAffectedUsers(message);
        
        Map<String, Object> reservationData = new HashMap<>();
        reservationData.put("title", title);
        reservationData.put("startTime", startTime);
        reservationData.put("oldRoom", oldRoom);
        reservationData.put("newRoom", newRoom);
        
        reminderService.createRoomChangeReminders(reservationId, oldRoom, newRoom, affectedUsers, reservationData);
        
        log.info("Room change reminders created for reservation: {}", reservationId);
    }
    
    private void handleCourseCancelled(Map<String, Object> message) {
        Long scheduleId = ((Number) message.get("scheduleId")).longValue();
        String title = (String) message.get("title");
        String teacher = (String) message.get("teacher");
        String groupName = (String) message.get("groupName");
        String startTime = (String) message.get("startTime");
        String reason = (String) message.getOrDefault("reason", "Non spécifiée");
        
        List<String> affectedUsers = getAffectedUsers(message);
        
        Map<String, Object> scheduleData = new HashMap<>();
        scheduleData.put("title", title);
        scheduleData.put("teacher", teacher);
        scheduleData.put("groupName", groupName);
        scheduleData.put("startTime", startTime);
        
        reminderService.createCancellationReminders(scheduleId, affectedUsers, scheduleData, reason);
        
        log.info("Cancellation reminders created for schedule: {}", scheduleId);
    }
    
    private void handleReservationCreated(Map<String, Object> message) {
        String resourceName = (String) message.get("resourceName");
        String startTime = (String) message.get("startTime");
        String endTime = (String) message.get("endTime");
        Long reservationId = ((Number) message.get("reservationId")).longValue();
        
        List<String> affectedUsers = getAffectedUsers(message);
        
        String subject = "Nouvelle réservation de salle";
        String messageText = String.format(
            "Une nouvelle réservation a été créée:\n" +
            "Salle: %s\n" +
            "Début: %s\n" +
            "Fin: %s\n" +
            "ID de réservation: %d",
            resourceName, startTime, endTime, reservationId
        );
        
        for (String userEmail : affectedUsers) {
            notificationService.createNotification(userEmail, subject, messageText, "EMAIL");
        }
        
        log.info("Notifications created for reservation.created event: {}", reservationId);
    }
    
    private void handleReservationUpdated(Map<String, Object> message) {
        String resourceName = (String) message.get("resourceName");
        String startTime = (String) message.get("startTime");
        String endTime = (String) message.get("endTime");
        Long reservationId = ((Number) message.get("reservationId")).longValue();
        
        List<String> affectedUsers = getAffectedUsers(message);
        
        String subject = "Modification de réservation";
        String messageText = String.format(
            "Une réservation a été modifiée:\n" +
            "Salle: %s\n" +
            "Nouveau créneau: %s - %s\n" +
            "ID de réservation: %d",
            resourceName, startTime, endTime, reservationId
        );
        
        for (String userEmail : affectedUsers) {
            notificationService.createNotification(userEmail, subject, messageText, "EMAIL");
        }
        
        log.info("Notifications created for reservation.updated event: {}", reservationId);
    }
    
    private void handleReservationCancelled(Map<String, Object> message) {
        String resourceName = (String) message.get("resourceName");
        String startTime = (String) message.get("startTime");
        Long reservationId = ((Number) message.get("reservationId")).longValue();
        String reason = (String) message.getOrDefault("reason", "Non spécifiée");
        
        List<String> affectedUsers = getAffectedUsers(message);
        
        String subject = "Annulation de réservation";
        String messageText = String.format(
            "Une réservation a été annulée:\n" +
            "Salle: %s\n" +
            "Créneau: %s\n" +
            "Raison: %s\n" +
            "ID de réservation: %d",
            resourceName, startTime, reason, reservationId
        );
        
        for (String userEmail : affectedUsers) {
            notificationService.createNotification(userEmail, subject, messageText, "EMAIL");
        }
        
        log.info("Notifications created for reservation.cancelled event: {}", reservationId);
    }
    
    private void handleRoomConflict(Map<String, Object> message) {
        String resourceName = (String) message.get("resourceName");
        String startTime = (String) message.get("startTime");
        String conflictDetails = (String) message.get("conflictDetails");
        
        // Notifier les administrateurs
        List<String> adminUsers = Arrays.asList("admin@iusjc.cm", "planning@iusjc.cm");
        
        String subject = "Conflit de salle détecté";
        String messageText = String.format(
            "Un conflit de réservation a été détecté:\n" +
            "Salle: %s\n" +
            "Heure: %s\n" +
            "Détails: %s\n" +
            "Action requise pour résoudre le conflit.",
            resourceName, startTime, conflictDetails
        );
        
        for (String adminEmail : adminUsers) {
            notificationService.createNotification(adminEmail, subject, messageText, "EMAIL");
        }
        
        log.info("Conflict notifications sent for room: {}", resourceName);
    }
    
    /**
     * Extraire les utilisateurs affectés du message
     */
    private List<String> getAffectedUsers(Map<String, Object> message) {
        // Par défaut, utiliser l'enseignant et l'admin
        List<String> users = Arrays.asList("admin@iusjc.cm");
        
        if (message.containsKey("teacherEmail")) {
            users = Arrays.asList((String) message.get("teacherEmail"), "admin@iusjc.cm");
        } else if (message.containsKey("teacher")) {
            // Construire l'email à partir du nom (exemple simple)
            String teacher = (String) message.get("teacher");
            String teacherEmail = teacher.toLowerCase().replace(" ", ".") + "@iusjc.cm";
            users = Arrays.asList(teacherEmail, "admin@iusjc.cm");
        }
        
        // Ajouter les étudiants si disponibles
        if (message.containsKey("studentEmails")) {
            @SuppressWarnings("unchecked")
            List<String> studentEmails = (List<String>) message.get("studentEmails");
            users.addAll(studentEmails);
        }
        
        return users;
    }
}
