package cm.iusjc.school.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NiveauDTO {
    private Long id;

    @NotBlank(message = "Niveau name is required")
    private String name;

    private String code;
    private Integer ordre;

    @NotNull(message = "Filiere ID is required")
    private Long filiereId;

    private String filiereName;
    private boolean active = true;
}
