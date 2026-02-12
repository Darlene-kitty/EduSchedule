package cm.iusjc.reservation.controller;

import cm.iusjc.reservation.service.PredictiveAnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/predictive-analytics")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class PredictiveAnalyticsController {
    
    private final PredictiveAnalyticsService predictiveAnalyticsService;
    
    /**
     * Prédit la demande future en salles
     */
    @GetMapping("/predict-demand")
    public ResponseEntity<Map<String, Object>> predictFutureDemand(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startPeriod,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endPeriod) {
        
        log.info("Predicting future demand from {} to {}", startPeriod, endPeriod);
        
        try {
            Map<String, Object> prediction = predictiveAnalyticsService.predictFutureDemand(startPeriod, endPeriod);
            return ResponseEntity.ok(prediction);
            
        } catch (Exception e) {
            log.error("Error predicting future demand", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Prédit la demande pour la semaine prochaine
     */
    @GetMapping("/predict-next-week")
    public ResponseEntity<Map<String, Object>> predictNextWeekDemand() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextWeekStart = now.plusWeeks(1).with(java.time.DayOfWeek.MONDAY).withHour(8).withMinute(0);
        LocalDateTime nextWeekEnd = nextWeekStart.plusDays(4).withHour(18).withMinute(0); // Lun-Ven
        
        return predictFutureDemand(nextWeekStart, nextWeekEnd);
    }
    
    /**
     * Prédit la demande pour le mois prochain
     */
    @GetMapping("/predict-next-month")
    public ResponseEntity<Map<String, Object>> predictNextMonthDemand() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextMonthStart = now.plusMonths(1).withDayOfMonth(1).withHour(8).withMinute(0);
        LocalDateTime nextMonthEnd = nextMonthStart.plusMonths(1).minusDays(1).withHour(18).withMinute(0);
        
        return predictFutureDemand(nextMonthStart, nextMonthEnd);
    }
    
    /**
     * Identifie les salles à risque de surcharge
     */
    @GetMapping("/overload-risks")
    public ResponseEntity<List<Map<String, Object>>> identifyOverloadRisks(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startPeriod,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endPeriod) {
        
        log.info("Identifying overload risks from {} to {}", startPeriod, endPeriod);
        
        try {
            List<Map<String, Object>> risks = predictiveAnalyticsService.identifyOverloadRisks(startPeriod, endPeriod);
            return ResponseEntity.ok(risks);
            
        } catch (Exception e) {
            log.error("Error identifying overload risks", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Identifie les risques pour la semaine prochaine
     */
    @GetMapping("/overload-risks-next-week")
    public ResponseEntity<List<Map<String, Object>>> identifyNextWeekRisks() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextWeekStart = now.plusWeeks(1).with(java.time.DayOfWeek.MONDAY).withHour(8).withMinute(0);
        LocalDateTime nextWeekEnd = nextWeekStart.plusDays(4).withHour(18).withMinute(0);
        
        return identifyOverloadRisks(nextWeekStart, nextWeekEnd);
    }
    
    /**
     * Suggère des optimisations proactives
     */
    @GetMapping("/proactive-optimizations")
    public ResponseEntity<Map<String, Object>> suggestProactiveOptimizations() {
        log.info("Generating proactive optimization suggestions");
        
        try {
            Map<String, Object> optimizations = predictiveAnalyticsService.suggestProactiveOptimizations();
            return ResponseEntity.ok(optimizations);
            
        } catch (Exception e) {
            log.error("Error generating proactive optimizations", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Calcule les métriques de performance prédictives
     */
    @GetMapping("/predictive-metrics")
    public ResponseEntity<Map<String, Object>> calculatePredictiveMetrics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime targetDate) {
        
        log.info("Calculating predictive metrics for {}", targetDate);
        
        try {
            Map<String, Object> metrics = predictiveAnalyticsService.calculatePredictiveMetrics(targetDate);
            return ResponseEntity.ok(metrics);
            
        } catch (Exception e) {
            log.error("Error calculating predictive metrics", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Métriques prédictives pour demain
     */
    @GetMapping("/predictive-metrics-tomorrow")
    public ResponseEntity<Map<String, Object>> calculateTomorrowMetrics() {
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1).withHour(12).withMinute(0);
        return calculatePredictiveMetrics(tomorrow);
    }
    
    /**
     * Métriques prédictives pour la semaine prochaine
     */
    @GetMapping("/predictive-metrics-next-week")
    public ResponseEntity<Map<String, Object>> calculateNextWeekMetrics() {
        LocalDateTime nextWeek = LocalDateTime.now().plusWeeks(1).with(java.time.DayOfWeek.WEDNESDAY).withHour(12).withMinute(0);
        return calculatePredictiveMetrics(nextWeek);
    }
    
    /**
     * Analyse des tendances d'utilisation
     */
    @GetMapping("/usage-trends")
    public ResponseEntity<Map<String, Object>> analyzeUsageTrends(
            @RequestParam(required = false, defaultValue = "30") Integer days) {
        
        log.info("Analyzing usage trends for {} days", days);
        
        try {
            LocalDateTime endPeriod = LocalDateTime.now();
            LocalDateTime startPeriod = endPeriod.minusDays(days);
            
            Map<String, Object> trends = predictiveAnalyticsService.predictFutureDemand(startPeriod, endPeriod);
            
            // Ajouter des métadonnées sur l'analyse
            trends.put("analysisType", "HISTORICAL_TRENDS");
            trends.put("analysisPeriodDays", days);
            trends.put("generatedAt", LocalDateTime.now());
            
            return ResponseEntity.ok(trends);
            
        } catch (Exception e) {
            log.error("Error analyzing usage trends", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Rapport de prédiction complet
     */
    @GetMapping("/comprehensive-report")
    public ResponseEntity<Map<String, Object>> generateComprehensiveReport(
            @RequestParam(required = false, defaultValue = "7") Integer forecastDays) {
        
        log.info("Generating comprehensive predictive report for {} days", forecastDays);
        
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime forecastEnd = now.plusDays(forecastDays);
            
            Map<String, Object> report = new java.util.HashMap<>();
            
            // Prédiction de la demande
            Map<String, Object> demandPrediction = predictiveAnalyticsService.predictFutureDemand(now, forecastEnd);
            
            // Risques de surcharge
            List<Map<String, Object>> overloadRisks = predictiveAnalyticsService.identifyOverloadRisks(now, forecastEnd);
            
            // Optimisations proactives
            Map<String, Object> optimizations = predictiveAnalyticsService.suggestProactiveOptimizations();
            
            // Métriques prédictives moyennes
            Map<String, Object> avgMetrics = predictiveAnalyticsService.calculatePredictiveMetrics(
                now.plusDays(forecastDays / 2)
            );
            
            // Assembler le rapport
            report.put("demandPrediction", demandPrediction);
            report.put("overloadRisks", overloadRisks);
            report.put("proactiveOptimizations", optimizations);
            report.put("averageMetrics", avgMetrics);
            report.put("reportPeriod", Map.of("start", now, "end", forecastEnd, "days", forecastDays));
            report.put("generatedAt", now);
            report.put("reportType", "COMPREHENSIVE_PREDICTIVE_ANALYSIS");
            
            // Résumé exécutif
            report.put("executiveSummary", generateExecutiveSummary(
                demandPrediction, overloadRisks, optimizations, avgMetrics
            ));
            
            return ResponseEntity.ok(report);
            
        } catch (Exception e) {
            log.error("Error generating comprehensive report", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Alertes prédictives
     */
    @GetMapping("/predictive-alerts")
    public ResponseEntity<List<Map<String, Object>>> getPredictiveAlerts(
            @RequestParam(required = false, defaultValue = "3") Integer daysAhead) {
        
        log.info("Getting predictive alerts for {} days ahead", daysAhead);
        
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime alertPeriod = now.plusDays(daysAhead);
            
            List<Map<String, Object>> alerts = new java.util.ArrayList<>();
            
            // Alertes de surcharge
            List<Map<String, Object>> overloadRisks = predictiveAnalyticsService.identifyOverloadRisks(now, alertPeriod);
            for (Map<String, Object> risk : overloadRisks) {
                if ("HIGH".equals(risk.get("riskLevel")) || "CRITICAL".equals(risk.get("riskLevel"))) {
                    Map<String, Object> alert = new java.util.HashMap<>();
                    alert.put("type", "OVERLOAD_RISK");
                    alert.put("severity", risk.get("riskLevel"));
                    alert.put("message", "Risque de surcharge pour la salle " + risk.get("roomName"));
                    alert.put("roomId", risk.get("roomId"));
                    alert.put("roomName", risk.get("roomName"));
                    alert.put("recommendations", risk.get("recommendations"));
                    alert.put("alertDate", now);
                    alert.put("targetDate", alertPeriod);
                    alerts.add(alert);
                }
            }
            
            // Alertes de performance
            Map<String, Object> metrics = predictiveAnalyticsService.calculatePredictiveMetrics(alertPeriod);
            Double performanceScore = (Double) metrics.get("predictedPerformanceScore");
            if (performanceScore != null && performanceScore < 70) {
                Map<String, Object> alert = new java.util.HashMap<>();
                alert.put("type", "PERFORMANCE_DEGRADATION");
                alert.put("severity", performanceScore < 50 ? "HIGH" : "MEDIUM");
                alert.put("message", "Performance prédite en baisse: " + String.format("%.1f", performanceScore) + "%");
                alert.put("predictedScore", performanceScore);
                alert.put("recommendations", List.of(
                    "Réviser la planification des réservations",
                    "Optimiser l'utilisation des salles",
                    "Prévoir des ressources supplémentaires"
                ));
                alert.put("alertDate", now);
                alert.put("targetDate", alertPeriod);
                alerts.add(alert);
            }
            
            // Trier par sévérité
            alerts.sort((a, b) -> {
                String severityA = (String) a.get("severity");
                String severityB = (String) b.get("severity");
                return getSeverityOrder(severityB) - getSeverityOrder(severityA);
            });
            
            return ResponseEntity.ok(alerts);
            
        } catch (Exception e) {
            log.error("Error getting predictive alerts", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    private Map<String, Object> generateExecutiveSummary(Map<String, Object> demandPrediction,
                                                        List<Map<String, Object>> overloadRisks,
                                                        Map<String, Object> optimizations,
                                                        Map<String, Object> avgMetrics) {
        Map<String, Object> summary = new java.util.HashMap<>();
        
        // Résumé de la demande
        @SuppressWarnings("unchecked")
        List<String> demandRecommendations = (List<String>) demandPrediction.get("recommendations");
        summary.put("demandOutlook", demandRecommendations != null && !demandRecommendations.isEmpty() ? 
            "Demande élevée prévue" : "Demande normale prévue");
        
        // Résumé des risques
        long highRisks = overloadRisks.stream()
            .mapToLong(risk -> "HIGH".equals(risk.get("riskLevel")) || "CRITICAL".equals(risk.get("riskLevel")) ? 1 : 0)
            .sum();
        summary.put("riskLevel", highRisks > 0 ? "HIGH" : overloadRisks.size() > 0 ? "MEDIUM" : "LOW");
        summary.put("risksCount", overloadRisks.size());
        
        // Résumé des optimisations
        String priority = (String) optimizations.get("priority");
        summary.put("optimizationPriority", priority);
        
        // Score de performance prédit
        Double performanceScore = (Double) avgMetrics.get("predictedPerformanceScore");
        summary.put("predictedPerformance", performanceScore != null ? 
            (performanceScore >= 80 ? "EXCELLENT" : performanceScore >= 70 ? "GOOD" : performanceScore >= 60 ? "FAIR" : "POOR") : 
            "UNKNOWN");
        
        // Recommandations principales
        List<String> keyRecommendations = new java.util.ArrayList<>();
        if (highRisks > 0) {
            keyRecommendations.add("Action immédiate requise pour " + highRisks + " salle(s) à risque");
        }
        if ("HIGH".equals(priority)) {
            keyRecommendations.add("Optimisations urgentes recommandées");
        }
        if (performanceScore != null && performanceScore < 70) {
            keyRecommendations.add("Amélioration de la performance nécessaire");
        }
        if (keyRecommendations.isEmpty()) {
            keyRecommendations.add("Situation stable, surveillance continue recommandée");
        }
        
        summary.put("keyRecommendations", keyRecommendations);
        summary.put("overallStatus", highRisks > 0 ? "ATTENTION_REQUIRED" : "STABLE");
        
        return summary;
    }
    
    private int getSeverityOrder(String severity) {
        return switch (severity != null ? severity.toUpperCase() : "LOW") {
            case "CRITICAL" -> 4;
            case "HIGH" -> 3;
            case "MEDIUM" -> 2;
            case "LOW" -> 1;
            default -> 0;
        };
    }
}