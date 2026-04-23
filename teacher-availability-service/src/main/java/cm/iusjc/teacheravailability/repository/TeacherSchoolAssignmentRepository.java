package cm.iusjc.teacheravailability.repository;

import cm.iusjc.teacheravailability.entity.ContractType;
import cm.iusjc.teacheravailability.entity.TeacherSchoolAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

@Repository
public interface TeacherSchoolAssignmentRepository extends JpaRepository<TeacherSchoolAssignment, Long> {
    
    // Find by teacher
    List<TeacherSchoolAssignment> findByTeacherIdAndIsActiveTrue(Long teacherId);
    
    // Find by school
    List<TeacherSchoolAssignment> findBySchoolIdAndIsActiveTrue(Long schoolId);
    
    // Find specific assignment
    Optional<TeacherSchoolAssignment> findByTeacherIdAndSchoolIdAndIsActiveTrue(Long teacherId, Long schoolId);
    
    // Find by contract type
    List<TeacherSchoolAssignment> findByContractTypeAndIsActiveTrue(ContractType contractType);
    
    // Find primary school assignments
    List<TeacherSchoolAssignment> findByTeacherIdAndIsPrimarySchoolTrueAndIsActiveTrue(Long teacherId);
    
    // Find teachers working on specific day
    @Query("SELECT tsa FROM TeacherSchoolAssignment tsa " +
           "WHERE tsa.schoolId = :schoolId AND tsa.isActive = true " +
           "AND :dayOfWeek MEMBER OF tsa.workingDays")
    List<TeacherSchoolAssignment> findBySchoolIdAndWorkingDay(
            @Param("schoolId") Long schoolId, 
            @Param("dayOfWeek") DayOfWeek dayOfWeek);
    
    // Find multi-school teachers
    @Query("SELECT tsa.teacherId FROM TeacherSchoolAssignment tsa " +
           "WHERE tsa.isActive = true " +
           "GROUP BY tsa.teacherId " +
           "HAVING COUNT(DISTINCT tsa.schoolId) > 1")
    List<Long> findMultiSchoolTeachers();
    
    // Count schools per teacher
    @Query("SELECT COUNT(DISTINCT tsa.schoolId) FROM TeacherSchoolAssignment tsa " +
           "WHERE tsa.teacherId = :teacherId AND tsa.isActive = true")
    Long countSchoolsByTeacherId(@Param("teacherId") Long teacherId);
    
    // Find teachers with travel time
    List<TeacherSchoolAssignment> findByTravelTimeMinutesGreaterThanAndIsActiveTrue(Integer travelTimeMinutes);
    
    // Statistics
    @Query("SELECT tsa.schoolId, COUNT(tsa) FROM TeacherSchoolAssignment tsa " +
           "WHERE tsa.isActive = true " +
           "GROUP BY tsa.schoolId")
    List<Object[]> countTeachersBySchool();
    
    // Delete by teacher
    void deleteByTeacherId(Long teacherId);
    
    // Delete by school
    void deleteBySchoolId(Long schoolId);
}