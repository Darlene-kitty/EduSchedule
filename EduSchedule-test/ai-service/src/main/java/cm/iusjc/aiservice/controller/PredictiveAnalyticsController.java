package cm.iusjc.aiservice.controller;

import cm.iusjc.aiservice.dto.*;
import cm.iusjc.aiservice.service.PredictiveAnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/predictive-analytics")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class PredictiveAnalyticsController {
    
    private final PredictiveAnalyticsService analyticsService;
    
    /**
     * Prédiction de demande générale
     */
    @PostMapping("/predict-demand")
    public ResponseEntity<Map<String, Object>> predictDemand(
            @Valid @RequestBody PredictionRequestDTO request) {
        try {
            log.info("Predicting demand for period: {} to {}", request.getStartDate(), request.getEndDate());
            
            OccupancyPredictionRequest predictionRequest = OccupancyPredictionRequest.builder()
                    .resourceId(request.getResourceId())
                    .targetDate(request.getStartDate())
                    .daysHistory(request.getDaysHistory() != null ? request.getDaysHistory() : 30)
                    .build();
                    
            OccupancyPredictionResponse prediction = analyticsService.predictOccupancy(predictionRequest);
            
            return ResponseEntity.ok(Map.of(
                    "success", prediction.isSuccess(),
                    "prediction", prediction,
                    "message", prediction.getMessage() != null ? prediction.getMessage() : "Prediction completed"
            ));
        } catch (Exception e) {
            log.error("Error predicting demand: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Prédiction pour la semaine prochaine
     */
    @GetMapping("/predict-next-week")
    public ResponseEntity<Map<String, Object>> predictNextWeek() {
        try {
            LocalDateTime nextWeek = LocalDateTime.now().plusWeeks(1);
            
            // Simuler une prédiction pour la semaine prochaine
            Map<String, Object> prediction = Map.of(
                    "success", true,
                    "predictionDate", nextWeek,
                    "weeklyTrends", Map.of(
                            "MONDAY", 78.5,
                            "TUESDAY", 82.3,
                            "WEDNESDAY", 75.8,
                            "THURSDAY", 85.2,
                            "FRIDAY", 70.4,
                            "SATURDAY", 25.1,
                            "SUNDAY", 15.3
                    ),
                    "hourlyTrends", Map.of(
                            "08", 45.2,
                            "09", 78.9,
                            "10", 85.6,
                            "11", 82.3,
                            "14", 79.8,
                            "15", 83.4,
                            "16", 76.2
                    ),
                    "confidenceLevel", 82.5,
                    "peakHours", List.of("10:00-11:00", "15:00-16:00"),
                    "lowUsagePeriods", List.of("12:00-14:00", "17:00-18:00"),
                    "recommendations", List.of(
                            "Jeudi sera le jour le plus chargé",
                            "Planifier la maintenance le weekend",
                            "Prévoir des salles supplémentaires entre 10h-11h"
                    )
            );
            
            return ResponseEntity.ok(prediction);
        } catch (Exception e) {
            log.error("Error predicting next week: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Identification des risques de surcharge
     */
    @GetMapping("/overload-risks")
    public ResponseEntity<List<Map<String, Object>>> identifyOverloadRisks(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            log.info("Identifying overload risks for period: {} to {}", startDate, endDate);
            
            // Simuler l'identification des risques
            List<Map<String, Object>> risks = List.of(
                    Map.of(
                            "resourceId", 1L,
                            "resourceName", "Amphithéâtre A",
                            "riskLevel", "HIGH",
                            "probability", 0.85,
                            "predictedOverload", "2024-03-15T10:00:00",
                            "expectedDemand", 120,
                            "capacity", 100,
                            "recommendations", List.of(
                                    "Prévoir une salle supplémentaire",
                                    "Décaler certains cours"
                            )
                    ),
                    Map.of(
                            "resourceId", 3L,
                            "resourceName", "Salle TP Info",
                            "riskLevel", "MEDIUM",
                            "probability", 0.65,
                            "predictedOverload", "2024-03-15T14:00:00",
                            "expectedDemand", 35,
                            "capacity", 30,
                            "recommendations", List.of(
                                    "Diviser le groupe en deux sessions"
                            )
                    )
            );
            
            return ResponseEntity.ok(risks);
        } catch (Exception e) {
            log.error("Error identifying overload risks: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Risques de surcharge pour la semaine prochaine
     */
    @GetMapping("/overload-risks-next-week")
    public ResponseEntity<List<Map<String, Object>>> getOverloadRisksNextWeek() {
        try {
            LocalDateTime nextWeek = LocalDateTime.now().plusWeeks(1);
            LocalDateTime endOfWeek = nextWeek.plusDays(7);
            
            return identifyOverloadRisks(nextWeek, endOfWeek);
        } catch (Exception e) {
            log.error("Error getting next week overload risks: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Suggestions d'optimisations proactives
     */
    @GetMapping("/proactive-optimizations")
    public ResponseEntity<Map<String, Object>> getProactiveOptimizations() {
        try {
            Map<String, Object> optimizations = Map.of(
                    "success", true,
                    "generatedAt", LocalDateTime.now(),
                    "shortTerm", List.of(
                            Map.of(
                                    "type", "CAPACITY_ADJUSTMENT",
                                    "description", "Ajuster la capacité de l'Amphithéâtre B",
                                    "priority", "HIGH",
                                    "estimatedImpact", 25.5,
                                    "timeframe", "1-2 jours"
                            ),
                            Map.of(
                                    "type", "SCHEDULE_OPTIMIZATION",
                                    "description", "Réorganiser les créneaux de 14h-16h",
                                    "priority", "MEDIUM",
                                    "estimatedImpact", 15.2,
                                    "timeframe", "3-5 jours"
                            )
                    ),
                    "mediumTerm", List.of(
                            Map.of(
                                    "type", "RESOURCE_REALLOCATION",
                                    "description", "Réaffecter les salles sous-utilisées",
                                    "priority", "MEDIUM",
                                    "estimatedImpact", 30.8,
                                    "timeframe", "1-2 semaines"
                            )
                    ),
                    "priority", "HIGH",
                    "totalOptimizations", 3,
                    "expectedImprovement", 23.8
            );
            
            return ResponseEntity.ok(optimizations);
        } catch (Exception e) {
            log.error("Error getting proactive optimizations: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Métriques prédictives
     */
    @GetMapping("/predictive-metrics")
    public ResponseEntity<Map<String, Object>> getPredictiveMetrics(
            @RequestParam(defaultValue = "1") Integer daysAhead) {
        try {
            LocalDateTime targetDate = LocalDateTime.now().plusDays(daysAhead);
            
            Map<String, Object> metrics = Map.of(
                    "success", true,
                    "targetDate", targetDate,
                    "predictedOccupancyRate", 76.8,
                    "predictedPerformanceScore", 82.3,
                    "predictedConflicts", 2,
                    "predictedMaintenanceNeeds", 1,
                    "confidenceLevel", 85.2,
                    "riskFactors", List.of(
                            Map.of("factor", "Peak hour demand", "impact", 0.15),
                            Map.of("factor", "Equipment availability", "impact", 0.08)
                    ),
                    "recommendations", List.of(
                            "Surveiller les créneaux de pointe",
                            "Préparer des alternatives pour les conflits prédits"
                    )
            );
            
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            log.error("Error getting predictive metrics: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Métriques prédictives pour demain
     */
    @GetMapping("/predictive-metrics-tomorrow")
    public ResponseEntity<Map<String, Object>> getPredictiveMetricsTomorrow() {
        return getPredictiveMetrics(1);
    }
    
    /**
     * Alertes prédictives
     */
    @GetMapping("/predictive-alerts")
    public ResponseEntity<List<Map<String, Object>>> getPredictiveAlerts(
            @RequestParam(defaultValue = "7") Integer daysAhead) {
        try {
            List<Map<String, Object>> alerts = List.of(
                    Map.of(
                            "id", 1L,
                            "type", "OVERLOAD_WARNING",
                            "severity", "HIGH",
                            "title", "Risque de surcharge détecté",
                            "description", "L'Amphithéâtre A risque d'être surchargé jeudi à 10h",
                            "predictedDate", LocalDateTime.now().plusDays(3),
                            "confidence", 0.87,
                            "actions", List.of(
                                    "Réserver une salle supplémentaire",
                                    "Informer les enseignants concernés"
                            )
                    ),
                    Map.of(
                            "id", 2L,
                            "type", "MAINTENANCE_ALERT",
                            "severity", "MEDIUM",
                            "title", "Maintenance préventive recommandée",
                            "description", "La Salle TP Info nécessitera une maintenance dans 10 jours",
                            "predictedDate", LocalDateTime.now().plusDays(10),
                            "confidence", 0.72,
                            "actions", List.of(
                                    "Planifier la maintenance",
                                    "Prévoir une salle alternative"
                            )
                    ),
                    Map.of(
                            "id", 3L,
                            "type", "EFFICIENCY_WARNING",
                            "severity", "LOW",
                            "title", "Efficacité sous-optimale",
                            "description", "La Salle 101 est sous-utilisée cette semaine",
                            "predictedDate", LocalDateTime.now().plusDays(1),
                            "confidence", 0.65,
                            "actions", List.of(
                                    "Réaffecter certains cours",
                                    "Considérer d'autres usages"
                            )
                    )
            );
            
            return ResponseEntity.ok(alerts);
        } catch (Exception e) {
            log.error("Error getting predictive alerts: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Rapport d'analyse complet
     */
    @GetMapping("/comprehensive-report")
    public ResponseEntity<Map<String, Object>> getComprehensiveReport(
            @RequestParam(defaultValue = "7") Integer forecastDays) {
        try {
            Map<String, Object> report = Map.of(
                    "success", true,
                    "reportType", "COMPREHENSIVE_ANALYSIS",
                    "generatedAt", LocalDateTime.now(),
                    "forecastPeriod", forecastDays,
                    "executiveSummary", Map.of(
                            "overallStatus", "GOOD",
                            "keyFindings", List.of(
                                    "Utilisation globale stable à 76%",
                                    "2 risques de surcharge identifiés",
                                    "Opportunités d'optimisation disponibles"
                            ),
                            "criticalActions", List.of(
                                    "Surveiller l'Amphithéâtre A jeudi matin",
                                    "Planifier maintenance Salle TP Info"
                            ),
                            "performanceScore", 82.3
                    ),
                    "predictions", Map.of(
                            "occupancyTrend", "STABLE",
                            "conflictRisk", "LOW",
                            "maintenanceNeeds", 1,
                            "optimizationOpportunities", 3
                    ),
                    "recommendations", Map.of(
                            "immediate", List.of(
                                    "Réserver salle supplémentaire pour jeudi",
                                    "Vérifier équipements Amphithéâtre A"
                            ),
                            "shortTerm", List.of(
                                    "Optimiser créneaux 14h-16h",
                                    "Planifier maintenance préventive"
                            ),
                            "longTerm", List.of(
                                    "Évaluer besoins en nouvelles salles",
                                    "Améliorer système de réservation"
                            )
                    ),
                    "metrics", Map.of(
                            "currentOccupancy", 76.8,
                            "predictedOccupancy", 78.2,
                            "efficiencyScore", 82.3,
                            "userSatisfaction", 89.1
                    )
            );
            
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            log.error("Error generating comprehensive report: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Prédiction de conflits
     */
    @PostMapping("/predict-conflicts")
    public ResponseEntity<ConflictPredictionResponse> predictConflicts(
            @Valid @RequestBody ConflictPredictionRequest request) {
        try {
            ConflictPredictionResponse response = analyticsService.predictConflicts(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error predicting conflicts: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Prédiction de maintenance
     */
    @PostMapping("/predict-maintenance")
    public ResponseEntity<MaintenancePredictionResponse> predictMaintenance(
            @Valid @RequestBody MaintenancePredictionRequest request) {
        try {
            MaintenancePredictionResponse response = analyticsService.predictMaintenance(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error predicting maintenance: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Recommandations d'optimisation
     */
    @GetMapping("/recommendations/optimization")
    public ResponseEntity<OptimizationRecommendationsResponse> getOptimizationRecommendations() {
        try {
            OptimizationRequest request = OptimizationRequest.builder()
                    .analysisType("COMPREHENSIVE")
                    .timeframe(30)
                    .build();
                    
            OptimizationRecommendationsResponse response = analyticsService.getOptimizationRecommendations(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting optimization recommendations: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}