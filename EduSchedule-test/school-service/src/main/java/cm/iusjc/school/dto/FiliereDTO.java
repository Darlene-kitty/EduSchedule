package cm.iusjc.school.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FiliereDTO {
    private Long id;

    @NotBlank(message = "Filiere name is required")
    private String name;

    private String code;
    private String description;

    @NotNull(message = "School ID is required")
    private Long schoolId;

    private String schoolName;
    private boolean active = true;
}
