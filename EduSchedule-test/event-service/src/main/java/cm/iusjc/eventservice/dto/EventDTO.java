package cm.iusjc.eventservice.dto;

import cm.iusjc.eventservice.entity.EventStatus;
import cm.iusjc.eventservice.entity.EventType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventDTO {
    
    private Long id;
    private String title;
    private String description;
    private EventType type;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private Long resourceId;
    private Long organizerId;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private EventStatus status;
    private Boolean isPublic;
    private Boolean requiresApproval;
    private Long approvedBy;
    private LocalDateTime approvedAt;
    private String cancelledReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Informations enrichies (optionnelles)
    private String resourceName;
    private String organizerName;
    private String approverName;
}