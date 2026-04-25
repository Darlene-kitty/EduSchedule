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

            // ══ STATISTIQUES DÉTAILLÉES ══
            statistics.setCoursesBySchool(calculateCoursesBySchool(courses));
            statistics.setCoursesByRoomType(calculateCoursesByRoomType(courses));
            statistics.setCoursesByTeacher(calculateCoursesByTeacher(courses));
            statistics.setRoomUsageDetails(calculateRoomUsageDetails(resources, reservations));
            statistics.setRoomAvailabilityByHour(calculateRoomAvailabilityByHour(schedules));
            statistics.setSchoolStatistics(calculateSchoolStatistics(courses, users));
            statistics.setTeacherWorkload(calculateTeacherWorkload(courses));

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
        // Champs détaillés
        stats.setCoursesBySchool(new HashMap<>());
        stats.setCoursesByRoomType(new HashMap<>());
        stats.setCoursesByTeacher(List.of());
        stats.setRoomUsageDetails(List.of());
        stats.setRoomAvailabilityByHour(new HashMap<>());
        stats.setSchoolStatistics(List.of());
        stats.setTeacherWorkload(List.of());
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

    // ══ MÉTHODES POUR LES STATISTIQUES DÉTAILLÉES ══

    private Map<String, Long> calculateCoursesBySchool(List<Map<String, Object>> courses) {
        if (courses == null || courses.isEmpty()) return new HashMap<>();
        return courses.stream()
                .collect(Collectors.groupingBy(
                        c -> {
                            Object school = c.get("schoolName");
                            if (school == null) school = c.get("school");
                            if (school == null) school = c.get("department");
                            return school != null ? school.toString() : "Non spécifié";
                        },
                        Collectors.counting()
                ));
    }

    private Map<String, Long> calculateCoursesByRoomType(List<Map<String, Object>> courses) {
        if (courses == null || courses.isEmpty()) return new HashMap<>();
        return courses.stream()
                .collect(Collectors.groupingBy(
                        c -> {
                            Object rt = c.get("roomType");
                            if (rt == null) rt = c.get("salleType");
                            return rt != null ? rt.toString() : "Standard";
                        },
                        Collectors.counting()
                ));
    }

    private List<StatisticsDTO.TeacherStatistic> calculateCoursesByTeacher(List<Map<String, Object>> courses) {
        if (courses == null || courses.isEmpty()) return List.of();
        Map<String, long[]> teacherMap = new HashMap<>();
        for (Map<String, Object> c : courses) {
            String name = getStringField(c, "teacherName", "enseignantNom", "teacher");
            if (name == null || name.isBlank()) continue;
            Object hoursObj = c.get("hours");
            if (hoursObj == null) hoursObj = c.get("heures");
            long hours = hoursObj != null ? Long.parseLong(hoursObj.toString()) : 2L;
            teacherMap.computeIfAbsent(name, k -> new long[]{0L, 0L});
            teacherMap.get(name)[0]++;       // courseCount
            teacherMap.get(name)[1] += hours; // totalHours
        }
        return teacherMap.entrySet().stream()
                .map(e -> new StatisticsDTO.TeacherStatistic(e.getKey(), null, e.getValue()[0], e.getValue()[1], ""))
                .sorted((a, b) -> Long.compare(b.getTotalHours(), a.getTotalHours()))
                .limit(10)
                .collect(Collectors.toList());
    }

    private List<StatisticsDTO.RoomUsageStatistic> calculateRoomUsageDetails(
            List<Map<String, Object>> resources, List<Map<String, Object>> reservations) {
        if (resources == null || resources.isEmpty()) return List.of();
        Map<String, Long> reservationsByRoom = new HashMap<>();
        if (reservations != null) {
            for (Map<String, Object> r : reservations) {
                String room = getStringField(r, "roomName", "salleName", "resourceName");
                if (room != null) reservationsByRoom.merge(room, 1L, Long::sum);
            }
        }
        long totalReservations = reservationsByRoom.values().stream().mapToLong(Long::longValue).sum();
        return resources.stream().map(res -> {
            String name = getStringField(res, "name", "nom", "roomName");
            if (name == null) name = "Salle " + res.getOrDefault("id", "?");
            long roomRes = reservationsByRoom.getOrDefault(name, 0L);
            double rate = totalReservations > 0 ? Math.min(100.0, (double) roomRes / Math.max(1, totalReservations / Math.max(1, resources.size())) * 100) : 0.0;
            Object capObj = res.get("capacity");
            if (capObj == null) capObj = res.get("capacite");
            int cap = capObj != null ? Integer.parseInt(capObj.toString()) : 30;
            String type = getStringField(res, "type", "roomType", "salleType");
            if (type == null) type = "Standard";
            return new StatisticsDTO.RoomUsageStatistic(name, null, Math.round(rate * 10.0) / 10.0, roomRes * 2, 40L - roomRes * 2, type, cap);
        }).sorted((a, b) -> Double.compare(b.getOccupancyRate(), a.getOccupancyRate()))
          .limit(15)
          .collect(Collectors.toList());
    }

    private Map<String, Double> calculateRoomAvailabilityByHour(List<Map<String, Object>> schedules) {
        Map<String, Double> result = new java.util.LinkedHashMap<>();
        String[] slots = {"08:00","09:00","10:00","11:00","12:00","13:00","14:00","15:00","16:00","17:00","18:00"};
        Map<String, Long> usedSlots = new HashMap<>();
        if (schedules != null) {
            for (Map<String, Object> s : schedules) {
                String slot = getStringField(s, "startTime", "heureDebut", "timeSlot");
                if (slot != null && slot.length() >= 5) {
                    String hour = slot.substring(0, 5);
                    usedSlots.merge(hour, 1L, Long::sum);
                }
            }
        }
        long totalRooms = Math.max(1, schedules != null ? schedules.size() / Math.max(1, slots.length) : 5);
        for (String slot : slots) {
            long used = usedSlots.getOrDefault(slot, 0L);
            double availability = Math.max(0, Math.min(100.0, 100.0 - (double) used / totalRooms * 100));
            result.put(slot, Math.round(availability * 10.0) / 10.0);
        }
        return result;
    }

    private List<StatisticsDTO.SchoolStatistic> calculateSchoolStatistics(
            List<Map<String, Object>> courses, List<Map<String, Object>> users) {
        if (courses == null || courses.isEmpty()) return List.of();
        Map<String, long[]> schoolMap = new HashMap<>(); // [courseCount, teacherCount, studentCount]
        for (Map<String, Object> c : courses) {
            String school = getStringField(c, "schoolName", "school", "department");
            if (school == null || school.isBlank()) school = "Non spécifié";
            schoolMap.computeIfAbsent(school, k -> new long[]{0L, 0L, 0L});
            schoolMap.get(school)[0]++;
        }
        if (users != null) {
            for (Map<String, Object> u : users) {
                String role = getStringField(u, "role", "roles");
                String school = getStringField(u, "schoolName", "school", "department");
                if (school == null || school.isBlank()) continue;
                if ("TEACHER".equalsIgnoreCase(role)) {
                    long[] arr = schoolMap.computeIfAbsent(school, k -> new long[]{0L, 0L, 0L});
                    arr[1]++;
                } else if ("STUDENT".equalsIgnoreCase(role)) {
                    long[] arr = schoolMap.computeIfAbsent(school, k -> new long[]{0L, 0L, 0L});
                    arr[2]++;
                }
            }
        }
        return schoolMap.entrySet().stream()
                .map(e -> new StatisticsDTO.SchoolStatistic(e.getKey(), null, e.getValue()[2], e.getValue()[0], e.getValue()[1], 0.0))
                .sorted((a, b) -> Long.compare(b.getCourseCount(), a.getCourseCount()))
                .collect(Collectors.toList());
    }

    private List<StatisticsDTO.TeacherWorkloadStatistic> calculateTeacherWorkload(List<Map<String, Object>> courses) {
        if (courses == null || courses.isEmpty()) return List.of();
        Map<String, long[]> teacherMap = new HashMap<>();
        for (Map<String, Object> c : courses) {
            String name = getStringField(c, "teacherName", "enseignantNom", "teacher");
            if (name == null || name.isBlank()) continue;
            Object hoursObj = c.get("hours");
            if (hoursObj == null) hoursObj = c.get("heures");
            long hours = hoursObj != null ? Long.parseLong(hoursObj.toString()) : 2L;
            teacherMap.computeIfAbsent(name, k -> new long[]{0L, 0L});
            teacherMap.get(name)[0]++;
            teacherMap.get(name)[1] += hours;
        }
        return teacherMap.entrySet().stream().map(e -> {
            long weeklyHours = e.getValue()[1] / Math.max(1, 16); // sur 16 semaines
            String status = weeklyHours > 20 ? "overloaded" : weeklyHours < 6 ? "underutilized" : "normal";
            return new StatisticsDTO.TeacherWorkloadStatistic(e.getKey(), null, weeklyHours, e.getValue()[0], status);
        }).sorted((a, b) -> Long.compare(b.getWeeklyHours(), a.getWeeklyHours()))
          .collect(Collectors.toList());
    }

    /** Cherche un champ parmi plusieurs noms possibles */
    private String getStringField(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            Object val = map.get(key);
            if (val != null && !val.toString().isBlank()) return val.toString();
        }
        return null;
    }
}