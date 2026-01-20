package cm.iusjc.eventservice.repository;

import cm.iusjc.eventservice.entity.Supervisor;
import cm.iusjc.eventservice.entity.SupervisorRole;
import cm.iusjc.eventservice.entity.SupervisorStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SupervisorRepository extends JpaRepository<Supervisor, Long> {
    
    // Recherche par examen
    List<Supervisor> findByExamIdOrderByRole(Long examId);
    
    // Recherche par utilisateur
    List<Supervisor> findByUserIdOrderByStartTime(Long userId);
    
    // Recherche par rôle
    List<Supervisor> findByRoleOrderByStartTime(SupervisorRole role);
    
    // Recherche par statut
    List<Supervisor> findByStatusOrderByStartTime(SupervisorStatus status);
    
    // Surveillants d'un examen par rôle
    List<Supervisor> findByExamIdAndRole(Long examId, SupervisorRole role);
    
    // Vérification de disponibilité d'un surveillant
    @Query("SELECT s FROM Supervisor s WHERE s.userId = :userId " +
           "AND s.status IN ('ASSIGNED', 'CONFIRMED', 'PRESENT') " +
           "AND ((s.startTime <= :endTime AND s.endTime >= :startTime))")
    List<Supervisor> findConflictingSupervisors(@Param("userId") Long userId,
                                               @Param("startTime") LocalDateTime startTime,
                                               @Param("endTime") LocalDateTime endTime);
    
    // Surveillants d'aujourd'hui
    @Query("SELECT s FROM Supervisor s WHERE DATE(s.startTime) = CURRENT_DATE " +
           "AND s.status IN ('ASSIGNED', 'CONFIRMED') ORDER BY s.startTime")
    List<Supervisor> findTodaySupervisors();
    
    // Statistiques par rôle
    @Query("SELECT s.role, COUNT(s) FROM Supervisor s GROUP BY s.role")
    List<Object[]> getSupervisorCountByRole();
}