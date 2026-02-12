package cm.iusjc.maintenanceservice.service;

import cm.iusjc.maintenanceservice.dto.*;
import cm.iusjc.maintenanceservice.entity.MaintenanceTask;
import cm.iusjc.maintenanceservice.entity.MaintenanceAlert;
import cm.iusjc.maintenanceservice.repository.MaintenanceTaskRepository;
import cm.iusjc.maintenanceservice.repository.MaintenanceAlertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PredictiveMaintenanceService {

    @Autowired
    private MaintenanceTaskRepository taskRepository;

    @Autowired
    private MaintenanceAlertRepository alertRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private UsageAnalysisService usageAnalysisService;

    @Autowired
    private MaintenanceSchedulingService schedulingService;

    @Autowired
    private NotificationService notificationService;

    public MaintenancePredictionResponse predictMaintenance(MaintenancePredictionRequest request) {
        try {
            // Analyser l'usage de la ressource
            UsageAnalysis usage = usageAnalysisService.analyzeResourceUsage(
                    request.getResourceId(), request.getAnalysisPeriod());

            // Récupérer l'historique de maintenance
            List<MaintenanceTask> history = taskRepository.findByResourceIdOrderByCompletedAtDesc(
                    request.getResourceId());

            // Analyser l'état actuel de la ressource
            ResourceCondition condition = analyzeResourceCondition(usage, history);

            // Calculer les prédictions de maintenance
            List<MaintenancePrediction> predictions = calculateMaintenancePredictions(
                    condition, usage, history);

            // Générer les recommandations
            List<MaintenanceRecommendation> recommendations = generateRecommendations(
                    predictions, condition);

            // Calculer les coûts estimés
            CostEstimate costEstimate = calculateCostEstimate(predictions);

            return MaintenancePredictionResponse.builder()
                    .success(true)
                    .resourceId(request.getResourceId())
                    .currentCondition(condition)
                    .predictions(predictions)
                    .recommendations(recommendations)
                    .costEstimate(costEstimate)
                    .analysisDate(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            return MaintenancePredictionResponse.builder()
                    .success(false)
                    .message("Erreur lors de la prédiction de maintenance: " + e.getMessage())
                    .build();
        }
    }

    public MaintenanceScheduleResponse scheduleMaintenanceTask(ScheduleMaintenanceRequest request) {
        try {
            // Valider la demande
            validateScheduleRequest(request);

            // Trouver le créneau optimal
            OptimalTimeSlot optimalSlot = schedulingService.findOptimalTimeSlot(
                    request.getResourceId(),
                    request.getEstimatedDuration(),
                    request.getPreferredDates(),
                    request.getPriority()
            );

            // Créer la tâche de maintenance
            MaintenanceTask task = new MaintenanceTask();
            task.setResourceId(request.getResourceId());
            task.setType(request.getType());
            task.setDescription(request.getDescription());
            task.setScheduledDate(optimalSlot.getStartTime());
            task.setEstimatedDuration(request.getEstimatedDuration());
            task.setPriority(request.getPriority());
            task.setStatus("SCHEDULED");
            task.setCreatedAt(LocalDateTime.now());

            MaintenanceTask savedTask = taskRepository.save(task);

            // Créer les notifications
            createMaintenanceNotifications(savedTask, optimalSlot);

            // Bloquer la ressource pendant la maintenance
            blockResourceForMaintenance(savedTask);

            return MaintenanceScheduleResponse.builder()
                    .success(true)
                    .taskId(savedTask.getId())
                    .scheduledDate(optimalSlot.getStartTime())
                    .estimatedEndDate(optimalSlot.getEndTime())
                    .impactLevel(optimalSlot.getImpactLevel())
                    .alternativeSlots(optimalSlot.getAlternatives())
                    .message("Maintenance programmée avec succès")
                    .build();

        } catch (Exception e) {
            return MaintenanceScheduleResponse.builder()
                    .success(false)
                    .message("Erreur lors de la programmation: " + e.getMessage())
                    .build();
        }
    }

    public MaintenanceAlertsResponse getMaintenanceAlerts(MaintenanceAlertsRequest request) {
        try {
            List<MaintenanceAlert> alerts = alertRepository.findActiveAlerts(
                    request.getResourceIds(),
                    request.getPriorityFilter(),
                    request.getTypeFilter()
            );

            // Enrichir les alertes avec des informations contextuelles
            List<EnrichedMaintenanceAlert> enrichedAlerts = alerts.stream()
                    .map(this::enrichAlert)
                    .sorted((a, b) -> comparePriority(a.getPriority(), b.getPriority()))
                    .collect(Collectors.toList());

            // Calculer les statistiques
            AlertStatistics stats = calculateAlertStatistics(enrichedAlerts);

            return MaintenanceAlertsResponse.builder()
                    .success(true)
                    .alerts(enrichedAlerts)
                    .statistics(stats)
                    .totalAlerts(enrichedAlerts.size())
                    .criticalAlerts(stats.getCriticalCount())
                    .build();

        } catch (Exception e) {
            return MaintenanceAlertsResponse.builder()
                    .success(false)
                    .message("Erreur lors de la récupération des alertes: " + e.getMessage())
                    .build();
        }
    }

    public MaintenanceCompletionResponse completeMaintenanceTask(Long taskId, 
            MaintenanceCompletionRequest request) {
        try {
            MaintenanceTask task = taskRepository.findById(taskId)
                    .orElseThrow(() -> new RuntimeException("Tâche de maintenance non trouvée"));

            // Mettre à jour la tâche
            task.setStatus("COMPLETED");
            task.setCompletedAt(LocalDateTime.now());
            task.setActualDuration(request.getActualDuration());
            task.setNotes(request.getNotes());
            task.setCompletedBy(request.getCompletedBy());

            // Enregistrer les actions effectuées
            if (request.getActionsPerformed() != null) {
                task.setActionsPerformed(request.getActionsPerformed());
            }

            // Enregistrer les pièces utilisées
            if (request.getPartsUsed() != null) {
                task.setPartsUsed(request.getPartsUsed());
            }

            MaintenanceTask completedTask = taskRepository.save(task);

            // Débloquer la ressource
            unblockResourceAfterMaintenance(completedTask);

            // Mettre à jour l'état de la ressource
            updateResourceConditionAfterMaintenance(completedTask, request);

            // Créer les notifications de fin
            notificationService.notifyMaintenanceCompleted(completedTask);

            // Analyser l'efficacité de la maintenance
            MaintenanceEfficiency efficiency = analyzeMaintenanceEfficiency(completedTask);

            return MaintenanceCompletionResponse.builder()
                    .success(true)
                    .taskId(taskId)
                    .completedAt(completedTask.getCompletedAt())
                    .actualDuration(completedTask.getActualDuration())
                    .efficiency(efficiency)
                    .message("Maintenance terminée avec succès")
                    .build();

        } catch (Exception e) {
            return MaintenanceCompletionResponse.builder()
                    .success(false)
                    .message("Erreur lors de la finalisation: " + e.getMessage())
                    .build();
        }
    }

    @Scheduled(fixedRate = 3600000) // Toutes les heures
    public void runPredictiveAnalysis() {
        try {
            // Récupérer toutes les ressources actives
            List<Long> resourceIds = getActiveResourceIds();

            for (Long resourceId : resourceIds) {
                // Analyser chaque ressource
                MaintenancePredictionRequest request = MaintenancePredictionRequest.builder()
                        .resourceId(resourceId)
                        .analysisPeriod(30) // 30 jours
                        .build();

                MaintenancePredictionResponse prediction = predictMaintenance(request);

                if (prediction.isSuccess()) {
                    // Créer des alertes si nécessaire
                    createAlertsFromPredictions(prediction);

                    // Programmer la maintenance préventive si recommandée
                    schedulePreventiveMaintenanceIfNeeded(prediction);
                }
            }

        } catch (Exception e) {
            System.err.println("Erreur lors de l'analyse prédictive: " + e.getMessage());
        }
    }

    @Scheduled(cron = "0 0 8 * * MON") // Tous les lundis à 8h
    public void generateWeeklyMaintenanceReport() {
        try {
            // Générer un rapport hebdomadaire de maintenance
            WeeklyMaintenanceReport report = generateWeeklyReport();
            
            // Envoyer le rapport aux responsables
            notificationService.sendWeeklyMaintenanceReport(report);

        } catch (Exception e) {
            System.err.println("Erreur lors de la génération du rapport: " + e.getMessage());
        }
    }

    private ResourceCondition analyzeResourceCondition(UsageAnalysis usage, 
            List<MaintenanceTask> history) {
        
        double overallScore = 100.0;
        List<String> issues = new ArrayList<>();
        
        // Analyser l'intensité d'usage
        if (usage.getIntensityScore() > 0.8) {
            overallScore -= 20;
            issues.add("Usage intensif détecté");
        }

        // Analyser la fréquence des pannes
        long recentIssues = history.stream()
                .filter(task -> "CORRECTIVE".equals(task.getType()))
                .filter(task -> task.getCompletedAt() != null && 
                        task.getCompletedAt().isAfter(LocalDateTime.now().minusDays(90)))
                .count();

        if (recentIssues > 3) {
            overallScore -= 30;
            issues.add("Pannes fréquentes récentes");
        }

        // Analyser l'âge depuis la dernière maintenance
        Optional<MaintenanceTask> lastMaintenance = history.stream()
                .filter(task -> task.getCompletedAt() != null)
                .findFirst();

        if (lastMaintenance.isPresent()) {
            long daysSinceLastMaintenance = ChronoUnit.DAYS.between(
                    lastMaintenance.get().getCompletedAt(), LocalDateTime.now());
            
            if (daysSinceLastMaintenance > 180) {
                overallScore -= 25;
                issues.add("Maintenance non effectuée depuis longtemps");
            }
        } else {
            overallScore -= 40;
            issues.add("Aucun historique de maintenance");
        }

        return ResourceCondition.builder()
                .overallScore(Math.max(0, overallScore))
                .usageIntensity(usage.getIntensityScore())
                .lastMaintenanceDate(lastMaintenance.map(MaintenanceTask::getCompletedAt).orElse(null))
                .issuesReported(recentIssues)
                .identifiedIssues(issues)
                .riskLevel(determineRiskLevel(overallScore))
                .build();
    }

    private List<MaintenancePrediction> calculateMaintenancePredictions(
            ResourceCondition condition, UsageAnalysis usage, List<MaintenanceTask> history) {
        
        List<MaintenancePrediction> predictions = new ArrayList<>();

        // Prédiction de maintenance préventive
        if (condition.getOverallScore() < 80) {
            LocalDateTime predictedDate = calculatePreventiveMaintenanceDate(condition, usage);
            
            predictions.add(MaintenancePrediction.builder()
                    .type("PREVENTIVE")
                    .predictedDate(predictedDate)
                    .urgency(calculateUrgency(condition.getOverallScore()))
                    .description("Maintenance préventive recommandée")
                    .estimatedDuration(calculateEstimatedDuration("PREVENTIVE", condition))
                    .confidence(0.85)
                    .build());
        }

        // Prédiction de maintenance corrective
        if (condition.getRiskLevel().equals("HIGH") || condition.getRiskLevel().equals("CRITICAL")) {
            LocalDateTime urgentDate = LocalDateTime.now().plusDays(
                    condition.getRiskLevel().equals("CRITICAL") ? 1 : 7);
            
            predictions.add(MaintenancePrediction.builder()
                    .type("CORRECTIVE")
                    .predictedDate(urgentDate)
                    .urgency(condition.getRiskLevel().equals("CRITICAL") ? 0.95 : 0.75)
                    .description("Maintenance corrective urgente")
                    .estimatedDuration(calculateEstimatedDuration("CORRECTIVE", condition))
                    .confidence(0.75)
                    .build());
        }

        // Prédiction de remplacement d'équipement
        if (condition.getOverallScore() < 40 && condition.getIssuesReported() > 5) {
            predictions.add(MaintenancePrediction.builder()
                    .type("REPLACEMENT")
                    .predictedDate(LocalDateTime.now().plusDays(30))
                    .urgency(0.6)
                    .description("Remplacement d'équipement recommandé")
                    .estimatedDuration(4.0)
                    .confidence(0.7)
                    .build());
        }

        return predictions;
    }

    private List<MaintenanceRecommendation> generateRecommendations(
            List<MaintenancePrediction> predictions, ResourceCondition condition) {
        
        List<MaintenanceRecommendation> recommendations = new ArrayList<>();

        // Recommandations basées sur les prédictions
        for (MaintenancePrediction prediction : predictions) {
            if (prediction.getUrgency() > 0.8) {
                recommendations.add(MaintenanceRecommendation.builder()
                        .type("URGENT_ACTION")
                        .title("Action urgente requise")
                        .description("Programmer immédiatement la maintenance " + prediction.getType().toLowerCase())
                        .priority("HIGH")
                        .estimatedImpact("Éviter une panne majeure")
                        .build());
            }
        }

        // Recommandations basées sur l'état
        if (condition.getUsageIntensity() > 0.8) {
            recommendations.add(MaintenanceRecommendation.builder()
                    .type("USAGE_OPTIMIZATION")
                    .title("Optimiser l'utilisation")
                    .description("Réduire l'intensité d'usage pour prolonger la durée de vie")
                    .priority("MEDIUM")
                    .estimatedImpact("Réduction de 30% des besoins de maintenance")
                    .build());
        }

        // Recommandations préventives
        if (condition.getLastMaintenanceDate() == null || 
            condition.getLastMaintenanceDate().isBefore(LocalDateTime.now().minusDays(90))) {
            
            recommendations.add(MaintenanceRecommendation.builder()
                    .type("PREVENTIVE_SCHEDULE")
                    .title("Établir un planning préventif")
                    .description("Mettre en place un programme de maintenance régulière")
                    .priority("MEDIUM")
                    .estimatedImpact("Réduction de 50% des pannes imprévisibles")
                    .build());
        }

        return recommendations;
    }

    private CostEstimate calculateCostEstimate(List<MaintenancePrediction> predictions) {
        double totalCost = 0;
        double preventiveCost = 0;
        double correctiveCost = 0;

        for (MaintenancePrediction prediction : predictions) {
            double cost = calculateMaintenanceCost(prediction);
            totalCost += cost;

            if ("PREVENTIVE".equals(prediction.getType())) {
                preventiveCost += cost;
            } else if ("CORRECTIVE".equals(prediction.getType())) {
                correctiveCost += cost;
            }
        }

        return CostEstimate.builder()
                .totalEstimatedCost(totalCost)
                .preventiveCost(preventiveCost)
                .correctiveCost(correctiveCost)
                .potentialSavings(correctiveCost * 0.6) // 60% d'économies avec la maintenance préventive
                .currency("EUR")
                .build();
    }

    private double calculateMaintenanceCost(MaintenancePrediction prediction) {
        double baseCost = 200.0; // Coût de base par heure
        double cost = baseCost * prediction.getEstimatedDuration();

        // Majoration selon le type
        switch (prediction.getType()) {
            case "CORRECTIVE":
                cost *= 1.5; // 50% plus cher
                break;
            case "REPLACEMENT":
                cost *= 3.0; // 3 fois plus cher
                break;
        }

        // Majoration selon l'urgence
        if (prediction.getUrgency() > 0.8) {
            cost *= 1.3; // 30% plus cher en urgence
        }

        return cost;
    }

    private LocalDateTime calculatePreventiveMaintenanceDate(ResourceCondition condition, 
            UsageAnalysis usage) {
        
        int baseDays = 30;
        
        // Ajuster selon l'état
        if (condition.getOverallScore() < 60) {
            baseDays = 7;
        } else if (condition.getOverallScore() < 80) {
            baseDays = 14;
        }

        // Ajuster selon l'usage
        if (usage.getIntensityScore() > 0.8) {
            baseDays = (int) (baseDays * 0.7);
        }

        return LocalDateTime.now().plusDays(baseDays);
    }

    private double calculateUrgency(double overallScore) {
        if (overallScore < 40) return 0.9;
        if (overallScore < 60) return 0.7;
        if (overallScore < 80) return 0.5;
        return 0.3;
    }

    private double calculateEstimatedDuration(String type, ResourceCondition condition) {
        double baseDuration = 2.0; // 2 heures par défaut

        switch (type) {
            case "PREVENTIVE":
                baseDuration = 1.5;
                break;
            case "CORRECTIVE":
                baseDuration = 3.0;
                break;
            case "REPLACEMENT":
                baseDuration = 6.0;
                break;
        }

        // Ajuster selon l'état
        if (condition.getOverallScore() < 50) {
            baseDuration *= 1.5;
        }

        return baseDuration;
    }

    private String determineRiskLevel(double overallScore) {
        if (overallScore < 30) return "CRITICAL";
        if (overallScore < 50) return "HIGH";
        if (overallScore < 70) return "MEDIUM";
        return "LOW";
    }

    private void validateScheduleRequest(ScheduleMaintenanceRequest request) {
        if (request.getResourceId() == null) {
            throw new IllegalArgumentException("L'ID de la ressource est requis");
        }
        if (request.getType() == null || request.getType().trim().isEmpty()) {
            throw new IllegalArgumentException("Le type de maintenance est requis");
        }
        if (request.getEstimatedDuration() == null || request.getEstimatedDuration() <= 0) {
            throw new IllegalArgumentException("La durée estimée doit être positive");
        }
    }

    private void createMaintenanceNotifications(MaintenanceTask task, OptimalTimeSlot slot) {
        // Créer les notifications pour les parties prenantes
        notificationService.notifyMaintenanceScheduled(task, slot);
    }

    private void blockResourceForMaintenance(MaintenanceTask task) {
        try {
            String url = "http://localhost:8085/api/reservations/block-resource";
            Map<String, Object> blockRequest = Map.of(
                    "resourceId", task.getResourceId(),
                    "startTime", task.getScheduledDate(),
                    "endTime", task.getScheduledDate().plusHours(task.getEstimatedDuration().longValue()),
                    "reason", "MAINTENANCE",
                    "description", task.getDescription()
            );
            
            restTemplate.postForObject(url, blockRequest, Object.class);
        } catch (Exception e) {
            System.err.println("Erreur lors du blocage de la ressource: " + e.getMessage());
        }
    }

    private void unblockResourceAfterMaintenance(MaintenanceTask task) {
        try {
            String url = "http://localhost:8085/api/reservations/unblock-resource/" + task.getResourceId();
            restTemplate.delete(url);
        } catch (Exception e) {
            System.err.println("Erreur lors du déblocage de la ressource: " + e.getMessage());
        }
    }

    private void updateResourceConditionAfterMaintenance(MaintenanceTask task, 
            MaintenanceCompletionRequest request) {
        // Mettre à jour l'état de la ressource après maintenance
        // Implémentation de la mise à jour de l'état
    }

    private MaintenanceEfficiency analyzeMaintenanceEfficiency(MaintenanceTask task) {
        double plannedDuration = task.getEstimatedDuration();
        double actualDuration = task.getActualDuration();
        
        double efficiency = plannedDuration / actualDuration;
        String rating = efficiency > 0.9 ? "EXCELLENT" : 
                       efficiency > 0.8 ? "GOOD" : 
                       efficiency > 0.7 ? "AVERAGE" : "POOR";

        return MaintenanceEfficiency.builder()
                .efficiency(efficiency)
                .rating(rating)
                .plannedDuration(plannedDuration)
                .actualDuration(actualDuration)
                .variance(actualDuration - plannedDuration)
                .build();
    }

    private List<Long> getActiveResourceIds() {
        try {
            String url = "http://localhost:8085/api/resources/active-ids";
            Long[] ids = restTemplate.getForObject(url, Long[].class);
            return ids != null ? Arrays.asList(ids) : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private void createAlertsFromPredictions(MaintenancePredictionResponse prediction) {
        for (MaintenancePrediction pred : prediction.getPredictions()) {
            if (pred.getUrgency() > 0.7) {
                MaintenanceAlert alert = new MaintenanceAlert();
                alert.setResourceId(prediction.getResourceId());
                alert.setType(pred.getType());
                alert.setPriority(pred.getUrgency() > 0.9 ? "CRITICAL" : "HIGH");
                alert.setDescription(pred.getDescription());
                alert.setPredictedDate(pred.getPredictedDate());
                alert.setCreatedAt(LocalDateTime.now());
                alert.setStatus("ACTIVE");

                alertRepository.save(alert);
            }
        }
    }

    private void schedulePreventiveMaintenanceIfNeeded(MaintenancePredictionResponse prediction) {
        // Programmer automatiquement la maintenance préventive si recommandée
        for (MaintenancePrediction pred : prediction.getPredictions()) {
            if ("PREVENTIVE".equals(pred.getType()) && pred.getUrgency() > 0.6) {
                ScheduleMaintenanceRequest request = ScheduleMaintenanceRequest.builder()
                        .resourceId(prediction.getResourceId())
                        .type("PREVENTIVE")
                        .description(pred.getDescription())
                        .estimatedDuration(pred.getEstimatedDuration())
                        .priority("MEDIUM")
                        .preferredDates(Arrays.asList(pred.getPredictedDate()))
                        .build();

                scheduleMaintenanceTask(request);
            }
        }
    }

    private WeeklyMaintenanceReport generateWeeklyReport() {
        LocalDateTime weekStart = LocalDateTime.now().minusDays(7);
        LocalDateTime weekEnd = LocalDateTime.now();

        List<MaintenanceTask> completedTasks = taskRepository.findCompletedTasksBetween(weekStart, weekEnd);
        List<MaintenanceAlert> activeAlerts = alertRepository.findActiveAlerts(null, null, null);

        return WeeklyMaintenanceReport.builder()
                .weekStart(weekStart)
                .weekEnd(weekEnd)
                .completedTasks(completedTasks.size())
                .activeAlerts(activeAlerts.size())
                .averageEfficiency(calculateAverageEfficiency(completedTasks))
                .totalCost(calculateTotalCost(completedTasks))
                .build();
    }

    private double calculateAverageEfficiency(List<MaintenanceTask> tasks) {
        return tasks.stream()
                .filter(task -> task.getEstimatedDuration() != null && task.getActualDuration() != null)
                .mapToDouble(task -> task.getEstimatedDuration() / task.getActualDuration())
                .average()
                .orElse(1.0);
    }

    private double calculateTotalCost(List<MaintenanceTask> tasks) {
        return tasks.stream()
                .mapToDouble(task -> task.getActualDuration() != null ? 
                        task.getActualDuration() * 200 : 0)
                .sum();
    }

    private EnrichedMaintenanceAlert enrichAlert(MaintenanceAlert alert) {
        // Enrichir l'alerte avec des informations contextuelles
        return EnrichedMaintenanceAlert.builder()
                .id(alert.getId())
                .resourceId(alert.getResourceId())
                .type(alert.getType())
                .priority(alert.getPriority())
                .description(alert.getDescription())
                .predictedDate(alert.getPredictedDate())
                .createdAt(alert.getCreatedAt())
                .status(alert.getStatus())
                .resourceName(getResourceName(alert.getResourceId()))
                .daysSinceCreated(ChronoUnit.DAYS.between(alert.getCreatedAt(), LocalDateTime.now()))
                .build();
    }

    private String getResourceName(Long resourceId) {
        try {
            String url = "http://localhost:8085/api/resources/" + resourceId + "/name";
            return restTemplate.getForObject(url, String.class);
        } catch (Exception e) {
            return "Ressource " + resourceId;
        }
    }

    private int comparePriority(String priority1, String priority2) {
        Map<String, Integer> priorityOrder = Map.of(
                "CRITICAL", 4,
                "HIGH", 3,
                "MEDIUM", 2,
                "LOW", 1
        );
        
        return Integer.compare(
                priorityOrder.getOrDefault(priority2, 0),
                priorityOrder.getOrDefault(priority1, 0)
        );
    }

    private AlertStatistics calculateAlertStatistics(List<EnrichedMaintenanceAlert> alerts) {
        long criticalCount = alerts.stream().filter(a -> "CRITICAL".equals(a.getPriority())).count();
        long highCount = alerts.stream().filter(a -> "HIGH".equals(a.getPriority())).count();
        long mediumCount = alerts.stream().filter(a -> "MEDIUM".equals(a.getPriority())).count();
        long lowCount = alerts.stream().filter(a -> "LOW".equals(a.getPriority())).count();

        return AlertStatistics.builder()
                .criticalCount(criticalCount)
                .highCount(highCount)
                .mediumCount(mediumCount)
                .lowCount(lowCount)
                .totalCount(alerts.size())
                .averageAge(alerts.stream().mapToLong(EnrichedMaintenanceAlert::getDaysSinceCreated).average().orElse(0))
                .build();
    }
}