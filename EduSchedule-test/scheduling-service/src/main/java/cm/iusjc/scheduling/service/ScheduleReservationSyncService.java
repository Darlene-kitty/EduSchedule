package cm.iusjc.scheduling.service;

import cm.iusjc.scheduling.entity.Schedule;
import cm.iusjc.scheduling.dto.ScheduleChangedEvent;
import cm.iusjc.scheduling.dto.ReservationRequest;
import cm.iusjc.scheduling.dto.ReservationDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleReservationSyncService {
    
    private final RestTemplate restTemplate;
    private static final String RESERVATION_SERVICE_URL = "http://reservation-service";
    
    @EventListener
    public void onScheduleCreated(ScheduleChangedEvent event) {
        if (event.getChangeType() == ScheduleChangedEvent.ChangeType.CREATED) {
            log.info("Creating reservation for new schedule: {}", event.getScheduleId());
            createReservationFromSchedule(event.getSchedule());
        }
    }
    
    @EventListener
    public void onScheduleUpdated(ScheduleChangedEvent event) {
        if (event.getChangeType() == ScheduleChangedEvent.ChangeType.UPDATED) {
            log.info("Updating reservation for modified schedule: {}", event.getScheduleId());
            updateReservationFromSchedule(event.getSchedule(), event.getOldSchedule());
        }
    }
    
    @EventListener
    public void onScheduleCancelled(ScheduleChangedEvent event) {
        if (event.getChangeType() == ScheduleChangedEvent.ChangeType.CANCELLED) {
            log.info("Cancelling reservation for cancelled schedule: {}", event.getScheduleId());
            cancelReservationFromSchedule(event.getSchedule());
        }
    }
    
    private void createReservationFromSchedule(Schedule schedule) {
        try {
            // Récupérer l'ID de la ressource (salle) depuis le service de ressources
            Long resourceId = getResourceIdByName(schedule.getRoom());
            if (resourceId == null) {
                log.warn("Cannot create reservation: room '{}' not found", schedule.getRoom());
                return;
            }
            
            // Récupérer l'ID du cours depuis le service de cours
            Long courseId = getCourseIdByName(schedule.getCourse());
            
            // Récupérer l'ID de l'enseignant
            Long teacherId = getTeacherIdByName(schedule.getTeacher());
            
            ReservationRequest reservationRequest = new ReservationRequest();
            reservationRequest.setResourceId(resourceId);
            reservationRequest.setCourseId(courseId);
            reservationRequest.setUserId(teacherId != null ? teacherId : 1L); // Utilisateur par défaut si enseignant non trouvé
            reservationRequest.setTitle(schedule.getTitle());
            reservationRequest.setDescription("Réservation automatique depuis l'emploi du temps: " + schedule.getDescription());
            reservationRequest.setStartTime(schedule.getStartTime());
            reservationRequest.setEndTime(schedule.getEndTime());
            reservationRequest.setType("COURSE");
            reservationRequest.setSetupTime(15); // 15 minutes de préparation
            reservationRequest.setCleanupTime(10); // 10 minutes de nettoyage
            reservationRequest.setNotes("Synchronisé depuis l'emploi du temps ID: " + schedule.getId());
            
            // Calculer le nombre d'étudiants attendus
            Integer expectedAttendees = estimateAttendeesFromGroup(schedule.getGroupName());
            reservationRequest.setExpectedAttendees(expectedAttendees);
            
            String url = RESERVATION_SERVICE_URL + "/api/reservations";
            ReservationDTO createdReservation = restTemplate.postForObject(url, reservationRequest, ReservationDTO.class);
            
            if (createdReservation != null) {
                log.info("Reservation created successfully: {} for schedule: {}", 
                        createdReservation.getId(), schedule.getId());
                
                // Stocker la liaison schedule-reservation pour les futures mises à jour
                storeScheduleReservationMapping(schedule.getId(), createdReservation.getId());
            }
            
        } catch (Exception e) {
            log.error("Failed to create reservation for schedule {}: {}", schedule.getId(), e.getMessage());
        }
    }
    
    private void updateReservationFromSchedule(Schedule newSchedule, Schedule oldSchedule) {
        try {
            // Récupérer l'ID de la réservation liée
            Long reservationId = getReservationIdByScheduleId(newSchedule.getId());
            if (reservationId == null) {
                log.warn("No reservation found for schedule: {}", newSchedule.getId());
                return;
            }
            
            // Vérifier si la salle a changé
            Long resourceId = getResourceIdByName(newSchedule.getRoom());
            if (resourceId == null) {
                log.warn("Cannot update reservation: room '{}' not found", newSchedule.getRoom());
                return;
            }
            
            Long courseId = getCourseIdByName(newSchedule.getCourse());
            Long teacherId = getTeacherIdByName(newSchedule.getTeacher());
            
            ReservationRequest updateRequest = new ReservationRequest();
            updateRequest.setResourceId(resourceId);
            updateRequest.setCourseId(courseId);
            updateRequest.setUserId(teacherId != null ? teacherId : 1L);
            updateRequest.setTitle(newSchedule.getTitle());
            updateRequest.setDescription("Réservation mise à jour depuis l'emploi du temps: " + newSchedule.getDescription());
            updateRequest.setStartTime(newSchedule.getStartTime());
            updateRequest.setEndTime(newSchedule.getEndTime());
            updateRequest.setType("COURSE");
            updateRequest.setSetupTime(15);
            updateRequest.setCleanupTime(10);
            updateRequest.setNotes("Synchronisé depuis l'emploi du temps ID: " + newSchedule.getId());
            
            Integer expectedAttendees = estimateAttendeesFromGroup(newSchedule.getGroupName());
            updateRequest.setExpectedAttendees(expectedAttendees);
            
            String url = RESERVATION_SERVICE_URL + "/api/reservations/" + reservationId;
            restTemplate.put(url, updateRequest);
            
            log.info("Reservation updated successfully: {} for schedule: {}", reservationId, newSchedule.getId());
            
        } catch (Exception e) {
            log.error("Failed to update reservation for schedule {}: {}", newSchedule.getId(), e.getMessage());
        }
    }
    
    private void cancelReservationFromSchedule(Schedule schedule) {
        try {
            Long reservationId = getReservationIdByScheduleId(schedule.getId());
            if (reservationId == null) {
                log.warn("No reservation found for schedule: {}", schedule.getId());
                return;
            }
            
            String url = RESERVATION_SERVICE_URL + "/api/reservations/" + reservationId + "/cancel" +
                        "?cancelledBy=1&reason=Cours annulé dans l'emploi du temps";
            
            restTemplate.postForObject(url, null, Void.class);
            
            log.info("Reservation cancelled successfully: {} for schedule: {}", reservationId, schedule.getId());
            
        } catch (Exception e) {
            log.error("Failed to cancel reservation for schedule {}: {}", schedule.getId(), e.getMessage());
        }
    }
    
    private Long getResourceIdByName(String roomName) {
        if (roomName == null || roomName.trim().isEmpty()) {
            return null;
        }
        
        try {
            String url = "http://resource-service/api/resources/search?name=" + roomName;
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response != null && response.containsKey("id")) {
                return Long.valueOf(response.get("id").toString());
            }
            
        } catch (Exception e) {
            log.warn("Failed to get resource ID for room '{}': {}", roomName, e.getMessage());
        }
        
        return null;
    }
    
    private Long getCourseIdByName(String courseName) {
        if (courseName == null || courseName.trim().isEmpty()) {
            return null;
        }
        
        try {
            String url = "http://course-service/api/v1/courses/search?query=" + courseName;
            Map[] courses = restTemplate.getForObject(url, Map[].class);
            
            if (courses != null && courses.length > 0) {
                return Long.valueOf(courses[0].get("id").toString());
            }
            
        } catch (Exception e) {
            log.warn("Failed to get course ID for course '{}': {}", courseName, e.getMessage());
        }
        
        return null;
    }
    
    private Long getTeacherIdByName(String teacherName) {
        if (teacherName == null || teacherName.trim().isEmpty()) {
            return null;
        }
        
        try {
            String url = "http://user-service/api/users/search?name=" + teacherName + "&role=TEACHER";
            Map[] users = restTemplate.getForObject(url, Map[].class);
            
            if (users != null && users.length > 0) {
                return Long.valueOf(users[0].get("id").toString());
            }
            
        } catch (Exception e) {
            log.warn("Failed to get teacher ID for teacher '{}': {}", teacherName, e.getMessage());
        }
        
        return null;
    }
    
    private Integer estimateAttendeesFromGroup(String groupName) {
        if (groupName == null || groupName.trim().isEmpty()) {
            return 30; // Défaut
        }
        
        // Logique d'estimation basée sur le nom du groupe
        String group = groupName.toLowerCase();
        
        if (group.contains("amphi") || group.contains("conférence")) {
            return 200; // Amphithéâtre
        } else if (group.contains("tp") || group.contains("labo")) {
            return 15; // TP/Laboratoire
        } else if (group.contains("td")) {
            return 25; // TD
        } else if (group.matches(".*[lm][12].*")) {
            return 40; // Licence/Master
        }
        
        return 30; // Défaut
    }
    
    private void storeScheduleReservationMapping(Long scheduleId, Long reservationId) {
        try {
            // Stocker la liaison dans une table de mapping ou cache
            // Pour simplifier, on utilise un appel REST vers un service de mapping
            String url = "http://scheduling-service/api/internal/schedule-reservation-mapping";
            Map<String, Long> mapping = Map.of(
                "scheduleId", scheduleId,
                "reservationId", reservationId
            );
            
            restTemplate.postForObject(url, mapping, Void.class);
            
        } catch (Exception e) {
            log.warn("Failed to store schedule-reservation mapping: {}", e.getMessage());
        }
    }
    
    private Long getReservationIdByScheduleId(Long scheduleId) {
        try {
            String url = "http://scheduling-service/api/internal/schedule-reservation-mapping/" + scheduleId;
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response != null && response.containsKey("reservationId")) {
                return Long.valueOf(response.get("reservationId").toString());
            }
            
        } catch (Exception e) {
            log.warn("Failed to get reservation ID for schedule {}: {}", scheduleId, e.getMessage());
        }
        
        return null;
    }
}