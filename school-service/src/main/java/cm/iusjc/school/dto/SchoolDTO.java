package cm.iusjc.school.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SchoolDTO {
    
    private Long id;
    
    // Champ principal — accepte aussi "nom" via alias dans le service
    @NotBlank(message = "School name is required")
    @Size(min = 2, max = 100, message = "School name must be between 2 and 100 characters")
    private String name;
    
    // Alias frontend : "sigle"
    private String sigle;
    
    @Size(max = 20, message = "School code cannot exceed 20 characters")
    private String code;
    
    @Size(max = 255, message = "Address cannot exceed 255 characters")
    private String address;
    
    @Size(max = 100, message = "City cannot exceed 100 characters")
    private String city;
    
    @Size(max = 20, message = "Postal code cannot exceed 20 characters")
    private String postalCode;
    
    @Size(max = 100, message = "Country cannot exceed 100 characters")
    private String country;
    
    @Size(max = 20, message = "Phone cannot exceed 20 characters")
    private String phone;
    
    // Alias frontend : "telephone"
    private String telephone;
    
    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String email;
    
    @Size(max = 255, message = "Website cannot exceed 255 characters")
    private String website;
    
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
    
    // Alias frontend : "directeur"
    private String directeur;
    
    // Alias frontend : "couleur"
    private String couleur;
    
    // Alias frontend : "filieres" / "niveaux"
    private List<String> filieres;
    private List<String> niveaux;
    
    // "active" côté backend, "enabled" côté frontend
    private boolean active = true;
    private Boolean enabled;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}