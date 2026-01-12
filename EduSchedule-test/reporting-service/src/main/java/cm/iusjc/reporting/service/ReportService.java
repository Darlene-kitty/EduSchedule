package cm.iusjc.reporting.service;

import cm.iusjc.reporting.dto.ReportDTO;
import cm.iusjc.reporting.dto.ReportRequest;
import cm.iusjc.reporting.dto.StatisticsDTO;
import cm.iusjc.reporting.entity.Report;
import cm.iusjc.reporting.entity.ReportFormat;
import cm.iusjc.reporting.entity.ReportStatus;
import cm.iusjc.reporting.entity.ReportType;
import cm.iusjc.reporting.repository.ReportRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReportService {
    
    private final ReportRepository reportRepository;
    private final StatisticsService statisticsService;
    private final PdfGenerationService pdfGenerationService;
    private final ObjectMapper objectMapper;
    
    @Value("${app.reports.output-directory}")
    private String outputDirectory;
    
    @Value("${app.reports.max-file-age-days}")
    private int maxFileAgeDays;
    
    /**
     * Génère un rapport de manière asynchrone
     */
    @Async
    public CompletableFuture<ReportDTO> generateReportAsync(ReportRequest request, Long userId) {
        log.info("Starting report generation for user {} with type {}", userId, request.getType());
        
        // Créer l'entrée de rapport
        Report report = createReportEntry(request, userId);
        report = reportRepository.save(report);
        
        try {
            // Générer le rapport selon le type
            byte[] reportData = generateReportData(request);
            
            // Sauvegarder le fichier
            String fileName = generateFileName(report);
            Path filePath = saveReportFile(reportData, fileName);
            
            // Mettre à jour le rapport
            report.setStatus(ReportStatus.COMPLETED);
            report.setFilePath(filePath.toString());
            report.setFileSize((long) reportData.length);
            report.setGeneratedAt(LocalDateTime.now());
            
            report = reportRepository.save(report);
            
            log.info("Report generation completed for report ID: {}", report.getId());
            return CompletableFuture.completedFuture(convertToDTO(report));
            
        } catch (Exception e) {
            log.error("Error generating report for user {}: {}", userId, e.getMessage(), e);
            
            // Marquer le rapport comme échoué
            report.setStatus(ReportStatus.FAILED);
            report.setErrorMessage(e.getMessage());
            reportRepository.save(report);
            
            return CompletableFuture.failedFuture(e);
        }
    }
    
    /**
     * Génère un rapport de manière synchrone
     */
    public ReportDTO generateReport(ReportRequest request, Long userId) {
        try {
            return generateReportAsync(request, userId).get();
        } catch (Exception e) {
            log.error("Error in synchronous report generation: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate report", e);
        }
    }
    
    /**
     * Récupère un rapport par ID
     */
    public Optional<ReportDTO> getReportById(Long id) {
        return reportRepository.findById(id)
                .map(this::convertToDTO);
    }
    
    /**
     * Récupère les rapports d'un utilisateur
     */
    public Page<ReportDTO> getReportsByUser(Long userId, Pageable pageable) {
        return reportRepository.findByGeneratedByOrderByCreatedAtDesc(userId, pageable)
                .map(this::convertToDTO);
    }
    
    /**
     * Récupère tous les rapports (admin)
     */
    public Page<ReportDTO> getAllReports(Pageable pageable) {
        return reportRepository.findAll(pageable)
                .map(this::convertToDTO);
    }
    
    /**
     * Supprime un rapport
     */
    public void deleteReport(Long id) {
        Optional<Report> reportOpt = reportRepository.findById(id);
        if (reportOpt.isPresent()) {
            Report report = reportOpt.get();
            
            // Supprimer le fichier physique
            if (report.getFilePath() != null) {
                try {
                    Files.deleteIfExists(Paths.get(report.getFilePath()));
                } catch (IOException e) {
                    log.warn("Failed to delete report file: {}", report.getFilePath(), e);
                }
            }
            
            // Supprimer l'entrée de la base de données
            reportRepository.delete(report);
            log.info("Report {} deleted successfully", id);
        }
    }
    
    /**
     * Nettoie les rapports expirés
     */
    public void cleanupExpiredReports() {
        LocalDateTime expiryDate = LocalDateTime.now().minusDays(maxFileAgeDays);
        List<Report> expiredReports = reportRepository.findExpiredReports(expiryDate);
        
        log.info("Found {} expired reports to cleanup", expiredReports.size());
        
        for (Report report : expiredReports) {
            try {
                deleteReport(report.getId());
            } catch (Exception e) {
                log.error("Error deleting expired report {}: {}", report.getId(), e.getMessage());
            }
        }
    }
    
    /**
     * Génère les statistiques du système
     */
    public StatisticsDTO getSystemStatistics() {
        return statisticsService.generateSystemStatistics();
    }
    
    private Report createReportEntry(ReportRequest request, Long userId) {
        Report report = new Report();
        report.setTitle(request.getTitle() != null ? request.getTitle() : 
                "Rapport " + request.getType().getDisplayName());
        report.setDescription(request.getDescription());
        report.setType(request.getType());
        report.setFormat(request.getFormat());
        report.setStatus(ReportStatus.GENERATING);
        report.setGeneratedBy(userId);
        
        // Sérialiser les paramètres
        try {
            report.setParameters(objectMapper.writeValueAsString(request));
        } catch (Exception e) {
            log.warn("Failed to serialize report parameters: {}", e.getMessage());
            report.setParameters("{}");
        }
        
        return report;
    }
    
    private byte[] generateReportData(ReportRequest request) throws IOException {
        switch (request.getType()) {
            case USER_STATISTICS:
            case COURSE_UTILIZATION:
            case ROOM_OCCUPANCY:
            case RESERVATION_SUMMARY:
            case MONTHLY_SUMMARY:
            case YEARLY_SUMMARY:
                return generateStatisticsReport(request);
            default:
                throw new IllegalArgumentException("Unsupported report type: " + request.getType());
        }
    }
    
    private byte[] generateStatisticsReport(ReportRequest request) throws IOException {
        StatisticsDTO statistics = statisticsService.generateSystemStatistics();
        
        switch (request.getFormat()) {
            case PDF:
                return pdfGenerationService.generateStatisticsPdf(statistics, request.getTitle());
            case JSON:
                return objectMapper.writeValueAsBytes(statistics);
            case CSV:
                return generateCsvReport(statistics);
            default:
                throw new IllegalArgumentException("Unsupported report format: " + request.getFormat());
        }
    }
    
    private byte[] generateCsvReport(StatisticsDTO statistics) {
        StringBuilder csv = new StringBuilder();
        csv.append("Métrique,Valeur\n");
        csv.append("Utilisateurs totaux,").append(statistics.getTotalUsers()).append("\n");
        csv.append("Cours totaux,").append(statistics.getTotalCourses()).append("\n");
        csv.append("Réservations totales,").append(statistics.getTotalReservations()).append("\n");
        csv.append("Ressources totales,").append(statistics.getTotalResources()).append("\n");
        csv.append("Occupation moyenne des salles,").append(statistics.getAverageRoomOccupancy()).append("%\n");
        csv.append("Utilisation moyenne des cours,").append(statistics.getAverageCourseUtilization()).append("%\n");
        
        return csv.toString().getBytes();
    }
    
    private String generateFileName(Report report) {
        String timestamp = LocalDateTime.now().toString().replaceAll("[^0-9]", "");
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return String.format("report_%s_%s_%s%s", 
                report.getType().name().toLowerCase(),
                timestamp,
                uuid,
                report.getFormat().getExtension());
    }
    
    private Path saveReportFile(byte[] data, String fileName) throws IOException {
        // Créer le répertoire de sortie s'il n'existe pas
        Path outputDir = Paths.get(outputDirectory);
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }
        
        Path filePath = outputDir.resolve(fileName);
        Files.write(filePath, data);
        
        log.info("Report file saved: {}", filePath);
        return filePath;
    }
    
    private ReportDTO convertToDTO(Report report) {
        ReportDTO dto = new ReportDTO();
        dto.setId(report.getId());
        dto.setTitle(report.getTitle());
        dto.setDescription(report.getDescription());
        dto.setType(report.getType());
        dto.setFormat(report.getFormat());
        dto.setStatus(report.getStatus());
        dto.setFilePath(report.getFilePath());
        dto.setFileSize(report.getFileSize());
        dto.setGeneratedBy(report.getGeneratedBy());
        dto.setGeneratedAt(report.getGeneratedAt());
        dto.setParameters(report.getParameters());
        dto.setErrorMessage(report.getErrorMessage());
        dto.setCreatedAt(report.getCreatedAt());
        dto.setUpdatedAt(report.getUpdatedAt());
        
        // Générer l'URL de téléchargement
        if (report.getStatus() == ReportStatus.COMPLETED && report.getFilePath() != null) {
            dto.setDownloadUrl("/api/v1/reports/" + report.getId() + "/download");
        }
        
        return dto;
    }
}