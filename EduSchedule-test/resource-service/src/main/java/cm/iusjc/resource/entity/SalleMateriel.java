package cm.iusjc.resource.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Relation formelle entre une Salle et un Matériel.
 * Remplace la simple liste de strings dans Room.equipments.
 * Permet de suivre la quantité, l'état et la disponibilité de chaque
 * équipement dans chaque salle.
 */
@Entity
@Table(name = "salle_materiels",
       uniqueConstraints = @UniqueConstraint(columnNames = {"salle_id", "materiel_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalleMateriel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "salle_id", nullable = false)
    private Salle salle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "materiel_id", nullable = false)
    private Materiel materiel;

    /** Quantité totale de cet équipement dans la salle */
    @Column(nullable = false)
    private Integer quantiteTotale = 1;

    /** Quantité actuellement disponible (non réservée) */
    @Column(nullable = false)
    private Integer quantiteDisponible = 1;

    /** Quantité actuellement réservée */
    @Column(nullable = false)
    private Integer quantiteReservee = 0;

    /** Indique si cet équipement est requis pour ce type de salle */
    @Column(nullable = false)
    private Boolean requis = false;

    /** Notes sur l'installation ou l'état dans cette salle */
    @Column(length = 500)
    private String notes;

    @Column(name = "date_installation")
    private LocalDateTime dateInstallation;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (quantiteDisponible == null) quantiteDisponible = quantiteTotale;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /** Vérifie si la quantité demandée est disponible */
    public boolean isDisponible(int quantiteDemandee) {
        return quantiteDisponible >= quantiteDemandee;
    }

    /** Réserve une quantité d'équipement */
    public void reserver(int quantite) {
        if (quantite > quantiteDisponible) {
            throw new IllegalStateException(
                "Quantité demandée (" + quantite + ") supérieure à la quantité disponible (" + quantiteDisponible + ")");
        }
        quantiteDisponible -= quantite;
        quantiteReservee += quantite;
    }

    /** Libère une quantité d'équipement réservé */
    public void liberer(int quantite) {
        quantiteReservee = Math.max(0, quantiteReservee - quantite);
        quantiteDisponible = Math.min(quantiteTotale, quantiteDisponible + quantite);
    }
}
