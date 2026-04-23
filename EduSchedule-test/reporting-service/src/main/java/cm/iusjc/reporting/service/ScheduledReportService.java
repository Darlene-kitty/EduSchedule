package cm.iusjc.reporting.service;

import cm.iusjc.reporting.dto.ReportDTO;
import cm.iusjc.reporting.dto.ReportRequest;
import cm.iusjc.reporting.entity.ReportFormat;
import cm.iusjc.reporting.entity.ReportType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Service de génération automatique de rapports planifiés.
 * - Rapport mensuel : 1er de chaque mois à 06:00
 * - Rapport hebdomadaire d'occupation des salles : chaque lundi à 07:00
 * - Rapport de disponibilité des enseignants : chaque vendredi à 18:00
 * - Nettoyage des rapports expirés : chaque nuit à 02:00
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduledReportService {

    private final ReportService reportService;

    /** ID système utilisé pour les rapports automatiques */
    private static final Long SYSTEM_USER_ID = 1L;

    // ── Rapport mensuel ──────────────────────────────────────────────────────

    /**
     * Génère automatiquement un résumé mensuel le 1er de chaque mois à 06:00.
     * Cron: seconde minute heure jour-du-mois mois jour-de-semaine
     */
    @Scheduled(cron = "0 0 6 1 * *")
    public void generateMonthlyReport() {
        log.info("Scheduled: generating monthly summary report");
        try {
            LocalDate now   = LocalDate.now();
            LocalDate start = now.withDayOfMonth(1).minusMonths(1);
            LocalDate end   = now.withDayOfMonth(1).minusDays(1);

            ReportRequest request = new ReportRequest();
            request.setType(ReportType.MONTHLY_SUMMARY);
            request.setFormat(ReportFormat.PDF);
            request.setTitle("Résumé mensuel — " + start.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
            request.setDescription("Rapport mensuel automatique généré le " + now);
            request.setStartDate(start);
            request.setEndDate(end);

            ReportDTO report = reportService.generateReport(request, SYSTEM_USER_ID);
            log.info("Monthly report generated: id={}, status={}", report.getId(), report.getStatus());
        } catch (Exception e) {
            log.error("Failed to generate monthly report: {}", e.getMessage(), e);
        }
    }

    // ── Rapport hebdomadaire occupation des salles ───────────────────────────

    /**
     * Génère un rapport d'occupation des salles chaque lundi à 07:00.
     */
    @Scheduled(cron = "0 0 7 * * MON")
    public void generateWeeklyRoomOccupancyReport() {
        log.info("Scheduled: generating weekly room occupancy report");
        try {
            LocalDate today = LocalDate.now();
            LocalDate start = today.minusDays(7);

            ReportRequest request = new ReportRequest();
            request.setType(ReportType.ROOM_OCCUPANCY);
            request.setFormat(ReportFormat.PDF);
            request.setTitle("Occupation des salles — semaine du " + start.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            request.setDescription("Rapport hebdomadaire automatique d'occupation des salles");
            request.setStartDate(start);
            request.setEndDate(today);

            ReportDTO report = reportService.generateReport(request, SYSTEM_USER_ID);
            log.info("Weekly room occupancy report generated: id={}, status={}", report.getId(), report.getStatus());
        } catch (Exception e) {
            log.error("Failed to generate weekly room occupancy report: {}", e.getMessage(), e);
        }
    }

    // ── Rapport hebdomadaire disponibilité enseignants ───────────────────────

    /**
     * Génère un rapport de disponibilité des enseignants chaque vendredi à 18:00.
     */
    @Scheduled(cron = "0 0 18 * * FRI")
    public void generateWeeklyTeacherAvailabilityReport() {
        log.info("Scheduled: generating weekly teacher availability report");
        try {
            LocalDate today = LocalDate.now();
            LocalDate start = today.minusDays(4); // Lundi de la semaine courante

            ReportRequest request = new ReportRequest();
            request.setType(ReportType.USER_STATISTICS);
            request.setFormat(ReportFormat.PDF);
            request.setTitle("Disponibilité enseignants — semaine du " + start.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            request.setDescription("Rapport hebdomadaire automatique de disponibilité des enseignants");
            request.setStartDate(start);
            request.setEndDate(today);

            ReportDTO report = reportService.generateReport(request, SYSTEM_USER_ID);
            log.info("Weekly teacher availability report generated: id={}, status={}", report.getId(), report.getStatus());
        } catch (Exception e) {
            log.error("Failed to generate weekly teacher availability report: {}", e.getMessage(), e);
        }
    }

    // ── Rapport annuel ───────────────────────────────────────────────────────

    /**
     * Génère un résumé annuel le 1er janvier à 05:00.
     */
    @Scheduled(cron = "0 0 5 1 1 *")
    public void generateYearlyReport() {
        log.info("Scheduled: generating yearly summary report");
        try {
            LocalDate now   = LocalDate.now();
            LocalDate start = now.withDayOfYear(1).minusYears(1);
            LocalDate end   = now.withDayOfYear(1).minusDays(1);

            ReportRequest request = new ReportRequest();
            request.setType(ReportType.YEARLY_SUMMARY);
            request.setFormat(ReportFormat.PDF);
            request.setTitle("Résumé annuel " + (now.getYear() - 1));
            request.setDescription("Rapport annuel automatique généré le " + now);
            request.setStartDate(start);
            request.setEndDate(end);

            ReportDTO report = reportService.generateReport(request, SYSTEM_USER_ID);
            log.info("Yearly report generated: id={}, status={}", report.getId(), report.getStatus());
        } catch (Exception e) {
            log.error("Failed to generate yearly report: {}", e.getMessage(), e);
        }
    }

    // ── Rapport quotidien utilisation des cours ──────────────────────────────

    /**
     * Génère un rapport d'utilisation des cours chaque jour à 23:30.
     */
    @Scheduled(cron = "0 30 23 * * MON-FRI")
    public void generateDailyCourseUtilizationReport() {
        log.info("Scheduled: generating daily course utilization report");
        try {
            LocalDate today = LocalDate.now();

            ReportRequest request = new ReportRequest();
            request.setType(ReportType.COURSE_UTILIZATION);
            request.setFormat(ReportFormat.CSV);
            request.setTitle("Utilisation des cours — " + today.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            request.setDescription("Rapport quotidien automatique d'utilisation des cours");
            request.setStartDate(today);
            request.setEndDate(today);

            ReportDTO report = reportService.generateReport(request, SYSTEM_USER_ID);
            log.info("Daily course utilization report generated: id={}, status={}", report.getId(), report.getStatus());
        } catch (Exception e) {
            log.error("Failed to generate daily course utilization report: {}", e.getMessage(), e);
        }
    }

    // ── Nettoyage des rapports expirés ───────────────────────────────────────

    /**
     * Nettoie les rapports expirés chaque nuit à 02:00.
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void cleanupExpiredReports() {
        log.info("Scheduled: cleaning up expired reports");
        try {
            reportService.cleanupExpiredReports();
            log.info("Expired reports cleanup completed");
        } catch (Exception e) {
            log.error("Failed to cleanup expired reports: {}", e.getMessage(), e);
        }
    }
}
