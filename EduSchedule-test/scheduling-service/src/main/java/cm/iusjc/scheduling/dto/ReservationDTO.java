package cm.iusjc.scheduling.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationDTO {
    
    private Long id;
    private Long resourceId;
    private String resourceName; // Nom de la ressource (récupéré du Resource Service)
    private Long courseId;
    private String courseName; // Nom du cours (récupéré du Course Service)
    private Long courseGroupId;
    private String courseGroupName; // Nom du groupe (récupéré du Course Service)
    private Long userId;
    private String userName; // Nom de l'utilisateur (récupéré du User Service)
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status; // Simplified as String instead of enum
    private String type; // Simplified as String instead of enum
    private String recurringPattern;
    private Long parentReservationId;
    private Integer expectedAttendees;
    private Integer setupTime;
    private Integer cleanupTime;
    private String notes;
    private Long approvedBy;
    private String approvedByName; // Nom de l'approbateur
    private LocalDateTime approvedAt;
    private Long cancelledBy;
    private String cancelledByName; // Nom de celui qui a annulé
    private LocalDateTime cancelledAt;
    private String cancellationReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Champs calculés
    private LocalDateTime effectiveStartTime;
    private LocalDateTime effectiveEndTime;
    private Boolean isRecurring;
    private Boolean isActive;
}