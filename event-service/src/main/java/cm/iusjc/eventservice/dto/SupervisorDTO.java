package cm.iusjc.eventservice.dto;

import cm.iusjc.eventservice.entity.SupervisorRole;
import cm.iusjc.eventservice.entity.SupervisorStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupervisorDTO {
    
    private Long id;
    private Long examId;
    private Long userId;
    private SupervisorRole role;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String notes;
    private SupervisorStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Informations enrichies
    private String userName;
    private String userEmail;
}