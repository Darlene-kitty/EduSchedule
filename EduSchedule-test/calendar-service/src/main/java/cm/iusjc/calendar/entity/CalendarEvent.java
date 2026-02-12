package cm.iusjc.calendar.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "calendar_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalendarEvent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "external_event_id")
    private String externalEventId; // ID de l'événement dans le calendrier externe
    
    @Column(name = "schedule_id")
    private Long scheduleId; // ID de l'emploi du temps associé
    
    @Column(name = "reservation_id")
    private Long reservationId; // ID de la réservation associée
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "integration_id", nullable = false)
    private CalendarIntegration integration;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;
    
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;
    
    @Column(name = "location")
    private String location;
    
    @Column(name = "attendees", columnDefinition = "TEXT")
    private String attendees; // JSON array des participants
    
    @Column(name = "is_all_day")
    private Boolean isAllDay = false;
    
    @Column(name = "recurrence_rule")
    private String recurrenceRule; // Règle de récurrence (RRULE)
    
    @Column(name = "sync_status", length = 20)
    @Enumerated(EnumType.STRING)
    private EventSyncStatus syncStatus = EventSyncStatus.PENDING;
    
    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;
    
    @Column(name = "sync_error", columnDefinition = "TEXT")
    private String syncError;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum EventSyncStatus {
        PENDING,        // En attente de synchronisation
        SYNCED,         // Synchronisé avec succès
        CONFLICT,       // Conflit détecté
        ERROR,          // Erreur de synchronisation
        DELETED         // Supprimé du calendrier externe
    }
}