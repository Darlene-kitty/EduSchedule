package cm.iusjc.school.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "categories_ue")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategorieUE {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String nom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeCategorie type = TypeCategorie.FONDAMENTALE;

    private Integer credits = 3;
    private Integer volumeHoraire = 30;
    private Integer coefficient = 2;
    private String description;
    private String couleur = "#1D4ED8";

    @Column(nullable = false)
    private Boolean active = true;

    public enum TypeCategorie {
        FONDAMENTALE, TRANSVERSALE, PROFESSIONNELLE, OPTIONNELLE
    }
}
