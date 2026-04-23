package cm.iusjc.reservation.controller;

import cm.iusjc.reservation.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Slf4j
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    /**
     * Récupère les statistiques du tableau de bord
     */
    @GetMapping("/dashboard-stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats(
            @RequestParam(defaultValue = "week") String period) {
        try {
            log.info("Récupération statistiques dashboard pour période: {}", period);
            
            Map<String, Object> stats = analyticsService.getDashboardStats(period);
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Erreur récupération statistiques dashboard: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Erreur récupération statistiques: " + e.getMessage()));
        }
    }

    /**
     * Récupère les données d'occupation par salle
     */
    @GetMapping("/room-occupancy")
    public ResponseEntity<List<Map<String, Object>>> getRoomOccupancy(
            @RequestParam(defaultValue = "week") String period) {
        try {
            log.info("Récupération occupation salles pour période: {}", period);
            
            List<Map<String, Object>> occupancy = analyticsService.getRoomOccupancy(period);
            
            return ResponseEntity.ok(occupancy);
        } catch (Exception e) {
            log.error("Erreur récupération occupation salles: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Récupère les données d'occupation par heure
     */
    @GetMapping("/hourly-occupancy")
    public ResponseEntity<List<Map<String, Object>>> getHourlyOccupancy(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date) {
        try {
            log.info("Récupération occupation horaire pour: {}", date.toLocalDate());
            
            List<Map<String, Object>> hourlyData = analyticsService.getHourlyOccupancy(date);
            
            return ResponseEntity.ok(hourlyData);
        } catch (Exception e) {
            log.error("Erreur récupération occupation horaire: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Récupère les données hebdomadaires
     */
    @GetMapping("/weekly-data")
    public ResponseEntity<List<Map<String, Object>>> getWeeklyData(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate) {
        try {
            log.info("Récupération données hebdomadaires depuis: {}", startDate.toLocalDate());
            
            List<Map<String, Object>> weeklyData = analyticsService.getWeeklyData(startDate);
            
            return ResponseEntity.ok(weeklyData);
        } catch (Exception e) {
            log.error("Erreur récupération données hebdomadaires: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Récupère la répartition par type de salle
     */
    @GetMapping("/room-type-distribution")
    public ResponseEntity<List<Map<String, Object>>> getRoomTypeDistribution() {
        try {
            log.info("Récupération répartition types de salles");
            
            List<Map<String, Object>> distribution = analyticsService.getRoomTypeDistribution();
            
            return ResponseEntity.ok(distribution);
        } catch (Exception e) {
            log.error("Erreur récupération répartition types: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Endpoint de santé pour les analytics
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "Analytics Service",
            "timestamp", LocalDateTime.now().toString()
        ));
    }

    /**
     * Récupère un résumé des métriques principales
     */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary(
            @RequestParam(defaultValue = "week") String period) {
        try {
            log.info("Récupération résumé analytics pour période: {}", period);
            
            Map<String, Object> dashboardStats = analyticsService.getDashboardStats(period);
            List<Map<String, Object>> roomOccupancy = analyticsService.getRoomOccupancy(period);
            List<Map<String, Object>> roomTypes = analyticsService.getRoomTypeDistribution();
            
            // Calculer quelques métriques supplémentaires
            long excellentRooms = roomOccupancy.stream()
                .mapToLong(room -> "excellent".equals(room.get("status")) ? 1 : 0)
                .sum();
            
            long poorRooms = roomOccupancy.stream()
                .mapToLong(room -> "poor".equals(room.get("status")) ? 1 : 0)
                .sum();
            
            return ResponseEntity.ok(Map.of(
                "period", period,
                "dashboardStats", dashboardStats,
                "roomPerformance", Map.of(
                    "excellentRooms", excellentRooms,
                    "poorRooms", poorRooms,
                    "totalRooms", roomOccupancy.size()
                ),
                "roomTypes", roomTypes,
                "generatedAt", LocalDateTime.now().toString()
            ));
        } catch (Exception e) {
            log.error("Erreur récupération résumé analytics: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Erreur récupération résumé: " + e.getMessage()));
        }
    }
}