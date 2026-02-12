package cm.iusjc.notification.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id")
    private Long userId;
    
    @Column(nullable = false)
    private String recipient;
    
    private String subject;
    
    private String title;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;
    
    @Column(nullable = false, length = 20)
    private String type; // EMAIL, SMS, PUSH, REMINDER
    
    @Column(nullable = false, length = 20)
    private String status = "PENDING"; // PENDING, SENT, FAILED, SCHEDULED
    
    @Column(name = "channel", length = 20)
    private String channel; // EMAIL, SMS, PUSH
    
    @Column(name = "related_entity_type", length = 50)
    private String relatedEntityType;
    
    @Column(name = "related_entity_id")
    private Long relatedEntityId;
    
    @Column(name = "is_read")
    private boolean read = false;
    
    @Column(name = "read_at")
    private LocalDateTime readAt;
    
    @Column(name = "is_sent")
    private boolean sent = false;
    
    @Column(name = "sent_at")
    private LocalDateTime sentAt;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Nouveaux champs pour les rappels et notifications avancées
    @Column(name = "scheduled_for")
    private LocalDateTime scheduledFor;
    
    @Column(name = "event_type", length = 50)
    private String eventType; // SCHEDULE_CHANGE, ROOM_CHANGE, COURSE_CANCELLATION, REMINDER
    
    @Column(name = "event_id")
    private Long eventId; // ID de l'événement associé (schedule, reservation, etc.)
    
    @Column(name = "priority", length = 10)
    private String priority = "NORMAL"; // LOW, NORMAL, HIGH, URGENT
    
    @Column(name = "retry_count")
    private Integer retryCount = 0;
    
    @Column(name = "max_retries")
    private Integer maxRetries = 3;
    
    @Column(name = "template_name", length = 100)
    private String templateName; // Nom du template utilisé
    
    @Column(columnDefinition = "TEXT")
    private String metadata; // JSON avec des données supplémentaires
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
