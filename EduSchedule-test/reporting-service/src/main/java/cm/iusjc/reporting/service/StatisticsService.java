package cm.iusjc.reporting.service;

import cm.iusjc.reporting.dto.StatisticsDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatisticsService {
    
    private final DataCollectionService dataCollectionService;
    
    /**
     * Génère les statistiques complètes du système
     */
    public StatisticsDTO generateSystemStatistics() {
        try {
            Map<String, Object> allData = dataCollectionService.collectAllData().block();
            
            if (allData == null || allData.isEmpty()) {
                log.warn("No data collected for statistics");
                return createEmptyStatistics();
            }
            
            StatisticsDTO statistics = new StatisticsDTO();
            
            // Données collectées
            List<Map<String, Object>> users = (List<Map<String, Object>>) allData.get("users");
            List<Map<String, Object>> courses = (List<Map<String, Object>>) allData.get("courses");
            List<Map<String, Object>> reservations = (List<Map<String, Object>>) allData.get("reservations");
            List<Map<String, Object>> schedules = (List<Map<String, Object>>) allData.get("schedules");
            List<Map<String, Object>> resources = (List<Map<String, Object>>) allData.get("resources");
            
            // Statistiques générales
            statistics.setTotalUsers(users != null ? (long) users.size() : 0L);
            statistics.setTotalCourses(courses != null ? (long) courses.size() : 0L);
            statistics.setTotalReservations(reservations != null ? (long) reservations.size() : 0L);
            statistics.setTotalResources(resources != null ? (long) resources.size() : 0L);
            
            // Répartition par rôle
            statistics.setUsersByRole(calculateUsersByRole(users));
            
            // Statistiques d'utilisation
            statistics.setReservationsByStatus(calculateReservationsByStatus(reservations));
            statistics.setCoursesByDepartment(calculateCoursesByDepartment(courses));
            statistics.setResourcesByType(calculateResourcesByType(resources));
            
            // Statistiques temporelles
            statistics.setReservationsByMonth(calculateReservationsByMonth(reservations));
            statistics.setCoursesByLevel(calculateCoursesByLevel(courses));
            
            // Taux d'occupation (calculs simplifiés)
            statistics.setAverageRoomOccupancy(calculateAverageRoomOccupancy(reservations, resources));
            statistics.setAverageCourseUtilization(calculateAverageCourseUtilization(courses, schedules));
            
            // Tendances
            statistics.setTrends(calculateTrends(allData));
            
            return statistics;
            
        } catch (Exception e) {
            log.error("Error generating system statistics: {}", e.getMessage(), e);
            return createEmptyStatistics();
        }
    }
    
    private StatisticsDTO createEmptyStatistics() {
        StatisticsDTO stats = new StatisticsDTO();
        stats.setTotalUsers(0L);
        stats.setTotalCourses(0L);
        stats.setTotalReservations(0L);
        stats.setTotalResources(0L);
        stats.setUsersByRole(new HashMap<>());
        stats.setReservationsByStatus(new HashMap<>());
        stats.setCoursesByDepartment(new HashMap<>());
        stats.setResourcesByType(new HashMap<>());
        stats.setReservationsByMonth(new HashMap<>());
        stats.setCoursesByLevel(new HashMap<>());
        stats.setAverageRoomOccupancy(0.0);
        stats.setAverageCourseUtilization(0.0);
        stats.setTrends(new HashMap<>());
        return stats;
    }
    
    private Map<String, Long> calculateUsersByRole(List<Map<String, Object>> users) {
        if (users == null || users.isEmpty()) {
            return Map.of("ADMIN", 0L, "TEACHER", 0L, "STUDENT", 0L);
        }
        
        return users.stream()
                .collect(Collectors.groupingBy(
                        user -> (String) user.getOrDefault("role", "UNKNOWN"),
                        Collectors.counting()
                ));
    }
    
    private Map<String, Long> calculateReservationsByStatus(List<Map<String, Object>> reservations) {
        if (reservations == null || reservations.isEmpty()) {
            return Map.of("PENDING", 0L, "APPROVED", 0L, "CANCELLED", 0L);
        }
        
        return reservations.stream()
                .collect(Collectors.groupingBy(
                        reservation -> (String) reservation.getOrDefault("status", "UNKNOWN"),
                        Collectors.counting()
                ));
    }
    
    private Map<String, Long> calculateCoursesByDepartment(List<Map<String, Object>> courses) {
        if (courses == null || courses.isEmpty()) {
            return new HashMap<>();
        }
        
        return courses.stream()
                .collect(Collectors.groupingBy(
                        course -> (String) course.getOrDefault("department", "Non spécifié"),
                        Collectors.counting()
                ));
    }
    
    private Map<String, Long> calculateResourcesByType(List<Map<String, Object>> resources) {
        if (resources == null || resources.isEmpty()) {
            return new HashMap<>();
        }
        
        return resources.stream()
                .collect(Collectors.groupingBy(
                        resource -> (String) resource.getOrDefault("type", "UNKNOWN"),
                        Collectors.counting()
                ));
    }
    
    private Map<String, Long> calculateReservationsByMonth(List<Map<String, Object>> reservations) {
        if (reservations == null || reservations.isEmpty()) {
            return new HashMap<>();
        }
        
        // Simplification : grouper par mois basé sur une date fictive
        return reservations.stream()
                .collect(Collectors.groupingBy(
                        reservation -> "2026-01", // Simplification pour l'exemple
                        Collectors.counting()
                ));
    }
    
    private Map<String, Long> calculateCoursesByLevel(List<Map<String, Object>> courses) {
        if (courses == null || courses.isEmpty()) {
            return new HashMap<>();
        }
        
        return courses.stream()
                .collect(Collectors.groupingBy(
                        course -> (String) course.getOrDefault("level", "Non spécifié"),
                        Collectors.counting()
                ));
    }
    
    private Double calculateAverageRoomOccupancy(List<Map<String, Object>> reservations, List<Map<String, Object>> resources) {
        if (reservations == null || reservations.isEmpty() || resources == null || resources.isEmpty()) {
            return 0.0;
        }
        
        // Calcul simplifié : pourcentage de réservations approuvées
        long approvedReservations = reservations.stream()
                .mapToLong(r -> "APPROVED".equals(r.get("status")) ? 1 : 0)
                .sum();
        
        return (double) approvedReservations / reservations.size() * 100;
    }
    
    private Double calculateAverageCourseUtilization(List<Map<String, Object>> courses, List<Map<String, Object>> schedules) {
        if (courses == null || courses.isEmpty()) {
            return 0.0;
        }
        
        // Calcul simplifié : pourcentage de cours avec emploi du temps
        if (schedules == null || schedules.isEmpty()) {
            return 0.0;
        }
        
        return Math.min(100.0, (double) schedules.size() / courses.size() * 100);
    }
    
    private Map<String, Object> calculateTrends(Map<String, Object> allData) {
        Map<String, Object> trends = new HashMap<>();
        
        // Tendances simplifiées
        trends.put("userGrowth", "stable");
        trends.put("reservationTrend", "increasing");
        trends.put("resourceUtilization", "optimal");
        
        return trends;
    }
}