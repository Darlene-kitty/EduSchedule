package cm.iusjc.reservation.dto;

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
public class IntelligentAssignmentRequest {
    
    private Long userId;
    private String type; // COURSE, EXAM, MEETING, EVENT, etc.
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer expectedAttendees;
    private List<String> requiredEquipments;
    private String preferredBuilding;
    private Integer preferredFloor;
    private boolean requiresAccessibility;
    private String priority; // HIGH, MEDIUM, LOW
    private String description;
    
    // Critères d'optimisation avancés
    private boolean allowAlternativeTime;
    private boolean allowAlternativeCapacity;
    private Integer maxWalkingDistance; // en mètres
    private String teacherId;
    private String courseId;
    private String groupId;
    
    // Préférences contextuelles
    private boolean preferQuietEnvironment;
    private boolean preferNaturalLight;
    private boolean allowSharedSpace;
}