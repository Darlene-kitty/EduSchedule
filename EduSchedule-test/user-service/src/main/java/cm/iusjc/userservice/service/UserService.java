package cm.iusjc.userservice.service;

import cm.iusjc.userservice.dto.RegisterRequest;
import cm.iusjc.userservice.dto.UserDTO;
import cm.iusjc.userservice.dto.ProfileUpdateRequest;
import cm.iusjc.userservice.dto.PasswordChangeRequest;
import cm.iusjc.userservice.entity.User;
import cm.iusjc.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final WelcomeEmailService welcomeEmailService;
    
    @Transactional
    public UserDTO createUser(RegisterRequest request) {
        log.info("Creating new user: {}", request.getUsername());
        
        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("Username already exists: {}", request.getUsername());
            throw new RuntimeException("Le nom d'utilisateur '" + request.getUsername() + "' est déjà utilisé");
        }
        
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Email already exists: {}", request.getEmail());
            throw new RuntimeException("L'adresse email '" + request.getEmail() + "' est déjà utilisée");
        }
        
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setEnabled(true);
        // Définir les nouveaux champs Spring Security
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        
        User savedUser = userRepository.save(user);
        
        // Envoyer l'email de bienvenue avec le mot de passe en clair
        try {
            welcomeEmailService.sendWelcomeEmail(
                savedUser.getEmail(), 
                savedUser.getUsername(), 
                request.getPassword() // Mot de passe en clair pour l'email
            );
            log.info("Welcome email sent for user: {}", savedUser.getUsername());
        } catch (Exception e) {
            log.error("Failed to send welcome email for user: {}. Error: {}", savedUser.getUsername(), e.getMessage());
            // Ne pas faire échouer la création si l'email échoue
        }
        
        return convertToDTO(savedUser);
    }
    
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public UserDTO getUserById(Long id) {
        log.debug("Fetching user by id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur avec l'ID " + id + " introuvable"));
        return convertToDTO(user);
    }
    
    public UserDTO getUserByUsername(String username) {
        log.debug("Fetching user by username: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Utilisateur '" + username + "' introuvable"));
        return convertToDTO(user);
    }
    
    public List<UserDTO> getUsersByRole(String role) {
        return userRepository.findByRole(role).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public UserDTO updateUser(Long id, RegisterRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());
        
        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }
    
    @Transactional
    public void deleteUser(Long id) {
        log.info("Deleting user with id: {}", id);
        if (!userRepository.existsById(id)) {
            log.warn("User not found for deletion: {}", id);
            throw new RuntimeException("Utilisateur avec l'ID " + id + " introuvable");
        }
        userRepository.deleteById(id);
        log.info("User deleted successfully: {}", id);
    }
    
    private UserDTO convertToDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getEnabled(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
    
    @Transactional
    public UserDTO updateProfile(String currentUsername, ProfileUpdateRequest request) {
        log.info("Updating profile for user: {}", currentUsername);
        
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        
        // Vérifier si le nouveau username est déjà pris (sauf si c'est le même)
        if (!user.getUsername().equals(request.getUsername()) && 
            userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Le nom d'utilisateur '" + request.getUsername() + "' est déjà utilisé");
        }
        
        // Vérifier si le nouvel email est déjà pris (sauf si c'est le même)
        if (!user.getEmail().equals(request.getEmail()) && 
            userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("L'adresse email '" + request.getEmail() + "' est déjà utilisée");
        }
        
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        
        // Seuls les admins peuvent changer le rôle
        if (request.getRole() != null && !request.getRole().isEmpty()) {
            user.setRole(request.getRole());
        }
        
        User updatedUser = userRepository.save(user);
        log.info("Profile updated successfully for user: {}", updatedUser.getUsername());
        
        return convertToDTO(updatedUser);
    }
    
    @Transactional
    public void changePassword(String username, PasswordChangeRequest request) {
        log.info("Changing password for user: {}", username);
        
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Les mots de passe ne correspondent pas");
        }
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        
        // Vérifier l'ancien mot de passe
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Mot de passe actuel incorrect");
        }
        
        // Mettre à jour le mot de passe
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        
        log.info("Password changed successfully for user: {}", username);
    }
}
