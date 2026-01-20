package cm.iusjc.reservation.repository;

import cm.iusjc.reservation.entity.Reservation;
import cm.iusjc.reservation.entity.ReservationStatus;
import cm.iusjc.reservation.entity.ReservationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    
    // Recherche de conflits
    @Query("SELECT r FROM Reservation r WHERE r.resourceId = :resourceId " +
           "AND r.status IN ('PENDING', 'CONFIRMED') " +
           "AND ((r.startTime <= :endTime AND r.endTime >= :startTime) " +
           "OR (r.startTime - FUNCTION('MINUTE', r.setupTime) <= :endTime " +
           "AND r.endTime + FUNCTION('MINUTE', r.cleanupTime) >= :startTime)) " +
           "AND (:excludeId IS NULL OR r.id != :excludeId)")
    List<Reservation> findConflictingReservations(
        @Param("resourceId") Long resourceId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime,
        @Param("excludeId") Long excludeId
    );
    
    // Réservations par ressource
    List<Reservation> findByResourceIdAndStatusIn(Long resourceId, List<ReservationStatus> statuses);
    
    // Réservations par utilisateur
    List<Reservation> findByUserIdOrderByStartTimeDesc(Long userId);
    
    Page<Reservation> findByUserIdOrderByStartTimeDesc(Long userId, Pageable pageable);
    
    // Réservations par cours
    List<Reservation> findByCourseIdOrderByStartTime(Long courseId);
    
    // Réservations par groupe de cours
    List<Reservation> findByCourseGroupIdOrderByStartTime(Long courseGroupId);
    
    // Réservations par période
    @Query("SELECT r FROM Reservation r WHERE r.startTime >= :startDate " +
           "AND r.endTime <= :endDate ORDER BY r.startTime")
    List<Reservation> findByDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    // Réservations par ressource et période
    @Query("SELECT r FROM Reservation r WHERE r.resourceId = :resourceId " +
           "AND r.startTime >= :startDate AND r.endTime <= :endDate " +
           "ORDER BY r.startTime")
    List<Reservation> findByResourceAndDateRange(
        @Param("resourceId") Long resourceId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    // Réservations par statut
    List<Reservation> findByStatusOrderByStartTime(ReservationStatus status);
    
    Page<Reservation> findByStatusOrderByStartTime(ReservationStatus status, Pageable pageable);
    
    // Réservations par type
    List<Reservation> findByTypeOrderByStartTime(ReservationType type);
    
    // Réservations récurrentes
    List<Reservation> findByParentReservationIdOrderByStartTime(Long parentId);
    
    // Réservations en attente d'approbation
    @Query("SELECT r FROM Reservation r WHERE r.status = 'PENDING' " +
           "ORDER BY r.createdAt")
    List<Reservation> findPendingReservations();
    
    // Réservations à venir pour un utilisateur
    @Query("SELECT r FROM Reservation r WHERE r.userId = :userId " +
           "AND r.startTime > :now AND r.status IN ('PENDING', 'CONFIRMED') " +
           "ORDER BY r.startTime")
    List<Reservation> findUpcomingReservationsForUser(
        @Param("userId") Long userId,
        @Param("now") LocalDateTime now
    );
    
    // Réservations du jour pour une ressource
    @Query("SELECT r FROM Reservation r WHERE r.resourceId = :resourceId " +
           "AND DATE(r.startTime) = DATE(:date) " +
           "AND r.status IN ('PENDING', 'CONFIRMED') " +
           "ORDER BY r.startTime")
    List<Reservation> findTodayReservationsForResource(
        @Param("resourceId") Long resourceId,
        @Param("date") LocalDateTime date
    );
    
    // Statistiques
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.status = :status")
    Long countByStatus(@Param("status") ReservationStatus status);
    
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.resourceId = :resourceId " +
           "AND r.startTime >= :startDate AND r.endTime <= :endDate")
    Long countByResourceAndDateRange(
        @Param("resourceId") Long resourceId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    // Recherche avec filtres
    @Query("SELECT r FROM Reservation r WHERE " +
           "(:resourceId IS NULL OR r.resourceId = :resourceId) AND " +
           "(:userId IS NULL OR r.userId = :userId) AND " +
           "(:status IS NULL OR r.status = :status) AND " +
           "(:type IS NULL OR r.type = :type) AND " +
           "(:startDate IS NULL OR r.startTime >= :startDate) AND " +
           "(:endDate IS NULL OR r.endTime <= :endDate) " +
           "ORDER BY r.startTime")
    Page<Reservation> findWithFilters(
        @Param("resourceId") Long resourceId,
        @Param("userId") Long userId,
        @Param("status") ReservationStatus status,
        @Param("type") ReservationType type,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );
}