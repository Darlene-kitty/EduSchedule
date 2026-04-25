package cm.iusjc.school.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AffectationDTO {
    private Long id;
    private Long etudiantId;
    private Long groupeId;
    private String groupeName;
    private String groupeCode;
    private Long niveauId;
    private String niveauName;
    private Long filiereId;
    private String filiereName;
    private Long schoolId;
    private String schoolName;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private boolean active;
}
