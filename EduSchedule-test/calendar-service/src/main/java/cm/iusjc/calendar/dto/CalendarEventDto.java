package cm.iusjc.calendar.dto;

import cm.iusjc.calendar.entity.CalendarEvent;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CalendarEventDto {
    private Long id;
    private String userId;
    private String externalEventId;
    private Long scheduleId;
    private Long reservationId;
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String location;
    private String attendees;
    private Boolean isAllDay;
    private String recurrenceRule;
    private CalendarEvent.EventSyncStatus syncStatus;
    private LocalDateTime lastSyncedAt;
    private String syncError;
    private LocalDateTime createdAt;
}