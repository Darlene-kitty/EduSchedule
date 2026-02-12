package cm.iusjc.teacheravailability.repository;

import cm.iusjc.teacheravailability.entity.AvailabilityType;
import cm.iusjc.teacheravailability.entity.TeacherAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface TeacherAvailabilityRepository extends JpaRepository<TeacherAvailability, Long> {
    
    // Find by teacher
    List<TeacherAvailability> findByTeacherIdAndIsActiveTrue(Long teacherId);
    
    // Find by teacher and school
    List<TeacherAvailability> findByTeacherIdAndSchoolIdAndIsActiveTrue(Long teacherId, Long schoolId);
    
    // Find by teacher and day
    List<TeacherAvailability> findByTeacherIdAndDayOfWeekAndIsActiveTrue(Long teacherId, DayOfWeek dayOfWeek);
    
    // Find by teacher, day and availability type
    List<TeacherAvailability> findByTeacherIdAndDayOfWeekAndAvailabilityTypeAndIsActiveTrue(
            Long teacherId, DayOfWeek dayOfWeek, AvailabilityType availabilityType);
    
    // Find by specific date
    List<TeacherAvailability> findByTeacherIdAndSpecificDateAndIsActiveTrue(Long teacherId, LocalDate specificDate);
    
    // Find recurring availabilities
    List<TeacherAvailability> findByTeacherIdAndIsRecurringTrueAndIsActiveTrue(Long teacherId);
    
    // Find conflicts
    @Query("SELECT ta FROM TeacherAvailability ta WHERE ta.teacherId = :teacherId " +
           "AND ta.dayOfWeek = :dayOfWeek AND ta.isActive = true " +
           "AND ((ta.startTime <= :endTime AND ta.endTime >= :startTime))")
    List<TeacherAvailability> findConflictingAvailabilities(
            @Param("teacherId") Long teacherId,
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime);
    
    // Find available slots
    @Query("SELECT ta FROM TeacherAvailability ta WHERE ta.teacherId = :teacherId " +
           "AND ta.dayOfWeek = :dayOfWeek AND ta.isActive = true " +
           "AND ta.availabilityType IN ('AVAILABLE', 'PREFERRED') " +
           "AND ta.startTime <= :time AND ta.endTime >= :time")
    List<TeacherAvailability> findAvailableAt(
            @Param("teacherId") Long teacherId,
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("time") LocalTime time);
    
    // Find by school
    List<TeacherAvailability> findBySchoolIdAndIsActiveTrue(Long schoolId);
    
    // Find preferred slots
    List<TeacherAvailability> findByTeacherIdAndAvailabilityTypeAndIsActiveTrue(
            Long teacherId, AvailabilityType availabilityType);
    
    // Find by priority level
    List<TeacherAvailability> findByTeacherIdAndPriorityLevelGreaterThanEqualAndIsActiveTrue(
            Long teacherId, Integer priorityLevel);
    
    // Statistics queries
    @Query("SELECT COUNT(ta) FROM TeacherAvailability ta WHERE ta.teacherId = :teacherId AND ta.isActive = true")
    Long countByTeacherId(@Param("teacherId") Long teacherId);
    
    @Query("SELECT ta.dayOfWeek, COUNT(ta) FROM TeacherAvailability ta " +
           "WHERE ta.teacherId = :teacherId AND ta.isActive = true " +
           "GROUP BY ta.dayOfWeek")
    List<Object[]> countByTeacherIdGroupByDay(@Param("teacherId") Long teacherId);
    
    // Delete by teacher
    void deleteByTeacherId(Long teacherId);
    
    // Delete by teacher and school
    void deleteByTeacherIdAndSchoolId(Long teacherId, Long schoolId);
}