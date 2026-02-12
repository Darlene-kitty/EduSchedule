package cm.iusjc.scheduling.repository;

import cm.iusjc.scheduling.entity.Schedule;
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
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    
    // Recherche de base
    Optional<Schedule> findByTitle(String title);
    List<Schedule> findByTitleContainingIgnoreCase(String title);
    boolean existsByTitle(String title);
    
    // Recherche par statut
    List<Schedule> findByStatus(String status);
    List<Schedule> findByStatusOrderByStartTimeAsc(String status);
    long countByStatus(String status);
    
    // Recherche par enseignant
    List<Schedule> findByTeacher(String teacher);
    List<Schedule> findByTeacherOrderByStartTimeAsc(String teacher);
    long countByTeacher(String teacher);
    
    // Recherche par cours
    List<Schedule> findByCourse(String course);
    List<Schedule> findByCourseOrderByStartTimeAsc(String course);
    long countByCourse(String course);
    
    // Recherche par salle
    List<Schedule> findByRoom(String room);
    List<Schedule> findByRoomOrderByStartTimeAsc(String room);
    long countByRoom(String room);
    
    // Recherche par groupe
    List<Schedule> findByGroupName(String groupName);
    List<Schedule> findByGroupNameOrderByStartTimeAsc(String groupName);
    long countByGroupName(String groupName);
    
    // Recherche par période
    List<Schedule> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);
    List<Schedule> findByStartTimeBetweenOrderByStartTimeAsc(LocalDateTime start, LocalDateTime end);
    
    List<Schedule> findByEndTimeBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT s FROM Schedule s WHERE s.startTime >= :start AND s.endTime <= :end")
    List<Schedule> findSchedulesInPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    // Recherche par date
    @Query("SELECT s FROM Schedule s WHERE DATE(s.startTime) = DATE(:date)")
    List<Schedule> findByDate(@Param("date") LocalDateTime date);
    
    @Query("SELECT s FROM Schedule s WHERE DATE(s.startTime) = DATE(:date) ORDER BY s.startTime ASC")
    List<Schedule> findByDateOrderByStartTime(@Param("date") LocalDateTime date);
    
    // Conflits de planning
    @Query("SELECT s FROM Schedule s WHERE s.room = :room AND " +
           "((s.startTime <= :startTime AND s.endTime > :startTime) OR " +
           "(s.startTime < :endTime AND s.endTime >= :endTime) OR " +
           "(s.startTime >= :startTime AND s.endTime <= :endTime)) AND " +
           "s.status = 'ACTIVE' AND s.id != :excludeId")
    List<Schedule> findRoomConflicts(@Param("room") String room, 
                                   @Param("startTime") LocalDateTime startTime,
                                   @Param("endTime") LocalDateTime endTime,
                                   @Param("excludeId") Long excludeId);
    
    @Query("SELECT s FROM Schedule s WHERE s.teacher = :teacher AND " +
           "((s.startTime <= :startTime AND s.endTime > :startTime) OR " +
           "(s.startTime < :endTime AND s.endTime >= :endTime) OR " +
           "(s.startTime >= :startTime AND s.endTime <= :endTime)) AND " +
           "s.status = 'ACTIVE' AND s.id != :excludeId")
    List<Schedule> findTeacherConflicts(@Param("teacher") String teacher,
                                      @Param("startTime") LocalDateTime startTime,
                                      @Param("endTime") LocalDateTime endTime,
                                      @Param("excludeId") Long excludeId);
    
    // Recherche avancée
    @Query("SELECT s FROM Schedule s WHERE " +
           "(:title IS NULL OR LOWER(s.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
           "(:teacher IS NULL OR LOWER(s.teacher) LIKE LOWER(CONCAT('%', :teacher, '%'))) AND " +
           "(:course IS NULL OR LOWER(s.course) LIKE LOWER(CONCAT('%', :course, '%'))) AND " +
           "(:room IS NULL OR LOWER(s.room) LIKE LOWER(CONCAT('%', :room, '%'))) AND " +
           "(:groupName IS NULL OR LOWER(s.groupName) LIKE LOWER(CONCAT('%', :groupName, '%'))) AND " +
           "(:status IS NULL OR s.status = :status)")
    List<Schedule> findSchedulesWithFilters(@Param("title") String title,
                                          @Param("teacher") String teacher,
                                          @Param("course") String course,
                                          @Param("room") String room,
                                          @Param("groupName") String groupName,
                                          @Param("status") String status);
    
    // Recherche textuelle globale
    @Query("SELECT s FROM Schedule s WHERE " +
           "LOWER(s.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(s.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(s.teacher) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(s.course) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(s.room) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(s.groupName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Schedule> searchSchedules(@Param("searchTerm") String searchTerm);
    
    // Statistiques
    @Query("SELECT s.status, COUNT(s) FROM Schedule s GROUP BY s.status")
    List<Object[]> getScheduleCountByStatus();
    
    @Query("SELECT s.teacher, COUNT(s) FROM Schedule s WHERE s.teacher IS NOT NULL GROUP BY s.teacher ORDER BY COUNT(s) DESC")
    List<Object[]> getScheduleCountByTeacher();
    
    @Query("SELECT s.course, COUNT(s) FROM Schedule s WHERE s.course IS NOT NULL GROUP BY s.course ORDER BY COUNT(s) DESC")
    List<Object[]> getScheduleCountByCourse();
    
    @Query("SELECT s.room, COUNT(s) FROM Schedule s WHERE s.room IS NOT NULL GROUP BY s.room ORDER BY COUNT(s) DESC")
    List<Object[]> getScheduleCountByRoom();
    
    @Query("SELECT DATE(s.startTime), COUNT(s) FROM Schedule s GROUP BY DATE(s.startTime) ORDER BY DATE(s.startTime)")
    List<Object[]> getScheduleCountByDate();
    
    // Plannings à venir
    @Query("SELECT s FROM Schedule s WHERE s.startTime > :now AND s.status = 'ACTIVE' ORDER BY s.startTime ASC")
    List<Schedule> findUpcomingSchedules(@Param("now") LocalDateTime now);
    
    @Query("SELECT s FROM Schedule s WHERE s.startTime > :now AND s.status = 'ACTIVE' ORDER BY s.startTime ASC")
    Page<Schedule> findUpcomingSchedules(@Param("now") LocalDateTime now, Pageable pageable);
    
    // Plannings en cours
    @Query("SELECT s FROM Schedule s WHERE s.startTime <= :now AND s.endTime > :now AND s.status = 'ACTIVE'")
    List<Schedule> findCurrentSchedules(@Param("now") LocalDateTime now);
    
    // Plannings passés
    @Query("SELECT s FROM Schedule s WHERE s.endTime < :now ORDER BY s.startTime DESC")
    List<Schedule> findPastSchedules(@Param("now") LocalDateTime now);
    
    @Query("SELECT s FROM Schedule s WHERE s.endTime < :now ORDER BY s.startTime DESC")
    Page<Schedule> findPastSchedules(@Param("now") LocalDateTime now, Pageable pageable);
    
    // Plannings de la semaine
    @Query("SELECT s FROM Schedule s WHERE s.startTime >= :weekStart AND s.startTime < :weekEnd ORDER BY s.startTime ASC")
    List<Schedule> findWeeklySchedules(@Param("weekStart") LocalDateTime weekStart, @Param("weekEnd") LocalDateTime weekEnd);
    
    // Plannings du mois
    @Query("SELECT s FROM Schedule s WHERE s.startTime >= :monthStart AND s.startTime < :monthEnd ORDER BY s.startTime ASC")
    List<Schedule> findMonthlySchedules(@Param("monthStart") LocalDateTime monthStart, @Param("monthEnd") LocalDateTime monthEnd);
    
    // Méthodes pour la détection de conflits enseignants
    @Query("SELECT s FROM Schedule s WHERE s.teacher = :teacher AND s.startTime >= :startTime AND s.endTime <= :endTime")
    List<Schedule> findByTeacherAndDateRange(@Param("teacher") String teacher, 
                                            @Param("startTime") LocalDateTime startTime, 
                                            @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT s FROM Schedule s WHERE s.teacher = :teacher AND DATE(s.startTime) = DATE(:date)")
    List<Schedule> findByTeacherAndDate(@Param("teacher") String teacher, 
                                       @Param("date") java.time.LocalDate date);
}