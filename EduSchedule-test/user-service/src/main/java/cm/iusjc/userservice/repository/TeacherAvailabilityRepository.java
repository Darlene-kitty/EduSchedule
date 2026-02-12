package cm.iusjc.userservice.repository;

import cm.iusjc.userservice.entity.TeacherAvailability;
import cm.iusjc.userservice.entity.AvailabilityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TeacherAvailabilityRepository extends JpaRepository<TeacherAvailability, Long> {
    
    // Disponibilités par enseignant
    List<TeacherAvailability> findByTeacherIdAndActiveTrue(Long teacherId);
    
    // Disponibilités par enseignant et école
    List<TeacherAvailability> findByTeacherIdAndSchoolIdAndActiveTrue(Long teacherId, Long schoolId);
    
    // Disponibilités par jour de la semaine
    List<TeacherAvailability> findByTeacherIdAndDayOfWeekAndActiveTrue(Long teacherId, DayOfWeek dayOfWeek);
    
    // Disponibilités par type
    List<TeacherAvailability> findByTeacherIdAndAvailabilityTypeAndActiveTrue(Long teacherId, AvailabilityType type);
    
    // Vérifier disponibilité à un créneau spécifique
    @Query("SELECT ta FROM TeacherAvailability ta WHERE ta.teacherId = :teacherId " +
           "AND ta.dayOfWeek = :dayOfWeek " +
           "AND ta.startTime <= :time " +
           "AND ta.endTime >= :time " +
           "AND ta.availabilityType = 'AVAILABLE' " +
           "AND ta.active = true")
    List<TeacherAvailability> findAvailableAt(@Param("teacherId") Long teacherId, 
                                             @Param("dayOfWeek") DayOfWeek dayOfWeek, 
                                             @Param("time") LocalTime time);
    
    // Trouver les conflits de disponibilité
    @Query("SELECT ta FROM TeacherAvailability ta WHERE ta.teacherId = :teacherId " +
           "AND ta.dayOfWeek = :dayOfWeek " +
           "AND ta.startTime < :endTime " +
           "AND ta.endTime > :startTime " +
           "AND ta.active = true " +
           "AND (:excludeId IS NULL OR ta.id != :excludeId)")
    List<TeacherAvailability> findConflictingAvailabilities(@Param("teacherId") Long teacherId,
                                                           @Param("dayOfWeek") DayOfWeek dayOfWeek,
                                                           @Param("startTime") LocalTime startTime,
                                                           @Param("endTime") LocalTime endTime,
                                                           @Param("excludeId") Long excludeId);
    
    // Disponibilités récurrentes
    List<TeacherAvailability> findByTeacherIdAndRecurringTrueAndActiveTrue(Long teacherId);
    
    // Disponibilités ponctuelles
    @Query("SELECT ta FROM TeacherAvailability ta WHERE ta.teacherId = :teacherId " +
           "AND ta.recurring = false " +
           "AND ta.specificDate BETWEEN :startDate AND :endDate " +
           "AND ta.active = true")
    List<TeacherAvailability> findSpecificAvailabilities(@Param("teacherId") Long teacherId,
                                                        @Param("startDate") LocalDateTime startDate,
                                                        @Param("endDate") LocalDateTime endDate);
    
    // Disponibilités par priorité
    List<TeacherAvailability> findByTeacherIdAndPriorityAndActiveTrue(Long teacherId, Integer priority);
    
    // Statistiques de disponibilité
    @Query("SELECT COUNT(ta) FROM TeacherAvailability ta WHERE ta.teacherId = :teacherId " +
           "AND ta.availabilityType = :type AND ta.active = true")
    Long countByTeacherAndType(@Param("teacherId") Long teacherId, @Param("type") AvailabilityType type);
    
    // Heures totales de disponibilité par semaine
    @Query("SELECT SUM(FUNCTION('TIMESTAMPDIFF', HOUR, ta.startTime, ta.endTime)) " +
           "FROM TeacherAvailability ta WHERE ta.teacherId = :teacherId " +
           "AND ta.availabilityType = 'AVAILABLE' AND ta.recurring = true AND ta.active = true")
    Long getTotalAvailableHoursPerWeek(@Param("teacherId") Long teacherId);
}