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

    @PostMapping
    public ResponseEntity<Map<String, Object>> createReservation(@Valid @RequestBody ReservationDTO reservationDTO) {
        ReservationDTO created = reservationService.createReservation(reservationDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
            "success", true, "message", "Reservation created successfully", "data", created));
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllReservations() {
        List<ReservationDTO> list = reservationService.getAllReservations();
        return ResponseEntity.ok(Map.of("success", true, "data", list, "total", list.size()));
    }

    @GetMapping("/paginated")
    public ResponseEntity<Map<String, Object>> getAllReservationsPaginated(Pageable pageable) {
        Page<ReservationDTO> page = reservationService.getAllReservations(pageable);
        return ResponseEntity.ok(Map.of(
            "success", true, "data", page.getContent(),
            "page", page.getNumber(), "size", page.getSize(),
            "totalElements", page.getTotalElements(), "totalPages", page.getTotalPages()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getReservationById(@PathVariable Long id) {
        return reservationService.getReservationById(id)
                .map(r -> ResponseEntity.ok(Map.<String, Object>of("success", true, "data", r)))
                .orElseThrow(() -> new cm.iusjc.reservation.exception.ResourceNotFoundException("Reservation not found with ID: " + id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getReservationsByUser(@PathVariable Long userId) {
        List<ReservationDTO> list = reservationService.getReservationsByUser(userId);
        return ResponseEntity.ok(Map.of("success", true, "data", list, "total", list.size()));
    }

    @GetMapping("/resource/{resourceId}")
    public ResponseEntity<Map<String, Object>> getReservationsByResource(@PathVariable Long resourceId) {
        List<ReservationDTO> list = reservationService.getReservationsByResource(resourceId);
        return ResponseEntity.ok(Map.of("success", true, "data", list, "total", list.size()));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Map<String, Object>> getReservationsByStatus(@PathVariable ReservationStatus status) {
        List<ReservationDTO> list = reservationService.getReservationsByStatus(status);
        return ResponseEntity.ok(Map.of("success", true, "data", list, "total", list.size()));
    }

    @GetMapping("/pending")
    public ResponseEntity<Map<String, Object>> getPendingReservations() {
        List<ReservationDTO> list = reservationService.getPendingReservations();
        return ResponseEntity.ok(Map.of("success", true, "data", list, "total", list.size()));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<Map<String, Object>> getUpcomingReservations() {
        List<ReservationDTO> list = reservationService.getUpcomingReservations();
        return ResponseEntity.ok(Map.of("success", true, "data", list, "total", list.size()));
    }

    @GetMapping("/current")
    public ResponseEntity<Map<String, Object>> getCurrentReservations() {
        List<ReservationDTO> list = reservationService.getCurrentReservations();
        return ResponseEntity.ok(Map.of("success", true, "data", list, "total", list.size()));
    }

    @GetMapping("/period")
    public ResponseEntity<Map<String, Object>> getReservationsByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        List<ReservationDTO> list = reservationService.getReservationsByPeriod(start, end);
        return ResponseEntity.ok(Map.of("success", true, "data", list, "total", list.size()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateReservation(
            @PathVariable Long id, @Valid @RequestBody ReservationDTO reservationDTO) {
        ReservationDTO updated = reservationService.updateReservation(id, reservationDTO);
        return ResponseEntity.ok(Map.of(
            "success", true, "message", "Reservation updated successfully", "data", updated));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<Map<String, Object>> approveReservation(
            @PathVariable Long id, @RequestParam Long approvedBy) {
        ReservationDTO approved = reservationService.approveReservation(id, approvedBy);
        return ResponseEntity.ok(Map.of(
            "success", true, "message", "Reservation approved successfully", "data", approved));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<Map<String, Object>> rejectReservation(
            @PathVariable Long id,
            @RequestParam Long rejectedBy,
            @RequestParam(required = false) String reason) {
        ReservationDTO rejected = reservationService.rejectReservation(id, rejectedBy, reason);
        return ResponseEntity.ok(Map.of(
            "success", true, "message", "Reservation rejected successfully", "data", rejected));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Map<String, Object>> cancelReservation(
            @PathVariable Long id,
            @RequestParam Long cancelledBy,
            @RequestParam(required = false) String reason) {
        ReservationDTO cancelled = reservationService.cancelReservation(id, cancelledBy, reason);
        return ResponseEntity.ok(Map.of(
            "success", true, "message", "Reservation cancelled successfully", "data", cancelled));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteReservation(@PathVariable Long id) {
        reservationService.deleteReservation(id);
        return ResponseEntity.ok(Map.of("success", true, "message", "Reservation deleted successfully"));
    }

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchReservations(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long resourceId,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) ReservationStatus status,
            @RequestParam(required = false) ReservationType type,
            @RequestParam(required = false) String searchTerm) {
        List<ReservationDTO> list = (searchTerm != null && !searchTerm.isBlank())
                ? reservationService.searchReservations(searchTerm)
                : reservationService.searchReservationsWithFilters(title, userId, resourceId, courseId, status, type);
        return ResponseEntity.ok(Map.of("success", true, "data", list, "total", list.size()));
    }

    @GetMapping("/conflicts/resource")
    public ResponseEntity<Map<String, Object>> checkResourceConflicts(
            @RequestParam Long resourceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(required = false) Long excludeId) {
        List<ReservationDTO> conflicts = reservationService.checkResourceConflicts(resourceId, startTime, endTime, excludeId);
        return ResponseEntity.ok(Map.of(
            "success", true, "data", conflicts,
            "hasConflicts", !conflicts.isEmpty(), "total", conflicts.size()));
    }

    @GetMapping("/conflicts/user")
    public ResponseEntity<Map<String, Object>> checkUserConflicts(
            @RequestParam Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(required = false) Long excludeId) {
        List<ReservationDTO> conflicts = reservationService.checkUserConflicts(userId, startTime, endTime, excludeId);
        return ResponseEntity.ok(Map.of(
            "success", true, "data", conflicts,
            "hasConflicts", !conflicts.isEmpty(), "total", conflicts.size()));
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getReservationStatistics() {
        ReservationService.ReservationStatistics stats = reservationService.getReservationStatistics();
        return ResponseEntity.ok(Map.of("success", true, "data", Map.of(
            "overview", stats,
            "statusStats", reservationService.getReservationStatisticsByStatus(),
            "typeStats", reservationService.getReservationStatisticsByType(),
            "resourceStats", reservationService.getReservationStatisticsByResource())));
    }
}
