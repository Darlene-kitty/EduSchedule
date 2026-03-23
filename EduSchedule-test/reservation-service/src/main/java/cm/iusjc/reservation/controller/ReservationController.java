package cm.iusjc.reservation.controller;

import cm.iusjc.reservation.dto.ReservationDTO;
import cm.iusjc.reservation.entity.ReservationStatus;
import cm.iusjc.reservation.entity.ReservationType;
import cm.iusjc.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
@Slf4j
public class ReservationController {
    
    private final ReservationService reservationService;
    
    /**
     * Crée une nouvelle réservation
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createReservation(@Valid @RequestBody ReservationDTO reservationDTO) {
        try {
            log.info("Creating reservation: {}", reservationDTO.getTitle());
            ReservationDTO createdReservation = reservationService.createReservation(reservationDTO);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "message", "Reservation created successfully",
                "data", createdReservation
            ));
        } catch (Exception e) {
            log.error("Error creating reservation: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Récupère toutes les réservations
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllReservations() {
        try {
            List<ReservationDTO> reservations = reservationService.getAllReservations();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", reservations,
                "total", reservations.size()
            ));
        } catch (Exception e) {
            log.error("Error fetching reservations: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Récupère les réservations avec pagination
     */
    @GetMapping("/paginated")
    public ResponseEntity<Map<String, Object>> getAllReservationsPaginated(Pageable pageable) {
        try {
            Page<ReservationDTO> reservationsPage = reservationService.getAllReservations(pageable);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", reservationsPage.getContent(),
                "page", reservationsPage.getNumber(),
                "size", reservationsPage.getSize(),
                "totalElements", reservationsPage.getTotalElements(),
                "totalPages", reservationsPage.getTotalPages()
            ));
        } catch (Exception e) {
            log.error("Error fetching paginated reservations: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Récupère une réservation par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getReservationById(@PathVariable Long id) {
        try {
            return reservationService.getReservationById(id)
                    .map(reservation -> ResponseEntity.ok(Map.of(
                        "success", true,
                        "data", reservation
                    )))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error fetching reservation by ID {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Récupère les réservations par utilisateur
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getReservationsByUser(@PathVariable Long userId) {
        try {
            List<ReservationDTO> reservations = reservationService.getReservationsByUser(userId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", reservations,
                "total", reservations.size()
            ));
        } catch (Exception e) {
            log.error("Error fetching reservations by user {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Récupère les réservations par ressource
     */
    @GetMapping("/resource/{resourceId}")
    public ResponseEntity<Map<String, Object>> getReservationsByResource(@PathVariable Long resourceId) {
        try {
            List<ReservationDTO> reservations = reservationService.getReservationsByResource(resourceId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", reservations,
                "total", reservations.size()
            ));
        } catch (Exception e) {
            log.error("Error fetching reservations by resource {}: {}", resourceId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Récupère les réservations par statut
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<Map<String, Object>> getReservationsByStatus(@PathVariable ReservationStatus status) {
        try {
            List<ReservationDTO> reservations = reservationService.getReservationsByStatus(status);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", reservations,
                "total", reservations.size()
            ));
        } catch (Exception e) {
            log.error("Error fetching reservations by status {}: {}", status, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Récupère les réservations en attente
     */
    @GetMapping("/pending")
    public ResponseEntity<Map<String, Object>> getPendingReservations() {
        try {
            List<ReservationDTO> reservations = reservationService.getPendingReservations();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", reservations,
                "total", reservations.size()
            ));
        } catch (Exception e) {
            log.error("Error fetching pending reservations: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Récupère les réservations à venir
     */
    @GetMapping("/upcoming")
    public ResponseEntity<Map<String, Object>> getUpcomingReservations() {
        try {
            List<ReservationDTO> reservations = reservationService.getUpcomingReservations();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", reservations,
                "total", reservations.size()
            ));
        } catch (Exception e) {
            log.error("Error fetching upcoming reservations: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Récupère les réservations en cours
     */
    @GetMapping("/current")
    public ResponseEntity<Map<String, Object>> getCurrentReservations() {
        try {
            List<ReservationDTO> reservations = reservationService.getCurrentReservations();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", reservations,
                "total", reservations.size()
            ));
        } catch (Exception e) {
            log.error("Error fetching current reservations: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Récupère les réservations par période
     */
    @GetMapping("/period")
    public ResponseEntity<Map<String, Object>> getReservationsByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        try {
            List<ReservationDTO> reservations = reservationService.getReservationsByPeriod(start, end);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", reservations,
                "total", reservations.size()
            ));
        } catch (Exception e) {
            log.error("Error fetching reservations by period: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Met à jour une réservation
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateReservation(
            @PathVariable Long id, 
            @Valid @RequestBody ReservationDTO reservationDTO) {
        try {
            ReservationDTO updatedReservation = reservationService.updateReservation(id, reservationDTO);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Reservation updated successfully",
                "data", updatedReservation
            ));
        } catch (Exception e) {
            log.error("Error updating reservation {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Approuve une réservation
     */
    @PatchMapping("/{id}/approve")
    public ResponseEntity<Map<String, Object>> approveReservation(
            @PathVariable Long id,
            @RequestParam Long approvedBy) {
        try {
            ReservationDTO approvedReservation = reservationService.approveReservation(id, approvedBy);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Reservation approved successfully",
                "data", approvedReservation
            ));
        } catch (Exception e) {
            log.error("Error approving reservation {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Rejette une réservation
     */
    @PatchMapping("/{id}/reject")
    public ResponseEntity<Map<String, Object>> rejectReservation(
            @PathVariable Long id,
            @RequestParam Long rejectedBy,
            @RequestParam(required = false) String reason) {
        try {
            ReservationDTO rejectedReservation = reservationService.rejectReservation(id, rejectedBy, reason);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Reservation rejected successfully",
                "data", rejectedReservation
            ));
        } catch (Exception e) {
            log.error("Error rejecting reservation {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Annule une réservation
     */
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Map<String, Object>> cancelReservation(
            @PathVariable Long id,
            @RequestParam Long cancelledBy,
            @RequestParam(required = false) String reason) {
        try {
            ReservationDTO cancelledReservation = reservationService.cancelReservation(id, cancelledBy, reason);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Reservation cancelled successfully",
                "data", cancelledReservation
            ));
        } catch (Exception e) {
            log.error("Error cancelling reservation {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Supprime une réservation
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteReservation(@PathVariable Long id) {
        try {
            reservationService.deleteReservation(id);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Reservation deleted successfully"
            ));
        } catch (Exception e) {
            log.error("Error deleting reservation {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Recherche des réservations
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchReservations(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long resourceId,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) ReservationStatus status,
            @RequestParam(required = false) ReservationType type,
            @RequestParam(required = false) String searchTerm) {
        try {
            List<ReservationDTO> reservations;
            
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                reservations = reservationService.searchReservations(searchTerm);
            } else {
                reservations = reservationService.searchReservationsWithFilters(title, userId, resourceId, courseId, status, type);
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", reservations,
                "total", reservations.size()
            ));
        } catch (Exception e) {
            log.error("Error searching reservations: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Vérifie les conflits de ressource
     */
    @GetMapping("/conflicts/resource")
    public ResponseEntity<Map<String, Object>> checkResourceConflicts(
            @RequestParam Long resourceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(required = false) Long excludeId) {
        try {
            List<ReservationDTO> conflicts = reservationService.checkResourceConflicts(resourceId, startTime, endTime, excludeId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", conflicts,
                "hasConflicts", !conflicts.isEmpty(),
                "total", conflicts.size()
            ));
        } catch (Exception e) {
            log.error("Error checking resource conflicts: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Vérifie les conflits d'utilisateur
     */
    @GetMapping("/conflicts/user")
    public ResponseEntity<Map<String, Object>> checkUserConflicts(
            @RequestParam Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(required = false) Long excludeId) {
        try {
            List<ReservationDTO> conflicts = reservationService.checkUserConflicts(userId, startTime, endTime, excludeId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", conflicts,
                "hasConflicts", !conflicts.isEmpty(),
                "total", conflicts.size()
            ));
        } catch (Exception e) {
            log.error("Error checking user conflicts: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Obtient les statistiques des réservations
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getReservationStatistics() {
        try {
            ReservationService.ReservationStatistics stats = reservationService.getReservationStatistics();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", Map.of(
                    "overview", stats,
                    "statusStats", reservationService.getReservationStatisticsByStatus(),
                    "typeStats", reservationService.getReservationStatisticsByType(),
                    "resourceStats", reservationService.getReservationStatisticsByResource()
                )
            ));
        } catch (Exception e) {
            log.error("Error fetching reservation statistics: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
}