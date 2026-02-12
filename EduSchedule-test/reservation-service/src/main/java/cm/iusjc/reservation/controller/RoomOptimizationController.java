package cm.iusjc.reservation.controller;

import cm.iusjc.reservation.dto.*;
import cm.iusjc.reservation.service.RoomOptimizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/room-optimization")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class RoomOptimizationController {
    
    private final RoomOptimizationService optimizationService;
    
    /**
     * Suggère les salles optimales selon les critères
     */
    @PostMapping("/suggest")
    public ResponseEntity<List<RoomSuggestionDTO>> suggestOptimalRooms(
            @Valid @RequestBody OptimizationCriteriaDTO criteria) {
        try {
            log.info("Suggesting optimal rooms for criteria: {}", criteria);
            List<RoomSuggestionDTO> suggestions = optimizationService.suggestOptimalRooms(criteria);
            return ResponseEntity.ok(suggestions);
        } catch (Exception e) {
            log.error("Error suggesting optimal rooms: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Suggère des salles pour un type de cours spécifique
     */
    @GetMapping("/suggest-for-course")
    public ResponseEntity<List<RoomSuggestionDTO>> suggestForCourse(
            @RequestParam String courseType,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam Integer capacity,
            @RequestParam(defaultValue = "5") Integer maxSuggestions) {
        try {
            log.info("Suggesting rooms for course type: {} at {}", courseType, startTime);
            
            OptimizationCriteriaDTO criteria = OptimizationCriteriaDTO.builder()
                    .startTime(startTime)
                    .endTime(endTime)
                    .minCapacity(capacity)
                    .courseType(courseType)
                    .maxSuggestions(maxSuggestions)
                    .build();
                    
            List<RoomSuggestionDTO> suggestions = optimizationService.suggestOptimalRooms(criteria);
            return ResponseEntity.ok(suggestions);
        } catch (Exception e) {
            log.error("Error suggesting rooms for course: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Suggestions d'urgence avec critères assouplis
     */
    @GetMapping("/emergency-suggest")
    public ResponseEntity<List<RoomSuggestionDTO>> emergencySuggest(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam Integer minCapacity) {
        try {
            log.info("Emergency room suggestions for {} at {}", minCapacity, startTime);
            
            OptimizationCriteriaDTO criteria = OptimizationCriteriaDTO.builder()
                    .startTime(startTime)
                    .endTime(endTime)
                    .minCapacity(minCapacity)
                    .maxSuggestions(10)
                    .emergencyMode(true)
                    .build();
                    
            List<RoomSuggestionDTO> suggestions = optimizationService.suggestOptimalRooms(criteria);
            return ResponseEntity.ok(suggestions);
        } catch (Exception e) {
            log.error("Error in emergency suggestions: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Résolution de conflits avec alternatives
     */
    @GetMapping("/resolve-conflict/{reservationId}")
    public ResponseEntity<List<ConflictResolutionDTO>> resolveConflict(@PathVariable Long reservationId) {
        try {
            log.info("Resolving conflict for reservation: {}", reservationId);
            List<ConflictResolutionDTO> resolutions = optimizationService.findConflictResolutions(reservationId);
            return ResponseEntity.ok(resolutions);
        } catch (Exception e) {
            log.error("Error resolving conflict: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Optimisation globale de l'utilisation des salles
     */
    @GetMapping("/optimize-global")
    public ResponseEntity<Map<String, Object>> optimizeGlobalRoomUsage(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startPeriod,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endPeriod) {
        try {
            log.info("Global optimization for period: {} to {}", startPeriod, endPeriod);
            Map<String, Object> optimization = optimizationService.optimizeGlobalRoomUsage(startPeriod, endPeriod);
            return ResponseEntity.ok(optimization);
        } catch (Exception e) {
            log.error("Error in global optimization: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Optimisation pour la semaine courante
     */
    @GetMapping("/optimize-current-week")
    public ResponseEntity<Map<String, Object>> optimizeCurrentWeek() {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startOfWeek = now.minusDays(now.getDayOfWeek().getValue() - 1).withHour(0).withMinute(0);
            LocalDateTime endOfWeek = startOfWeek.plusDays(6).withHour(23).withMinute(59);
            
            Map<String, Object> optimization = optimizationService.optimizeGlobalRoomUsage(startOfWeek, endOfWeek);
            return ResponseEntity.ok(optimization);
        } catch (Exception e) {
            log.error("Error optimizing current week: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Statistiques d'optimisation
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getOptimizationStats() {
        try {
            Map<String, Object> stats = optimizationService.getOptimizationStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting optimization stats: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Calcul du score d'efficacité d'une salle
     */
    @GetMapping("/efficiency-score/{roomId}")
    public ResponseEntity<Map<String, Object>> getRoomEfficiencyScore(
            @PathVariable Long roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            double score = optimizationService.calculateRoomEfficiencyScore(roomId, startDate, endDate);
            String rating = getRatingFromScore(score);
            
            return ResponseEntity.ok(Map.of(
                    "roomId", roomId,
                    "efficiencyScore", score,
                    "rating", rating,
                    "period", Map.of(
                            "startDate", startDate,
                            "endDate", endDate
                    )
            ));
        } catch (Exception e) {
            log.error("Error calculating efficiency score: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Recommandations d'optimisation
     */
    @GetMapping("/recommendations")
    public ResponseEntity<Map<String, Object>> getOptimizationRecommendations(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            List<Map<String, Object>> recommendations = optimizationService.getOptimizationRecommendations(startDate, endDate);
            Map<String, Object> response = new HashMap<>();
            response.put("recommendations", recommendations);
            response.put("count", recommendations.size());
            response.put("period", Map.of("start", startDate, "end", endDate));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting recommendations: {}", e.getMessage());
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
            Map<String, Object> analysis = optimizationService.getComparativeAnalysis(startDate, endDate);
            return ResponseEntity.ok(analysis);
        } catch (Exception e) {
            log.error("Error in comparative analysis: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Recherche de salle optimale pour une demande
     */
    @PostMapping("/find-optimal-room")
    public ResponseEntity<Map<String, Object>> findOptimalRoom(
            @Valid @RequestBody ReservationRequestDTO request) {
        try {
            RoomSuggestionDTO result = optimizationService.findOptimalRoom(request);
            if (result == null) {
                return ResponseEntity.ok(Map.of(
                        "success", false,
                        "message", "Aucune salle optimale trouvée"
                ));
            }
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", result
            ));
        } catch (Exception e) {
            log.error("Error finding optimal room: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    private String getRatingFromScore(double score) {
        if (score >= 90) return "EXCELLENT";
        if (score >= 75) return "GOOD";
        if (score >= 60) return "AVERAGE";
        if (score >= 40) return "POOR";
        return "CRITICAL";
    }
}