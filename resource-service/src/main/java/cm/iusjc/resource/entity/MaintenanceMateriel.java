package cm.iusjc.resource.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Historique de maintenance d'un matériel (pannes, réparations, interventions).
 */
@Entity
@Table(name = "maintenance_materiels")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceMateriel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "materiel_id", nullable = false)
    private Materiel materiel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeIntervention typeIntervention;

    @Column(nullable = false)
    private LocalDateTime dateDebut;

    private LocalDateTime dateFin;

    @Column(nullable = false)
    private String description;

    private String technicien;

    private String coutReparation; // montant en FCFA (stocké en String pour flexibilité)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutIntervention statut = StatutIntervention.PLANIFIEE;

    /** Nombre d'heures d'utilisation au moment de la panne/intervention */
    private Long heuresUtilisationAuMoment;

    @Column(length = 1000)
    private String notes;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum TypeIntervention {
        PANNE, REPARATION, MAINTENANCE_PREVENTIVE, INSPECTION, REMPLACEMENT
    }

    public enum StatutIntervention {
        PLANIFIEE, EN_COURS, TERMINEE, ANNULEE
    }
}
