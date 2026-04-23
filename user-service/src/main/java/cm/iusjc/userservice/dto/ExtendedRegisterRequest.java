package cm.iusjc.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExtendedRegisterRequest {
    
    // Champs obligatoires existants
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Username can only contain letters, numbers, underscores and hyphens")
    private String username;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;
    
    @NotBlank(message = "Role is required")
    @Pattern(regexp = "^(ADMIN|TEACHER|STUDENT)$", message = "Role must be ADMIN, TEACHER or STUDENT")
    private String role;
    
    // Nouveaux champs optionnels
    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String firstName;
    
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;
    
    @Pattern(regexp = "^\\+?[0-9\\s\\-\\(\\)]{8,20}$", message = "Invalid phone number format")
    private String phone;
    
    @Size(max = 200, message = "Address must not exceed 200 characters")
    private String address;
    
    @Size(max = 100, message = "Department must not exceed 100 characters")
    private String department;
    
    @Size(max = 100, message = "Specialization must not exceed 100 characters")
    private String specialization;
    
    @Size(max = 500, message = "Bio must not exceed 500 characters")
    private String bio;
    
    // Pour les étudiants
    @Size(max = 50, message = "Student ID must not exceed 50 characters")
    private String studentId;
    
    @Pattern(regexp = "^(L1|L2|L3|M1|M2|DOCTORAT)$", message = "Level must be L1, L2, L3, M1, M2, or DOCTORAT")
    private String level;
    
    // Pour les enseignants
    @Size(max = 100, message = "Title must not exceed 100 characters")
    private String title; // Dr, Prof, etc.
    
    @Size(max = 100, message = "Office must not exceed 100 characters")
    private String office;
}