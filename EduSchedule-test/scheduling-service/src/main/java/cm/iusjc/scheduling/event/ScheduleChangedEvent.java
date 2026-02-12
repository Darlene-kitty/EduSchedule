package cm.iusjc.scheduling.event;

import cm.iusjc.scheduling.entity.Schedule;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ScheduleChangedEvent extends ApplicationEvent {
    
    private final Schedule schedule;
    private final Schedule oldSchedule;
    private final ChangeType changeType;
    private final Long userId; // Utilisateur qui a effectué le changement
    
    public ScheduleChangedEvent(Object source, Schedule schedule, ChangeType changeType, Long userId) {
        super(source);
        this.schedule = schedule;
        this.oldSchedule = null;
        this.changeType = changeType;
        this.userId = userId;
    }
    
    public ScheduleChangedEvent(Object source, Schedule schedule, Schedule oldSchedule, ChangeType changeType, Long userId) {
        super(source);
        this.schedule = schedule;
        this.oldSchedule = oldSchedule;
        this.changeType = changeType;
        this.userId = userId;
    }
    
    public enum ChangeType {
        CREATED,
        UPDATED,
        CANCELLED,
        DELETED,
        RESCHEDULED
    }
}