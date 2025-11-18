package cm.iusjc.school.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "groupes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Groupe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String code;
    private Integer capacite;

    @ManyToOne
    @JoinColumn(name = "niveau_id", nullable = false)
    private Niveau niveau;

    @Column(nullable = false)
    private Boolean active = true;
}
