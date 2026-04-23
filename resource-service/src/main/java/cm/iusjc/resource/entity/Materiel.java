package cm.iusjc.resource.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "materiels")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Materiel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String nom;

    @ManyToOne
    @JoinColumn(name = "type_materiel_id")
    private TypeMateriel typeMateriel;

    private String marque;
    private String modele;
    private String numeroSerie;
    private String ecole;
    private String salle;

    @Enumerated(EnumType.STRING)
    private EtatMateriel etat = EtatMateriel.BON_ETAT;

    private String dateAcquisition;
    private Long valeur;
    private String description;

    @Column(nullable = false)
    private Boolean active = true;

    public enum EtatMateriel {
        BON_ETAT, USAGE, EN_PANNE, EN_MAINTENANCE
    }
}
