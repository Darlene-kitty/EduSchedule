package cm.iusjc.reservation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationRequestDTO {
    
    @NotNull
    private LocalDateTime startTime;
    
    @NotNull
    private LocalDateTime endTime;
    
    @NotNull
    private Integer expectedAttendees;
    
    @NotNull
    private String type; // COURSE, EXAM, MEETING, EVENT, MAINTENANCE, OTHER
    
    private List<String> requiredEquipments;
    
    private Long preferredRoomId;
    
    private Long courseId;
    
    private Long teacherId;
    
    private String description;
    
    private String priority; // LOW, NORMAL, HIGH, URGENT
    
    @Builder.Default
    private Boolean flexibleTiming = false;
    
    @Builder.Default
    private Integer timingFlexibilityMinutes = 0;
    
    @Builder.Default
    private Boolean accessibilityRequired = false;
    
    private String specialRequirements;
    
    private Long schoolId;
    
    private String contactEmail;
    
    private String contactPhone;
}