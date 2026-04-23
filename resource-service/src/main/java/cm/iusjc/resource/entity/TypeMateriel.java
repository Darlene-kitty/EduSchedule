package cm.iusjc.resource.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "types_materiel")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TypeMateriel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String nom;

    private String icone = "devices";
    private String couleur = "#1D4ED8";
    private String description;

    @Column(nullable = false)
    private Boolean active = true;
}
