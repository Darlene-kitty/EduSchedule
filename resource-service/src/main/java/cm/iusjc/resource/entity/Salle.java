package cm.iusjc.resource.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "salles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Salle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    private String batiment;
    private String etage;
    private Integer capacite;

    @Enumerated(EnumType.STRING)
    private TypeSalle type;

    @Column(nullable = false)
    private Boolean disponible = true;

    @Column(nullable = false)
    private Boolean active = true;

    /** Référence optionnelle à l'école (school-service ID) pour le filtrage */
    @Column(name = "school_id")
    private Long schoolId;

    public enum TypeSalle {
        AMPHITHEATRE, SALLE_COURS, LABORATOIRE, SALLE_TP, SALLE_TD, BIBLIOTHEQUE
    }
}
