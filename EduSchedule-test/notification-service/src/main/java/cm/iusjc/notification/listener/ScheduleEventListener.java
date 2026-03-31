package cm.iusjc.notification.listener;

import cm.iusjc.notification.dto.ScheduleChangeEventDTO;
import cm.iusjc.notification.service.NotificationService;
import cm.iusjc.notification.service.ReminderService;
import cm.iusjc.notification.service.ScheduleNotificationService;
import cm.iusjc.notification.service.WebSocketNotificationService;
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
    private final ScheduleNotificationService scheduleNotificationService;
    private final WebSocketNotificationService wsService;
    
    @RabbitListener(queues = "schedule-notifications")
    public void handleScheduleEvent(Map<String, Object> message) {
        String event = (String) message.get("event");
        log.info("Received schedule event: {}", event);
        
        try {
            ScheduleChangeEventDTO eventDTO = convertToScheduleChangeEvent(message, event);
            scheduleNotificationService.processScheduleChangeEvent(eventDTO);

            // Push WebSocket temps réel
            wsService.broadcastScheduleChange(
                    eventDTO.getEventType(),
                    eventDTO.getScheduleId(),
                    eventDTO.getCourseName(),
                    eventDTO.getRoom(),
                    eventDTO.getChangeDescription()
            );
        } catch (Exception e) {
            log.error("Error processing schedule event", e);
        }
    }
    
    @RabbitListener(queues = "teacher-availability-notifications")
    public void handleTeacherAvailabilityEvent(Map<String, Object> message) {
        String event = (String) message.get("event");
        log.info("Received teacher availability event: {}", event);
        
        try {
            switch (event) {
                case "availability.created":
                    handleAvailabilityCreated(message);
                    break;
                case "availability.updated":
                    handleAvailabilityUpdated(message);
                    break;
                case "availability.conflict":
                    handleAvailabilityConflict(message);
                    break;
                case "multi-school.assigned":
                    handleMultiSchoolAssignment(message);
                    break;
                default:
                    log.warn("Unknown availability event type: {}", event);
            }
        } catch (Exception e) {
            log.error("Error processing teacher availability event", e);
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
                case "reservation.approved":
                    handleReservationApproved(message);
                    break;
                case "reservation.rejected":
                    handleReservationRejected(message);
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
    
    private ScheduleChangeEventDTO convertToScheduleChangeEvent(Map<String, Object> message, String event) {
        ScheduleChangeEventDTO eventDTO = new ScheduleChangeEventDTO();
        
        switch (event) {
            case "schedule.created":
                eventDTO.setEventType("CREATED");
                break;
            case "schedule.updated":
                eventDTO.setEventType("UPDATED");
                break;
            case "schedule.deleted":
                eventDTO.setEventType("DELETED");
                break;
            case "course.cancelled":
                eventDTO.setEventType("CANCELLED");
                break;
            case "room.changed":
                eventDTO.setEventType("ROOM_CHANGED");
                break;
            default:
                eventDTO.setEventType("UPDATED");
        }
        
        eventDTO.setScheduleId(getLongValue(message, "scheduleId"));
        eventDTO.setTitle((String) message.get("title"));
        eventDTO.setCourseName((String) message.get("title"));
        eventDTO.setCourseCode((String) message.get("courseCode"));
        eventDTO.setTeacherName((String) message.get("teacher"));
        eventDTO.setTeacherEmail((String) message.get("teacherEmail"));
        eventDTO.setGroupName((String) message.get("groupName"));
        eventDTO.setRoom((String) message.get("room"));
        eventDTO.setPreviousRoom((String) message.get("oldRoom"));
        eventDTO.setSchoolName((String) message.get("schoolName"));
        eventDTO.setChangeReason((String) message.get("reason"));
        eventDTO.setChangeDescription((String) message.get("changeDescription"));
        
        eventDTO.setNotificationChannels(Arrays.asList("EMAIL"));
        eventDTO.setSendImmediately(true);
        eventDTO.setReminderMinutesBefore(30);
        
        if ("CANCELLED".equals(eventDTO.getEventType()) || "ROOM_CHANGED".equals(eventDTO.getEventType())) {
            eventDTO.setPriority("HIGH");
        } else {
            eventDTO.setPriority("NORMAL");
        }
        
        eventDTO.setEventTimestamp(LocalDateTime.now());
        
        return eventDTO;
    }
    
    private void handleAvailabilityCreated(Map<String, Object> message) {
        Long teacherId = getLongValue(message, "teacherId");
        String teacherEmail = (String) message.get("teacherEmail");
        String dayOfWeek = (String) message.get("dayOfWeek");
        String timeSlot = (String) message.get("timeSlot");
        
        String subject = "✅ Nouvelle disponibilité enregistrée";
        String messageText = String.format(
            "Votre disponibilité a été enregistrée avec succès :\n\n" +
            "📅 Jour : %s\n" +
            "⏰ Créneau : %s\n\n" +
            "Cette disponibilité sera prise en compte lors de la planification des cours.",
            dayOfWeek, timeSlot
        );
        
        if (teacherEmail != null) {
            notificationService.createNotification(teacherEmail, subject, messageText, "EMAIL");
        }
        
        log.info("Availability creation notification sent to teacher: {}", teacherId);
    }
    
    private void handleAvailabilityUpdated(Map<String, Object> message) {
        Long teacherId = getLongValue(message, "teacherId");
        String teacherEmail = (String) message.get("teacherEmail");
        String changeDescription = (String) message.get("changeDescription");
        
        String subject = "🔄 Disponibilité modifiée";
        String messageText = String.format(
            "Vos disponibilités ont été mises à jour :\n\n" +
            "📝 Modifications : %s\n\n" +
            "Les nouveaux créneaux seront pris en compte pour la planification.",
            changeDescription != null ? changeDescription : "Créneaux mis à jour"
        );
        
        if (teacherEmail != null) {
            notificationService.createNotification(teacherEmail, subject, messageText, "EMAIL");
        }
        
        log.info("Availability update notification sent to teacher: {}", teacherId);
    }
    
    private void handleAvailabilityConflict(Map<String, Object> message) {
        Long teacherId = getLongValue(message, "teacherId");
        String teacherEmail = (String) message.get("teacherEmail");
        String conflictDetails = (String) message.get("conflictDetails");
        String conflictType = (String) message.get("conflictType");
        
        String subject = "⚠️ Conflit de disponibilité détecté";
        String messageText = String.format(
            "Un conflit a été détecté dans vos disponibilités :\n\n" +
            "🔍 Type de conflit : %s\n" +
            "📋 Détails : %s\n\n" +
            "Veuillez vérifier et ajuster vos créneaux de disponibilité.",
            conflictType != null ? conflictType : "Chevauchement de créneaux",
            conflictDetails != null ? conflictDetails : "Conflit détecté"
        );
        
        if (teacherEmail != null) {
            notificationService.createNotification(teacherEmail, subject, messageText, "EMAIL");
        }
        
        notificationService.createNotification("admin@iusjc.cm", subject, messageText, "EMAIL");
        
        log.info("Availability conflict notification sent for teacher: {}", teacherId);
    }
    
    private void handleMultiSchoolAssignment(Map<String, Object> message) {
        Long teacherId = getLongValue(message, "teacherId");
        String teacherEmail = (String) message.get("teacherEmail");
        String schoolName = (String) message.get("schoolName");
        String workingDays = (String) message.get("workingDays");
        
        String subject = "🏫 Nouvelle affectation multi-établissements";
        String messageText = String.format(
            "Vous avez été affecté(e) à un nouvel établissement :\n\n" +
            "🏫 École : %s\n" +
            "📅 Jours de travail : %s\n\n" +
            "Veuillez mettre à jour vos disponibilités en conséquence.",
            schoolName != null ? schoolName : "Non spécifié",
            workingDays != null ? workingDays : "À définir"
        );
        
        if (teacherEmail != null) {
            notificationService.createNotification(teacherEmail, subject, messageText, "EMAIL");
        }
        
        log.info("Multi-school assignment notification sent to teacher: {}", teacherId);
    }
    
    private void handleReservationApproved(Map<String, Object> message) {
        Long reservationId = getLongValue(message, "reservationId");
        String title = (String) message.getOrDefault("title", "Réservation");
        String startTime = (String) message.get("startTime");
        String endTime   = (String) message.get("endTime");
        List<String> affectedUsers = getAffectedUsers(message);

        String subject = "✅ Réservation confirmée";
        String body = String.format(
            "Votre réservation a été approuvée :\n\n📋 %s\n📅 %s → %s\n\nVous pouvez consulter les détails dans l'application.",
            title, startTime, endTime);

        for (String email : affectedUsers) {
            notificationService.createNotification(email, subject, body, "EMAIL");
        }

        // Push WebSocket
        Long userId = getLongValue(message, "userId");
        wsService.broadcastReservationEvent("approved", reservationId, title, userId, "CONFIRMED");
        log.info("Approval notification sent for reservation {}", reservationId);
    }

    private void handleReservationRejected(Map<String, Object> message) {
        Long reservationId = getLongValue(message, "reservationId");
        String title  = (String) message.getOrDefault("title", "Réservation");
        String reason = (String) message.getOrDefault("reason", "Non spécifiée");
        List<String> affectedUsers = getAffectedUsers(message);

        String subject = "❌ Réservation refusée";
        String body = String.format(
            "Votre réservation a été refusée :\n\n📋 %s\n📝 Motif : %s\n\nVeuillez contacter l'administration pour plus d'informations.",
            title, reason);

        for (String email : affectedUsers) {
            notificationService.createNotification(email, subject, body, "EMAIL");
        }

        // Push WebSocket
        Long userId = getLongValue(message, "userId");
        wsService.broadcastReservationEvent("rejected", reservationId, title, userId, "REJECTED");
        log.info("Rejection notification sent for reservation {}", reservationId);
    }

    private void handleReservationCreated(Map<String, Object> message) {
        String resourceName = (String) message.get("resourceName");
        String startTime = (String) message.get("startTime");
        String endTime = (String) message.get("endTime");
        Long reservationId = getLongValue(message, "reservationId");
        String title = (String) message.getOrDefault("title", "Nouvelle réservation");
        
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

        // Push WebSocket — notifie les admins en temps réel
        wsService.broadcastReservationEvent("created", reservationId, title,
                getLongValue(message, "userId"), "PENDING");
        log.info("Notifications created for reservation.created event: {}", reservationId);
    }
    
    private void handleReservationUpdated(Map<String, Object> message) {
        String resourceName = (String) message.get("resourceName");
        String startTime = (String) message.get("startTime");
        String endTime = (String) message.get("endTime");
        Long reservationId = getLongValue(message, "reservationId");
        
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
        Long reservationId = getLongValue(message, "reservationId");
        String reason = (String) message.getOrDefault("reason", "Non spécifiée");
        String title = (String) message.getOrDefault("title", "Réservation");
        
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

        // Push WebSocket
        wsService.broadcastReservationEvent("cancelled", reservationId, title,
                getLongValue(message, "userId"), "CANCELLED");
        log.info("Notifications created for reservation.cancelled event: {}", reservationId);
    }
    
    private void handleRoomConflict(Map<String, Object> message) {
        String resourceName = (String) message.get("resourceName");
        String startTime = (String) message.get("startTime");
        String conflictDetails = (String) message.get("conflictDetails");
        
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
    
    private Long getLongValue(Map<String, Object> message, String key) {
        Object value = message.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return null;
    }
    
    private List<String> getAffectedUsers(Map<String, Object> message) {
        List<String> users = Arrays.asList("admin@iusjc.cm");
        
        if (message.containsKey("teacherEmail")) {
            users = Arrays.asList((String) message.get("teacherEmail"), "admin@iusjc.cm");
        } else if (message.containsKey("teacher")) {
            String teacher = (String) message.get("teacher");
            String teacherEmail = teacher.toLowerCase().replace(" ", ".") + "@iusjc.cm";
            users = Arrays.asList(teacherEmail, "admin@iusjc.cm");
        }
        
        if (message.containsKey("studentEmails")) {
            @SuppressWarnings("unchecked")
            List<String> studentEmails = (List<String>) message.get("studentEmails");
            users.addAll(studentEmails);
        }
        
        return users;
    }
}