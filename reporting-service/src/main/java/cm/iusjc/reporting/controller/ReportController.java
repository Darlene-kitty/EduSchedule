package cm.iusjc.reporting.controller;

import cm.iusjc.reporting.dto.ReportDTO;
import cm.iusjc.reporting.dto.ReportRequest;
import cm.iusjc.reporting.dto.ScheduledReportConfigDTO;
import cm.iusjc.reporting.dto.StatisticsDTO;
import cm.iusjc.reporting.entity.ReportFormat;
import cm.iusjc.reporting.entity.ReportType;
import cm.iusjc.reporting.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Slf4j
public class ReportController {
    
    private final ReportService reportService;
    
    /**
     * Génère un nouveau rapport
     */
    @PostMapping("/generate")
    public ResponseEntity<ReportDTO> generateReport(
            @Valid @RequestBody ReportRequest request,
            @RequestHeader(value = "X-User-ID", defaultValue = "1") Long userId) {
        
        log.info("Generating report of type {} for user {}", request.getType(), userId);
        
        try {
            ReportDTO report = reportService.generateReport(request, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(report);
        } catch (Exception e) {
            log.error("Error generating report: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Génère un rapport de manière asynchrone
     */
    @PostMapping("/generate-async")
    public ResponseEntity<ReportDTO> generateReportAsync(
            @Valid @RequestBody ReportRequest request,
            @RequestHeader(value = "X-User-ID", defaultValue = "1") Long userId) {
        
        log.info("Starting async report generation of type {} for user {}", request.getType(), userId);
        
        try {
            // Démarrer la génération asynchrone
            reportService.generateReportAsync(request, userId);
            
            // Retourner immédiatement avec le statut ACCEPTED
            ReportDTO response = new ReportDTO();
            response.setTitle(request.getTitle());
            response.setType(request.getType());
            response.setFormat(request.getFormat());
            response.setGeneratedBy(userId);
            
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
        } catch (Exception e) {
            log.error("Error starting async report generation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Récupère un rapport par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReportDTO> getReport(@PathVariable Long id) {
        Optional<ReportDTO> report = reportService.getReportById(id);
        return report.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Récupère les rapports d'un utilisateur
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<ReportDTO>> getUserReports(
            @PathVariable Long userId,
            Pageable pageable) {
        
        Page<ReportDTO> reports = reportService.getReportsByUser(userId, pageable);
        return ResponseEntity.ok(reports);
    }
    
    /**
     * Récupère tous les rapports (admin)
     */
    @GetMapping
    public ResponseEntity<Page<ReportDTO>> getAllReports(Pageable pageable) {
        Page<ReportDTO> reports = reportService.getAllReports(pageable);
        return ResponseEntity.ok(reports);
    }
    
    /**
     * Télécharge un rapport
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadReport(@PathVariable Long id) {
        Optional<ReportDTO> reportOpt = reportService.getReportById(id);
        
        if (reportOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        ReportDTO report = reportOpt.get();
        
        if (report.getFilePath() == null) {
            return ResponseEntity.notFound().build();
        }
        
        try {
            File file = new File(report.getFilePath());
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }
            
            Resource resource = new FileSystemResource(file);
            
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=\"" + file.getName() + "\"");
            headers.add(HttpHeaders.CONTENT_TYPE, report.getFormat().getMimeType());
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(file.length())
                    .body(resource);
                    
        } catch (Exception e) {
            log.error("Error downloading report {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Supprime un rapport
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReport(@PathVariable Long id) {
        try {
            reportService.deleteReport(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting report {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Récupère les statistiques du système
     */
    @GetMapping("/statistics")
    public ResponseEntity<StatisticsDTO> getSystemStatistics() {
        try {
            StatisticsDTO statistics = reportService.getSystemStatistics();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            log.error("Error getting system statistics: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Nettoie les rapports expirés
     */
    @PostMapping("/cleanup")
    public ResponseEntity<String> cleanupExpiredReports() {
        try {
            reportService.cleanupExpiredReports();
            return ResponseEntity.ok("Cleanup completed successfully");
        } catch (Exception e) {
            log.error("Error during cleanup: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Cleanup failed: " + e.getMessage());
        }
    }
    
    /**
     * Endpoint de test
     */
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Reporting Service is running!");
    }
    
    /**
     * Liste les rapports planifiés configurés
     */
    @GetMapping("/scheduled")
    public ResponseEntity<List<ScheduledReportConfigDTO>> getScheduledReports() {
        List<ScheduledReportConfigDTO> configs = List.of(
                new ScheduledReportConfigDTO("Résumé mensuel", "0 0 6 1 * *", 
                        "Généré le 1er de chaque mois à 06:00", ReportType.MONTHLY_SUMMARY, ReportFormat.PDF, true, "Prochain: 1er du mois à 06:00"),
                new ScheduledReportConfigDTO("Occupation salles hebdomadaire", "0 0 7 * * MON", 
                        "Généré chaque lundi à 07:00", ReportType.ROOM_OCCUPANCY, ReportFormat.PDF, true, "Prochain: Lundi à 07:00"),
                new ScheduledReportConfigDTO("Disponibilité enseignants", "0 0 18 * * FRI", 
                        "Généré chaque vendredi à 18:00", ReportType.USER_STATISTICS, ReportFormat.PDF, true, "Prochain: Vendredi à 18:00"),
                new ScheduledReportConfigDTO("Résumé annuel", "0 0 5 1 1 *", 
                        "Généré le 1er janvier à 05:00", ReportType.YEARLY_SUMMARY, ReportFormat.PDF, true, "Prochain: 1er janvier à 05:00"),
                new ScheduledReportConfigDTO("Utilisation cours quotidienne", "0 30 23 * * MON-FRI", 
                        "Généré chaque jour ouvré à 23:30", ReportType.COURSE_UTILIZATION, ReportFormat.CSV, true, "Prochain: Aujourd'hui à 23:30")
        );
        return ResponseEntity.ok(configs);
    }
}