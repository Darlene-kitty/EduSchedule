package cm.iusjc.aiservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdvancedDashboardResponse {
    
    private boolean success;
    private String message;
    private String period;
    private LocalDateTime generatedAt;
    private BaseMetrics baseMetrics;
    private AdvancedMetrics advancedMetrics;
    private List<Alert> alerts;
    private List<Recommendation> recommendations;
    private double performanceScore;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class BaseMetrics {
    
    private ReservationMetrics reservationMetrics;
    private OccupancyMetrics occupancyMetrics;
    private ResourceMetrics resourceMetrics;
    private UserMetrics userMetrics;
    private PerformanceMetrics performanceMetrics;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class AdvancedMetrics {
    
    private double systemEfficiency;
    private double userSatisfactionScore;
    private double resourceOptimizationRate;
    private double predictabilityIndex;
    private double systemResilienceScore;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class ReservationMetrics {
    
    private int totalReservations;
    private double successRate;
    private double conflictRate;
    private double demandVariability;
    private double averageBookingTime;
    private int cancelledReservations;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class OccupancyMetrics {
    
    private double averageOccupancyRate;
    private double peakOccupancyRate;
    private double patternConsistency;
    private double utilizationEfficiency;
    private int totalOccupiedHours;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class ResourceMetrics {
    
    private double utilizationRate;
    private double capacityOptimization;
    private double maintenanceEfficiency;
    private double redundancyLevel;
    private int totalResources;
    private int availableResources;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class UserMetrics {
    
    private int activeUsers;
    private double satisfactionScore;
    private double engagementRate;
    private int newUsers;
    private double retentionRate;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class PerformanceMetrics {
    
    private double averageResponseTime;
    private double errorRate;
    private double peakHandlingCapacity;
    private double errorRecoveryRate;
    private double systemUptime;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class Alert {
    
    private String type;
    private String severity; // HIGH, MEDIUM, LOW
    private String title;
    private String message;
    private boolean actionRequired;
    private List<String> suggestedActions;
    private LocalDateTime timestamp;
    private String category;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class Recommendation {
    
    private String category;
    private String priority; // HIGH, MEDIUM, LOW
    private String title;
    private String description;
    private String expectedImpact;
    private String implementationEffort; // HIGH, MEDIUM, LOW
    private String timeframe;
    private List<String> steps;
    private double confidenceScore;
}