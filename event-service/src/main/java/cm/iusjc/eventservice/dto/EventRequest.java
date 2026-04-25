package cm.iusjc.eventservice.dto;

import cm.iusjc.eventservice.entity.EventType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventRequest {
    
    @NotBlank(message = "Title is required")
    private String title;
    
    private String description;
    
    @NotNull(message = "Event type is required")
    private EventType type;
    
    @NotNull(message = "Start date time is required")
    private LocalDateTime startDateTime;
    
    @NotNull(message = "End date time is required")
    private LocalDateTime endDateTime;
    
    @NotNull(message = "Resource ID is required")
    private Long resourceId;
    
    @NotNull(message = "Organizer ID is required")
    private Long organizerId;
    
    private Integer maxParticipants;
    private Boolean registrationRequired = false;
    private LocalDateTime registrationDeadline;
    private String externalParticipants;
    private String equipmentNeeded;
    private String specialRequirements;
}