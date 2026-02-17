# Maintenance Service

Service de maintenance prédictive pour EduSchedule.

## Build

Le module a un `pom.xml` et une classe principale `MaintenanceServiceApplication` pour permettre le build Maven et Docker.

Le fichier `PredictiveMaintenanceService.java.disabled` est l’ancienne implémentation conservée pour référence ; il dépend d’entités/repositories/DTOs non présents. Pour le réactiver, ajouter les dépendances (JPA, DTOs, repositories, etc.) puis renommer le fichier en `.java`.
