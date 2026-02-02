package cm.iusjc.teacheravailabilityservice.repository;

import cm.iusjc.teacheravailabilityservice.entity.TeacherAvailability;
import cm.iusjc.teacheravailabilityservice.entity.TeacherAvailability.AvailabilityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TeacherAvailabilityRepository extends JpaRepository<TeacherAvailability, Long> {
    
    // Recherche par enseignant
    List<TeacherAvailability> findByTeacherId(Long teacherId);
    
    List<TeacherAvailability> findByTeacherIdAndStatus(Long teacherId, AvailabilityStatus status);
    
    // Recherche par date
    @Query("SELECT ta FROM TeacherAvailability ta WHERE ta.teacherId = :teacherId " +
           "AND ta.status = 'ACTIVE' " +
           "AND ta.effectiveDate <= :date " +
           "AND (ta.endDate IS NULL OR ta.endDate >= :date)")
    Optional<TeacherAvailability> findActiveAvailabilityForTeacherOnDate(
            @Param("teacherId") Long teacherId, 
            @Param("date") LocalDate date);
    
    // Recherche par période
    @Query("SELECT ta FROM TeacherAvailability ta WHERE ta.teacherId = :teacherId " +
           "AND ta.status = 'ACTIVE' " +
           "AND ta.effectiveDate <= :endDate " +
           "AND (ta.endDate IS NULL OR ta.endDate >= :startDate)")
    List<TeacherAvailability> findActiveAvailabilitiesForTeacherInPeriod(
            @Param("teacherId") Long teacherId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    // Recherche par statut
    List<TeacherAvailability> findByStatus(AvailabilityStatus status);
    
    @Query("SELECT ta FROM TeacherAvailability ta WHERE ta.status = :status " +
           "AND ta.effectiveDate <= :date " +
           "AND (ta.endDate IS NULL OR ta.endDate >= :date)")
    List<TeacherAvailability> findByStatusAndActiveOnDate(
            @Param("status") AvailabilityStatus status,
            @Param("date") LocalDate date);
    
    // Recherche par créateur
    List<TeacherAvailability> findByCreatedBy(Long createdBy);
    
    // Recherche avec jointure sur les créneaux
    @Query("SELECT DISTINCT ta FROM TeacherAvailability ta " +
           "LEFT JOIN FETCH ta.availableSlots " +
           "WHERE ta.teacherId = :teacherId AND ta.status = 'ACTIVE'")
    List<TeacherAvailability> findActiveAvailabilitiesWithSlotsForTeacher(@Param("teacherId") Long teacherId);
    
    // Statistiques
    @Query("SELECT COUNT(ta) FROM TeacherAvailability ta WHERE ta.status = 'ACTIVE'")
    long countActiveAvailabilities();
    
    @Query("SELECT COUNT(DISTINCT ta.teacherId) FROM TeacherAvailability ta WHERE ta.status = 'ACTIVE'")
    long countTeachersWithActiveAvailabilities();
    
    // Recherche par nom d'enseignant (cache)
    List<TeacherAvailability> findByTeacherNameContainingIgnoreCase(String teacherName);
    
    // Vérification d'existence
    boolean existsByTeacherIdAndEffectiveDateAndStatus(Long teacherId, LocalDate effectiveDate, AvailabilityStatus status);
    
    // Suppression par enseignant
    void deleteByTeacherIdAndStatus(Long teacherId, AvailabilityStatus status);
    
    // Recherche des disponibilités expirées
    @Query("SELECT ta FROM TeacherAvailability ta WHERE ta.endDate < :date AND ta.status = 'ACTIVE'")
    List<TeacherAvailability> findExpiredAvailabilities(@Param("date") LocalDate date);
    
    // Recherche des disponibilités futures
    @Query("SELECT ta FROM TeacherAvailability ta WHERE ta.effectiveDate > :date")
    List<TeacherAvailability> findFutureAvailabilities(@Param("date") LocalDate date);
}