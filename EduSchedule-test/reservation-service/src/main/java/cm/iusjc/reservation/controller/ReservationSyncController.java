package cm.iusjc.reservation.controller;

import cm.iusjc.reservation.service.ReservationScheduleSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ReservationSyncController {

    private final ReservationScheduleSyncService syncService;

    /**
     * Endpoint pour synchroniser depuis un emploi du temps
     */
    @PostMapping("/sync-from-schedule")
    public ResponseEntity<Map<String, Object>> syncFromSchedule(@RequestBody Map<String, Object> scheduleData) {
        try {
            log.info("Réception synchronisation depuis Schedule");
            
            Map<String, Object> result = syncService.createReservationFromSchedule(scheduleData);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Erreur synchronisation depuis Schedule: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Erreur de synchronisation: " + e.getMessage()));
        }
    }

    /**
     * Endpoint pour mettre à jour depuis un emploi du temps
     */
    @PutMapping("/sync-update-from-schedule/{scheduleId}")
    public ResponseEntity<Map<String, Object>> syncUpdateFromSchedule(
            @PathVariable Long scheduleId, 
            @RequestBody Map<String, Object> scheduleData) {
        try {
            log.info("Mise à jour synchronisation depuis Schedule ID: {}", scheduleId);
            
            Map<String, Object> result = syncService.updateReservationFromSchedule(scheduleId, scheduleData);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Erreur mise à jour synchronisation: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Erreur de mise à jour: " + e.getMessage()));
        }
    }

    /**
     * Endpoint pour supprimer depuis un emploi du temps
     */
    @DeleteMapping("/sync-delete-from-schedule/{scheduleId}")
    public ResponseEntity<Map<String, Object>> syncDeleteFromSchedule(@PathVariable Long scheduleId) {
        try {
            log.info("Suppression synchronisation depuis Schedule ID: {}", scheduleId);
            
            syncService.deleteReservationFromSchedule(scheduleId);
            
            return ResponseEntity.ok(Map.of("message", "Réservation supprimée avec succès"));
        } catch (Exception e) {
            log.error("Erreur suppression synchronisation: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Erreur de suppression: " + e.getMessage()));
        }
    }

    /**
     * Endpoint pour obtenir le statut de synchronisation
     */
    @GetMapping("/sync-status/{scheduleId}")
    public ResponseEntity<Map<String, Object>> getSyncStatus(@PathVariable Long scheduleId) {
        try {
            Map<String, Object> status = syncService.getSyncStatus(scheduleId);
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            log.error("Erreur récupération statut sync: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Erreur de récupération: " + e.getMessage()));
        }
    }
}