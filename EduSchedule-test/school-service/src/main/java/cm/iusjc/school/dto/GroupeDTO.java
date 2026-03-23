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
    private boolean active = true;
}
