# Roadmap des Améliorations - User Service (Disponibilités)

## 🎯 Vision : Faire Évoluer User Service vers les Fonctionnalités Avancées

Actuellement, User Service contient une implémentation **basique mais fonctionnelle** des disponibilités. Voici les améliorations à ajouter progressivement pour atteindre le niveau du Teacher Availability Service.

---

## 📋 **PHASE 1 : Améliorations Immédiates (1-2 semaines)**

### 1.1 **Validation Avancée des Créneaux d'1 Heure**

#### Ajouter dans `TeacherAvailability.java` :
```java
@PrePersist
@PreUpdate
private void validateTimeSlot() {
    // Validation stricte des créneaux d'1 heure
    Duration duration = Duration.between(startTime, endTime);
    if (duration.toMinutes() != 60) {
        throw new IllegalArgumentException("Les créneaux doivent durer exactement 1 heure");
    }
    
    // Validation des heures de travail (8h-18h)
    if (startTime.isBefore(LocalTime.of(8, 0)) || endTime.isAfter(LocalTime.of(18, 0))) {
        throw new IllegalArgumentException("Les créneaux doivent être entre 8h et 18h");
    }
    
    // Exclusion automatique pause déjeuner (12h-14h)
    if ((startTime.isBefore(LocalTime.of(14, 0)) && endTime.isAfter(LocalTime.of(12, 0)))) {
        throw new IllegalArgumentException("Pause déjeuner (12h-14h) non autorisée");
    }
}

// Méthode utilitaire
public boolean isOneHourSlot() {
    return Duration.between(startTime, endTime).toMinutes() == 60;
}
```

### 1.2 **Détection de Conflits Améliorée**

#### Créer `ConflictDetectionService.java` :
```java
@Service
@RequiredArgsConstructor
public class ConflictDetectionService {
    
    private final TeacherAvailabilityRepository availabilityRepository;
    
    public List<ConflictInfo> detectConflicts(Long teacherId, TeacherAvailabilityRequest request) {
        List<ConflictInfo> conflicts = new ArrayList<>();
        
        // 1. Conflits de chevauchement
        List<TeacherAvailability> overlapping = availabilityRepository
            .findConflictingAvailabilities(teacherId, request.getDayOfWeek(), 
                                         request.getStartTime(), request.getEndTime(), null);
        
        overlapping.forEach(availability -> {
            conflicts.add(new ConflictInfo(
                ConflictType.OVERLAP,
                "Chevauchement avec créneau existant",
                availability.getId()
            ));
        });
        
        // 2. Vérification limites horaires quotidiennes
        long dailyHours = calculateDailyHours(teacherId, request.getDayOfWeek());
        if (dailyHours >= 8) {
            conflicts.add(new ConflictInfo(
                ConflictType.DAILY_LIMIT,
                "Limite de 8h/jour dépassée",
                null
            ));
        }
        
        // 3. Vérification limites hebdomadaires
        long weeklyHours = calculateWeeklyHours(teacherId);
        if (weeklyHours >= 40) {
            conflicts.add(new ConflictInfo(
                ConflictType.WEEKLY_LIMIT,
                "Limite de 40h/semaine dépassée",
                null
            ));
        }
        
        return conflicts;
    }
    
    public enum ConflictType {
        OVERLAP, DAILY_LIMIT, WEEKLY_LIMIT, INTER_SCHOOL, TRAVEL_TIME
    }
    
    @Data
    public static class ConflictInfo {
        private ConflictType type;
        private String message;
        private Long conflictingAvailabilityId;
    }
}
```

### 1.3 **Statistiques et Métriques**

#### Ajouter dans `TeacherAvailabilityService.java` :
```java
public TeacherAvailabilityStats getTeacherStats(Long teacherId) {
    List<TeacherAvailability> availabilities = availabilityRepository
        .findByTeacherIdAndActiveTrue(teacherId);
    
    return TeacherAvailabilityStats.builder()
        .totalSlots(availabilities.size())
        .availableSlots(countByType(availabilities, AvailabilityType.AVAILABLE))
        .preferredSlots(countByType(availabilities, AvailabilityType.PREFERRED))
        .unavailableSlots(countByType(availabilities, AvailabilityType.UNAVAILABLE))
        .weeklyHours(calculateWeeklyHours(availabilities))
        .dailyAverageHours(calculateDailyAverage(availabilities))
        .build();
}

public GlobalAvailabilityStats getGlobalStats() {
    return GlobalAvailabilityStats.builder()
        .totalActiveAvailabilities(availabilityRepository.countByActiveTrue())
        .teachersWithAvailabilities(availabilityRepository.countDistinctTeachers())
        .averageHoursPerTeacher(availabilityRepository.getAverageHoursPerTeacher())
        .mostActiveDay(availabilityRepository.getMostActiveDay())
        .build();
}
```

---

## 📋 **PHASE 2 : Fonctionnalités Avancées (2-4 semaines)**

### 2.1 **Gestion des Périodes et Statuts**

#### Étendre `TeacherAvailability.java` :
```java
@Entity
public class TeacherAvailability {
    // Champs existants...
    
    // NOUVEAUX CHAMPS
    @Column(name = "effective_date")
    private LocalDate effectiveDate; // Date de début de validité
    
    @Column(name = "end_date")
    private LocalDate endDate; // Date de fin (null = indéfini)
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private AvailabilityStatus status = AvailabilityStatus.ACTIVE;
    
    @Column(name = "max_hours_per_day")
    private Integer maxHoursPerDay = 8;
    
    @Column(name = "max_hours_per_week")
    private Integer maxHoursPerWeek = 40;
    
    @Column(name = "created_by")
    private Long createdBy;
    
    // NOUVELLES MÉTHODES
    public boolean isActiveOn(LocalDate date) {
        if (status != AvailabilityStatus.ACTIVE) return false;
        if (date.isBefore(effectiveDate)) return false;
        return endDate == null || !date.isAfter(endDate);
    }
    
    public enum AvailabilityStatus {
        ACTIVE,      // Actif
        INACTIVE,    // Inactif temporairement
        SUSPENDED,   // Suspendu (congé, maladie)
        ARCHIVED     // Archivé (historique)
    }
}
```

### 2.2 **Gestion Multi-Écoles Avancée**

#### Créer `MultiSchoolAvailabilityService.java` :
```java
@Service
@RequiredArgsConstructor
public class MultiSchoolAvailabilityService {
    
    private final TeacherAvailabilityRepository availabilityRepository;
    private final SchoolServiceClient schoolServiceClient;
    
    public List<InterSchoolConflict> detectInterSchoolConflicts(Long teacherId, 
                                                               TeacherAvailabilityRequest request) {
        List<InterSchoolConflict> conflicts = new ArrayList<>();
        
        // Récupérer toutes les disponibilités du jour pour cet enseignant
        List<TeacherAvailability> dayAvailabilities = availabilityRepository
            .findByTeacherIdAndDayOfWeekAndActiveTrue(teacherId, request.getDayOfWeek());
        
        for (TeacherAvailability existing : dayAvailabilities) {
            if (existing.getSchoolId() != null && 
                !existing.getSchoolId().equals(request.getSchoolId())) {
                
                // Calculer le temps de déplacement entre écoles
                int travelTime = calculateTravelTime(existing.getSchoolId(), request.getSchoolId());
                
                // Vérifier si le temps de déplacement est suffisant
                LocalTime existingEnd = existing.getEndTime();
                LocalTime newStart = request.getStartTime();
                
                long minutesBetween = Duration.between(existingEnd, newStart).toMinutes();
                
                if (minutesBetween < travelTime) {
                    conflicts.add(new InterSchoolConflict(
                        existing.getSchoolId(),
                        request.getSchoolId(),
                        travelTime,
                        (int) minutesBetween,
                        "Temps de déplacement insuffisant entre écoles"
                    ));
                }
            }
        }
        
        return conflicts;
    }
    
    private int calculateTravelTime(Long fromSchoolId, Long toSchoolId) {
        // Logique de calcul du temps de déplacement
        // Peut utiliser une API de géolocalisation ou une table de distances
        return 30; // 30 minutes par défaut
    }
}
```

### 2.3 **Notifications Automatiques**

#### Créer `AvailabilityNotificationService.java` :
```java
@Service
@RequiredArgsConstructor
public class AvailabilityNotificationService {
    
    private final NotificationServiceClient notificationClient;
    private final UserRepository userRepository;
    
    @EventListener
    @Async
    public void onAvailabilityChanged(AvailabilityChangedEvent event) {
        TeacherAvailability availability = event.getAvailability();
        ChangeType changeType = event.getChangeType();
        
        // Notifier l'enseignant
        User teacher = userRepository.findById(availability.getTeacherId()).orElse(null);
        if (teacher != null) {
            sendTeacherNotification(teacher, availability, changeType);
        }
        
        // Notifier les administrateurs
        List<User> admins = userRepository.findByRole(Role.ADMIN);
        admins.forEach(admin -> sendAdminNotification(admin, availability, changeType));
        
        // Notifier les services dépendants (scheduling, etc.)
        publishToSchedulingService(availability, changeType);
    }
    
    private void sendTeacherNotification(User teacher, TeacherAvailability availability, ChangeType changeType) {
        String message = buildNotificationMessage(availability, changeType);
        
        // Email
        notificationClient.sendEmail(teacher.getEmail(), 
            "Modification de vos disponibilités", message);
        
        // SMS si numéro disponible
        if (teacher.getPhoneNumber() != null) {
            notificationClient.sendSMS(teacher.getPhoneNumber(), 
                buildSMSMessage(availability, changeType));
        }
        
        // Notification push
        notificationClient.sendPushNotification(teacher.getId(), 
            "Disponibilités", message);
    }
}
```

---

## 📋 **PHASE 3 : Intelligence et Optimisation (4-6 semaines)**

### 3.1 **Algorithmes d'Optimisation**

#### Créer `AvailabilityOptimizationService.java` :
```java
@Service
@RequiredArgsConstructor
public class AvailabilityOptimizationService {
    
    public List<OptimalSlotSuggestion> suggestOptimalSlots(Long teacherId, 
                                                          OptimizationCriteria criteria) {
        List<OptimalSlotSuggestion> suggestions = new ArrayList<>();
        
        // 1. Analyser les préférences historiques
        Map<DayOfWeek, List<LocalTime>> preferences = analyzeHistoricalPreferences(teacherId);
        
        // 2. Analyser la charge de travail actuelle
        WorkloadAnalysis workload = analyzeCurrentWorkload(teacherId);
        
        // 3. Considérer les contraintes multi-écoles
        List<SchoolConstraint> schoolConstraints = getSchoolConstraints(teacherId);
        
        // 4. Générer des suggestions optimales
        for (DayOfWeek day : DayOfWeek.values()) {
            List<LocalTime> availableSlots = findAvailableSlots(teacherId, day);
            
            for (LocalTime slot : availableSlots) {
                double score = calculateOptimalityScore(slot, day, preferences, workload, schoolConstraints);
                
                if (score > criteria.getMinScore()) {
                    suggestions.add(new OptimalSlotSuggestion(
                        day, slot, slot.plusHours(1), score, 
                        generateReasoningExplanation(score, preferences, workload)
                    ));
                }
            }
        }
        
        return suggestions.stream()
            .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
            .limit(criteria.getMaxSuggestions())
            .collect(Collectors.toList());
    }
    
    private double calculateOptimalityScore(LocalTime slot, DayOfWeek day, 
                                          Map<DayOfWeek, List<LocalTime>> preferences,
                                          WorkloadAnalysis workload,
                                          List<SchoolConstraint> constraints) {
        double score = 0.0;
        
        // Facteur préférence historique (40%)
        if (preferences.get(day).contains(slot)) {
            score += 0.4;
        }
        
        // Facteur équilibrage charge de travail (30%)
        if (workload.getDailyHours(day) < 6) {
            score += 0.3;
        }
        
        // Facteur optimisation déplacements (20%)
        score += calculateTravelOptimizationScore(slot, day, constraints) * 0.2;
        
        // Facteur créneaux préférés généraux (10%)
        if (isPreferredTimeSlot(slot)) {
            score += 0.1;
        }
        
        return score;
    }
}
```

### 3.2 **Analytics et Rapports**

#### Créer `AvailabilityAnalyticsService.java` :
```java
@Service
@RequiredArgsConstructor
public class AvailabilityAnalyticsService {
    
    public AvailabilityReport generateReport(Long teacherId, LocalDate startDate, LocalDate endDate) {
        return AvailabilityReport.builder()
            .teacherId(teacherId)
            .period(new DateRange(startDate, endDate))
            .utilizationRate(calculateUtilizationRate(teacherId, startDate, endDate))
            .preferredSlotsUsage(analyzePreferredSlotsUsage(teacherId, startDate, endDate))
            .conflictHistory(getConflictHistory(teacherId, startDate, endDate))
            .optimizationSuggestions(generateOptimizationSuggestions(teacherId))
            .trendAnalysis(analyzeTrends(teacherId, startDate, endDate))
            .build();
    }
    
    public GlobalAvailabilityInsights getGlobalInsights() {
        return GlobalAvailabilityInsights.builder()
            .totalTeachers(getTotalTeachersCount())
            .averageAvailabilityPerTeacher(getAverageAvailabilityPerTeacher())
            .peakHours(identifyPeakHours())
            .underutilizedSlots(identifyUnderutilizedSlots())
            .schoolDistribution(getAvailabilityBySchool())
            .seasonalTrends(analyzeSeasonalTrends())
            .build();
    }
    
    public List<PredictiveInsight> generatePredictiveInsights(Long teacherId) {
        List<PredictiveInsight> insights = new ArrayList<>();
        
        // Prédiction de surcharge
        if (predictOverload(teacherId)) {
            insights.add(new PredictiveInsight(
                InsightType.OVERLOAD_WARNING,
                "Risque de surcharge détecté pour les 2 prochaines semaines",
                Severity.HIGH,
                generateOverloadRecommendations(teacherId)
            ));
        }
        
        // Prédiction de sous-utilisation
        if (predictUnderutilization(teacherId)) {
            insights.add(new PredictiveInsight(
                InsightType.UNDERUTILIZATION,
                "Créneaux sous-utilisés détectés",
                Severity.MEDIUM,
                generateUtilizationRecommendations(teacherId)
            ));
        }
        
        return insights;
    }
}
```

---

## 📋 **PHASE 4 : Intégrations Avancées (6-8 semaines)**

### 4.1 **Intégration avec Scheduling Service**

#### Créer `SchedulingIntegrationService.java` :
```java
@Service
@RequiredArgsConstructor
public class SchedulingIntegrationService {
    
    private final SchedulingServiceClient schedulingClient;
    private final TeacherAvailabilityRepository availabilityRepository;
    
    @EventListener
    public void onScheduleCreated(ScheduleCreatedEvent event) {
        Schedule schedule = event.getSchedule();
        
        // Marquer automatiquement le créneau comme occupé
        markSlotAsBusy(schedule.getTeacherId(), schedule.getDayOfWeek(), 
                      schedule.getStartTime(), schedule.getEndTime());
        
        // Vérifier les conflits potentiels
        List<ConflictInfo> conflicts = detectSchedulingConflicts(schedule);
        if (!conflicts.isEmpty()) {
            publishConflictAlert(schedule, conflicts);
        }
    }
    
    public List<AvailableSlotForScheduling> getAvailableSlotsForScheduling(
            Long teacherId, LocalDate startDate, LocalDate endDate) {
        
        List<TeacherAvailability> availabilities = availabilityRepository
            .findAvailableInPeriod(teacherId, startDate, endDate);
        
        return availabilities.stream()
            .filter(a -> a.getAvailabilityType() == AvailabilityType.AVAILABLE)
            .map(this::mapToSchedulingSlot)
            .collect(Collectors.toList());
    }
    
    public boolean canScheduleAt(Long teacherId, LocalDateTime startTime, LocalDateTime endTime) {
        // Vérification disponibilité
        boolean isAvailable = isTeacherAvailable(teacherId, startTime, endTime);
        
        // Vérification conflits multi-écoles
        boolean hasInterSchoolConflicts = hasInterSchoolConflicts(teacherId, startTime, endTime);
        
        // Vérification limites horaires
        boolean withinLimits = isWithinHourlyLimits(teacherId, startTime, endTime);
        
        return isAvailable && !hasInterSchoolConflicts && withinLimits;
    }
}
```

### 4.2 **API REST Avancée**

#### Étendre `TeacherAvailabilityController.java` :
```java
@RestController
@RequestMapping("/api/teacher-availability")
public class TeacherAvailabilityController {
    // Méthodes existantes...
    
    // NOUVELLES MÉTHODES AVANCÉES
    
    @GetMapping("/teacher/{teacherId}/stats")
    public ResponseEntity<TeacherAvailabilityStats> getTeacherStats(@PathVariable Long teacherId) {
        TeacherAvailabilityStats stats = availabilityService.getTeacherStats(teacherId);
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/teacher/{teacherId}/optimization")
    public ResponseEntity<List<OptimalSlotSuggestion>> getOptimizationSuggestions(
            @PathVariable Long teacherId,
            @RequestParam(defaultValue = "5") int maxSuggestions) {
        
        OptimizationCriteria criteria = OptimizationCriteria.builder()
            .maxSuggestions(maxSuggestions)
            .minScore(0.5)
            .build();
            
        List<OptimalSlotSuggestion> suggestions = optimizationService
            .suggestOptimalSlots(teacherId, criteria);
        return ResponseEntity.ok(suggestions);
    }
    
    @PostMapping("/teacher/{teacherId}/bulk")
    public ResponseEntity<BulkOperationResult> bulkCreateAvailabilities(
            @PathVariable Long teacherId,
            @RequestBody BulkAvailabilityRequest request) {
        
        BulkOperationResult result = availabilityService.bulkCreateAvailabilities(teacherId, request);
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/teacher/{teacherId}/conflicts")
    public ResponseEntity<List<ConflictInfo>> getConflicts(@PathVariable Long teacherId) {
        List<ConflictInfo> conflicts = conflictDetectionService.detectAllConflicts(teacherId);
        return ResponseEntity.ok(conflicts);
    }
    
    @PostMapping("/teacher/{teacherId}/resolve-conflicts")
    public ResponseEntity<ConflictResolutionResult> resolveConflicts(
            @PathVariable Long teacherId,
            @RequestBody ConflictResolutionRequest request) {
        
        ConflictResolutionResult result = conflictDetectionService
            .resolveConflicts(teacherId, request);
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/analytics/global")
    public ResponseEntity<GlobalAvailabilityInsights> getGlobalAnalytics() {
        GlobalAvailabilityInsights insights = analyticsService.getGlobalInsights();
        return ResponseEntity.ok(insights);
    }
    
    @GetMapping("/teacher/{teacherId}/report")
    public ResponseEntity<AvailabilityReport> generateReport(
            @PathVariable Long teacherId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        
        AvailabilityReport report = analyticsService.generateReport(teacherId, startDate, endDate);
        return ResponseEntity.ok(report);
    }
}
```

---

## 🎯 **Priorités et Planning**

### **🔥 URGENT (Semaine 1-2)**
1. ✅ Validation stricte créneaux 1h
2. ✅ Détection conflits basique
3. ✅ Statistiques simples
4. ✅ Tests d'intégration

### **🚀 IMPORTANT (Semaine 3-4)**
1. Gestion périodes et statuts
2. Multi-écoles avancé
3. Notifications automatiques
4. Cache optimisé

### **💡 ÉVOLUTION (Semaine 5-8)**
1. Algorithmes d'optimisation
2. Analytics et rapports
3. Intégration scheduling
4. API REST complète

---

## 📊 **Métriques de Succès**

### **Phase 1 :**
- ✅ 100% des créneaux font exactement 1h
- ✅ 0 conflit de chevauchement
- ✅ Temps de réponse < 200ms

### **Phase 2 :**
- ✅ Support multi-écoles opérationnel
- ✅ Notifications automatiques actives
- ✅ Gestion des périodes fonctionnelle

### **Phase 3 :**
- ✅ Suggestions d'optimisation pertinentes
- ✅ Rapports analytics complets
- ✅ Prédictions fiables

### **Phase 4 :**
- ✅ Intégration scheduling transparente
- ✅ API REST complète
- ✅ Performance optimale

---

## 🎉 **Conclusion**

Cette roadmap vous permettra de faire évoluer progressivement User Service vers un système de gestion des disponibilités **de niveau entreprise**, tout en maintenant la **compatibilité** avec l'implémentation actuelle des créneaux d'1 heure.

**Commencez par la Phase 1** une fois que le système actuel fonctionne parfaitement ! 🚀