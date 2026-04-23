package cm.iusjc.resource.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "equipements")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Equipement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @ManyToOne
    @JoinColumn(name = "salle_id")
    private Salle salle;

    @Enumerated(EnumType.STRING)
    private TypeEquipement type;

    @Column(nullable = false)
    private Boolean fonctionnel = true;

    public enum TypeEquipement {
        PROJECTEUR, ORDINATEUR, TABLEAU_BLANC, CLIMATISATION, SONO, AUTRE
    }
}
