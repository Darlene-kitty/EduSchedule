package cm.iusjc.school.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AutoAffectationRequestDTO {

    /**
     * Liste des IDs d'étudiants (user-service IDs) à affecter automatiquement.
     * L'algorithme round-robin les distribue équitablement entre les groupes du niveau,
     * en respectant la capacité maximale de chaque groupe.
     */
    @NotEmpty(message = "La liste des étudiants ne peut pas être vide")
    private List<Long> etudiantIds;

    /**
     * Si true, les étudiants déjà affectés à un groupe du même niveau seront
     * réaffectés (leur ancienne affectation sera clôturée).
     * Si false (défaut), les étudiants déjà affectés sont ignorés.
     */
    private boolean forceReaffectation = false;
}
