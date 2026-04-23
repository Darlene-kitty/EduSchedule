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
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    
    // Recherche de base
    List<Reservation> findByTitleContainingIgnoreCase(String title);
    boolean existsByTitle(String title);
    
    // Recherche par utilisateur
    List<Reservation> findByUserId(Long userId);
    List<Reservation> findByUserIdOrderByStartTimeDesc(Long userId);
    Page<Reservation> findByUserIdOrderByStartTimeDesc(Long userId, Pageable pageable);
    long countByUserId(Long userId);
    
    // Recherche par ressource
    List<Reservation> findByResourceId(Long resourceId);
    List<Reservation> findByResourceIdOrderByStartTimeAsc(Long resourceId);
    Page<Reservation> findByResourceIdOrderByStartTimeAsc(Long resourceId, Pageable pageable);
    long countByResourceId(Long resourceId);
    
    // Recherche par cours
    List<Reservation> findByCourseId(Long courseId);
    List<Reservation> findByCourseIdOrderByStartTimeAsc(Long courseId);
    long countByCourseId(Long courseId);
    
    // Recherche par groupe de cours
    List<Reservation> findByCourseGroupId(Long courseGroupId);
    List<Reservation> findByCourseGroupIdOrderByStartTimeAsc(Long courseGroupId);
    long countByCourseGroupId(Long courseGroupId);
    
    // Recherche par statut
    List<Reservation> findByStatus(ReservationStatus status);
    List<Reservation> findByStatusOrderByStartTimeAsc(ReservationStatus status);
    Page<Reservation> findByStatusOrderByStartTimeAsc(ReservationStatus status, Pageable pageable);
    long countByStatus(ReservationStatus status);
    
    // Recherche par type
    List<Reservation> findByType(ReservationType type);
    List<Reservation> findByTypeOrderByStartTimeAsc(ReservationType type);
    long countByType(ReservationType type);
    
    // Recherche par période
    List<Reservation> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);
    List<Reservation> findByStartTimeBetweenOrderByStartTimeAsc(LocalDateTime start, LocalDateTime end);
    
    List<Reservation> findByEndTimeBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT r FROM Reservation r WHERE r.startTime >= :start AND r.endTime <= :end")
    List<Reservation> findReservationsInPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    // Recherche par date
    @Query("SELECT r FROM Reservation r WHERE DATE(r.startTime) = DATE(:date)")
    List<Reservation> findByDate(@Param("date") LocalDateTime date);
    
    @Query("SELECT r FROM Reservation r WHERE DATE(r.startTime) = DATE(:date) ORDER BY r.startTime ASC")
    List<Reservation> findByDateOrderByStartTime(@Param("date") LocalDateTime date);
    
    // Conflits de réservation
    @Query("SELECT r FROM Reservation r WHERE r.resourceId = :resourceId AND " +
           "r.status IN ('PENDING', 'CONFIRMED') AND " +
           "((r.startTime <= :startTime AND r.endTime > :startTime) OR " +
           "(r.startTime < :endTime AND r.endTime >= :endTime) OR " +
           "(r.startTime >= :startTime AND r.endTime <= :endTime)) AND " +
           "r.id != :excludeId")
    List<Reservation> findResourceConflicts(@Param("resourceId") Long resourceId,
                                          @Param("startTime") LocalDateTime startTime,
                                          @Param("endTime") LocalDateTime endTime,
                                          @Param("excludeId") Long excludeId);
    
    @Query("SELECT r FROM Reservation r WHERE r.userId = :userId AND " +
           "r.status IN ('PENDING', 'CONFIRMED') AND " +
           "((r.startTime <= :startTime AND r.endTime > :startTime) OR " +
           "(r.startTime < :endTime AND r.endTime >= :endTime) OR " +
           "(r.startTime >= :startTime AND r.endTime <= :endTime)) AND " +
           "r.id != :excludeId")
    List<Reservation> findUserConflicts(@Param("userId") Long userId,
                                      @Param("startTime") LocalDateTime startTime,
                                      @Param("endTime") LocalDateTime endTime,
                                      @Param("excludeId") Long excludeId);
    
    // Recherche avancée
    @Query("SELECT r FROM Reservation r WHERE " +
           "(:title IS NULL OR LOWER(r.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
           "(:userId IS NULL OR r.userId = :userId) AND " +
           "(:resourceId IS NULL OR r.resourceId = :resourceId) AND " +
           "(:courseId IS NULL OR r.courseId = :courseId) AND " +
           "(:status IS NULL OR r.status = :status) AND " +
           "(:type IS NULL OR r.type = :type)")
    List<Reservation> findReservationsWithFilters(@Param("title") String title,
                                                 @Param("userId") Long userId,
                                                 @Param("resourceId") Long resourceId,
                                                 @Param("courseId") Long courseId,
                                                 @Param("status") ReservationStatus status,
                                                 @Param("type") ReservationType type);
    
    // Recherche textuelle globale
    @Query("SELECT r FROM Reservation r WHERE " +
           "LOWER(r.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(r.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(r.notes) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Reservation> searchReservations(@Param("searchTerm") String searchTerm);
    
    // Réservations récurrentes
    List<Reservation> findByParentReservationId(Long parentReservationId);
    List<Reservation> findByParentReservationIdOrderByStartTimeAsc(Long parentReservationId);
    
    @Query("SELECT r FROM Reservation r WHERE r.recurringPattern IS NOT NULL AND r.recurringPattern != ''")
    List<Reservation> findRecurringReservations();
    
    // Réservations en attente d'approbation
    @Query("SELECT r FROM Reservation r WHERE r.status = 'PENDING' ORDER BY r.createdAt ASC")
    List<Reservation> findPendingReservations();
    
    @Query("SELECT r FROM Reservation r WHERE r.status = 'PENDING' ORDER BY r.createdAt ASC")
    Page<Reservation> findPendingReservations(Pageable pageable);
    
    // Réservations à venir
    @Query("SELECT r FROM Reservation r WHERE r.startTime > :now AND r.status = 'CONFIRMED' ORDER BY r.startTime ASC")
    List<Reservation> findUpcomingReservations(@Param("now") LocalDateTime now);
    
    @Query("SELECT r FROM Reservation r WHERE r.startTime > :now AND r.status = 'CONFIRMED' ORDER BY r.startTime ASC")
    Page<Reservation> findUpcomingReservations(@Param("now") LocalDateTime now, Pageable pageable);
    
    // Réservations en cours
    @Query("SELECT r FROM Reservation r WHERE r.startTime <= :now AND r.endTime > :now AND r.status = 'CONFIRMED'")
    List<Reservation> findCurrentReservations(@Param("now") LocalDateTime now);
    
    // Réservations passées
    @Query("SELECT r FROM Reservation r WHERE r.endTime < :now ORDER BY r.startTime DESC")
    List<Reservation> findPastReservations(@Param("now") LocalDateTime now);
    
    @Query("SELECT r FROM Reservation r WHERE r.endTime < :now ORDER BY r.startTime DESC")
    Page<Reservation> findPastReservations(@Param("now") LocalDateTime now, Pageable pageable);
    
    // Réservations de la semaine
    @Query("SELECT r FROM Reservation r WHERE r.startTime >= :weekStart AND r.startTime < :weekEnd ORDER BY r.startTime ASC")
    List<Reservation> findWeeklyReservations(@Param("weekStart") LocalDateTime weekStart, @Param("weekEnd") LocalDateTime weekEnd);
    
    // Réservations du mois
    @Query("SELECT r FROM Reservation r WHERE r.startTime >= :monthStart AND r.startTime < :monthEnd ORDER BY r.startTime ASC")
    List<Reservation> findMonthlyReservations(@Param("monthStart") LocalDateTime monthStart, @Param("monthEnd") LocalDateTime monthEnd);
    
    // Statistiques
    @Query("SELECT r.status, COUNT(r) FROM Reservation r GROUP BY r.status")
    List<Object[]> getReservationCountByStatus();
    
    @Query("SELECT r.type, COUNT(r) FROM Reservation r GROUP BY r.type")
    List<Object[]> getReservationCountByType();
    
    @Query("SELECT r.resourceId, COUNT(r) FROM Reservation r GROUP BY r.resourceId ORDER BY COUNT(r) DESC")
    List<Object[]> getReservationCountByResource();
    
    @Query("SELECT r.userId, COUNT(r) FROM Reservation r GROUP BY r.userId ORDER BY COUNT(r) DESC")
    List<Object[]> getReservationCountByUser();
    
    @Query("SELECT DATE(r.startTime), COUNT(r) FROM Reservation r GROUP BY DATE(r.startTime) ORDER BY DATE(r.startTime)")
    List<Object[]> getReservationCountByDate();
    
    @Query("SELECT HOUR(r.startTime), COUNT(r) FROM Reservation r GROUP BY HOUR(r.startTime) ORDER BY HOUR(r.startTime)")
    List<Object[]> getReservationCountByHour();
    
    // Réservations approuvées par
    List<Reservation> findByApprovedBy(Long approvedBy);
    List<Reservation> findByApprovedByOrderByApprovedAtDesc(Long approvedBy);
    
    // Réservations annulées par
    List<Reservation> findByCancelledBy(Long cancelledBy);
    List<Reservation> findByCancelledByOrderByCancelledAtDesc(Long cancelledBy);
    
    // Réservations avec emploi du temps
    List<Reservation> findByScheduleId(Long scheduleId);
    
    // Réservations nécessitant une action
    @Query("SELECT r FROM Reservation r WHERE r.status = 'PENDING' AND r.createdAt < :cutoffTime")
    List<Reservation> findStaleReservations(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    // Utilisation des ressources
    @Query("SELECT r.resourceId, AVG(TIMESTAMPDIFF(MINUTE, r.startTime, r.endTime)) FROM Reservation r " +
           "WHERE r.status = 'CONFIRMED' GROUP BY r.resourceId")
    List<Object[]> getAverageReservationDurationByResource();
    
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.resourceId = :resourceId AND " +
           "r.status = 'CONFIRMED' AND r.startTime >= :start AND r.endTime <= :end")
    Long getResourceUtilizationCount(@Param("resourceId") Long resourceId,
                                   @Param("start") LocalDateTime start,
                                   @Param("end") LocalDateTime end);
    
    // Méthodes pour la détection de conflits avec temps de préparation
    @Query("SELECT r FROM Reservation r WHERE r.resourceId = :resourceId AND " +
           "r.status IN ('PENDING', 'CONFIRMED') AND " +
           "((r.startTime <= :startTime AND r.endTime > :startTime) OR " +
           "(r.startTime < :endTime AND r.endTime >= :endTime) OR " +
           "(r.startTime >= :startTime AND r.endTime <= :endTime)) AND " +
           "(:excludeId IS NULL OR r.id != :excludeId)")
    List<Reservation> findConflictingReservationsWithSetup(@Param("resourceId") Long resourceId,
                                                          @Param("startTime") LocalDateTime startTime,
                                                          @Param("endTime") LocalDateTime endTime,
                                                          @Param("excludeId") Long excludeId);
    
    // Méthodes pour la détection de conflits sur plusieurs ressources
    @Query("SELECT r FROM Reservation r WHERE r.resourceId IN :resourceIds AND " +
           "r.status IN ('PENDING', 'CONFIRMED') AND " +
           "((r.startTime <= :startTime AND r.endTime > :startTime) OR " +
           "(r.startTime < :endTime AND r.endTime >= :endTime) OR " +
           "(r.startTime >= :startTime AND r.endTime <= :endTime))")
    List<Reservation> findConflictingReservationsForMultipleResources(@Param("resourceIds") List<Long> resourceIds,
                                                                     @Param("startTime") LocalDateTime startTime,
                                                                     @Param("endTime") LocalDateTime endTime);
    
    // Compter les réservations pour une ressource à une date donnée
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.resourceId = :resourceId AND " +
           "DATE(r.startTime) = DATE(:date) AND r.status IN ('PENDING', 'CONFIRMED')")
    Long countReservationsForResourceAndDate(@Param("resourceId") Long resourceId,
                                            @Param("date") LocalDateTime date);
    
    // Méthode pour trouver les réservations en conflit (alias)
    @Query("SELECT r FROM Reservation r WHERE r.resourceId = :resourceId AND " +
           "r.status IN ('PENDING', 'CONFIRMED') AND " +
           "((r.startTime <= :startTime AND r.endTime > :startTime) OR " +
           "(r.startTime < :endTime AND r.endTime >= :endTime) OR " +
           "(r.startTime >= :startTime AND r.endTime <= :endTime))")
    List<Reservation> findConflictingReservations(@Param("resourceId") Long resourceId,
                                                 @Param("startTime") LocalDateTime startTime,
                                                 @Param("endTime") LocalDateTime endTime);
    
    // Recherche par ressource et période
    @Query("SELECT r FROM Reservation r WHERE r.resourceId = :resourceId AND " +
           "r.startTime >= :startTime AND r.startTime <= :endTime")
    List<Reservation> findByResourceIdAndStartTimeBetween(@Param("resourceId") Long resourceId,
                                                         @Param("startTime") LocalDateTime startTime,
                                                         @Param("endTime") LocalDateTime endTime);
}