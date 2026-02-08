package cm.iusjc.reservation.service;

import cm.iusjc.reservation.dto.ReservationDTO;
import cm.iusjc.reservation.entity.Reservation;
import cm.iusjc.reservation.entity.ReservationStatus;
import cm.iusjc.reservation.entity.ReservationType;
import cm.iusjc.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationService {
    
    private final ReservationRepository reservationRepository;
    
    /**
     * Crée une nouvelle réservation
     */
    @Transactional
    @CacheEvict(value = "reservations", allEntries = true)
    public ReservationDTO createReservation(ReservationDTO reservationDTO) {
        log.info("Creating new reservation: {}", reservationDTO.getTitle());
        
        // Validation des dates
        validateReservationTimes(reservationDTO.getStartTime(), reservationDTO.getEndTime());
        
        // Vérifier les conflits
        checkForConflicts(reservationDTO, null);
        
        Reservation reservation = new Reservation();
        reservation.setResourceId(reservationDTO.getResourceId());
        reservation.setCourseId(reservationDTO.getCourseId());
        reservation.setCourseGroupId(reservationDTO.getCourseGroupId());
        reservation.setUserId(reservationDTO.getUserId());
        reservation.setTitle(reservationDTO.getTitle());
        reservation.setDescription(reservationDTO.getDescription());
        reservation.setStartTime(reservationDTO.getStartTime());
        reservation.setEndTime(reservationDTO.getEndTime());
        reservation.setStatus(reservationDTO.getStatus() != null ? reservationDTO.getStatus() : ReservationStatus.PENDING);
        reservation.setType(reservationDTO.getType());
        reservation.setRecurringPattern(reservationDTO.getRecurringPattern());
        reservation.setParentReservationId(reservationDTO.getParentReservationId());
        reservation.setScheduleId(reservationDTO.getScheduleId());
        reservation.setExpectedAttendees(reservationDTO.getExpectedAttendees());
        reservation.setSetupTime(reservationDTO.getSetupTime() != null ? reservationDTO.getSetupTime() : 0);
        reservation.setCleanupTime(reservationDTO.getCleanupTime() != null ? reservationDTO.getCleanupTime() : 0);
        reservation.setNotes(reservationDTO.getNotes());
        reservation.setCreatedAt(LocalDateTime.now());
        reservation.setUpdatedAt(LocalDateTime.now());
        
        Reservation savedReservation = reservationRepository.save(reservation);
        log.info("Reservation created successfully with ID: {}", savedReservation.getId());
        
        return convertToDTO(savedReservation);
    }
    
    /**
     * Récupère toutes les réservations
     */
    @Cacheable(value = "reservations")
    public List<ReservationDTO> getAllReservations() {
        log.debug("Fetching all reservations");
        return reservationRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les réservations avec pagination
     */
    public Page<ReservationDTO> getAllReservations(Pageable pageable) {
        log.debug("Fetching reservations with pagination: {}", pageable);
        return reservationRepository.findAll(pageable)
                .map(this::convertToDTO);
    }
    
    /**
     * Récupère une réservation par ID
     */
    @Cacheable(value = "reservations", key = "#id")
    public Optional<ReservationDTO> getReservationById(Long id) {
        log.debug("Fetching reservation by ID: {}", id);
        return reservationRepository.findById(id)
                .map(this::convertToDTO);
    }
    
    /**
     * Récupère les réservations par utilisateur
     */
    public List<ReservationDTO> getReservationsByUser(Long userId) {
        log.debug("Fetching reservations by user: {}", userId);
        return reservationRepository.findByUserIdOrderByStartTimeDesc(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les réservations par utilisateur avec pagination
     */
    public Page<ReservationDTO> getReservationsByUser(Long userId, Pageable pageable) {
        log.debug("Fetching reservations by user {} with pagination: {}", userId, pageable);
        return reservationRepository.findByUserIdOrderByStartTimeDesc(userId, pageable)
                .map(this::convertToDTO);
    }
    
    /**
     * Récupère les réservations par ressource
     */
    public List<ReservationDTO> getReservationsByResource(Long resourceId) {
        log.debug("Fetching reservations by resource: {}", resourceId);
        return reservationRepository.findByResourceIdOrderByStartTimeAsc(resourceId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les réservations par cours
     */
    public List<ReservationDTO> getReservationsByCourse(Long courseId) {
        log.debug("Fetching reservations by course: {}", courseId);
        return reservationRepository.findByCourseIdOrderByStartTimeAsc(courseId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les réservations par statut
     */
    public List<ReservationDTO> getReservationsByStatus(ReservationStatus status) {
        log.debug("Fetching reservations by status: {}", status);
        return reservationRepository.findByStatusOrderByStartTimeAsc(status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les réservations par type
     */
    public List<ReservationDTO> getReservationsByType(ReservationType type) {
        log.debug("Fetching reservations by type: {}", type);
        return reservationRepository.findByTypeOrderByStartTimeAsc(type).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les réservations en attente
     */
    @Cacheable(value = "pendingReservations")
    public List<ReservationDTO> getPendingReservations() {
        log.debug("Fetching pending reservations");
        return reservationRepository.findPendingReservations().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les réservations à venir
     */
    @Cacheable(value = "upcomingReservations")
    public List<ReservationDTO> getUpcomingReservations() {
        log.debug("Fetching upcoming reservations");
        return reservationRepository.findUpcomingReservations(LocalDateTime.now()).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les réservations en cours
     */
    public List<ReservationDTO> getCurrentReservations() {
        log.debug("Fetching current reservations");
        return reservationRepository.findCurrentReservations(LocalDateTime.now()).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les réservations par période
     */
    public List<ReservationDTO> getReservationsByPeriod(LocalDateTime start, LocalDateTime end) {
        log.debug("Fetching reservations between {} and {}", start, end);
        return reservationRepository.findReservationsInPeriod(start, end).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Met à jour une réservation
     */
    @Transactional
    @CacheEvict(value = {"reservations", "pendingReservations", "upcomingReservations"}, allEntries = true)
    public ReservationDTO updateReservation(Long id, ReservationDTO reservationDTO) {
        log.info("Updating reservation with ID: {}", id);
        
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found with ID: " + id));
        
        // Validation des dates
        validateReservationTimes(reservationDTO.getStartTime(), reservationDTO.getEndTime());
        
        // Vérifier les conflits (exclure la réservation actuelle)
        checkForConflicts(reservationDTO, id);
        
        reservation.setResourceId(reservationDTO.getResourceId());
        reservation.setCourseId(reservationDTO.getCourseId());
        reservation.setCourseGroupId(reservationDTO.getCourseGroupId());
        reservation.setTitle(reservationDTO.getTitle());
        reservation.setDescription(reservationDTO.getDescription());
        reservation.setStartTime(reservationDTO.getStartTime());
        reservation.setEndTime(reservationDTO.getEndTime());
        reservation.setType(reservationDTO.getType());
        reservation.setRecurringPattern(reservationDTO.getRecurringPattern());
        reservation.setScheduleId(reservationDTO.getScheduleId());
        reservation.setExpectedAttendees(reservationDTO.getExpectedAttendees());
        reservation.setSetupTime(reservationDTO.getSetupTime());
        reservation.setCleanupTime(reservationDTO.getCleanupTime());
        reservation.setNotes(reservationDTO.getNotes());
        reservation.setUpdatedAt(LocalDateTime.now());
        
        Reservation updatedReservation = reservationRepository.save(reservation);
        log.info("Reservation updated successfully: {}", updatedReservation.getId());
        
        return convertToDTO(updatedReservation);
    }
    
    /**
     * Approuve une réservation
     */
    @Transactional
    @CacheEvict(value = {"reservations", "pendingReservations", "upcomingReservations"}, allEntries = true)
    public ReservationDTO approveReservation(Long id, Long approvedBy) {
        log.info("Approving reservation with ID: {} by user: {}", id, approvedBy);
        
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found with ID: " + id));
        
        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new RuntimeException("Only pending reservations can be approved");
        }
        
        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setApprovedBy(approvedBy);
        reservation.setApprovedAt(LocalDateTime.now());
        reservation.setUpdatedAt(LocalDateTime.now());
        
        Reservation approvedReservation = reservationRepository.save(reservation);
        log.info("Reservation approved: {}", approvedReservation.getId());
        
        return convertToDTO(approvedReservation);
    }
    
    /**
     * Rejette une réservation
     */
    @Transactional
    @CacheEvict(value = {"reservations", "pendingReservations", "upcomingReservations"}, allEntries = true)
    public ReservationDTO rejectReservation(Long id, Long rejectedBy, String reason) {
        log.info("Rejecting reservation with ID: {} by user: {}", id, rejectedBy);
        
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found with ID: " + id));
        
        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new RuntimeException("Only pending reservations can be rejected");
        }
        
        reservation.setStatus(ReservationStatus.REJECTED);
        reservation.setCancelledBy(rejectedBy);
        reservation.setCancelledAt(LocalDateTime.now());
        reservation.setCancellationReason(reason);
        reservation.setUpdatedAt(LocalDateTime.now());
        
        Reservation rejectedReservation = reservationRepository.save(reservation);
        log.info("Reservation rejected: {}", rejectedReservation.getId());
        
        return convertToDTO(rejectedReservation);
    }
    
    /**
     * Annule une réservation
     */
    @Transactional
    @CacheEvict(value = {"reservations", "pendingReservations", "upcomingReservations"}, allEntries = true)
    public ReservationDTO cancelReservation(Long id, Long cancelledBy, String reason) {
        log.info("Cancelling reservation with ID: {} by user: {}", id, cancelledBy);
        
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found with ID: " + id));
        
        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new RuntimeException("Reservation is already cancelled");
        }
        
        reservation.setStatus(ReservationStatus.CANCELLED);
        reservation.setCancelledBy(cancelledBy);
        reservation.setCancelledAt(LocalDateTime.now());
        reservation.setCancellationReason(reason);
        reservation.setUpdatedAt(LocalDateTime.now());
        
        Reservation cancelledReservation = reservationRepository.save(reservation);
        log.info("Reservation cancelled: {}", cancelledReservation.getId());
        
        return convertToDTO(cancelledReservation);
    }
    
    /**
     * Supprime une réservation
     */
    @Transactional
    @CacheEvict(value = {"reservations", "pendingReservations", "upcomingReservations"}, allEntries = true)
    public void deleteReservation(Long id) {
        log.info("Deleting reservation with ID: {}", id);
        
        if (!reservationRepository.existsById(id)) {
            throw new RuntimeException("Reservation not found with ID: " + id);
        }
        
        reservationRepository.deleteById(id);
        log.info("Reservation deleted: {}", id);
    }
    
    /**
     * Recherche des réservations par titre
     */
    public List<ReservationDTO> searchReservationsByTitle(String title) {
        log.debug("Searching reservations by title containing: {}", title);
        return reservationRepository.findByTitleContainingIgnoreCase(title).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Recherche avancée de réservations
     */
    public List<ReservationDTO> searchReservationsWithFilters(String title, Long userId, Long resourceId, 
            Long courseId, ReservationStatus status, ReservationType type) {
        log.debug("Searching reservations with advanced filters");
        return reservationRepository.findReservationsWithFilters(title, userId, resourceId, courseId, status, type).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Recherche textuelle globale
     */
    public List<ReservationDTO> searchReservations(String searchTerm) {
        log.debug("Searching reservations with term: {}", searchTerm);
        return reservationRepository.searchReservations(searchTerm).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Vérifie les conflits de ressource
     */
    public List<ReservationDTO> checkResourceConflicts(Long resourceId, LocalDateTime startTime, LocalDateTime endTime, Long excludeId) {
        Long excludeIdSafe = excludeId != null ? excludeId : -1L;
        return reservationRepository.findResourceConflicts(resourceId, startTime, endTime, excludeIdSafe).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Vérifie les conflits d'utilisateur
     */
    public List<ReservationDTO> checkUserConflicts(Long userId, LocalDateTime startTime, LocalDateTime endTime, Long excludeId) {
        Long excludeIdSafe = excludeId != null ? excludeId : -1L;
        return reservationRepository.findUserConflicts(userId, startTime, endTime, excludeIdSafe).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtient les statistiques des réservations
     */
    public ReservationStatistics getReservationStatistics() {
        long totalReservations = reservationRepository.count();
        long pendingReservations = reservationRepository.countByStatus(ReservationStatus.PENDING);
        long confirmedReservations = reservationRepository.countByStatus(ReservationStatus.CONFIRMED);
        long cancelledReservations = reservationRepository.countByStatus(ReservationStatus.CANCELLED);
        long rejectedReservations = reservationRepository.countByStatus(ReservationStatus.REJECTED);
        
        return ReservationStatistics.builder()
                .totalReservations(totalReservations)
                .pendingReservations(pendingReservations)
                .confirmedReservations(confirmedReservations)
                .cancelledReservations(cancelledReservations)
                .rejectedReservations(rejectedReservations)
                .build();
    }
    
    /**
     * Obtient les statistiques par statut
     */
    public List<Object[]> getReservationStatisticsByStatus() {
        return reservationRepository.getReservationCountByStatus();
    }
    
    /**
     * Obtient les statistiques par type
     */
    public List<Object[]> getReservationStatisticsByType() {
        return reservationRepository.getReservationCountByType();
    }
    
    /**
     * Obtient les statistiques par ressource
     */
    public List<Object[]> getReservationStatisticsByResource() {
        return reservationRepository.getReservationCountByResource();
    }
    
    /**
     * Vérifie si une réservation existe
     */
    public boolean existsById(Long id) {
        return reservationRepository.existsById(id);
    }
    
    /**
     * Compte le nombre total de réservations
     */
    public long countReservations() {
        return reservationRepository.count();
    }
    
    /**
     * Compte les réservations par statut
     */
    public long countReservationsByStatus(ReservationStatus status) {
        return reservationRepository.countByStatus(status);
    }
    
    /**
     * Validation des heures de réservation
     */
    private void validateReservationTimes(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            throw new RuntimeException("Start time and end time are required");
        }
        
        if (startTime.isAfter(endTime)) {
            throw new RuntimeException("Start time must be before end time");
        }
        
        if (startTime.isBefore(LocalDateTime.now().minus(1, ChronoUnit.HOURS))) {
            throw new RuntimeException("Cannot create reservation in the past");
        }
        
        long durationMinutes = ChronoUnit.MINUTES.between(startTime, endTime);
        if (durationMinutes < 15) {
            throw new RuntimeException("Reservation duration must be at least 15 minutes");
        }
        
        if (durationMinutes > 720) { // 12 heures
            throw new RuntimeException("Reservation duration cannot exceed 12 hours");
        }
    }
    
    /**
     * Vérifie les conflits avant création/modification
     */
    private void checkForConflicts(ReservationDTO reservationDTO, Long excludeId) {
        // Vérifier les conflits de ressource
        List<ReservationDTO> resourceConflicts = checkResourceConflicts(
            reservationDTO.getResourceId(), 
            reservationDTO.getStartTime(), 
            reservationDTO.getEndTime(), 
            excludeId
        );
        if (!resourceConflicts.isEmpty()) {
            throw new RuntimeException("Resource conflict detected: Resource " + reservationDTO.getResourceId() + 
                " is already reserved during this time");
        }
        
        // Vérifier les conflits d'utilisateur (optionnel, selon les règles métier)
        List<ReservationDTO> userConflicts = checkUserConflicts(
            reservationDTO.getUserId(), 
            reservationDTO.getStartTime(), 
            reservationDTO.getEndTime(), 
            excludeId
        );
        if (!userConflicts.isEmpty()) {
            log.warn("User {} has overlapping reservations", reservationDTO.getUserId());
            // Ne pas bloquer, juste avertir
        }
    }
    
    /**
     * Convertit une entité Reservation en DTO
     */
    private ReservationDTO convertToDTO(Reservation reservation) {
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
        dto.setScheduleId(reservation.getScheduleId());
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
        return dto;
    }
    
    /**
     * Classe pour les statistiques des réservations
     */
    @lombok.Builder
    @lombok.Data
    public static class ReservationStatistics {
        private long totalReservations;
        private long pendingReservations;
        private long confirmedReservations;
        private long cancelledReservations;
        private long rejectedReservations;
    }
}