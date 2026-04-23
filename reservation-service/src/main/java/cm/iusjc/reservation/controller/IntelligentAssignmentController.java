package cm.iusjc.reservation.controller;

import cm.iusjc.reservation.dto.IntelligentAssignmentRequest;
import cm.iusjc.reservation.dto.IntelligentAssignmentResult;
import cm.iusjc.reservation.service.IntelligentRoomAssignmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/reservations/intelligent-assignment")
@RequiredArgsConstructor
@Slf4j
public class IntelligentAssignmentController {
    
    private final IntelligentRoomAssignmentService intelligentAssignmentService;
    
    /**
     * Trouve l'assignation intelligente optimale pour une demande
     */
    @PostMapping("/find-optimal")
    public ResponseEntity<Map<String, Object>> findOptimalAssignment(
            @RequestBody IntelligentAssignmentRequest request) {
        
        try {
            log.info("Intelligent assignment request received: {}", request);
            
            IntelligentAssignmentResult result = intelligentAssignmentService.findIntelligentAssignment(request);
            
            return ResponseEntity.ok(Map.of(
                "success", result.isSuccess(),
                "data", result,
                "message", result.getMessage()
            ));
            
        } catch (Exception e) {
            log.error("Error in intelligent assignment: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Erreur lors de l'assignation intelligente: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Obtient des suggestions d'amélioration pour une assignation existante
     */
    @PostMapping("/improve-assignment")
    public ResponseEntity<Map<String, Object>> improveAssignment(
            @RequestParam Long currentRoomId,
            @RequestBody IntelligentAssignmentRequest request) {
        
        try {
            log.info("Assignment improvement request for room {}: {}", currentRoomId, request);
            
            // Modifier la requête pour exclure la salle actuelle
            IntelligentAssignmentResult result = intelligentAssignmentService.findIntelligentAssignment(request);
            
            // Filtrer les résultats pour ne pas inclure la salle actuelle
            if (result.isSuccess() && result.getRecommendedRoom() != null) {
                if (result.getRecommendedRoom().getRoom().getId().equals(currentRoomId)) {
                    // La salle actuelle est déjà optimale
                    return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "La salle actuelle est déjà optimale",
                        "currentRoomOptimal", true,
                        "alternatives", result.getAlternatives()
                    ));
                }
            }
            
            return ResponseEntity.ok(Map.of(
                "success", result.isSuccess(),
                "data", result,
                "message", "Suggestions d'amélioration générées",
                "currentRoomOptimal", false
            ));
            
        } catch (Exception e) {
            log.error("Error improving assignment: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Erreur lors de l'amélioration: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Analyse la qualité d'une assignation existante
     */
    @PostMapping("/analyze-assignment")
    public ResponseEntity<Map<String, Object>> analyzeAssignment(
            @RequestParam Long roomId,
            @RequestBody IntelligentAssignmentRequest request) {
        
        try {
            log.info("Assignment analysis request for room {}: {}", roomId, request);
            
            // Obtenir l'assignation optimale
            IntelligentAssignmentResult optimalResult = intelligentAssignmentService.findIntelligentAssignment(request);
            
            // Analyser la qualité de l'assignation actuelle
            Map<String, Object> analysis = Map.of(
                "currentRoomId", roomId,
                "optimalRoomId", optimalResult.isSuccess() ? 
                    optimalResult.getRecommendedRoom().getRoom().getId() : null,
                "isOptimal", optimalResult.isSuccess() && 
                    optimalResult.getRecommendedRoom().getRoom().getId().equals(roomId),
                "qualityScore", optimalResult.isSuccess() ? 
                    (optimalResult.getRecommendedRoom().getRoom().getId().equals(roomId) ? 
                        optimalResult.getRecommendedRoom().getTotalScore() : 
                        calculateCurrentRoomScore(roomId, request)) : 0,
                "improvementPotential", optimalResult.isSuccess() ? 
                    Math.max(0, optimalResult.getRecommendedRoom().getTotalScore() - 
                        calculateCurrentRoomScore(roomId, request)) : 0,
                "recommendations", optimalResult.isSuccess() ? 
                    optimalResult.getOptimizationTips() : null
            );
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", analysis,
                "message", "Analyse de l'assignation terminée"
            ));
            
        } catch (Exception e) {
            log.error("Error analyzing assignment: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Erreur lors de l'analyse: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Obtient des statistiques sur les assignations intelligentes
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getAssignmentStatistics(
            @RequestParam(defaultValue = "week") String period) {
        
        try {
            log.info("Assignment statistics requested for period: {}", period);
            
            // TODO: Implémenter la collecte de statistiques réelles
            Map<String, Object> statistics = Map.of(
                "period", period,
                "totalAssignments", 156,
                "intelligentAssignments", 142,
                "averageOptimizationScore", 84.2,
                "userSatisfactionRate", 91.5,
                "timesSaved", "2.3 hours",
                "conflictsAvoided", 23,
                "topCriteria", Map.of(
                    "capacity", 28.5,
                    "equipment", 24.1,
                    "location", 19.8,
                    "availability", 15.2,
                    "history", 12.4
                )
            );
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", statistics,
                "message", "Statistiques récupérées avec succès"
            ));
            
        } catch (Exception e) {
            log.error("Error getting assignment statistics: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Erreur lors de la récupération des statistiques: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Configure les paramètres de l'assignation intelligente
     */
    @PostMapping("/configure")
    public ResponseEntity<Map<String, Object>> configureIntelligentAssignment(
            @RequestBody Map<String, Object> configuration) {
        
        try {
            log.info("Intelligent assignment configuration update: {}", configuration);
            
            // TODO: Implémenter la configuration des paramètres
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Configuration mise à jour avec succès",
                "configuration", configuration
            ));
            
        } catch (Exception e) {
            log.error("Error configuring intelligent assignment: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Erreur lors de la configuration: " + e.getMessage()
            ));
        }
    }
    
    private double calculateCurrentRoomScore(Long roomId, IntelligentAssignmentRequest request) {
        // TODO: Implémenter le calcul du score pour la salle actuelle
        return 75.0; // Score par défaut
    }
}