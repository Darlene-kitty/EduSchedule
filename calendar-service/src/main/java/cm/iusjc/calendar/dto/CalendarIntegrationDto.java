package cm.iusjc.calendar.dto;

import cm.iusjc.calendar.entity.CalendarIntegration;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CalendarIntegrationDto {
    private Long id;
    private String userId;
    private CalendarIntegration.CalendarProvider provider;
    private String calendarId;
    private String accessToken;
    private String refreshToken;
    private LocalDateTime tokenExpiresAt;
    private Boolean enabled;
    private Boolean syncEnabled;
    private CalendarIntegration.SyncDirection syncDirection;
    private LocalDateTime lastSyncAt;
    private CalendarIntegration.SyncStatus syncStatus;
    private String syncError;
    private LocalDateTime createdAt;
}