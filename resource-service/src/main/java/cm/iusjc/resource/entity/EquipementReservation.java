package cm.iusjc.resource.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Réservation d'un équipement spécifique liée à une réservation de salle.
 * Permet de tracer quels équipements ont été alloués pour quel cours/type de cours.
 */
@Entity
@Table(name = "equipement_reservations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EquipementReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ID de la réservation dans le reservation-service */
    @Column(name = "reservation_id", nullable = false)
    private Long reservationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "salle_materiel_id", nullable = false)
    private SalleMateriel salleMateriel;

    /** Quantité réservée */
    @Column(nullable = false)
    private Integer quantite = 1;

    /** Type de cours pour lequel l'équipement est réservé (CM, TD, TP, EXAM…) */
    @Column(name = "type_cours", length = 20)
    private String typeCours;

    @Column(name = "date_debut", nullable = false)
    private LocalDateTime dateDebut;

    @Column(name = "date_fin", nullable = false)
    private LocalDateTime dateFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutReservationEquipement statut = StatutReservationEquipement.ACTIVE;

    @Column(length = 500)
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum StatutReservationEquipement {
        ACTIVE, TERMINEE, ANNULEE
    }
}
