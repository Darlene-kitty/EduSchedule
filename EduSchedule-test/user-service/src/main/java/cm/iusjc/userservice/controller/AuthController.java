package cm.iusjc.userservice.controller;

import cm.iusjc.userservice.dto.*;
import cm.iusjc.userservice.service.AuthService;
import cm.iusjc.userservice.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    
    private final AuthService authService;
    private final PasswordResetService passwordResetService;
    
    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@Valid @RequestBody RegisterRequest request) {
        UserDTO user = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }
    
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        UserDTO user = authService.getCurrentUser(username);
        return ResponseEntity.ok(user);
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        LoginResponse response = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestBody(required = false) RefreshTokenRequest request) {
        try {
            // Révoquer le refresh token si fourni
            if (request != null && request.getRefreshToken() != null && !request.getRefreshToken().isEmpty()) {
                authService.revokeRefreshToken(request.getRefreshToken());
                log.info("Refresh token revoked successfully");
            } else {
                log.info("Logout without refresh token - client-side logout only");
            }
            return ResponseEntity.ok("Logged out successfully");
        } catch (Exception e) {
            log.error("Error during logout: {}", e.getMessage());
            // Même en cas d'erreur, on confirme la déconnexion côté client
            return ResponseEntity.ok("Logged out successfully");
        }
    }
    
    // === ENDPOINTS POUR LA RÉINITIALISATION DE MOT DE PASSE ===
    
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            passwordResetService.initiatePasswordReset(request.getEmail());
            return ResponseEntity.ok(ApiResponse.success(
                "Si cet email existe dans notre système, vous recevrez un lien de réinitialisation."
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Une erreur est survenue lors de l'envoi de l'email."));
        }
    }
    
    @GetMapping("/reset-password/validate")
    public ResponseEntity<ApiResponse> validateResetToken(@RequestParam("token") String token) {
        boolean isValid = passwordResetService.validateResetToken(token);
        
        if (isValid) {
            return ResponseEntity.ok(ApiResponse.success("Token valide"));
        } else {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Token invalide ou expiré"));
        }
    }
    
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            passwordResetService.resetPassword(
                request.getToken(), 
                request.getNewPassword(), 
                request.getConfirmPassword()
            );
            return ResponseEntity.ok(ApiResponse.success(
                "Votre mot de passe a été modifié avec succès. Vous pouvez maintenant vous connecter."
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Une erreur est survenue lors de la réinitialisation du mot de passe."));
        }
    }
}
