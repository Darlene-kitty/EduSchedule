package cm.iusjc.scheduling.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomOptimizationRequest {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int expectedAttendees;
    private String courseType; // COURS, TD, TP, EXAMEN, etc.
    private int durationMinutes;
    private List<String> requiredEquipment;
    private String preferredBuilding;
    private Long previousRoomId; // Pour calculer le temps de déplacement
    private List<Long> excludeRoomIds; // Salles à exclure
    private String teacherId; // Pour les préférences de l'enseignant
    private String priority; // HIGH, NORMAL, LOW
    
    public RoomOptimizationRequest(LocalDateTime startTime, LocalDateTime endTime, 
                                  int expectedAttendees, String courseType) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.expectedAttendees = expectedAttendees;
        this.courseType = courseType;
        this.durationMinutes = (int) java.time.Duration.between(startTime, endTime).toMinutes();
        this.priority = "NORMAL";
    }
}