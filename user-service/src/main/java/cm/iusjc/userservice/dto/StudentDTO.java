package cm.iusjc.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentDTO {
    private Long id;

    @NotBlank(message = "Matricule is required")
    private String matricule;

    @NotBlank(message = "Nom is required")
    private String nom;

    @NotBlank(message = "Prenom is required")
    private String prenom;

    @Email(message = "Email invalide")
    @NotBlank(message = "Email is required")
    private String email;

    private String telephone;
    private String filiere;
    private String niveau;
    private String classe;
    private String dateNaissance;
    private Boolean enabled = true;
}
