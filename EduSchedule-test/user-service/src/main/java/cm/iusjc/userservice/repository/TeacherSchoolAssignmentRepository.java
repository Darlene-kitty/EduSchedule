package cm.iusjc.userservice.repository;

import cm.iusjc.userservice.entity.TeacherSchoolAssignment;
import cm.iusjc.userservice.entity.ContractType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TeacherSchoolAssignmentRepository extends JpaRepository<TeacherSchoolAssignment, Long> {
    
    // Assignations par enseignant
    List<TeacherSchoolAssignment> findByTeacherIdAndActiveTrue(Long teacherId);
    
    // Assignations par école
    List<TeacherSchoolAssignment> findBySchoolIdAndActiveTrue(Long schoolId);
    
    // Assignations actives à une date donnée
    @Query("SELECT tsa FROM TeacherSchoolAssignment tsa WHERE tsa.teacherId = :teacherId " +
           "AND tsa.active = true " +
           "AND tsa.effectiveFrom <= :date " +
           "AND (tsa.effectiveTo IS NULL OR tsa.effectiveTo >= :date)")
    List<TeacherSchoolAssignment> findActiveAssignmentsAt(@Param("teacherId") Long teacherId, 
                                                         @Param("date") LocalDateTime date);
    
    // Assignation principale (priorité 1)
    @Query("SELECT tsa FROM TeacherSchoolAssignment tsa WHERE tsa.teacherId = :teacherId " +
           "AND tsa.priority = 1 AND tsa.active = true " +
           "AND tsa.effectiveFrom <= :date " +
           "AND (tsa.effectiveTo IS NULL OR tsa.effectiveTo >= :date)")
    TeacherSchoolAssignment findPrimaryAssignment(@Param("teacherId") Long teacherId, 
                                                 @Param("date") LocalDateTime date);
    
    // Assignations par type de contrat
    List<TeacherSchoolAssignment> findByTeacherIdAndContractTypeAndActiveTrue(Long teacherId, ContractType contractType);
    
    // Vérifier si un enseignant travaille dans plusieurs écoles
    @Query("SELECT COUNT(DISTINCT tsa.schoolId) FROM TeacherSchoolAssignment tsa " +
           "WHERE tsa.teacherId = :teacherId AND tsa.active = true " +
           "AND tsa.effectiveFrom <= :date " +
           "AND (tsa.effectiveTo IS NULL OR tsa.effectiveTo >= :date)")
    Long countActiveSchools(@Param("teacherId") Long teacherId, @Param("date") LocalDateTime date);
    
    // Enseignants multi-écoles
    @Query("SELECT DISTINCT tsa.teacherId FROM TeacherSchoolAssignment tsa " +
           "WHERE tsa.active = true " +
           "AND tsa.effectiveFrom <= :date " +
           "AND (tsa.effectiveTo IS NULL OR tsa.effectiveTo >= :date) " +
           "GROUP BY tsa.teacherId HAVING COUNT(DISTINCT tsa.schoolId) > 1")
    List<Long> findMultiSchoolTeachers(@Param("date") LocalDateTime date);
    
    // Temps de déplacement entre écoles
    @Query("SELECT tsa1.travelTimeMinutes + tsa2.travelTimeMinutes " +
           "FROM TeacherSchoolAssignment tsa1, TeacherSchoolAssignment tsa2 " +
           "WHERE tsa1.teacherId = :teacherId AND tsa2.teacherId = :teacherId " +
           "AND tsa1.schoolId = :fromSchoolId AND tsa2.schoolId = :toSchoolId " +
           "AND tsa1.active = true AND tsa2.active = true")
    Integer calculateTravelTime(@Param("teacherId") Long teacherId,
                               @Param("fromSchoolId") Long fromSchoolId,
                               @Param("toSchoolId") Long toSchoolId);
    
    // Charge de travail totale par enseignant
    @Query("SELECT SUM(tsa.maxHoursPerWeek) FROM TeacherSchoolAssignment tsa " +
           "WHERE tsa.teacherId = :teacherId AND tsa.active = true " +
           "AND tsa.effectiveFrom <= :date " +
           "AND (tsa.effectiveTo IS NULL OR tsa.effectiveTo >= :date)")
    Integer getTotalWeeklyHours(@Param("teacherId") Long teacherId, @Param("date") LocalDateTime date);
    
    // Additional methods needed by TeacherSchoolAssignmentService
    @Query("SELECT COUNT(DISTINCT tsa.schoolId) FROM TeacherSchoolAssignment tsa " +
           "WHERE tsa.teacherId = :teacherId AND tsa.active = true")
    Long countActiveSchoolsByTeacherId(@Param("teacherId") Long teacherId);
    
    @Query("SELECT tsa FROM TeacherSchoolAssignment tsa WHERE tsa.teacherId = :teacherId AND tsa.isActive = true")
    List<TeacherSchoolAssignment> findByTeacherIdAndIsActiveTrue(@Param("teacherId") Long teacherId);
    
    @Query("SELECT tsa FROM TeacherSchoolAssignment tsa WHERE tsa.schoolId = :schoolId AND tsa.isActive = true")
    List<TeacherSchoolAssignment> findBySchoolIdAndIsActiveTrue(@Param("schoolId") Long schoolId);
    
    @Query("SELECT tsa FROM TeacherSchoolAssignment tsa WHERE tsa.teacherId = :teacherId AND tsa.schoolId = :schoolId AND tsa.isActive = true")
    Optional<TeacherSchoolAssignment> findByTeacherIdAndSchoolIdAndIsActiveTrue(@Param("teacherId") Long teacherId, @Param("schoolId") Long schoolId);
    
    @Query("SELECT tsa FROM TeacherSchoolAssignment tsa JOIN tsa.workingDays wd WHERE tsa.teacherId = :teacherId AND wd = :workingDay AND tsa.isActive = true")
    List<TeacherSchoolAssignment> findByTeacherIdAndWorkingDay(@Param("teacherId") Long teacherId, @Param("workingDay") java.time.DayOfWeek workingDay);
    
    @Query("SELECT tsa FROM TeacherSchoolAssignment tsa WHERE tsa.teacherId = :teacherId AND tsa.isPrimarySchool = true AND tsa.isActive = true")
    Optional<TeacherSchoolAssignment> findPrimarySchoolByTeacherId(@Param("teacherId") Long teacherId);
}