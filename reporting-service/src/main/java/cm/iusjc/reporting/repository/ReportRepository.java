package cm.iusjc.reporting.repository;

import cm.iusjc.reporting.entity.Report;
import cm.iusjc.reporting.entity.ReportStatus;
import cm.iusjc.reporting.entity.ReportType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    
    // Recherche par utilisateur
    Page<Report> findByGeneratedByOrderByCreatedAtDesc(Long generatedBy, Pageable pageable);
    
    // Recherche par type
    Page<Report> findByTypeOrderByCreatedAtDesc(ReportType type, Pageable pageable);
    
    // Recherche par statut
    Page<Report> findByStatusOrderByCreatedAtDesc(ReportStatus status, Pageable pageable);
    
    // Recherche par utilisateur et statut
    Page<Report> findByGeneratedByAndStatusOrderByCreatedAtDesc(Long generatedBy, ReportStatus status, Pageable pageable);
    
    // Recherche par plage de dates
    @Query("SELECT r FROM Report r WHERE r.createdAt BETWEEN :startDate AND :endDate ORDER BY r.createdAt DESC")
    Page<Report> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                @Param("endDate") LocalDateTime endDate, 
                                Pageable pageable);
    
    // Rapports expirés
    @Query("SELECT r FROM Report r WHERE r.status = 'COMPLETED' AND r.generatedAt < :expiryDate")
    List<Report> findExpiredReports(@Param("expiryDate") LocalDateTime expiryDate);
    
    // Statistiques des rapports
    @Query("SELECT r.type, COUNT(r) FROM Report r GROUP BY r.type")
    List<Object[]> getReportCountByType();
    
    @Query("SELECT r.status, COUNT(r) FROM Report r GROUP BY r.status")
    List<Object[]> getReportCountByStatus();
    
    // Rapports récents par utilisateur
    @Query("SELECT r FROM Report r WHERE r.generatedBy = :userId AND r.status = 'COMPLETED' ORDER BY r.generatedAt DESC")
    List<Report> findRecentCompletedReportsByUser(@Param("userId") Long userId, Pageable pageable);
}