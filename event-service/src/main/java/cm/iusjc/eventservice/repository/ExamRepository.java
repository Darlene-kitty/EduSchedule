package cm.iusjc.eventservice.repository;

import cm.iusjc.eventservice.entity.Exam;
import cm.iusjc.eventservice.entity.ExamStatus;
import cm.iusjc.eventservice.entity.ExamType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Long> {
    
    // Recherche par cours
    Page<Exam> findByCourseIdOrderByStartDateTimeDesc(Long courseId, Pageable pageable);
    
    // Recherche par type
    Page<Exam> findByTypeOrderByStartDateTimeDesc(ExamType type, Pageable pageable);
    
    // Recherche par statut
    Page<Exam> findByStatusOrderByStartDateTimeDesc(ExamStatus status, Pageable pageable);
    
    // Recherche par créateur
    Page<Exam> findByCreatedByOrderByStartDateTimeDesc(Long createdBy, Pageable pageable);
    
    // Recherche par enseignant
    Page<Exam> findByTeacherIdOrderByStartDateTimeDesc(Long teacherId, Pageable pageable);
    
    // Recherche par ressource
    Page<Exam> findByResourceIdOrderByStartDateTimeDesc(Long resourceId, Pageable pageable);
    
    // Recherche par plage de dates
    @Query("SELECT e FROM Exam e WHERE e.startDateTime BETWEEN :startDate AND :endDate ORDER BY e.startDateTime")
    List<Exam> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                              @Param("endDate") LocalDateTime endDate);
    
    // Vérification de conflits de ressource
    @Query("SELECT e FROM Exam e WHERE e.resourceId = :resourceId " +
           "AND e.status IN ('SCHEDULED', 'CONFIRMED') " +
           "AND ((e.startDateTime <= :endDateTime AND e.endDateTime >= :startDateTime))")
    List<Exam> findConflictingExams(@Param("resourceId") Long resourceId,
                                   @Param("startDateTime") LocalDateTime startDateTime,
                                   @Param("endDateTime") LocalDateTime endDateTime);
    
    // Examens à venir
    @Query("SELECT e FROM Exam e WHERE e.startDateTime > :now AND e.status IN ('SCHEDULED', 'CONFIRMED') ORDER BY e.startDateTime")
    List<Exam> findUpcomingExams(@Param("now") LocalDateTime now, Pageable pageable);
    
    // Examens du jour
    @Query("SELECT e FROM Exam e WHERE DATE(e.startDateTime) = DATE(:date) ORDER BY e.startDateTime")
    List<Exam> findExamsByDate(@Param("date") LocalDateTime date);
    
    // Examens d'aujourd'hui
    @Query("SELECT e FROM Exam e WHERE DATE(e.startDateTime) = CURRENT_DATE ORDER BY e.startDateTime")
    List<Exam> findTodayExams();
    
    // Examens de la semaine
    @Query("SELECT e FROM Exam e WHERE e.startDateTime BETWEEN :startOfWeek AND :endOfWeek ORDER BY e.startDateTime")
    List<Exam> findWeekExams(@Param("startOfWeek") LocalDateTime startOfWeek, @Param("endOfWeek") LocalDateTime endOfWeek);
    
    // Statistiques par type
    @Query("SELECT e.type, COUNT(e) FROM Exam e GROUP BY e.type")
    List<Object[]> getExamCountByType();
    
    // Examens par période (pour calendrier)
    @Query("SELECT e FROM Exam e WHERE e.startDateTime >= :startOfMonth AND e.startDateTime < :endOfMonth ORDER BY e.startDateTime")
    List<Exam> findExamsByMonth(@Param("startOfMonth") LocalDateTime startOfMonth,
                               @Param("endOfMonth") LocalDateTime endOfMonth);
}