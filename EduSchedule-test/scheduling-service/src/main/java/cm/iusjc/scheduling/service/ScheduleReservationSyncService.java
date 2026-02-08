package cm.iusjc.scheduling.service;

import cm.iusjc.scheduling.dto.ScheduleDTO;
import cm.iusjc.scheduling.entity.Schedule;
import cm.iusjc.scheduling.event.ScheduleChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleReservationSyncService {
    
    private final RestTemplate restTemplate;
    private final String RESERVATION_SERVICE_URL = "http://reservation-service/api/v1/reservations";
    
    /**
     * Synchronise automatiquement les emplois du temps avec les réservations
     */
    @EventListener
    public void handleScheduleCreated(ScheduleChangedEvent event) {
        if (event.getChangeType() == ScheduleChangedEvent.ChangeType.CREATED) {
            createReservationFromSchedule(event.getSchedule());
        } else if (event.getChangeType() == ScheduleChangedEvent.ChangeType.UPDATED) {
            updateReservationFromSchedule(event.getSchedule(), event.getOldSchedule());
        } else if (event.getChangeType() == ScheduleChangedEvent.ChangeType.CANCELLED) {
            cancelReservationFromSchedule(event.getSchedule());
        }
    }
    
    /**
     * Crée automatiquement une réservation lors de la création d'un emploi du temps
     */
    private void createReservationFromSchedule(Schedule schedule) {
        try {
            log.info("Creating reservation from schedule: {}", schedule.getId());
            
            // Récupérer l'ID de la salle depuis le room-service
            Long roomId = getRoomIdByName(schedule.getRoom());
            if (roomId == null) {
                log.warn("Room not found for schedule: {} - {}", schedule.getId(), schedule.getRoom());
                return;
            }
            
            // Créer la demande de réservation
            Map<String, Object> reservationRequest = new HashMap<>();
            reservationRequest.put("resourceId", roomId);
            reservationRequest.put("title", schedule.getTitle());
            reservationRequest.put("description", "Réservation automatique depuis emploi du temps: " + schedule.getDescription());
            reservationRequest.put("startTime", schedule.getStartTime().toString());
            reservationRequest.put("endTime", schedule.getEndTime().toString());
            reservationRequest.put("type", "COURSE");
            reservationRequest.put("userId", 1L); // TODO: Récupérer l'utilisateur réel
            reservationRequest.put("courseId", schedule.getCourse());
            reservationRequest.put("courseGroupId", schedule.getGroupName());
            reservationRequest.put("setupTime", 15); // 15 minutes de préparation
            reservationRequest.put("cleanupTime", 10); // 10 minutes de nettoyage
            reservationRequest.put("notes", "Synchronisé depuis emploi du temps ID: " + schedule.getId());
            
            // Envoyer la requête
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(reservationRequest, headers);
            
            Map<String, Object> response = restTemplate.postForObject(
                RESERVATION_SERVICE_URL + "/sync-from-schedule", 
                entity, 
                Map.class
            );
            
            if (response != null && response.get("id") != null) {
                log.info("Reservation created successfully: {} for schedule: {}", 
                    response.get("id"), schedule.getId());
                
                // Optionnel: Stocker la liaison schedule-reservation
                updateScheduleWithReservationId(schedule.getId(), (Long) response.get("id"));
            }
            
        } catch (Exception e) {
            log.error("Error creating reservation from schedule {}: {}", schedule.getId(), e.getMessage());
        }
    }
    
    /**
     * Met à jour la réservation lors de la modification d'un emploi du temps
     */
    private void updateReservationFromSchedule(Schedule newSchedule, Schedule oldSchedule) {
        try {
            log.info("Updating reservation from schedule: {}", newSchedule.getId());
            
            // Récupérer l'ID de réservation associé
            Long reservationId = getReservationIdBySchedule(newSchedule.getId());
            if (reservationId == null) {
                log.warn("No reservation found for schedule: {}", newSchedule.getId());
                // Créer une nouvelle réservation si elle n'existe pas
                createReservationFromSchedule(newSchedule);
                return;
            }
            
            // Vérifier si la salle a changé
            Long roomId = getRoomIdByName(newSchedule.getRoom());
            if (roomId == null) {
                log.warn("Room not found for updated schedule: {} - {}", 
                    newSchedule.getId(), newSchedule.getRoom());
                return;
            }
            
            // Créer la demande de mise à jour
            Map<String, Object> updateRequest = new HashMap<>();
            updateRequest.put("resourceId", roomId);
            updateRequest.put("title", newSchedule.getTitle());
            updateRequest.put("description", "Réservation mise à jour depuis emploi du temps: " + newSchedule.getDescription());
            updateRequest.put("startTime", newSchedule.getStartTime().toString());
            updateRequest.put("endTime", newSchedule.getEndTime().toString());
            updateRequest.put("courseId", newSchedule.getCourse());
            updateRequest.put("courseGroupId", newSchedule.getGroupName());
            updateRequest.put("notes", "Synchronisé depuis emploi du temps ID: " + newSchedule.getId() + 
                " (Mis à jour le " + LocalDateTime.now() + ")");
            
            // Envoyer la requête de mise à jour
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(updateRequest, headers);
            
            restTemplate.exchange(
                RESERVATION_SERVICE_URL + "/" + reservationId + "/sync-update",
                HttpMethod.PUT,
                entity,
                Map.class
            );
            
            log.info("Reservation updated successfully: {} for schedule: {}", 
                reservationId, newSchedule.getId());
                
        } catch (Exception e) {
            log.error("Error updating reservation from schedule {}: {}", newSchedule.getId(), e.getMessage());
        }
    }
    
    /**
     * Annule la réservation lors de l'annulation d'un emploi du temps
     */
    private void cancelReservationFromSchedule(Schedule schedule) {
        try {
            log.info("Cancelling reservation from schedule: {}", schedule.getId());
            
            Long reservationId = getReservationIdBySchedule(schedule.getId());
            if (reservationId == null) {
                log.warn("No reservation found to cancel for schedule: {}", schedule.getId());
                return;
            }
            
            // Créer la demande d'annulation
            Map<String, Object> cancelRequest = new HashMap<>();
            cancelRequest.put("reason", "Emploi du temps annulé - ID: " + schedule.getId());
            cancelRequest.put("cancelledBy", 1L); // TODO: Récupérer l'utilisateur réel
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(cancelRequest, headers);
            
            restTemplate.exchange(
                RESERVATION_SERVICE_URL + "/" + reservationId + "/cancel-from-schedule",
                HttpMethod.PUT,
                entity,
                Map.class
            );
            
            log.info("Reservation cancelled successfully: {} for schedule: {}", 
                reservationId, schedule.getId());
                
        } catch (Exception e) {
            log.error("Error cancelling reservation from schedule {}: {}", schedule.getId(), e.getMessage());
        }
    }
    
    /**
     * Synchronisation inverse: met à jour l'emploi du temps depuis une réservation
     */
    public void syncScheduleFromReservation(Long reservationId, Map<String, Object> reservationData) {
        try {
            log.info("Syncing schedule from reservation: {}", reservationId);
            
            // Récupérer l'emploi du temps associé
            Long scheduleId = getScheduleIdByReservation(reservationId);
            if (scheduleId == null) {
                log.info("No schedule found for reservation: {}, creating new one", reservationId);
                createScheduleFromReservation(reservationData);
                return;
            }
            
            // Mettre à jour l'emploi du temps existant
            updateScheduleFromReservation(scheduleId, reservationData);
            
        } catch (Exception e) {
            log.error("Error syncing schedule from reservation {}: {}", reservationId, e.getMessage());
        }
    }
    
    private void createScheduleFromReservation(Map<String, Object> reservationData) {
        // TODO: Implémenter la création d'emploi du temps depuis une réservation
        log.info("Creating schedule from reservation data");
    }
    
    private void updateScheduleFromReservation(Long scheduleId, Map<String, Object> reservationData) {
        // TODO: Implémenter la mise à jour d'emploi du temps depuis une réservation
        log.info("Updating schedule {} from reservation data", scheduleId);
    }
    
    /**
     * Utilitaires pour récupérer les IDs
     */
    private Long getRoomIdByName(String roomName) {
        try {
            Map<String, Object> response = restTemplate.getForObject(
                "http://room-service/api/rooms/by-name/" + roomName, 
                Map.class
            );
            return response != null ? ((Number) response.get("id")).longValue() : null;
        } catch (Exception e) {
            log.warn("Error getting room ID for name {}: {}", roomName, e.getMessage());
            return null;
        }
    }
    
    private Long getReservationIdBySchedule(Long scheduleId) {
        try {
            Map<String, Object> response = restTemplate.getForObject(
                RESERVATION_SERVICE_URL + "/by-schedule/" + scheduleId, 
                Map.class
            );
            return response != null ? ((Number) response.get("id")).longValue() : null;
        } catch (Exception e) {
            log.warn("Error getting reservation ID for schedule {}: {}", scheduleId, e.getMessage());
            return null;
        }
    }
    
    private Long getScheduleIdByReservation(Long reservationId) {
        try {
            Map<String, Object> response = restTemplate.getForObject(
                "http://scheduling-service/api/schedules/by-reservation/" + reservationId, 
                Map.class
            );
            return response != null ? ((Number) response.get("id")).longValue() : null;
        } catch (Exception e) {
            log.warn("Error getting schedule ID for reservation {}: {}", reservationId, e.getMessage());
            return null;
        }
    }
    
    private void updateScheduleWithReservationId(Long scheduleId, Long reservationId) {
        // TODO: Implémenter le stockage de la liaison schedule-reservation
        log.info("Linking schedule {} with reservation {}", scheduleId, reservationId);
    }
}