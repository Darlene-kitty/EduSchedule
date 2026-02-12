package cm.iusjc.reportingservice.service;

import cm.iusjc.reportingservice.dto.*;
import cm.iusjc.reportingservice.entity.CustomReport;
import cm.iusjc.reportingservice.entity.ReportTemplate;
import cm.iusjc.reportingservice.repository.CustomReportRepository;
import cm.iusjc.reportingservice.repository.ReportTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CustomReportService {

    @Autowired
    private CustomReportRepository customReportRepository;

    @Autowired
    private ReportTemplateRepository templateRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ReportDataService reportDataService;

    @Autowired
    private ReportExportService exportService;

    public CustomReportResponse createCustomReport(CustomReportRequest request) {
        try {
            // Valider la configuration
            validateReportConfig(request);

            // Créer le rapport
            CustomReport report = new CustomReport();
            report.setTitle(request.getTitle());
            report.setType(request.getType());
            report.setConfig(request);
            report.setCreatedAt(LocalDateTime.now());
            report.setStatus("CREATED");

            CustomReport savedReport = customReportRepository.save(report);

            return CustomReportResponse.builder()
                    .success(true)
                    .reportId(savedReport.getId())
                    .message("Rapport personnalisé créé avec succès")
                    .build();

        } catch (Exception e) {
            return CustomReportResponse.builder()
                    .success(false)
                    .message("Erreur lors de la création du rapport: " + e.getMessage())
                    .build();
        }
    }

    public ReportGenerationResponse generateReport(Long reportId) {
        try {
            CustomReport report = customReportRepository.findById(reportId)
                    .orElseThrow(() -> new RuntimeException("Rapport non trouvé"));

            // Collecter les données selon la configuration
            ReportData data = collectReportData(report.getConfig());

            // Générer le contenu du rapport
            ReportContent content = generateReportContent(data, report.getConfig());

            // Sauvegarder le résultat
            report.setGeneratedAt(LocalDateTime.now());
            report.setStatus("GENERATED");
            report.setContent(content);
            customReportRepository.save(report);

            return ReportGenerationResponse.builder()
                    .success(true)
                    .reportId(reportId)
                    .content(content)
                    .generatedAt(LocalDateTime.now())
                    .message("Rapport généré avec succès")
                    .build();

        } catch (Exception e) {
            return ReportGenerationResponse.builder()
                    .success(false)
                    .message("Erreur lors de la génération: " + e.getMessage())
                    .build();
        }
    }

    public List<ReportTemplate> getReportTemplates() {
        return templateRepository.findAll();
    }

    public ReportExportResponse exportReport(Long reportId, String format) {
        try {
            CustomReport report = customReportRepository.findById(reportId)
                    .orElseThrow(() -> new RuntimeException("Rapport non trouvé"));

            if (report.getContent() == null) {
                throw new RuntimeException("Rapport non généré");
            }

            // Exporter selon le format demandé
            byte[] exportData = exportService.exportReport(report.getContent(), format);
            String filename = generateFilename(report, format);

            return ReportExportResponse.builder()
                    .success(true)
                    .filename(filename)
                    .data(exportData)
                    .format(format)
                    .size(exportData.length)
                    .build();

        } catch (Exception e) {
            return ReportExportResponse.builder()
                    .success(false)
                    .message("Erreur lors de l'export: " + e.getMessage())
                    .build();
        }
    }

    private ReportData collectReportData(CustomReportRequest config) {
        ReportData data = new ReportData();

        switch (config.getType()) {
            case OCCUPANCY:
                data = reportDataService.collectOccupancyData(config);
                break;
            case PERFORMANCE:
                data = reportDataService.collectPerformanceData(config);
                break;
            case EQUIPMENT:
                data = reportDataService.collectEquipmentData(config);
                break;
            case CONFLICTS:
                data = reportDataService.collectConflictsData(config);
                break;
            case TRENDS:
                data = reportDataService.collectTrendsData(config);
                break;
        }

        return data;
    }

    private ReportContent generateReportContent(ReportData data, CustomReportRequest config) {
        ReportContent content = new ReportContent();
        content.setTitle(config.getTitle());
        content.setGeneratedAt(LocalDateTime.now());
        content.setPeriod(config.getDateRange());

        // Générer les sections selon la configuration
        List<ReportSection> sections = new ArrayList<>();

        // Section résumé
        sections.add(generateSummarySection(data));

        // Sections de données
        if (config.getVisualizations().contains("TABLE")) {
            sections.add(generateTableSection(data, config));
        }

        if (config.getVisualizations().contains("CHART")) {
            sections.add(generateChartSection(data, config));
        }

        if (config.getVisualizations().contains("GRAPH")) {
            sections.add(generateGraphSection(data, config));
        }

        if (config.getVisualizations().contains("HEATMAP")) {
            sections.add(generateHeatmapSection(data, config));
        }

        content.setSections(sections);
        return content;
    }

    private ReportSection generateSummarySection(ReportData data) {
        ReportSection section = new ReportSection();
        section.setTitle("Résumé Exécutif");
        section.setType("SUMMARY");

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalRecords", data.getTotalRecords());
        summary.put("averageValue", data.getAverageValue());
        summary.put("maxValue", data.getMaxValue());
        summary.put("minValue", data.getMinValue());
        summary.put("trend", data.getTrend());

        section.setData(summary);
        return section;
    }

    private ReportSection generateTableSection(ReportData data, CustomReportRequest config) {
        ReportSection section = new ReportSection();
        section.setTitle("Données Détaillées");
        section.setType("TABLE");

        // Grouper les données selon la configuration
        Map<String, List<Map<String, Object>>> groupedData = groupDataBy(data, config.getGroupBy());
        section.setData(groupedData);

        return section;
    }

    private ReportSection generateChartSection(ReportData data, CustomReportRequest config) {
        ReportSection section = new ReportSection();
        section.setTitle("Graphiques");
        section.setType("CHART");

        // Préparer les données pour les graphiques
        Map<String, Object> chartData = new HashMap<>();
        chartData.put("labels", data.getLabels());
        chartData.put("datasets", data.getDatasets());
        chartData.put("type", determineChartType(config.getType()));

        section.setData(chartData);
        return section;
    }

    private ReportSection generateGraphSection(ReportData data, CustomReportRequest config) {
        ReportSection section = new ReportSection();
        section.setTitle("Graphiques Avancés");
        section.setType("GRAPH");

        Map<String, Object> graphData = new HashMap<>();
        graphData.put("nodes", data.getNodes());
        graphData.put("edges", data.getEdges());
        graphData.put("layout", "force-directed");

        section.setData(graphData);
        return section;
    }

    private ReportSection generateHeatmapSection(ReportData data, CustomReportRequest config) {
        ReportSection section = new ReportSection();
        section.setTitle("Carte de Chaleur");
        section.setType("HEATMAP");

        Map<String, Object> heatmapData = new HashMap<>();
        heatmapData.put("matrix", data.getMatrix());
        heatmapData.put("xLabels", data.getXLabels());
        heatmapData.put("yLabels", data.getYLabels());
        heatmapData.put("colorScale", "viridis");

        section.setData(heatmapData);
        return section;
    }

    private Map<String, List<Map<String, Object>>> groupDataBy(ReportData data, String groupBy) {
        return data.getRecords().stream()
                .collect(Collectors.groupingBy(record -> 
                    record.getOrDefault(groupBy, "Unknown").toString()));
    }

    private String determineChartType(String reportType) {
        switch (reportType) {
            case "OCCUPANCY": return "bar";
            case "PERFORMANCE": return "line";
            case "EQUIPMENT": return "pie";
            case "CONFLICTS": return "scatter";
            case "TRENDS": return "area";
            default: return "bar";
        }
    }

    private void validateReportConfig(CustomReportRequest config) {
        if (config.getTitle() == null || config.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Le titre du rapport est requis");
        }

        if (config.getType() == null) {
            throw new IllegalArgumentException("Le type de rapport est requis");
        }

        if (config.getDateRange() == null) {
            throw new IllegalArgumentException("La période est requise");
        }

        if (config.getVisualizations() == null || config.getVisualizations().isEmpty()) {
            throw new IllegalArgumentException("Au moins une visualisation est requise");
        }
    }

    private String generateFilename(CustomReport report, String format) {
        String timestamp = LocalDateTime.now().toString().replaceAll("[^0-9]", "");
        return String.format("%s_%s.%s", 
                report.getTitle().replaceAll("[^a-zA-Z0-9]", "_"),
                timestamp,
                format.toLowerCase());
    }
}