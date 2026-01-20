package cm.iusjc.reporting.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}