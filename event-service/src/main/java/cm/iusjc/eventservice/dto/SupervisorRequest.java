package cm.iusjc.eventservice.dto;

import cm.iusjc.eventservice.entity.SupervisorRole;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupervisorRequest {
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotNull(message = "Supervisor role is required")
    private SupervisorRole role;
    
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String notes;
}