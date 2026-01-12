package cm.iusjc.reservation.service;

import cm.iusjc.reservation.dto.*;
import cm.iusjc.reservation.entity.Reservation;
import cm.iusjc.reservation.entity.ReservationStatus;
import cm.iusjc.reservation.entity.ReservationType;
import cm.iusjc.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationService {
    
    private final ReservationRepository reservationRepository;
    private final ConflictDetectionService conflictDetectionService;
    
    @Transactional
    public ReservationDTO createReservation(ReservationRequest request) {
        log.info("Creating new reservation for resource: {} by user: {}", 
                request.getResourceId(), request.getUserId());
        
        // Vérifier les conflits
        List<Reservation> conflicts = conflictDetectionService.checkConflicts(
            request.getResourceId(),
            request.getStartTime(),
            request.getEndTime(),
            request.getSetupTime(),
            request.getCleanupTime(),
            null
        );
        
        if (!conflicts.isEmpty()) {
            throw new RuntimeException("Conflict detected with existing reservations");
        }
        
        // Créer la réservation
        Reservation reservation = new Reservation();
        reservation.setResourceId(request.getResourceId());
        reservation.setCourseId(request.getCourseId());
        reservation.setCourseGroupId(request.getCourseGroupId());
        reservation.setUserId(request.getUserId());
        reservation.setTitle(request.getTitle());
        reservation.setDescription(request.getDescription());
        reservation.setStartTime(request.getStartTime());
        reservation.setEndTime(request.getEndTime());
        reservation.setType(request.getType());
        reservation.setRecurringPattern(request.getRecurringPattern());
        reservation.setExpectedAttendees(request.getExpectedAttendees());
        reservation.setSetupTime(request.getSetupTime());
        reservation.setCleanupTime(request.getCleanupTime());
        reservation.setNotes(request.getNotes());
        
        // Déterminer le statut initial
        reservation.setStatus(determineInitialStatus(request));
        
        Reservation savedReservation = reservationRepository.save(reservation);
        log.info("Reservation created successfully: {}", savedReservation.getId());
        
        return mapToDTO(savedReservation);
    }
    
    public ReservationDTO getReservationById(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found: " + id));
        return mapToDTO(reservation);
    }
    
    public List<ReservationDTO> getReservationsByUser(Long userId) {
        return reservationRepository.findByUserIdOrderByStartTimeDesc(userId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    public Page<ReservationDTO> getReservationsByUser(Long userId, Pageable pageable) {
        return reservationRepository.findByUserIdOrderByStartTimeDesc(userId, pageable)
                .map(this::mapToDTO);
    }
    
    public List<ReservationDTO> getReservationsByResource(Long resourceId) {
        List<ReservationStatus> activeStatuses = List.of(
            ReservationStatus.PENDING, 
            ReservationStatus.CONFIRMED
        );
        return reservationRepository.findByResourceIdAndStatusIn(resourceId, activeStatuses)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    public List<ReservationDTO> getReservationsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return reservationRepository.findByDateRange(startDate, endDate)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    public Page<ReservationDTO> getReservationsWithFilters(
            Long resourceId, Long userId, ReservationStatus status, ReservationType type,
            LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return reservationRepository.findWithFilters(
                resourceId, userId, status, type, startDate, endDate, pageable)
                .map(this::mapToDTO);
    }
    
    public List<ReservationDTO> getPendingReservations() {
        return reservationRepository.findPendingReservations()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public ReservationDTO updateReservation(Long id, ReservationRequest request) {
        log.info("Updating reservation: {}", id);
        
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found: " + id));
        
        // Vérifier les conflits (en excluant cette réservation)
        List<Reservation> conflicts = conflictDetectionService.checkConflicts(
            request.getResourceId(),
            request.getStartTime(),
            request.getEndTime(),
            request.getSetupTime(),
            request.getCleanupTime(),
            id
        );
        
        if (!conflicts.isEmpty()) {
            throw new RuntimeException("Conflict detected with existing reservations");
        }
        
        // Mettre à jour les champs
        reservation.setResourceId(request.getResourceId());
        reservation.setCourseId(request.getCourseId());
        reservation.setCourseGroupId(request.getCourseGroupId());
        reservation.setTitle(request.getTitle());
        reservation.setDescription(request.getDescription());
        reservation.setStartTime(request.getStartTime());
        reservation.setEndTime(request.getEndTime());
        reservation.setType(request.getType());
        reservation.setRecurringPattern(request.getRecurringPattern());
        reservation.setExpectedAttendees(request.getExpectedAttendees());
        reservation.setSetupTime(request.getSetupTime());
        reservation.setCleanupTime(request.getCleanupTime());
        reservation.setNotes(request.getNotes());
        
        Reservation updatedReservation = reservationRepository.save(reservation);
        log.info("Reservation updated successfully: {}", updatedReservation.getId());
        
        return mapToDTO(updatedReservation);
    }
    
    @Transactional
    public void approveReservation(Long id, Long approvedBy) {
        log.info("Approving reservation: {} by user: {}", id, approvedBy);
        
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found: " + id));
        
        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException("Only pending reservations can be approved");
        }
        
        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setApprovedBy(approvedBy);
        reservation.setApprovedAt(LocalDateTime.now());
        
        reservationRepository.save(reservation);
        log.info("Reservation approved successfully: {}", id);
    }
    
    @Transactional
    public void cancelReservation(Long id, Long cancelledBy, String reason) {
        log.info("Cancelling reservation: {} by user: {}", id, cancelledBy);
        
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found: " + id));
        
        if (!reservation.isActive()) {
            throw new IllegalStateException("Only active reservations can be cancelled");
        }
        
        reservation.setStatus(ReservationStatus.CANCELLED);
        reservation.setCancelledBy(cancelledBy);
        reservation.setCancelledAt(LocalDateTime.now());
        reservation.setCancellationReason(reason);
        
        reservationRepository.save(reservation);
        log.info("Reservation cancelled successfully: {}", id);
    }
    
    public List<ReservationDTO> checkConflicts(ConflictCheckRequest request) {
        List<Reservation> conflicts = conflictDetectionService.checkConflicts(
            request.getResourceId(),
            request.getStartTime(),
            request.getEndTime(),
            request.getSetupTime(),
            request.getCleanupTime(),
            request.getExcludeReservationId()
        );
        
        return conflicts.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    private ReservationStatus determineInitialStatus(ReservationRequest request) {
        // Logique pour déterminer le statut initial
        if (request.getType() == ReservationType.COURSE && request.getCourseId() != null) {
            return ReservationStatus.CONFIRMED;
        }
        return ReservationStatus.PENDING;
    }
    
    private ReservationDTO mapToDTO(Reservation reservation) {
        ReservationDTO dto = new ReservationDTO();
        dto.setId(reservation.getId());
        dto.setResourceId(reservation.getResourceId());
        dto.setCourseId(reservation.getCourseId());
        dto.setCourseGroupId(reservation.getCourseGroupId());
        dto.setUserId(reservation.getUserId());
        dto.setTitle(reservation.getTitle());
        dto.setDescription(reservation.getDescription());
        dto.setStartTime(reservation.getStartTime());
        dto.setEndTime(reservation.getEndTime());
        dto.setStatus(reservation.getStatus());
        dto.setType(reservation.getType());
        dto.setRecurringPattern(reservation.getRecurringPattern());
        dto.setParentReservationId(reservation.getParentReservationId());
        dto.setExpectedAttendees(reservation.getExpectedAttendees());
        dto.setSetupTime(reservation.getSetupTime());
        dto.setCleanupTime(reservation.getCleanupTime());
        dto.setNotes(reservation.getNotes());
        dto.setApprovedBy(reservation.getApprovedBy());
        dto.setApprovedAt(reservation.getApprovedAt());
        dto.setCancelledBy(reservation.getCancelledBy());
        dto.setCancelledAt(reservation.getCancelledAt());
        dto.setCancellationReason(reservation.getCancellationReason());
        dto.setCreatedAt(reservation.getCreatedAt());
        dto.setUpdatedAt(reservation.getUpdatedAt());
        
        // Champs calculés
        dto.setEffectiveStartTime(reservation.getEffectiveStartTime());
        dto.setEffectiveEndTime(reservation.getEffectiveEndTime());
        dto.setIsRecurring(reservation.isRecurring());
        dto.setIsActive(reservation.isActive());
        
        return dto;
    }