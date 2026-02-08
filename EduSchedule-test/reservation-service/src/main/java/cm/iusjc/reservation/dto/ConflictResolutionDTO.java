package cm.iusjc.reservation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConflictResolutionDTO {
    
    private Long id;
    
    private String resolutionType; // ALTERNATIVE_ROOM, TIME_SHIFT, SPLIT_GROUP, CANCEL
    
    private String title;
    
    private String description;
    
    private Double feasibilityScore;
    
    private String impact; // LOW, MEDIUM, HIGH
    
    private String impactLevel; // Alias pour impact
    
    private Integer estimatedEffort; // En minutes
    
    private List<String> requiredActions;
    
    // Pour ALTERNATIVE_ROOM
    private RoomSuggestionDTO alternativeRoom;
    
    private Long newResourceId;
    
    // Pour TIME_SHIFT
    private LocalDateTime suggestedStartTime;
    private LocalDateTime suggestedEndTime;
    private LocalDateTime newStartTime;
    private LocalDateTime newEndTime;
    private List<LocalDateTime> alternativeTimeSlots;
    
    // Pour SPLIT_GROUP
    private List<GroupSplitSuggestion> groupSplits;
    
    // Informations sur l'impact
    private List<Long> affectedUserIds;
    private List<String> affectedResources;
    private String notificationMessage;
    
    // Métadonnées
    private LocalDateTime generatedAt;
    private String generatedBy;
    private Boolean autoApplicable;
    private String priority; // LOW, MEDIUM, HIGH, URGENT
    private String additionalInfo;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GroupSplitSuggestion {
        private String groupName;
        private Integer groupSize;
        private RoomSuggestionDTO assignedRoom;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private List<Long> studentIds;
    }
}