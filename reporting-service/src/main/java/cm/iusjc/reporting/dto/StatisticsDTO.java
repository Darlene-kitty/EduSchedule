package cm.iusjc.reporting.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsDTO {
    
    // Statistiques générales
    private Long totalUsers;
    private Long totalCourses;
    private Long totalReservations;
    private Long totalResources;
    private Long totalRooms;
    private Long totalSchools;
    
    // Répartition par rôle
    private Map<String, Long> usersByRole;
    
    // Statistiques d'utilisation
    private Map<String, Long> reservationsByStatus;
    private Map<String, Long> coursesByDepartment;
    private Map<String, Long> resourcesByType;
    
    // Statistiques temporelles
    private Map<String, Long> reservationsByMonth;
    private Map<String, Long> coursesByLevel;
    
    // Taux d'occupation
    private Double averageRoomOccupancy;
    private Double averageCourseUtilization;
    
    // Tendances
    private Map<String, Object> trends;
    
    // ══ NOUVELLES STATISTIQUES DÉTAILLÉES ══
    
    // Répartition des cours par école
    private Map<String, Long> coursesBySchool;
    
    // Répartition des cours par enseignant (top 10)
    private List<TeacherStatistic> coursesByTeacher;
    
    // Répartition des cours par type de salle
    private Map<String, Long> coursesByRoomType;
    
    // Utilisation des salles (détail par salle)
    private List<RoomUsageStatistic> roomUsageDetails;
    
    // Disponibilité des salles par heure
    private Map<String, Double> roomAvailabilityByHour;
    
    // Statistiques par école
    private List<SchoolStatistic> schoolStatistics;
    
    // Charge de travail des enseignants
    private List<TeacherWorkloadStatistic> teacherWorkload;
    
    // ══ CLASSES INTERNES POUR STATISTIQUES DÉTAILLÉES ══
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeacherStatistic {
        private String teacherName;
        private Long teacherId;
        private Long courseCount;
        private Long totalHours;
        private String department;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoomUsageStatistic {
        private String roomName;
        private Long roomId;
        private Double occupancyRate;
        private Long totalHours;
        private Long availableHours;
        private String roomType;
        private Integer capacity;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SchoolStatistic {
        private String schoolName;
        private Long schoolId;
        private Long studentCount;
        private Long courseCount;
        private Long teacherCount;
        private Double averageClassSize;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeacherWorkloadStatistic {
        private String teacherName;
        private Long teacherId;
        private Long weeklyHours;
        private Long courseCount;
        private String status; // "normal", "overloaded", "underutilized"
    }
}