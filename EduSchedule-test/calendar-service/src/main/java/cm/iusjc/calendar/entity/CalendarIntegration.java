package cm.iusjc.calendar.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "calendar_integrations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalendarIntegration {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String userId; // Email ou ID de l'utilisateur
    
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private CalendarProvider provider; // GOOGLE, OUTLOOK, APPLE
    
    @Column(nullable = false)
    private String calendarId; // ID du calendrier externe
    
    @Column(name = "access_token", columnDefinition = "TEXT")
    private String accessToken;
    
    @Column(name = "refresh_token", columnDefinition = "TEXT")
    private String refreshToken;
    
    @Column(name = "token_expires_at")
    private LocalDateTime tokenExpiresAt;
    
    @Column(nullable = false)
    private Boolean enabled = true;
    
    @Column(nullable = false)
    private Boolean syncEnabled = true;
    
    @Column(name = "sync_direction", length = 20)
    @Enumerated(EnumType.STRING)
    private SyncDirection syncDirection = SyncDirection.BIDIRECTIONAL;
    
    @Column(name = "last_sync_at")
    private LocalDateTime lastSyncAt;
    
    @Column(name = "sync_status", length = 20)
    @Enumerated(EnumType.STRING)
    private SyncStatus syncStatus = SyncStatus.PENDING;
    
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
    
    public enum CalendarProvider {
        GOOGLE, OUTLOOK, APPLE, ICAL
    }
    
    public enum SyncDirection {
        IMPORT_ONLY,    // Importer seulement depuis le calendrier externe
        EXPORT_ONLY,    // Exporter seulement vers le calendrier externe
        BIDIRECTIONAL   // Synchronisation bidirectionnelle
    }
    
    public enum SyncStatus {
        PENDING,        // En attente de synchronisation
        SYNCING,        // Synchronisation en cours
        SUCCESS,        // Dernière synchronisation réussie
        ERROR,          // Erreur lors de la synchronisation
        DISABLED        // Synchronisation désactivée
    }
}