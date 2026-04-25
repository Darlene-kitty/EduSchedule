package cm.iusjc.school.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupeDTO {
    private Long id;

    @NotBlank(message = "Groupe name is required")
    private String name;

    private String code;
    private Integer capacite;

    @NotNull(message = "Niveau ID is required")
    private Long niveauId;

    private String niveauName;
    private String niveauCode;

    // Filière (parent du niveau)
    private Long filiereId;
    private String filiereName;
    private String filiereCode;

    // École (parent de la filière)
    private Long schoolId;
    private String schoolName;
    private String schoolCode;
    private String schoolCouleur;

    private boolean active = true;

    /** Nombre d'étudiants actuellement affectés (calculé dynamiquement, non persisté) */
    private int effectif = 0;

    /** Places disponibles = capacite - effectif */
    public int getPlacesDisponibles() {
        if (capacite == null) return Integer.MAX_VALUE;
        return Math.max(0, capacite - effectif);
    }
}
