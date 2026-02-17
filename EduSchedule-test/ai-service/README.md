# AI Service

Service d'analytics et prédictions pour EduSchedule.

## Build

Le module compile avec les parties opérationnelles suivantes :
- **AdvancedAnalyticsController** + **AdvancedAnalyticsService** : tableau de bord avancé et métriques.
- **AiServiceApplication** : point d'entrée Spring Boot.

Les fichiers suivants sont désactivés (`.java.disabled`) car ils dépendent de modèles/DTOs/repositories non présents dans le projet :
- `PredictiveAnalyticsController.java.disabled`
- `PredictiveAnalyticsService.java.disabled`
- `MLOptimizationService.java.disabled`

Pour les réactiver, ajouter les classes manquantes (model, repository, DTOs type ConflictPredictionRequest, MaintenancePredictionResponse, etc.) puis renommer les fichiers en `.java`.
