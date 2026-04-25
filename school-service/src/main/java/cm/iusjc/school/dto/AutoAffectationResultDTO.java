package cm.iusjc.school.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AutoAffectationResultDTO {
    private int totalDemandes;
    private int affectes;
    private int ignores;       // déjà affectés et forceReaffectation=false
    private int rejetes;       // groupes pleins, aucune place disponible
    private List<AffectationDTO> affectations;
    private List<Long> etudiantsRejetes;
    private String message;
}
