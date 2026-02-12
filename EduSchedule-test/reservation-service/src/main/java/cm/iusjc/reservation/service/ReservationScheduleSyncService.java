package cm.iusjc.reservation.service;

import cm.iusjc.reservation.entity.Reservation;
import cm.iusjc.reservation.entity.ReservationStatus;
import cm.iusjc.reservation.entity.ReservationType;
import cm.iusjc.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationScheduleSyncService {

    private final ReservationRepository reservationRepository;
    private final RestTemplate restTemplate;
    private final ResourceService resourceService;

    @Value("${scheduling.service.url:http://localhost:8086}")
    private String schedulingServiceUrl;

    /**
     * Crée une réservation depuis les données d'emploi du temps
     */
    public Map<String, Object> createReservationFromSchedule(Map<String, Object> scheduleData) {
        try {
            log.info("Création réservation depuis Schedule");
            
            Reservation reservation = buildReservationFromScheduleData(scheduleData);
            
            // Vérifier les conflits
            if (hasConflicts(reservation)) {
                throw new RuntimeException("Conflit détecté avec une réservation existante");
            }
            
            reservation = reservationRepository.save(reservation);
            
            log.info("Réservation créée avec ID: {}", reservation.getId());
            
            return Map.of(
                "reservationId", reservation.getId(),
                "status", "created",
                "message", "Réservation créée avec succès"
            );
            
        } catch (Exception e) {
            log.error("Erreur création réservation: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de la création: " + e.getMessage());
        }
    }

    /**
     * Met à jour une réservation depuis les données d'emploi du temps
     */
    public Map<String, Object> updateReservationFromSchedule(Long scheduleId, Map<String, Object> scheduleData) {
        try {
            log.info("Mise à jour réservation pour Schedule ID: {}", scheduleId);
            
            // Chercher la réservation liée à ce schedule
            Optional<Reservation> existingReservation = findReservationByScheduleId(scheduleId);
            
            if (existingReservation.isPresent()) {
                Reservation reservation = existingReservation.get();
                updateReservationFromScheduleData(reservation, scheduleData);
                
                // Vérifier les conflits (en excluant cette réservation)
                if (hasConflictsExcluding(reservation, reservation.getId())) {
                    throw new RuntimeException("Conflit détecté avec une autre réservation");
                }
                
                reservation = reservationRepository.save(reservation);
                
                return Map.of(
                    "reservationId", reservation.getId(),
                    "status", "updated",
                    "message", "Réservation mise à jour avec succès"
                );
            } else {
                // Créer une nouvelle réservation si elle n'existe pas
                return createReservationFromSchedule(scheduleData);
            }
            
        } catch (Exception e) {
            log.error("Erreur mise à jour réservation: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de la mise à jour: " + e.getMessage());
        }
    }

    /**
     * Supprime une réservation liée à un emploi du temps
     */
    public void deleteReservationFromSchedule(Long scheduleId) {
        try {
            log.info("Suppression réservation pour Schedule ID: {}", scheduleId);
            
            Optional<Reservation> reservation = findReservationByScheduleId(scheduleId);
            
            if (reservation.isPresent()) {
                reservationRepository.delete(reservation.get());
                log.info("Réservation supprimée pour Schedule ID: {}", scheduleId);
            } else {
                log.warn("Aucune réservation trouvée pour Schedule ID: {}", scheduleId);
            }
            
        } catch (Exception e) {
            log.error("Erreur suppression réservation: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de la suppression: " + e.getMessage());
        }
    }

    /**
     * Synchronise une réservation vers l'emploi du temps
     */
    public void syncReservationToSchedule(Reservation reservation) {
        try {
            log.info("Synchronisation Reservation -> Schedule pour ID: {}", reservation.getId());
            
            Map<String, Object> scheduleData = buildScheduleDataFromReservation(reservation);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(scheduleData, headers);
            
            String url = schedulingServiceUrl + "/api/schedules/sync-from-reservation";
            restTemplate.postForObject(url, request, Map.class);
            
            log.info("Synchronisation réussie pour Reservation ID: {}", reservation.getId());
            
        } catch (Exception e) {
            log.error("Erreur synchronisation vers Schedule: {}", e.getMessage());
        }
    }

    /**
     * Obtient le statut de synchronisation
     */
    public Map<String, Object> getSyncStatus(Long scheduleId) {
        Optional<Reservation> reservation = findReservationByScheduleId(scheduleId);
        
        if (reservation.isPresent()) {
            Reservation res = reservation.get();
            return Map.of(
                "scheduleId", scheduleId,
                "reservationId", res.getId(),
                "synchronized", true,
                "status", res.getStatus().toString(),
                "lastUpdated", res.getUpdatedAt().toString()
            );
        } else {
            return Map.of(
                "scheduleId", scheduleId,
                "synchronized", false,
                "message", "Aucune réservation liée trouvée"
            );
        }
    }

    /**
     * Construit une réservation depuis les données d'emploi du temps
     */
    private Reservation buildReservationFromScheduleData(Map<String, Object> scheduleData) {
        Reservation reservation = new Reservation();
        
        reservation.setTitle((String) scheduleData.get("title"));
        reservation.setDescription((String) scheduleData.get("description"));
        reservation.setStartTime(LocalDateTime.parse((String) scheduleData.get("startTime")));
        reservation.setEndTime(LocalDateTime.parse((String) scheduleData.get("endTime")));
        
        // Mapping du statut
        String status = (String) scheduleData.get("status");
        reservation.setStatus(mapScheduleStatusToReservationStatus(status));
        
        // Type de réservation
        reservation.setType(ReservationType.COURSE);
        
        // Recherche de la ressource par nom
        String resourceName = (String) scheduleData.get("resourceName");
        if (resourceName != null) {
            Long resourceId = resourceService.findResourceIdByName(resourceName);
            reservation.setResourceId(resourceId);
        }
        
        // Utilisateur par défaut (système)
        reservation.setUserId(1L); // ID utilisateur système
        
        // Métadonnées de synchronisation
        reservation.setNotes("Synchronisé depuis emploi du temps - Schedule ID: " + scheduleData.get("scheduleId"));
        
        return reservation;
    }

    /**
     * Met à jour une réservation depuis les données d'emploi du temps
     */
    private void updateReservationFromScheduleData(Reservation reservation, Map<String, Object> scheduleData) {
        reservation.setTitle((String) scheduleData.get("title"));
        reservation.setDescription((String) scheduleData.get("description"));
        reservation.setStartTime(LocalDateTime.parse((String) scheduleData.get("startTime")));
        reservation.setEndTime(LocalDateTime.parse((String) scheduleData.get("endTime")));
        
        String status = (String) scheduleData.get("status");
        reservation.setStatus(mapScheduleStatusToReservationStatus(status));
        
        String resourceName = (String) scheduleData.get("resourceName");
        if (resourceName != null) {
            Long resourceId = resourceService.findResourceIdByName(resourceName);
            reservation.setResourceId(resourceId);
        }
    }

    /**
     * Construit les données d'emploi du temps depuis une réservation
     */
    private Map<String, Object> buildScheduleDataFromReservation(Reservation reservation) {
        Map<String, Object> data = new HashMap<>();
        
        data.put("title", reservation.getTitle());
        data.put("description", reservation.getDescription());
        data.put("startTime", reservation.getStartTime().toString());
        data.put("endTime", reservation.getEndTime().toString());
        data.put("status", mapReservationStatusToScheduleStatus(reservation.getStatus()));
        
        // Informations sur la ressource
        String resourceName = resourceService.getResourceNameById(reservation.getResourceId());
        data.put("room", resourceName);
        
        data.put("reservationId", reservation.getId());
        data.put("syncSource", "RESERVATION");
        
        return data;
    }

    /**
     * Trouve une réservation par ID d'emploi du temps
     */
    private Optional<Reservation> findReservationByScheduleId(Long scheduleId) {
        // Recherche par notes contenant l'ID du schedule
        return reservationRepository.findAll().stream()
            .filter(r -> r.getNotes() != null && r.getNotes().contains("Schedule ID: " + scheduleId))
            .findFirst();
    }

    /**
     * Vérifie les conflits
     */
    private boolean hasConflicts(Reservation reservation) {
        return reservationRepository.findAll().stream()
            .anyMatch(existing -> existing.getResourceId().equals(reservation.getResourceId()) &&
                     existing.isConflictWith(reservation.getStartTime(), reservation.getEndTime()));
    }

    /**
     * Vérifie les conflits en excluant une réservation
     */
    private boolean hasConflictsExcluding(Reservation reservation, Long excludeId) {
        return reservationRepository.findAll().stream()
            .filter(r -> !r.getId().equals(excludeId))
            .anyMatch(existing -> existing.getResourceId().equals(reservation.getResourceId()) &&
                     existing.isConflictWith(reservation.getStartTime(), reservation.getEndTime()));
    }

    /**
     * Mappe le statut d'emploi du temps vers le statut de réservation
     */
    private ReservationStatus mapScheduleStatusToReservationStatus(String scheduleStatus) {
        return switch (scheduleStatus) {
            case "ACTIVE" -> ReservationStatus.CONFIRMED;
            case "CANCELLED" -> ReservationStatus.CANCELLED;
            case "COMPLETED" -> ReservationStatus.COMPLETED;
            default -> ReservationStatus.PENDING;
        };
    }

    /**
     * Mappe le statut de réservation vers le statut d'emploi du temps
     */
    private String mapReservationStatusToScheduleStatus(ReservationStatus reservationStatus) {
        return switch (reservationStatus) {
            case CONFIRMED -> "ACTIVE";
            case CANCELLED -> "CANCELLED";
            case COMPLETED -> "COMPLETED";
            case REJECTED -> "CANCELLED";
            default -> "ACTIVE";
        };
    }
}