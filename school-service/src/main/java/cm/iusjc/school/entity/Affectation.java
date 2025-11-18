package cm.iusjc.school.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "affectations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Affectation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long etudiantId;

    @ManyToOne
    @JoinColumn(name = "groupe_id", nullable = false)
    private Groupe groupe;

    @Column(nullable = false)
    private LocalDate dateDebut;

    private LocalDate dateFin;

    @Column(nullable = false)
    private Boolean active = true;
}
