package cm.iusjc.aiservice.service;

import cm.iusjc.aiservice.dto.*;
import cm.iusjc.aiservice.model.PredictionModel;
import cm.iusjc.aiservice.repository.PredictionHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PredictiveAnalyticsService {

    @Autowired
    private PredictionHistoryRepository historyRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private MachineLearningService mlService;

    @Autowired
    private DataCollectionService dataService;

    public OccupancyPredictionResponse predictOccupancy(OccupancyPredictionRequest request) {
        try {
            // Collecter les données historiques
            List<HistoricalData> historicalData = dataService.getHistoricalOccupancy(
                    request.getResourceId(), 
                    request.getDaysHistory()
            );

            // Analyser les patterns
            PatternAnalysis patterns = analyzePatterns(historicalData, request.getTargetDate());

            // Calculer la prédiction
            PredictionResult prediction = calculateOccupancyPrediction(patterns, request);

            // Générer les recommandations
            List<String> recommendations = generateRecommendations(prediction, patterns);

            return OccupancyPredictionResponse.builder()
                    .success(true)
                    .resourceId(request.getResourceId())
                    .predictionDate(request.getTargetDate())
                    .predictedOccupancy(prediction.getValue())
                    .confidence(prediction.getConfidence())
                    .factors(patterns.getFactors())
                    .recommendations(recommendations)
                    .modelVersion("v2.1")
                    .build();

        } catch (Exception e) {
            return OccupancyPredictionResponse.builder()
                    .success(false)
                    .message("Erreur lors de la prédiction: " + e.getMessage())
                    .build();
        }
    }

    public ConflictPredictionResponse predictConflicts(ConflictPredictionRequest request) {
        try {
            // Analyser les patterns de conflits historiques
            List<ConflictPattern> conflictPatterns = analyzeConflictPatterns(request);

            // Prédire les conflits potentiels
            List<PotentialConflict> potentialConflicts = identifyPotentialConflicts(
                    request, conflictPatterns
            );

            // Calculer les probabilités
            potentialConflicts.forEach(conflict -> {
                double probability = calculateConflictProbability(conflict, conflictPatterns);
                conflict.setProbability(probability);
            });

            // Trier par probabilité décroissante
            potentialConflicts.sort((a, b) -> 
                    Double.compare(b.getProbability(), a.getProbability()));

            return ConflictPredictionResponse.builder()
                    .success(true)
                    .predictionPeriod(request.getPeriod())
                    .potentialConflicts(potentialConflicts)
                    .totalConflictsExpected(potentialConflicts.size())
                    .highRiskConflicts(potentialConflicts.stream()
                            .mapToInt(c -> c.getProbability() > 0.7 ? 1 : 0).sum())
                    .recommendations(generateConflictRecommendations(potentialConflicts))
                    .build();

        } catch (Exception e) {
            return ConflictPredictionResponse.builder()
                    .success(false)
                    .message("Erreur lors de la prédiction de conflits: " + e.getMessage())
                    .build();
        }
    }

    public MaintenancePredictionResponse predictMaintenance(MaintenancePredictionRequest request) {
        try {
            // Collecter les données d'usage et de maintenance
            UsageData usageData = dataService.getUsageData(request.getResourceId());
            MaintenanceHistory maintenanceHistory = dataService.getMaintenanceHistory(request.getResourceId());

            // Analyser l'état actuel
            ResourceCondition condition = analyzeResourceCondition(usageData, maintenanceHistory);

            // Prédire les besoins de maintenance
            MaintenancePrediction prediction = calculateMaintenancePrediction(condition, request);

            // Générer le planning optimal
            MaintenanceSchedule schedule = generateOptimalSchedule(prediction, request);

            return MaintenancePredictionResponse.builder()
                    .success(true)
                    .resourceId(request.getResourceId())
                    .currentCondition(condition)
                    .prediction(prediction)
                    .recommendedSchedule(schedule)
                    .costEstimate(calculateMaintenanceCost(prediction))
                    .urgencyLevel(determineUrgencyLevel(prediction))
                    .build();

        } catch (Exception e) {
            return MaintenancePredictionResponse.builder()
                    .success(false)
                    .message("Erreur lors de la prédiction de maintenance: " + e.getMessage())
                    .build();
        }
    }

    public OptimizationRecommendationsResponse getOptimizationRecommendations(
            OptimizationRequest request) {
        try {
            // Analyser l'état actuel du système
            SystemAnalysis analysis = analyzeCurrentSystem(request);

            // Identifier les opportunités d'optimisation
            List<OptimizationOpportunity> opportunities = identifyOptimizationOpportunities(analysis);

            // Calculer l'impact potentiel
            opportunities.forEach(opp -> {
                ImpactAnalysis impact = calculateImpact(opp, analysis);
                opp.setImpact(impact);
            });

            // Prioriser les recommandations
            opportunities.sort((a, b) -> 
                    Double.compare(b.getImpact().getScore(), a.getImpact().getScore()));

            // Générer le plan d'action
            ActionPlan actionPlan = generateActionPlan(opportunities);

            return OptimizationRecommendationsResponse.builder()
                    .success(true)
                    .currentSystemScore(analysis.getOverallScore())
                    .opportunities(opportunities)
                    .actionPlan(actionPlan)
                    .expectedImprovement(calculateExpectedImprovement(opportunities))
                    .implementationTimeframe(estimateImplementationTime(opportunities))
                    .build();

        } catch (Exception e) {
            return OptimizationRecommendationsResponse.builder()
                    .success(false)
                    .message("Erreur lors de la génération des recommandations: " + e.getMessage())
                    .build();
        }
    }

    private PatternAnalysis analyzePatterns(List<HistoricalData> data, LocalDateTime targetDate) {
        PatternAnalysis analysis = new PatternAnalysis();
        
        // Analyser les tendances temporelles
        double historicalTrend = calculateHistoricalTrend(data);
        analysis.addFactor("Historical pattern", historicalTrend);

        // Analyser les effets saisonniers
        double seasonalEffect = calculateSeasonalEffect(data, targetDate);
        analysis.addFactor("Seasonal trend", seasonalEffect);

        // Analyser l'effet du jour de la semaine
        double dayOfWeekEffect = calculateDayOfWeekEffect(data, targetDate.getDayOfWeek());
        analysis.addFactor("Day of week effect", dayOfWeekEffect);

        // Analyser les événements spéciaux
        double specialEventEffect = calculateSpecialEventEffect(targetDate);
        analysis.addFactor("Special events", specialEventEffect);

        return analysis;
    }

    private PredictionResult calculateOccupancyPrediction(PatternAnalysis patterns, 
            OccupancyPredictionRequest request) {
        
        // Modèle de régression linéaire avec pondération
        double baseOccupancy = getBaseOccupancy(request.getResourceId());
        double prediction = baseOccupancy;

        // Appliquer les facteurs
        for (Map.Entry<String, Double> factor : patterns.getFactors().entrySet()) {
            prediction += factor.getValue();
        }

        // Normaliser entre 0 et 100
        prediction = Math.max(0, Math.min(100, prediction));

        // Calculer la confiance basée sur la variance historique
        double confidence = calculateConfidence(patterns, request);

        return PredictionResult.builder()
                .value(prediction)
                .confidence(confidence)
                .build();
    }

    private List<String> generateRecommendations(PredictionResult prediction, PatternAnalysis patterns) {
        List<String> recommendations = new ArrayList<>();

        if (prediction.getValue() > 85) {
            recommendations.add("Consider booking alternative room");
            recommendations.add("High occupancy expected - prepare backup options");
        }

        if (prediction.getValue() < 30) {
            recommendations.add("Schedule maintenance during low usage");
            recommendations.add("Consider consolidating with other activities");
        }

        if (prediction.getConfidence() < 70) {
            recommendations.add("Monitor closely - prediction uncertainty is high");
        }

        // Recommandations basées sur les patterns
        patterns.getFactors().entrySet().stream()
                .filter(entry -> Math.abs(entry.getValue()) > 10)
                .forEach(entry -> {
                    if (entry.getValue() > 0) {
                        recommendations.add("High impact from " + entry.getKey() + " - plan accordingly");
                    }
                });

        return recommendations;
    }

    private double calculateHistoricalTrend(List<HistoricalData> data) {
        if (data.size() < 2) return 0;

        // Régression linéaire simple
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        int n = data.size();

        for (int i = 0; i < n; i++) {
            double x = i;
            double y = data.get(i).getOccupancy();
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += x * x;
        }

        double slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
        return slope * n; // Projection sur la période
    }

    private double calculateSeasonalEffect(List<HistoricalData> data, LocalDateTime targetDate) {
        // Analyser les patterns mensuels
        Map<Integer, Double> monthlyAverages = data.stream()
                .collect(Collectors.groupingBy(
                        d -> d.getDate().getMonthValue(),
                        Collectors.averagingDouble(HistoricalData::getOccupancy)
                ));

        double overallAverage = data.stream()
                .mapToDouble(HistoricalData::getOccupancy)
                .average()
                .orElse(0);

        int targetMonth = targetDate.getMonthValue();
        double monthlyAverage = monthlyAverages.getOrDefault(targetMonth, overallAverage);

        return monthlyAverage - overallAverage;
    }

    private double calculateDayOfWeekEffect(List<HistoricalData> data, DayOfWeek targetDay) {
        Map<DayOfWeek, Double> dayAverages = data.stream()
                .collect(Collectors.groupingBy(
                        d -> d.getDate().getDayOfWeek(),
                        Collectors.averagingDouble(HistoricalData::getOccupancy)
                ));

        double overallAverage = data.stream()
                .mapToDouble(HistoricalData::getOccupancy)
                .average()
                .orElse(0);

        double dayAverage = dayAverages.getOrDefault(targetDay, overallAverage);
        return dayAverage - overallAverage;
    }

    private double calculateSpecialEventEffect(LocalDateTime targetDate) {
        // Vérifier les événements spéciaux (examens, vacances, etc.)
        // Pour l'instant, retourner 0 - à implémenter avec un calendrier d'événements
        return 0;
    }

    private double getBaseOccupancy(Long resourceId) {
        // Récupérer l'occupation moyenne historique
        try {
            String url = "http://localhost:8085/api/reservations/analytics/average-occupancy/" + resourceId;
            Double average = restTemplate.getForObject(url, Double.class);
            return average != null ? average : 50.0;
        } catch (Exception e) {
            return 50.0; // Valeur par défaut
        }
    }

    private double calculateConfidence(PatternAnalysis patterns, OccupancyPredictionRequest request) {
        // Calculer la confiance basée sur la cohérence des patterns
        double baseConfidence = 80.0;
        
        // Réduire la confiance si les patterns sont incohérents
        double variance = patterns.getFactors().values().stream()
                .mapToDouble(v -> v * v)
                .average()
                .orElse(0);

        double confidenceReduction = Math.min(30, variance / 10);
        return Math.max(50, baseConfidence - confidenceReduction);
    }

    // Méthodes pour la prédiction de conflits
    private List<ConflictPattern> analyzeConflictPatterns(ConflictPredictionRequest request) {
        // Analyser les patterns de conflits historiques
        List<ConflictPattern> patterns = new ArrayList<>();
        
        // Pattern 1: Conflits de double réservation
        patterns.add(ConflictPattern.builder()
                .type("DOUBLE_BOOKING")
                .frequency(0.15)
                .timePatterns(Arrays.asList("09:00-10:00", "14:00-15:00"))
                .riskFactors(Arrays.asList("Popular rooms", "Peak hours"))
                .build());

        // Pattern 2: Conflits d'enseignants
        patterns.add(ConflictPattern.builder()
                .type("TEACHER_OVERLAP")
                .frequency(0.08)
                .timePatterns(Arrays.asList("10:00-12:00"))
                .riskFactors(Arrays.asList("Multiple courses", "Schedule changes"))
                .build());

        return patterns;
    }

    private List<PotentialConflict> identifyPotentialConflicts(
            ConflictPredictionRequest request, List<ConflictPattern> patterns) {
        
        List<PotentialConflict> conflicts = new ArrayList<>();
        
        // Simuler l'identification de conflits potentiels
        // En production, ceci analyserait les données réelles
        
        return conflicts;
    }

    private double calculateConflictProbability(PotentialConflict conflict, 
            List<ConflictPattern> patterns) {
        
        return patterns.stream()
                .filter(p -> p.getType().equals(conflict.getType()))
                .mapToDouble(ConflictPattern::getFrequency)
                .findFirst()
                .orElse(0.1);
    }

    private List<String> generateConflictRecommendations(List<PotentialConflict> conflicts) {
        List<String> recommendations = new ArrayList<>();
        
        if (conflicts.size() > 5) {
            recommendations.add("High conflict risk - review scheduling policies");
        }
        
        recommendations.add("Monitor peak hours for potential conflicts");
        recommendations.add("Implement automated conflict detection");
        
        return recommendations;
    }

    // Méthodes pour la prédiction de maintenance
    private ResourceCondition analyzeResourceCondition(UsageData usage, MaintenanceHistory history) {
        return ResourceCondition.builder()
                .overallScore(85.0)
                .usageIntensity(usage.getIntensity())
                .lastMaintenanceDate(history.getLastMaintenanceDate())
                .issuesReported(history.getIssuesCount())
                .build();
    }

    private MaintenancePrediction calculateMaintenancePrediction(ResourceCondition condition, 
            MaintenancePredictionRequest request) {
        
        LocalDateTime predictedDate = LocalDateTime.now().plusDays(30);
        String type = "PREVENTIVE";
        double urgency = 0.3;

        if (condition.getOverallScore() < 70) {
            predictedDate = LocalDateTime.now().plusDays(7);
            type = "CORRECTIVE";
            urgency = 0.8;
        }

        return MaintenancePrediction.builder()
                .predictedDate(predictedDate)
                .type(type)
                .urgency(urgency)
                .description("Maintenance prédictive basée sur l'usage")
                .estimatedDuration(2.0)
                .build();
    }

    private MaintenanceSchedule generateOptimalSchedule(MaintenancePrediction prediction, 
            MaintenancePredictionRequest request) {
        
        return MaintenanceSchedule.builder()
                .recommendedDate(prediction.getPredictedDate())
                .alternativeDates(Arrays.asList(
                        prediction.getPredictedDate().plusDays(1),
                        prediction.getPredictedDate().plusDays(2)
                ))
                .optimalTimeSlot("08:00-10:00")
                .impactLevel("LOW")
                .build();
    }

    private double calculateMaintenanceCost(MaintenancePrediction prediction) {
        double baseCost = 200.0;
        if ("CORRECTIVE".equals(prediction.getType())) {
            baseCost *= 1.5;
        }
        return baseCost * prediction.getEstimatedDuration();
    }

    private String determineUrgencyLevel(MaintenancePrediction prediction) {
        if (prediction.getUrgency() > 0.7) return "HIGH";
        if (prediction.getUrgency() > 0.4) return "MEDIUM";
        return "LOW";
    }

    // Méthodes pour les recommandations d'optimisation
    private SystemAnalysis analyzeCurrentSystem(OptimizationRequest request) {
        return SystemAnalysis.builder()
                .overallScore(75.0)
                .occupancyEfficiency(80.0)
                .resourceUtilization(70.0)
                .conflictRate(5.0)
                .userSatisfaction(85.0)
                .build();
    }

    private List<OptimizationOpportunity> identifyOptimizationOpportunities(SystemAnalysis analysis) {
        List<OptimizationOpportunity> opportunities = new ArrayList<>();
        
        if (analysis.getResourceUtilization() < 75) {
            opportunities.add(OptimizationOpportunity.builder()
                    .type("RESOURCE_OPTIMIZATION")
                    .title("Optimiser l'utilisation des ressources")
                    .description("Améliorer l'assignation des salles")
                    .priority("HIGH")
                    .build());
        }

        if (analysis.getConflictRate() > 3) {
            opportunities.add(OptimizationOpportunity.builder()
                    .type("CONFLICT_REDUCTION")
                    .title("Réduire les conflits")
                    .description("Implémenter une détection proactive")
                    .priority("MEDIUM")
                    .build());
        }

        return opportunities;
    }

    private ImpactAnalysis calculateImpact(OptimizationOpportunity opportunity, SystemAnalysis analysis) {
        double score = 0;
        
        switch (opportunity.getType()) {
            case "RESOURCE_OPTIMIZATION":
                score = (100 - analysis.getResourceUtilization()) * 0.8;
                break;
            case "CONFLICT_REDUCTION":
                score = analysis.getConflictRate() * 10;
                break;
        }

        return ImpactAnalysis.builder()
                .score(score)
                .expectedImprovement(score / 100)
                .implementationEffort("MEDIUM")
                .timeToValue(30)
                .build();
    }

    private ActionPlan generateActionPlan(List<OptimizationOpportunity> opportunities) {
        return ActionPlan.builder()
                .totalOpportunities(opportunities.size())
                .highPriorityCount(opportunities.stream()
                        .mapToInt(o -> "HIGH".equals(o.getPriority()) ? 1 : 0).sum())
                .estimatedTimeframe(90)
                .phases(Arrays.asList("Analysis", "Implementation", "Validation"))
                .build();
    }

    private double calculateExpectedImprovement(List<OptimizationOpportunity> opportunities) {
        return opportunities.stream()
                .mapToDouble(o -> o.getImpact().getExpectedImprovement())
                .sum();
    }

    private int estimateImplementationTime(List<OptimizationOpportunity> opportunities) {
        return opportunities.stream()
                .mapToInt(o -> o.getImpact().getTimeToValue())
                .max()
                .orElse(30);
    }
}