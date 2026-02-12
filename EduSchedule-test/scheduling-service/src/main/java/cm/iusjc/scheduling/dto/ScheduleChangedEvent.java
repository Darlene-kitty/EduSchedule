package cm.iusjc.scheduling.dto;

import cm.iusjc.scheduling.entity.Schedule;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleChangedEvent {
    
    private Long scheduleId;
    private Schedule schedule;
    private Schedule oldSchedule; // Pour les modifications
    private ChangeType changeType;
    private Long changedBy; // ID de l'utilisateur qui a fait le changement
    private LocalDateTime changeTime;
    private String reason; // Raison du changement (optionnel)
    
    public enum ChangeType {
        CREATED,
        UPDATED,
        CANCELLED,
        DELETED
    }
    
    public ScheduleChangedEvent(Long scheduleId, Schedule schedule, ChangeType changeType, Long changedBy) {
        this.scheduleId = scheduleId;
        this.schedule = schedule;
        this.changeType = changeType;
        this.changedBy = changedBy;
        this.changeTime = LocalDateTime.now();
    }
    
    public ScheduleChangedEvent(Long scheduleId, Schedule schedule, Schedule oldSchedule, 
                               ChangeType changeType, Long changedBy) {
        this.scheduleId = scheduleId;
        this.schedule = schedule;
        this.oldSchedule = oldSchedule;
        this.changeType = changeType;
        this.changedBy = changedBy;
        this.changeTime = LocalDateTime.now();
    }
}