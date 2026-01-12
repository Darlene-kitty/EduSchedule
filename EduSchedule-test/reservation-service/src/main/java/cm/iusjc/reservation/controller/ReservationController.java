package cm.iusjc.reservation.controller;

import cm.iusjc.reservation.dto.*;
import cm.iusjc.reservation.entity.ReservationStatus;
import cm.iusjc.reservation.entity.ReservationType;
import cm.iusjc.reservation.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@Slf4j
public class ReservationController {
    
    private final ReservationService reservationService;
    
    @PostMapping
    public ResponseEntity<ReservationDTO> createReservation(@Valid @RequestBody ReservationRequest request) {
        log.info("Creating reservation for resource: {}", request.getResourceId());
        ReservationDTO reservation = reservationService.createReservation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(reservation);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ReservationDTO> getReservation(@PathVariable Long id) {
        ReservationDTO reservation = reservationService.getReservationById(id);
        return ResponseEntity.ok(reservation);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ReservationDTO> updateReservation(
            @PathVariable Long id, 
            @Valid @RequestBody ReservationRequest request) {
        log.info("Updating reservation: {}", id);
        ReservationDTO reservation = reservationService.updateReservation(id, request);
        return ResponseEntity.ok(reservation);
    }
    
    @PostMapping("/{id}/approve")
    public ResponseEntity<Void> approveReservation(
            @PathVariable Long id,
            @RequestParam Long approvedBy) {
        log.info("Approving reservation: {} by user: {}", id, approvedBy);
        reservationService.approveReservation(id, approvedBy);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelReservation(
            @PathVariable Long id,
            @RequestParam Long cancelledBy,
            @RequestParam(required = false) String reason) {
        log.info("Cancelling reservation: {} by user: {}", id, cancelledBy);
        reservationService.cancelReservation(id, cancelledBy, reason);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReservationDTO>> getReservationsByUser(@PathVariable Long userId) {
        List<ReservationDTO> reservations = reservationService.getReservationsByUser(userId);
        return ResponseEntity.ok(reservations);
    }
    
    @GetMapping("/user/{userId}/paginated")
    public ResponseEntity<Page<ReservationDTO>> getReservationsByUser(
            @PathVariable Long userId, 
            Pageable pageable) {
        Page<ReservationDTO> reservations = reservationService.getReservationsByUser(userId, pageable);
        return ResponseEntity.ok(reservations);
    }
    
    @GetMapping("/resource/{resourceId}")
    public ResponseEntity<List<ReservationDTO>> getReservationsByResource(@PathVariable Long resourceId) {
        List<ReservationDTO> reservations = reservationService.getReservationsByResource(resourceId);
        return ResponseEntity.ok(reservations);
    }
    
    @GetMapping("/date-range")
    public ResponseEntity<List<ReservationDTO>> getReservationsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<ReservationDTO> reservations = reservationService.getReservationsByDateRange(startDate, endDate);
        return ResponseEntity.ok(reservations);
    }
    
    @GetMapping("/search")
    public ResponseEntity<Page<ReservationDTO>> searchReservations(
            @RequestParam(required = false) Long resourceId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) ReservationStatus status,
            @RequestParam(required = false) ReservationType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Pageable pageable) {
        
        Page<ReservationDTO> reservations = reservationService.getReservationsWithFilters(
            resourceId, userId, status, type, startDate, endDate, pageable);
        return ResponseEntity.ok(reservations);
    }
    
    @GetMapping("/pending")
    public ResponseEntity<List<ReservationDTO>> getPendingReservations() {
        List<ReservationDTO> reservations = reservationService.getPendingReservations();
        return ResponseEntity.ok(reservations);
    }
    
    @PostMapping("/check-conflicts")
    public ResponseEntity<List<ReservationDTO>> checkConflicts(@Valid @RequestBody ConflictCheckRequest request) {
        List<ReservationDTO> conflicts = reservationService.checkConflicts(request);
        return ResponseEntity.ok(conflicts);
    }
    
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleException(RuntimeException e) {
        log.error("Error in reservation controller: {}", e.getMessage());
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}