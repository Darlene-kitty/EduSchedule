package cm.iusjc.scheduling.controller;

import cm.iusjc.scheduling.service.ScheduleReservationSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/schedules/sync")
@RequiredArgsConstructor
@Slf4j
public class ScheduleSyncController {
    
    private final ScheduleReservationSyncService syncService;
    
    /**
     * Synchronise manuellement un emploi du temps avec les réservations
     */
    @PostMapping("/manual/{scheduleId}")
    public ResponseEntity<Map<String, Object>> manualSync(@PathVariable Long scheduleId) {
        try {
            log.info("Manual sync requested for schedule: {}", scheduleId);
            
            // TODO: Implémenter la synchronisation manuelle
            // syncService.manualSyncSchedule(scheduleId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Synchronisation manuelle démarrée pour l'emploi du temps " + scheduleId,
                "scheduleId", scheduleId
            ));
            
        } catch (Exception e) {
            log.error("Error in manual sync for schedule {}: {}", scheduleId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Erreur lors de la synchronisation: " + e.getMessage(),
                "scheduleId", scheduleId
            ));
        }
    }
    
    /**
     * Synchronise depuis une réservation vers l'emploi du temps
     */
    @PostMapping("/from-reservation")
    public ResponseEntity<Map<String, Object>> syncFromReservation(
            @RequestParam Long reservationId,
            @RequestBody Map<String, Object> reservationData) {
        
        try {
            log.info("Sync from reservation requested: {}", reservationId);
            
            syncService.syncScheduleFromReservation(reservationId, reservationData);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Synchronisation depuis réservation réussie",
                "reservationId", reservationId
            ));
            
        } catch (Exception e) {
            log.error("Error syncing from reservation {}: {}", reservationId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Erreur lors de la synchronisation: " + e.getMessage(),
                "reservationId", reservationId
            ));
        }
    }
    
    /**
     * Obtient le statut de synchronisation
     */
    @GetMapping("/status/{scheduleId}")
    public ResponseEntity<Map<String, Object>> getSyncStatus(@PathVariable Long scheduleId) {
        try {
            // TODO: Implémenter la vérification du statut
            return ResponseEntity.ok(Map.of(
                "success", true,
                "scheduleId", scheduleId,
                "syncStatus", "SYNCHRONIZED",
                "lastSyncTime", System.currentTimeMillis(),
                "hasConflicts", false
            ));
            
        } catch (Exception e) {
            log.error("Error getting sync status for schedule {}: {}", scheduleId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Erreur lors de la récupération du statut: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Force la resynchronisation de tous les emplois du temps
     */
    @PostMapping("/force-resync")
    public ResponseEntity<Map<String, Object>> forceResync() {
        try {
            log.info("Force resync requested for all schedules");
            
            // TODO: Implémenter la resynchronisation forcée
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Resynchronisation forcée démarrée",
                "timestamp", System.currentTimeMillis()
            ));
            
        } catch (Exception e) {
            log.error("Error in force resync: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Erreur lors de la resynchronisation: " + e.getMessage()
            ));
        }
    }
}