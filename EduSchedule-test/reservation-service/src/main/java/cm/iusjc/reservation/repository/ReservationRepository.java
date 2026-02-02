package cm.iusjc.reservation.repository;

import cm.iusjc.reservation.entity.Reservation;
import cm.iusjc.reservation.entity.ReservationStatus;
import cm.iusjc.reservation.entity.ReservationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.QueryHint;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    
    // Recherche de conflits optimisée avec index et cache
    @Query(value = "SELECT r.* FROM reservations r " +
           "WHERE r.resource_id = :resourceId " +
           "AND r.status IN ('PENDING', 'CONFIRMED') " +
           "AND r.start_time <= :endTime " +
           "AND r.end_time >= :startTime " +
           "AND (:excludeId IS NULL OR r.id != :excludeId) " +
           "ORDER BY r.start_time", 
           nativeQuery = true)
    @QueryHints({
        @QueryHint(name = "org.hibernate.cacheable", value = "true"),
        @QueryHint(name = "org.hibernate.cacheRegion", value = "conflictQueries")
    })
    List<Reservation> findConflictingReservations(
        @Param("resourceId") Long resourceId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime,
        @Param("excludeId") Long excludeId
    );
    
    // Recherche de conflits avec setup/cleanup optimisée
    @Query(value = "SELECT r.* FROM reservations r " +
           "WHERE r.resource_id = :resourceId " +
           "AND r.status IN ('PENDING', 'CONFIRMED') " +
           "AND (r.start_time - INTERVAL COALESCE(r.setup_time, 0) MINUTE) <= :endTime " +
           "AND (r.end_time + INTERVAL COALESCE(r.cleanup_time, 0) MINUTE) >= :startTime " +
           "AND (:excludeId IS NULL OR r.id != :excludeId) " +
           "ORDER BY r.start_time", 
           nativeQuery = true)
    @QueryHints({
        @QueryHint(name = "org.hibernate.cacheable", value = "true"),
        @QueryHint(name = "org.hibernate.cacheRegion", value = "conflictQueries")
    })
    List<Reservation> findConflictingReservationsWithSetup(
        @Param("resourceId") Long resourceId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime,
        @Param("excludeId") Long excludeId
    );
    
    // Recherche rapide de disponibilité par ressource et jour
    @Query(value = "SELECT COUNT(*) FROM reservations r " +
           "WHERE r.resource_id = :resourceId " +
           "AND r.status IN ('PENDING', 'CONFIRMED') " +
           "AND DATE(r.start_time) = DATE(:date)", 
           nativeQuery = true)
    @QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
    Long countReservationsForResourceAndDate(
        @Param("resourceId") Long resourceId,
        @Param("date") LocalDateTime date
    );
    
    // Index optimisé pour les conflits multi-ressources
    @Query(value = "SELECT r.* FROM reservations r " +
           "WHERE r.resource_id IN (:resourceIds) " +
           "AND r.status IN ('PENDING', 'CONFIRMED') " +
           "AND r.start_time <= :endTime " +
           "AND r.end_time >= :startTime " +
           "ORDER BY r.resource_id, r.start_time", 
           nativeQuery = true)
    @QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
    List<Reservation> findConflictingReservationsForMultipleResources(
        @Param("resourceIds") List<Long> resourceIds,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
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
    
    // Réservations par utilisateur avec pagination optimisée
    @Query(value = "SELECT r.* FROM reservations r " +
           "WHERE r.user_id = :userId " +
           "ORDER BY r.start_time DESC " +
           "LIMIT :limit OFFSET :offset", 
           nativeQuery = true)
    List<Reservation> findByUserIdOrderByStartTimeDescOptimized(
        @Param("userId") Long userId,
        @Param("limit") int limit,
        @Param("offset") int offset
    );
    
    // Recherche avec filtres optimisée
    @Query(value = "SELECT r.* FROM reservations r WHERE " +
           "(:resourceId IS NULL OR r.resource_id = :resourceId) AND " +
           "(:userId IS NULL OR r.user_id = :userId) AND " +
           "(:status IS NULL OR r.status = :status) AND " +
           "(:type IS NULL OR r.type = :type) AND " +
           "(:startDate IS NULL OR r.start_time >= :startDate) AND " +
           "(:endDate IS NULL OR r.end_time <= :endDate) " +
           "ORDER BY r.start_time " +
           "LIMIT :limit OFFSET :offset", 
           nativeQuery = true)
    List<Reservation> findWithFiltersOptimized(
        @Param("resourceId") Long resourceId,
        @Param("userId") Long userId,
        @Param("status") String status,
        @Param("type") String type,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("limit") int limit,
        @Param("offset") int offset
    );
    
    // Statistiques d'occupation par ressource et période
    @Query(value = "SELECT " +
           "r.resource_id, " +
           "COUNT(*) as total_reservations, " +
           "SUM(TIMESTAMPDIFF(MINUTE, r.start_time, r.end_time)) as total_minutes, " +
           "AVG(TIMESTAMPDIFF(MINUTE, r.start_time, r.end_time)) as avg_duration " +
           "FROM reservations r " +
           "WHERE r.start_time >= :startDate " +
           "AND r.end_time <= :endDate " +
           "AND r.status IN ('CONFIRMED', 'COMPLETED') " +
           "GROUP BY r.resource_id " +
           "ORDER BY total_minutes DESC", 
           nativeQuery = true)
    @QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
    List<Object[]> getOccupancyStatsByResourceAndPeriod(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    // Créneaux libres pour une ressource
    @Query(value = "SELECT " +
           "CASE " +
           "  WHEN LAG(r.end_time) OVER (ORDER BY r.start_time) IS NULL THEN :dayStart " +
           "  ELSE LAG(r.end_time) OVER (ORDER BY r.start_time) " +
           "END as free_start, " +
           "r.start_time as free_end " +
           "FROM reservations r " +
           "WHERE r.resource_id = :resourceId " +
           "AND DATE(r.start_time) = DATE(:date) " +
           "AND r.status IN ('PENDING', 'CONFIRMED') " +
           "ORDER BY r.start_time", 
           nativeQuery = true)
    List<Object[]> findFreeSlots(
        @Param("resourceId") Long resourceId,
        @Param("date") LocalDateTime date,
        @Param("dayStart") LocalDateTime dayStart
    );
    
    // Réservation liée à un emploi du temps
    Optional<Reservation> findByScheduleId(Long scheduleId);
}