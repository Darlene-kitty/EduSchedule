package cm.iusjc.school.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategorieUEDTO {
    private Long id;

    @NotBlank(message = "Code is required")
    private String code;

    @NotBlank(message = "Nom is required")
    private String nom;

    private String type;
    private Integer credits;
    private Integer volumeHoraire;
    private Integer coefficient;
    private String description;
    private String couleur;
    private boolean active = true;
}
