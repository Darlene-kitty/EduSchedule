package cm.iusjc.reservation.service;

import cm.iusjc.reservation.dto.ReservationDTO;
import cm.iusjc.reservation.entity.Reservation;
import cm.iusjc.reservation.entity.ReservationStatus;
import cm.iusjc.reservation.entity.ReservationType;
import cm.iusjc.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleReservationSyncService {
    
    private final ReservationRepository reservationRepository;
    private final ReservationService reservationService;
    private final RabbitTemplate rabbitTemplate;
    
    /**
     * Écoute les événements de création d'emploi du temps
     */
    @RabbitListener(queues = "schedule-created")
    @Transactional
    public void handleScheduleCreated(Map<String, Object> scheduleEvent) {
        log.info("Handling schedule created event: {}", scheduleEvent);
        
        try {
            // Créer automatiquement une réservation pour l'emploi du temps
            Reservation reservation = createReservationFromSchedule(scheduleEvent);
            
            if (reservation != null) {
                log.info("Created reservation {} for schedule {}", 
                    reservation.getId(), scheduleEvent.get("scheduleId"));
                
                // Notifier la création de la réservation
                notifyReservationCreated(reservation, scheduleEvent);
            }
            
        } catch (Exception e) {
            log.error("Error handling schedule created event", e);
            // Envoyer un événement d'erreur pour rollback si nécessaire
            notifyScheduleReservationError(scheduleEvent, "CREATION_FAILED", e.getMessage());
        }
    }
    
    /**
     * Écoute les événements de modification d'emploi du temps
     */
    @RabbitListener(queues = "schedule-updated")
    @Transactional
    public void handleScheduleUpdated(Map<String, Object> scheduleEvent) {
        log.info("Handling schedule updated event: {}", scheduleEvent);
        
        try {
            Long scheduleId = getLongValue(scheduleEvent, "scheduleId");
            
            // Trouver la réservation correspondante
            Optional<Reservation> existingReservation = findReservationByScheduleId(scheduleId);
            
            if (existingReservation.isPresent()) {
                // Mettre à jour la réservation existante
                Reservation updatedReservation = updateReservationFromSchedule(
                    existingReservation.get(), scheduleEvent
                );
                
                log.info("Updated reservation {} for schedule {}", 
                    updatedReservation.getId(), scheduleId);
                
                notifyReservationUpdated(updatedReservation, scheduleEvent);
                
            } else {
                // Créer une nouvelle réservation si elle n'existe pas
                log.warn("No existing reservation found for schedule {}, creating new one", scheduleId);
                Reservation newReservation = createReservationFromSchedule(scheduleEvent);
                
                if (newReservation != null) {
                    notifyReservationCreated(newReservation, scheduleEvent);
                }
            }
            
        } catch (Exception e) {
            log.error("Error handling schedule updated event", e);
            notifyScheduleReservationError(scheduleEvent, "UPDATE_FAILED", e.getMessage());
        }
    }
    
    /**
     * Écoute les événements de suppression d'emploi du temps
     */
    @RabbitListener(queues = "schedule-deleted")
    @Transactional
    public void handleScheduleDeleted(Map<String, Object> scheduleEvent) {
        log.info("Handling schedule deleted event: {}", scheduleEvent);
        
        try {
            Long scheduleId = getLongValue(scheduleEvent, "scheduleId");
            
            // Trouver et supprimer la réservation correspondante
            Optional<Reservation> existingReservation = findReservationByScheduleId(scheduleId);
            
            if (existingReservation.isPresent()) {
                Reservation reservation = existingReservation.get();
                
                // Marquer comme annulée plutôt que supprimer
                reservation.setStatus(ReservationStatus.CANCELLED);
                reservation.setUpdatedAt(LocalDateTime.now());
                reservation.setNotes(reservation.getNotes() + " [Annulée automatiquement - emploi du temps supprimé]");
                
                reservationRepository.save(reservation);
                
                log.info("Cancelled reservation {} for deleted schedule {}", 
                    reservation.getId(), scheduleId);
                
                notifyReservationCancelled(reservation, scheduleEvent);
                
            } else {
                log.warn("No reservation found for deleted schedule {}", scheduleId);
            }
            
        } catch (Exception e) {
            log.error("Error handling schedule deleted event", e);
            notifyScheduleReservationError(scheduleEvent, "DELETION_FAILED", e.getMessage());
        }
    }
    
    /**
     * Écoute les événements de modification de réservation pour synchroniser l'emploi du temps
     */
    @RabbitListener(queues = "reservation-updated")
    @Transactional
    public void handleReservationUpdated(Map<String, Object> reservationEvent) {
        log.info("Handling reservation updated event: {}", reservationEvent);
        
        try {
            Long reservationId = getLongValue(reservationEvent, "reservationId");
            Long scheduleId = getLongValue(reservationEvent, "scheduleId");
            
            if (scheduleId != null) {
                // Synchroniser les changements vers l'emploi du temps
                syncReservationToSchedule(reservationId, scheduleId, reservationEvent);
            }
            
        } catch (Exception e) {
            log.error("Error handling reservation updated event", e);
        }
    }
    
    /**
     * Crée une réservation à partir d'un emploi du temps
     */
    private Reservation createReservationFromSchedule(Map<String, Object> scheduleEvent) {
        try {
            ReservationDTO reservationDTO = new ReservationDTO();
            
            // Mapper les données de l'emploi du temps vers la réservation
            reservationDTO.setResourceId(getLongValue(scheduleEvent, "roomId"));
            reservationDTO.setUserId(getLongValue(scheduleEvent, "teacherId"));
            reservationDTO.setCourseId(getLongValue(scheduleEvent, "courseId"));
            reservationDTO.setCourseGroupId(getLongValue(scheduleEvent, "groupId"));
            
            // Dates et heures
            reservationDTO.setStartTime(parseDateTime(scheduleEvent, "startTime"));
            reservationDTO.setEndTime(parseDateTime(scheduleEvent, "endTime"));
            
            // Détails
            reservationDTO.setTitle((String) scheduleEvent.get("title"));
            reservationDTO.setDescription("Réservation automatique pour: " + scheduleEvent.get("title"));
            reservationDTO.setExpectedAttendees(getIntegerValue(scheduleEvent, "expectedAttendees", 30));
            
            // Type et statut
            reservationDTO.setType(ReservationType.COURSE);
            reservationDTO.setStatus(ReservationStatus.CONFIRMED); // Auto-confirmée pour les emplois du temps
            
            // Métadonnées
            reservationDTO.setNotes("Créée automatiquement depuis l'emploi du temps ID: " + 
                scheduleEvent.get("scheduleId"));
            
            // Créer la réservation
            ReservationDTO createdDTO = reservationService.createReservation(reservationDTO);
            
            // Convertir le DTO en entity pour le retour
            if (createdDTO != null) {
                return reservationRepository.findById(createdDTO.getId()).orElse(null);
            }
            return null;
            
        } catch (Exception e) {
            log.error("Error creating reservation from schedule", e);
            return null;
        }
    }
    
    /**
     * Met à jour une réservation à partir d'un emploi du temps modifié
     */
    private Reservation updateReservationFromSchedule(Reservation existingReservation, 
                                                     Map<String, Object> scheduleEvent) {
        try {
            // Mettre à jour les champs modifiables
            Long newResourceId = getLongValue(scheduleEvent, "roomId");
            if (newResourceId != null && !newResourceId.equals(existingReservation.getResourceId())) {
                existingReservation.setResourceId(newResourceId);
            }
            
            LocalDateTime newStartTime = parseDateTime(scheduleEvent, "startTime");
            if (newStartTime != null && !newStartTime.equals(existingReservation.getStartTime())) {
                existingReservation.setStartTime(newStartTime);
            }
            
            LocalDateTime newEndTime = parseDateTime(scheduleEvent, "endTime");
            if (newEndTime != null && !newEndTime.equals(existingReservation.getEndTime())) {
                existingReservation.setEndTime(newEndTime);
            }
            
            String newTitle = (String) scheduleEvent.get("title");
            if (newTitle != null && !newTitle.equals(existingReservation.getTitle())) {
                existingReservation.setTitle(newTitle);
            }
            
            // Mettre à jour les métadonnées
            existingReservation.setUpdatedAt(LocalDateTime.now());
            existingReservation.setNotes(existingReservation.getNotes() + 
                " [Mise à jour automatique depuis l'emploi du temps]");
            
            return reservationRepository.save(existingReservation);
            
        } catch (Exception e) {
            log.error("Error updating reservation from schedule", e);
            throw e;
        }
    }
    
    /**
     * Synchronise les changements de réservation vers l'emploi du temps
     */
    private void syncReservationToSchedule(Long reservationId, Long scheduleId, Map<String, Object> reservationEvent) {
        try {
            // Préparer l'événement de synchronisation
            Map<String, Object> syncEvent = Map.of(
                "scheduleId", scheduleId,
                "reservationId", reservationId,
                "syncType", "RESERVATION_TO_SCHEDULE",
                "changes", reservationEvent,
                "timestamp", LocalDateTime.now()
            );
            
            // Envoyer vers le service de planification
            rabbitTemplate.convertAndSend("schedule-sync", syncEvent);
            
            log.info("Sent schedule sync event for reservation {} -> schedule {}", 
                reservationId, scheduleId);
            
        } catch (Exception e) {
            log.error("Error syncing reservation to schedule", e);
        }
    }
    
    /**
     * Trouve une réservation par ID d'emploi du temps
     */
    private Optional<Reservation> findReservationByScheduleId(Long scheduleId) {
        // Rechercher par notes contenant l'ID de l'emploi du temps
        return reservationRepository.findAll().stream()
            .filter(r -> r.getNotes() != null && r.getNotes().contains("emploi du temps ID: " + scheduleId))
            .findFirst();
    }
    
    /**
     * Notifie la création d'une réservation
     */
    private void notifyReservationCreated(Reservation reservation, Map<String, Object> scheduleEvent) {
        try {
            Map<String, Object> notification = Map.of(
                "event", "reservation.created.from.schedule",
                "reservationId", reservation.getId(),
                "scheduleId", scheduleEvent.get("scheduleId"),
                "resourceId", reservation.getResourceId(),
                "startTime", reservation.getStartTime(),
                "endTime", reservation.getEndTime(),
                "title", reservation.getTitle(),
                "timestamp", LocalDateTime.now()
            );
            
            rabbitTemplate.convertAndSend("reservation-notifications", notification);
            
        } catch (Exception e) {
            log.error("Error sending reservation created notification", e);
        }
    }
    
    /**
     * Notifie la mise à jour d'une réservation
     */
    private void notifyReservationUpdated(Reservation reservation, Map<String, Object> scheduleEvent) {
        try {
            Map<String, Object> notification = Map.of(
                "event", "reservation.updated.from.schedule",
                "reservationId", reservation.getId(),
                "scheduleId", scheduleEvent.get("scheduleId"),
                "resourceId", reservation.getResourceId(),
                "startTime", reservation.getStartTime(),
                "endTime", reservation.getEndTime(),
                "title", reservation.getTitle(),
                "timestamp", LocalDateTime.now()
            );
            
            rabbitTemplate.convertAndSend("reservation-notifications", notification);
            
        } catch (Exception e) {
            log.error("Error sending reservation updated notification", e);
        }
    }
    
    /**
     * Notifie l'annulation d'une réservation
     */
    private void notifyReservationCancelled(Reservation reservation, Map<String, Object> scheduleEvent) {
        try {
            Map<String, Object> notification = Map.of(
                "event", "reservation.cancelled.from.schedule",
                "reservationId", reservation.getId(),
                "scheduleId", scheduleEvent.get("scheduleId"),
                "resourceId", reservation.getResourceId(),
                "title", reservation.getTitle(),
                "reason", "Emploi du temps supprimé",
                "timestamp", LocalDateTime.now()
            );
            
            rabbitTemplate.convertAndSend("reservation-notifications", notification);
            
        } catch (Exception e) {
            log.error("Error sending reservation cancelled notification", e);
        }
    }
    
    /**
     * Notifie une erreur de synchronisation
     */
    private void notifyScheduleReservationError(Map<String, Object> scheduleEvent, String errorType, String errorMessage) {
        try {
            Map<String, Object> errorNotification = Map.of(
                "event", "schedule.reservation.sync.error",
                "scheduleId", scheduleEvent.get("scheduleId"),
                "errorType", errorType,
                "errorMessage", errorMessage,
                "originalEvent", scheduleEvent,
                "timestamp", LocalDateTime.now()
            );
            
            rabbitTemplate.convertAndSend("sync-errors", errorNotification);
            
        } catch (Exception e) {
            log.error("Error sending sync error notification", e);
        }
    }
    
    // Méthodes utilitaires
    private Long getLongValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        } else if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
    
    private Integer getIntegerValue(Map<String, Object> map, String key, Integer defaultValue) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
    
    private LocalDateTime parseDateTime(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof LocalDateTime) {
            return (LocalDateTime) value;
        } else if (value instanceof String) {
            try {
                return LocalDateTime.parse((String) value);
            } catch (Exception e) {
                log.warn("Error parsing datetime from string: {}", value);
                return null;
            }
        }
        return null;
    }
}