package cm.iusjc.reservation.service;

import cm.iusjc.reservation.entity.Reservation;
import cm.iusjc.reservation.entity.ReservationStatus;
import cm.iusjc.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final ReservationRepository reservationRepository;
    private final ResourceService resourceService;

    /**
     * Récupère les statistiques du tableau de bord
     */
    public Map<String, Object> getDashboardStats(String period) {
        log.info("Génération statistiques dashboard pour période: {}", period);
        
        LocalDateTime[] dateRange = getDateRange(period);
        LocalDateTime startDate = dateRange[0];
        LocalDateTime endDate = dateRange[1];
        
        List<Reservation> reservations = getReservationsInPeriod(startDate, endDate);
        List<Map<String, Object>> allRooms = resourceService.getAllResources();
        
        // Calculs des métriques
        int totalRooms = allRooms.size();
        int activeReservations = (int) reservations.stream()
            .filter(r -> r.getStatus() == ReservationStatus.CONFIRMED || r.getStatus() == ReservationStatus.PENDING)
            .count();
        
        double occupancyRate = calculateOverallOccupancyRate(reservations, allRooms, startDate, endDate);
        double efficiencyScore = calculateOverallEfficiencyScore(reservations, allRooms, startDate, endDate);
        
        // Calcul des tendances (comparaison avec période précédente)
        LocalDateTime[] previousDateRange = getPreviousDateRange(period);
        List<Reservation> previousReservations = getReservationsInPeriod(previousDateRange[0], previousDateRange[1]);
        
        Map<String, Object> trends = calculateTrends(reservations, previousReservations, allRooms, dateRange, previousDateRange);
        
        return Map.of(
            "totalRooms", totalRooms,
            "activeReservations", activeReservations,
            "occupancyRate", Math.round(occupancyRate),
            "efficiencyScore", Math.round(efficiencyScore),
            "trends", trends,
            "period", Map.of(
                "startDate", startDate.toString(),
                "endDate", endDate.toString()
            )
        );
    }

    /**
     * Récupère les données d'occupation par salle
     */
    public List<Map<String, Object>> getRoomOccupancy(String period) {
        log.info("Génération données occupation salles pour période: {}", period);
        
        LocalDateTime[] dateRange = getDateRange(period);
        LocalDateTime startDate = dateRange[0];
        LocalDateTime endDate = dateRange[1];
        
        List<Map<String, Object>> allRooms = resourceService.getAllResources();
        List<Reservation> reservations = getReservationsInPeriod(startDate, endDate);
        
        return allRooms.stream()
            .map(room -> {
                Long resourceId = (Long) room.get("id");
                List<Reservation> roomReservations = reservations.stream()
                    .filter(r -> r.getResourceId().equals(resourceId))
                    .collect(Collectors.toList());
                
                double occupancyRate = calculateRoomOccupancyRate(roomReservations, startDate, endDate);
                double averageCapacityUsage = calculateAverageCapacityUsage(roomReservations, (Integer) room.get("capacity"));
                double efficiencyScore = (occupancyRate * 0.6) + (averageCapacityUsage * 0.4);
                
                String status = getStatusFromScore(efficiencyScore);
                
                return Map.of(
                    "resourceId", resourceId,
                    "resourceName", room.get("name"),
                    "occupancyRate", Math.round(occupancyRate),
                    "totalReservations", roomReservations.size(),
                    "averageCapacityUsage", Math.round(averageCapacityUsage),
                    "efficiencyScore", Math.round(efficiencyScore),
                    "status", status
                );
            })
            .sorted((a, b) -> Double.compare((Double) b.get("efficiencyScore"), (Double) a.get("efficiencyScore")))
            .collect(Collectors.toList());
    }

    /**
     * Récupère les données d'occupation par heure
     */
    public List<Map<String, Object>> getHourlyOccupancy(LocalDateTime date) {
        log.info("Génération données occupation horaire pour: {}", date.toLocalDate());
        
        List<Map<String, Object>> hourlyData = new ArrayList<>();
        
        for (int hour = 8; hour <= 17; hour++) {
            LocalDateTime hourStart = date.withHour(hour).withMinute(0).withSecond(0);
            LocalDateTime hourEnd = hourStart.plusHours(1);
            
            List<Reservation> hourReservations = reservationRepository.findAll().stream()
                .filter(r -> r.getStartTime().isBefore(hourEnd) && r.getEndTime().isAfter(hourStart))
                .filter(r -> r.getStatus() == ReservationStatus.CONFIRMED)
                .collect(Collectors.toList());
            
            // Calculer l'occupation par type de salle
            Map<String, Integer> occupancyByType = calculateOccupancyByRoomType(hourReservations);
            
            hourlyData.add(Map.of(
                "time", String.format("%02d:00", hour),
                "amphitheater", occupancyByType.getOrDefault("AMPHITHEATER", 0),
                "classroom", occupancyByType.getOrDefault("CLASSROOM", 0),
                "lab", occupancyByType.getOrDefault("LABORATORY", 0)
            ));
        }
        
        return hourlyData;
    }

    /**
     * Récupère les données hebdomadaires
     */
    public List<Map<String, Object>> getWeeklyData(LocalDateTime startDate) {
        log.info("Génération données hebdomadaires depuis: {}", startDate.toLocalDate());
        
        List<Map<String, Object>> weeklyData = new ArrayList<>();
        String[] dayNames = {"Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim"};
        
        for (int i = 0; i < 7; i++) {
            LocalDateTime dayStart = startDate.plusDays(i).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime dayEnd = dayStart.plusDays(1);
            
            List<Reservation> dayReservations = getReservationsInPeriod(dayStart, dayEnd);
            
            int reservationCount = dayReservations.size();
            double occupancyRate = calculateDayOccupancyRate(dayReservations, dayStart, dayEnd);
            
            weeklyData.add(Map.of(
                "day", dayNames[i],
                "reservations", reservationCount,
                "occupancy", Math.round(occupancyRate)
            ));
        }
        
        return weeklyData;
    }

    /**
     * Récupère la répartition par type de salle
     */
    public List<Map<String, Object>> getRoomTypeDistribution() {
        log.info("Génération répartition types de salles");
        
        List<Map<String, Object>> allRooms = resourceService.getAllResources();
        
        Map<String, Long> typeCount = allRooms.stream()
            .collect(Collectors.groupingBy(
                room -> (String) room.get("type"),
                Collectors.counting()
            ));
        
        long totalRooms = allRooms.size();
        
        return typeCount.entrySet().stream()
            .map(entry -> {
                String typeName = getTypeDisplayName(entry.getKey());
                long count = entry.getValue();
                double percentage = (double) count / totalRooms * 100;
                
                Map<String, Object> result = new HashMap<>();
                result.put("name", typeName);
                result.put("value", Math.round(percentage));
                result.put("count", count);
                return result;
            })
            .sorted((a, b) -> Long.compare(((Number) b.get("count")).longValue(), ((Number) a.get("count")).longValue()))
            .collect(Collectors.toList());
    }

    // Méthodes utilitaires privées

    private LocalDateTime[] getDateRange(String period) {
        LocalDateTime now = LocalDateTime.now();
        return switch (period) {
            case "day" -> new LocalDateTime[]{
                now.withHour(0).withMinute(0).withSecond(0),
                now.withHour(23).withMinute(59).withSecond(59)
            };
            case "week" -> new LocalDateTime[]{
                now.minusDays(now.getDayOfWeek().getValue() - 1).withHour(0).withMinute(0).withSecond(0),
                now.plusDays(7 - now.getDayOfWeek().getValue()).withHour(23).withMinute(59).withSecond(59)
            };
            case "month" -> new LocalDateTime[]{
                now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0),
                now.withDayOfMonth(now.toLocalDate().lengthOfMonth()).withHour(23).withMinute(59).withSecond(59)
            };
            case "quarter" -> new LocalDateTime[]{
                now.minusMonths(2).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0),
                now.withDayOfMonth(now.toLocalDate().lengthOfMonth()).withHour(23).withMinute(59).withSecond(59)
            };
            default -> new LocalDateTime[]{
                now.minusDays(7).withHour(0).withMinute(0).withSecond(0),
                now.withHour(23).withMinute(59).withSecond(59)
            };
        };
    }

    private LocalDateTime[] getPreviousDateRange(String period) {
        LocalDateTime[] currentRange = getDateRange(period);
        long daysDiff = java.time.Duration.between(currentRange[0], currentRange[1]).toDays();
        
        return new LocalDateTime[]{
            currentRange[0].minusDays(daysDiff + 1),
            currentRange[1].minusDays(daysDiff + 1)
        };
    }

    private List<Reservation> getReservationsInPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        return reservationRepository.findAll().stream()
            .filter(r -> !r.getStartTime().isBefore(startDate) && !r.getEndTime().isAfter(endDate))
            .collect(Collectors.toList());
    }

    private double calculateOverallOccupancyRate(List<Reservation> reservations, List<Map<String, Object>> rooms, LocalDateTime startDate, LocalDateTime endDate) {
        if (rooms.isEmpty()) return 0.0;
        
        long totalPossibleHours = rooms.size() * java.time.Duration.between(startDate, endDate).toHours();
        long totalUsedHours = reservations.stream()
            .filter(r -> r.getStatus() == ReservationStatus.CONFIRMED)
            .mapToLong(r -> java.time.Duration.between(r.getStartTime(), r.getEndTime()).toHours())
            .sum();
        
        return totalPossibleHours > 0 ? (double) totalUsedHours / totalPossibleHours * 100 : 0.0;
    }

    private double calculateOverallEfficiencyScore(List<Reservation> reservations, List<Map<String, Object>> rooms, LocalDateTime startDate, LocalDateTime endDate) {
        if (rooms.isEmpty()) return 0.0;
        
        double totalScore = rooms.stream()
            .mapToDouble(room -> {
                Long resourceId = (Long) room.get("id");
                List<Reservation> roomReservations = reservations.stream()
                    .filter(r -> r.getResourceId().equals(resourceId))
                    .collect(Collectors.toList());
                
                double occupancyRate = calculateRoomOccupancyRate(roomReservations, startDate, endDate);
                double capacityUsage = calculateAverageCapacityUsage(roomReservations, (Integer) room.get("capacity"));
                
                return (occupancyRate * 0.6) + (capacityUsage * 0.4);
            })
            .sum();
        
        return totalScore / rooms.size();
    }

    private double calculateRoomOccupancyRate(List<Reservation> roomReservations, LocalDateTime startDate, LocalDateTime endDate) {
        long totalPossibleMinutes = java.time.Duration.between(startDate, endDate).toMinutes();
        long totalUsedMinutes = roomReservations.stream()
            .filter(r -> r.getStatus() == ReservationStatus.CONFIRMED)
            .mapToLong(r -> java.time.Duration.between(r.getStartTime(), r.getEndTime()).toMinutes())
            .sum();
        
        return totalPossibleMinutes > 0 ? (double) totalUsedMinutes / totalPossibleMinutes * 100 : 0.0;
    }

    private double calculateAverageCapacityUsage(List<Reservation> reservations, Integer roomCapacity) {
        if (reservations.isEmpty() || roomCapacity == null || roomCapacity == 0) return 0.0;
        
        double averageAttendees = reservations.stream()
            .filter(r -> r.getExpectedAttendees() != null && r.getExpectedAttendees() > 0)
            .mapToInt(Reservation::getExpectedAttendees)
            .average()
            .orElse(0.0);
        
        return (averageAttendees / roomCapacity) * 100;
    }

    private Map<String, Object> calculateTrends(List<Reservation> current, List<Reservation> previous, 
                                               List<Map<String, Object>> rooms, LocalDateTime[] currentRange, LocalDateTime[] previousRange) {
        
        double currentOccupancy = calculateOverallOccupancyRate(current, rooms, currentRange[0], currentRange[1]);
        double previousOccupancy = calculateOverallOccupancyRate(previous, rooms, previousRange[0], previousRange[1]);
        
        double currentEfficiency = calculateOverallEfficiencyScore(current, rooms, currentRange[0], currentRange[1]);
        double previousEfficiency = calculateOverallEfficiencyScore(previous, rooms, previousRange[0], previousRange[1]);
        
        int reservationsTrend = calculatePercentageChange(current.size(), previous.size());
        int occupancyTrend = calculatePercentageChange(currentOccupancy, previousOccupancy);
        int efficiencyTrend = calculatePercentageChange(currentEfficiency, previousEfficiency);
        
        return Map.of(
            "reservations", reservationsTrend,
            "occupancy", occupancyTrend,
            "efficiency", efficiencyTrend
        );
    }

    private int calculatePercentageChange(double current, double previous) {
        if (previous == 0) return current > 0 ? 100 : 0;
        return (int) Math.round(((current - previous) / previous) * 100);
    }

    private Map<String, Integer> calculateOccupancyByRoomType(List<Reservation> reservations) {
        Map<String, Integer> occupancyByType = new HashMap<>();
        
        for (Reservation reservation : reservations) {
            String roomType = resourceService.getRoomType(reservation.getResourceId());
            occupancyByType.merge(roomType, 1, Integer::sum);
        }
        
        return occupancyByType;
    }

    private double calculateDayOccupancyRate(List<Reservation> dayReservations, LocalDateTime dayStart, LocalDateTime dayEnd) {
        // Calculer le taux d'occupation pour une journée (8h-18h = 10h de travail)
        long workingMinutes = 10 * 60; // 10 heures en minutes
        long usedMinutes = dayReservations.stream()
            .filter(r -> r.getStatus() == ReservationStatus.CONFIRMED)
            .mapToLong(r -> java.time.Duration.between(
                r.getStartTime().isAfter(dayStart) ? r.getStartTime() : dayStart,
                r.getEndTime().isBefore(dayEnd) ? r.getEndTime() : dayEnd
            ).toMinutes())
            .sum();
        
        return workingMinutes > 0 ? (double) usedMinutes / workingMinutes * 100 : 0.0;
    }

    private String getStatusFromScore(double score) {
        if (score >= 80) return "excellent";
        if (score >= 60) return "good";
        if (score >= 40) return "average";
        return "poor";
    }

    private String getTypeDisplayName(String type) {
        return switch (type) {
            case "AMPHITHEATER" -> "Amphithéâtres";
            case "CLASSROOM" -> "Salles de classe";
            case "LABORATORY" -> "Laboratoires";
            case "MEETING_ROOM" -> "Salles de réunion";
            case "OFFICE" -> "Bureaux";
            default -> type;
        };
    }
}