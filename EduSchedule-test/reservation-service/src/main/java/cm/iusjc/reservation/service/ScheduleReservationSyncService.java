package cm.iusjc.reservation.service;

import cm.iusjc.reservation.entity.Reservation;
import cm.iusjc.reservation.entity.ReservationType;
import cm.iusjc.reservation.entity.ReservationStatus;
import cm.iusjc.reservation.repository.ReservationRepository;
import cm.iusjc.reservation.dto.ScheduleDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleReservationSyncService {
    
    private final ReservationRepository reservationRepository;
    private final RestTemplate restTemplate;
    
    @EventListener
    @Transactional
    public void onScheduleCreated(ScheduleCreatedEvent event) {
        log.info("Schedule created event received: {}", event.getScheduleId());
        
        try {
            ScheduleDTO schedule = getScheduleById(event.getScheduleId());
            if (schedule != null && schedule.getRoomId() != null) {
                createReservationFromSchedule(schedule);
            }
        } catch (Exception e) {
            log.error("Error creating reservation from schedule: {}", e.getMessage());
        }
    }
    
    @EventListener
    @Transactional
    public void onScheduleUpdated(ScheduleUpdatedEvent event) {
        log.info("Schedule updated event received: {}", event.getScheduleId());
        
        try {
            ScheduleDTO schedule = getScheduleById(event.getScheduleId());
            if (schedule != null) {
                updateReservationFromSchedule(schedule);
            }
        } catch (Exception e) {
            log.error("Error updating reservation from schedule: {}", e.getMessage());
        }
    }
    
    @EventListener
    @Transactional
    public void onScheduleDeleted(ScheduleDeletedEvent event) {
        log.info("Schedule deleted event received: {}", event.getScheduleId());
        
        try {
            cancelReservationForSchedule(event.getScheduleId());
        } catch (Exception e) {
            log.error("Error cancelling reservation for deleted schedule: {}", e.getMessage());
        }
    }
    
    private ScheduleDTO getScheduleById(Long scheduleId) {
        try {
            String url = "http://scheduling-service/api/schedules/" + scheduleId;
            return restTemplate.getForObject(url, ScheduleDTO.class);
        } catch (Exception e) {
            log.warn("Could not fetch schedule {}: {}", scheduleId, e.getMessage());
            return null;
        }
    }
    
    private void createReservationFromSchedule(ScheduleDTO schedule) {
        log.info("Creating reservation from schedule: {}", schedule.getId());
        
        // Vérifier si une réservation existe déjà pour ce schedule
        Optional<Reservation> existing = reservationRepository.findByScheduleId(schedule.getId());
        if (existing.isPresent()) {
            log.info("Reservation already exists for schedule: {}", schedule.getId());
            return;
        }
        
        Reservation reservation = new Reservation();
        reservation.setResourceId(schedule.getRoomId());
        reservation.setCourseId(schedule.getCourseId());
        reservation.setCourseGroupId(schedule.getGroupId());
        reservation.setUserId(schedule.getTeacherId());
        reservation.setTitle(schedule.getTitle());
        reservation.setDescription("Réservation automatique depuis l'emploi du temps");
        reservation.setStartTime(schedule.getStartTime());
        reservation.setEndTime(schedule.getEndTime());
        reservation.setType(ReservationType.COURSE);
        reservation.setStatus(ReservationStatus.CONFIRMED); // Auto-confirmé pour les emplois du temps
        reservation.setExpectedAttendees(schedule.getExpectedAttendees());
        reservation.setSetupTime(15); // Défaut
        reservation.setCleanupTime(15); // Défaut
        reservation.setNotes("Créé automatiquement depuis l'emploi du temps ID: " + schedule.getId());
        
        // Ajouter un champ pour lier à l'emploi du temps
        reservation.setScheduleId(schedule.getId());
        
        reservationRepository.save(reservation);
        log.info("Reservation created successfully for schedule: {}", schedule.getId());
    }
    
    private void updateReservationFromSchedule(ScheduleDTO schedule) {
        log.info("Updating reservation from schedule: {}", schedule.getId());
        
        Optional<Reservation> existingOpt = reservationRepository.findByScheduleId(schedule.getId());
        if (existingOpt.isEmpty()) {
            // Créer la réservation si elle n'existe pas
            createReservationFromSchedule(schedule);
            return;
        }
        
        Reservation reservation = existingOpt.get();
        
        // Mettre à jour les champs modifiables
        if (schedule.getRoomId() != null) {
            reservation.setResourceId(schedule.getRoomId());
        }
        reservation.setTitle(schedule.getTitle());
        reservation.setStartTime(schedule.getStartTime());
        reservation.setEndTime(schedule.getEndTime());
        reservation.setExpectedAttendees(schedule.getExpectedAttendees());
        reservation.setNotes("Mis à jour automatiquement depuis l'emploi du temps ID: " + schedule.getId());
        
        reservationRepository.save(reservation);
        log.info("Reservation updated successfully for schedule: {}", schedule.getId());
    }
    
    private void cancelReservationForSchedule(Long scheduleId) {
        log.info("Cancelling reservation for deleted schedule: {}", scheduleId);
        
        Optional<Reservation> existingOpt = reservationRepository.findByScheduleId(scheduleId);
        if (existingOpt.isEmpty()) {
            log.info("No reservation found for schedule: {}", scheduleId);
            return;
        }
        
        Reservation reservation = existingOpt.get();
        reservation.setStatus(ReservationStatus.CANCELLED);
        reservation.setCancelledAt(LocalDateTime.now());
        reservation.setCancellationReason("Emploi du temps supprimé");
        reservation.setNotes(reservation.getNotes() + " - Annulé automatiquement (emploi du temps supprimé)");
        
        reservationRepository.save(reservation);
        log.info("Reservation cancelled successfully for schedule: {}", scheduleId);
    }
    
    @Transactional
    public void syncAllSchedulesToReservations() {
        log.info("Starting full synchronization of schedules to reservations");
        
        try {
            // Récupérer tous les emplois du temps actifs
            String url = "http://scheduling-service/api/schedules?status=ACTIVE";
            ScheduleDTO[] schedules = restTemplate.getForObject(url, ScheduleDTO[].class);
            
            if (schedules != null) {
                for (ScheduleDTO schedule : schedules) {
                    if (schedule.getRoomId() != null) {
                        Optional<Reservation> existing = reservationRepository.findByScheduleId(schedule.getId());
                        if (existing.isEmpty()) {
                            createReservationFromSchedule(schedule);
                        }
                    }
                }
                log.info("Full synchronization completed. Processed {} schedules", schedules.length);
            }
        } catch (Exception e) {
            log.error("Error during full synchronization: {}", e.getMessage());
        }
    }
    
    // Classes d'événements
    public static class ScheduleCreatedEvent {
        private final Long scheduleId;
        
        public ScheduleCreatedEvent(Long scheduleId) {
            this.scheduleId = scheduleId;
        }
        
        public Long getScheduleId() {
            return scheduleId;
        }
    }
    
    public static class ScheduleUpdatedEvent {
        private final Long scheduleId;
        
        public ScheduleUpdatedEvent(Long scheduleId) {
            this.scheduleId = scheduleId;
        }
        
        public Long getScheduleId() {
            return scheduleId;
        }
    }
    
    public static class ScheduleDeletedEvent {
        private final Long scheduleId;
        
        public ScheduleDeletedEvent(Long scheduleId) {
            this.scheduleId = scheduleId;
        }
        
        public Long getScheduleId() {
            return scheduleId;
        }
    }
}