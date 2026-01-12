package cm.iusjc.reservation.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "resource_id", nullable = false)
    private Long resourceId; // ID de la salle/ressource
    
    @Column(name = "course_id")
    private Long courseId; // ID du cours (optionnel)
    
    @Column(name = "course_group_id")
    private Long courseGroupId; // ID du groupe de cours (optionnel)
    
    @Column(name = "user_id", nullable = false)
    private Long userId; // ID de l'utilisateur qui fait la réservation
    
    @Column(nullable = false, length = 100)
    private String title; // Titre de la réservation
    
    @Column(length = 500)
    private String description; // Description détaillée
    
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime; // Heure de début
    
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime; // Heure de fin
    
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ReservationStatus status = ReservationStatus.PENDING; // Statut de la réservation
    
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ReservationType type; // Type de réservation
    
    @Column(name = "recurring_pattern")
    private String recurringPattern; // Pattern de récurrence (JSON)
    
    @Column(name = "parent_reservation_id")
    private Long parentReservationId; // Pour les réservations récurrentes
    
    @Column(name = "expected_attendees")
    private Integer expectedAttendees; // Nombre d'participants attendus
    
    @Column(name = "setup_time")
    private Integer setupTime = 0; // Temps de préparation en minutes
    
    @Column(name = "cleanup_time")
    private Integer cleanupTime = 0; // Temps de nettoyage en minutes
    
    @Column(length = 500)
    private String notes; // Notes additionnelles
    
    @Column(name = "approved_by")
    private Long approvedBy; // ID de l'utilisateur qui a approuvé
    
    @Column(name = "approved_at")
    private LocalDateTime approvedAt; // Date d'approbation
    
    @Column(name = "cancelled_by")
    private Long cancelledBy; // ID de l'utilisateur qui a annulé
    
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt; // Date d'annulation
    
    @Column(name = "cancellation_reason")
    private String cancellationReason; // Raison de l'annulation
    
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
    
    // Méthodes utilitaires
    public boolean isActive() {
        return status == ReservationStatus.CONFIRMED || status == ReservationStatus.PENDING;
    }
    
    public boolean isRecurring() {
        return recurringPattern != null && !recurringPattern.isEmpty();
    }
    
    public boolean isConflictWith(LocalDateTime start, LocalDateTime end) {
        return isActive() && 
               startTime.isBefore(end) && 
               endTime.isAfter(start);
    }
    
    public LocalDateTime getEffectiveStartTime() {
        return startTime.minusMinutes(setupTime);
    }
    
    public LocalDateTime getEffectiveEndTime() {
        return endTime.plusMinutes(cleanupTime);
    }
}