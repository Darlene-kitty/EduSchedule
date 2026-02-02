package cm.iusjc.reservation.controller;

import cm.iusjc.reservation.dto.OptimizationResult;
import cm.iusjc.reservation.dto.OptimizationSuggestion;
import cm.iusjc.reservation.dto.ReservationRequest;
import cm.iusjc.reservation.entity.ReservationType;
import cm.iusjc.reservation.service.RoomOptimizationService;
import cm.iusjc.reservation.service.RoomOptimizationService.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reservations/optimization")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class RoomOptimizationController {

    private final RoomOptimizationService optimizationService;

    /**
     * Trouve la salle optimale pour une demande
     */
    @PostMapping("/find-optimal-room")
    public ResponseEntity<OptimizationResult> findOptimalRoom(@RequestBody Map<String, Object> requestData) {
        try {
            log.info("Recherche salle optimale");
            
            ReservationRequest request = buildReservationRequest(requestData);
            OptimizationResult result = optimizationService.findOptimalRoom(request);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Erreur recherche salle optimale: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Optimise l'utilisation des salles pour une période
     */
    @GetMapping("/optimize-usage")
    public ResponseEntity<List<OptimizationSuggestion>> optimizeRoomUsage(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            log.info("Optimisation utilisation salles du {} au {}", startDate, endDate);
            
            List<OptimizationSuggestion> suggestions = optimizationService.optimizeRoomUsage(startDate, endDate);
            
            return ResponseEntity.ok(suggestions);
        } catch (Exception e) {
            log.error("Erreur optimisation utilisation: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Calcule le score d'efficacité d'une salle
     */
    @GetMapping("/efficiency-score/{resourceId}")
    public ResponseEntity<Map<String, Object>> getRoomEfficiencyScore(
            @PathVariable Long resourceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            log.info("Calcul score efficacité salle ID: {}", resourceId);
            
            double score = optimizationService.calculateRoomEfficiencyScore(resourceId, startDate, endDate);
            
            return ResponseEntity.ok(Map.of(
                "resourceId", resourceId,
                "efficiencyScore", score,
                "rating", getRatingFromScore(score),
                "period", Map.of(
                    "startDate", startDate.toString(),
                    "endDate", endDate.toString()
                )
            ));
        } catch (Exception e) {
            log.error("Erreur calcul score efficacité: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Obtient les recommandations d'optimisation pour toutes les salles
     */
    @GetMapping("/recommendations")
    public ResponseEntity<Map<String, Object>> getOptimizationRecommendations(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            log.info("Génération recommandations d'optimisation");
            
            List<OptimizationSuggestion> suggestions = optimizationService.optimizeRoomUsage(startDate, endDate);
            
            // Grouper les suggestions par type
            Map<String, List<OptimizationSuggestion>> groupedSuggestions = suggestions.stream()
                .collect(java.util.stream.Collectors.groupingBy(OptimizationSuggestion::getType));
            
            return ResponseEntity.ok(Map.of(
                "period", Map.of(
                    "startDate", startDate.toString(),
                    "endDate", endDate.toString()
                ),
                "totalSuggestions", suggestions.size(),
                "suggestionsByType", groupedSuggestions,
                "summary", Map.of(
                    "underutilized", groupedSuggestions.getOrDefault("UNDERUTILIZED", List.of()).size(),
                    "overutilized", groupedSuggestions.getOrDefault("OVERUTILIZED", List.of()).size(),
                    "reorganization", groupedSuggestions.getOrDefault("REORGANIZATION", List.of()).size()
                )
            ));
        } catch (Exception e) {
            log.error("Erreur génération recommandations: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Analyse comparative des salles
     */
    @GetMapping("/comparative-analysis")
    public ResponseEntity<Map<String, Object>> getComparativeAnalysis(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            log.info("Analyse comparative des salles");
            
            // Cette méthode nécessiterait une implémentation plus complexe
            // Pour l'instant, retourner une structure de base
            
            return ResponseEntity.ok(Map.of(
                "period", Map.of(
                    "startDate", startDate.toString(),
                    "endDate", endDate.toString()
                ),
                "analysis", "Analyse comparative en cours de développement",
                "status", "PARTIAL_IMPLEMENTATION"
            ));
        } catch (Exception e) {
            log.error("Erreur analyse comparative: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Construit une demande de réservation depuis les données reçues
     */
    private ReservationRequest buildReservationRequest(Map<String, Object> requestData) {
        ReservationRequest request = new ReservationRequest();
        
        request.setStartTime(LocalDateTime.parse((String) requestData.get("startTime")));
        request.setEndTime(LocalDateTime.parse((String) requestData.get("endTime")));
        request.setExpectedAttendees((Integer) requestData.getOrDefault("expectedAttendees", 1));
        
        String typeStr = (String) requestData.getOrDefault("type", "COURSE");
        request.setType(ReservationType.valueOf(typeStr));
        
        @SuppressWarnings("unchecked")
        List<String> equipments = (List<String>) requestData.getOrDefault("requiredEquipments", List.of());
        request.setRequiredEquipments(equipments);
        
        return request;
    }

    /**
     * Convertit un score en rating textuel
     */
    private String getRatingFromScore(double score) {
        if (score >= 0.8) return "EXCELLENT";
        if (score >= 0.6) return "GOOD";
        if (score >= 0.4) return "AVERAGE";
        if (score >= 0.2) return "POOR";
        return "VERY_POOR";
    }
}