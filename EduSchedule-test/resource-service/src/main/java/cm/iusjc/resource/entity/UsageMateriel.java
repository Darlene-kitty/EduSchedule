package cm.iusjc.resource.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Suivi de l'utilisation d'un matériel lors d'une réservation/séance.
 * Permet de calculer l'usure réelle et de déclencher des alertes de maintenance préventive.
 */
@Entity
@Table(name = "usage_materiels")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsageMateriel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "materiel_id", nullable = false)
    private Materiel materiel;

    /** ID de la réservation dans le reservation-service */
    @Column(name = "reservation_id")
    private Long reservationId;

    /** ID du cours associé (optionnel) */
    @Column(name = "cours_id")
    private Long coursId;

    /** Type de cours : CM, TD, TP, EXAMEN */
    @Column(name = "type_cours")
    private String typeCours;

    @Column(nullable = false)
    private LocalDateTime dateDebut;

    @Column(nullable = false)
    private LocalDateTime dateFin;

    /** Durée calculée en minutes */
    @Column(name = "duree_minutes")
    private Long dureeMinutes;

    /** Signalement d'un problème pendant l'utilisation */
    @Column(name = "probleme_signale")
    private Boolean problemeSignale = false;

    @Column(name = "description_probleme", length = 500)
    private String descriptionProbleme;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (dateDebut != null && dateFin != null) {
            dureeMinutes = java.time.Duration.between(dateDebut, dateFin).toMinutes();
        }
    }
}
