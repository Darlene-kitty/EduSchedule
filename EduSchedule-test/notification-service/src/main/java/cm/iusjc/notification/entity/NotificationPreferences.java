package cm.iusjc.notification.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification_preferences")
@Data
@NoArgsConstructor
public class NotificationPreferences {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "email_enabled")
    private boolean emailEnabled = true;

    @Column(name = "push_enabled")
    private boolean pushEnabled = true;

    @Column(name = "schedule_changes")
    private boolean scheduleChanges = true;

    @Column(name = "conflict_alerts")
    private boolean conflictAlerts = true;

    @Column(name = "reservation_updates")
    private boolean reservationUpdates = true;

    @Column(name = "reminder_notifications")
    private boolean reminderNotifications = true;

    @Column(name = "reminder_minutes_before")
    private int reminderMinutesBefore = 30;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
