package cm.iusjc.userservice.service;

import cm.iusjc.userservice.entity.PasswordResetToken;
import cm.iusjc.userservice.entity.User;
import cm.iusjc.userservice.repository.PasswordResetTokenRepository;
import cm.iusjc.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {
    
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    
    @Value("${app.password-reset.token-expiry-hours:24}")
    private int tokenExpiryHours;
    
    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;
    
    private final SecureRandom secureRandom = new SecureRandom();
    
    @Transactional
    public void initiatePasswordReset(String email) {
        log.info("Initiating password reset for email: {}", email);
        
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            log.warn("Password reset requested for non-existent email: {}", email);
            // Pour des raisons de sécurité, on ne révèle pas si l'email existe ou non
            return;
        }
        
        User user = userOpt.get();
        
        // Invalider tous les tokens existants pour cet utilisateur
        invalidateExistingTokens(user);
        
        // Générer un nouveau token
        String token = generateSecureToken();
        
        // Créer l'entité token
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusHours(tokenExpiryHours));
        resetToken.setUsed(false);
        
        passwordResetTokenRepository.save(resetToken);
        
        // Envoyer l'email
        sendPasswordResetEmail(user, token);
        
        log.info("Password reset token generated and email sent for user: {}", user.getUsername());
    }
    
    @Transactional
    public void resetPassword(String token, String newPassword, String confirmPassword) {
        log.info("Attempting password reset with token");
        
        // Vérifier que les mots de passe correspondent
        if (!newPassword.equals(confirmPassword)) {
            throw new RuntimeException("Les mots de passe ne correspondent pas");
        }
        
        // Trouver le token
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token de réinitialisation invalide"));
        
        // Vérifier si le token est expiré
        if (resetToken.isExpired()) {
            throw new RuntimeException("Le token de réinitialisation a expiré");
        }
        
        // Vérifier si le token a déjà été utilisé
        if (resetToken.getUsed()) {
            throw new RuntimeException("Ce token a déjà été utilisé");
        }
        
        // Mettre à jour le mot de passe
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        // Marquer le token comme utilisé
        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
        
        // Envoyer email de confirmation
        sendPasswordResetConfirmationEmail(user);
        
        log.info("Password reset successfully for user: {}", user.getUsername());
    }
    
    public boolean validateResetToken(String token) {
        Optional<PasswordResetToken> resetTokenOpt = passwordResetTokenRepository.findByToken(token);
        
        if (resetTokenOpt.isEmpty()) {
            return false;
        }
        
        PasswordResetToken resetToken = resetTokenOpt.get();
        return !resetToken.isExpired() && !resetToken.getUsed();
    }
    
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("Cleaning up expired password reset tokens");
        passwordResetTokenRepository.deleteByExpiryDateBefore(LocalDateTime.now());
    }
    
    private void invalidateExistingTokens(User user) {
        // Cette méthode nécessiterait une requête personnalisée dans le repository
        // Pour l'instant, on laisse les anciens tokens expirer naturellement
        log.debug("Invalidating existing tokens for user: {}", user.getUsername());
    }
    
    private String generateSecureToken() {
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }
    
    private void sendPasswordResetEmail(User user, String token) {
        String resetUrl = frontendUrl + "/reset-password?token=" + token;
        
        String subject = "Réinitialisation de votre mot de passe - EduSchedule";
        String message = buildPasswordResetEmailContent(user.getUsername(), resetUrl);
        
        emailService.sendEmail(user.getEmail(), subject, message);
    }
    
    private void sendPasswordResetConfirmationEmail(User user) {
        String subject = "Mot de passe modifié avec succès - EduSchedule";
        String message = buildPasswordResetConfirmationEmailContent(user.getUsername());
        
        emailService.sendEmail(user.getEmail(), subject, message);
    }
    
    private String buildPasswordResetEmailContent(String username, String resetUrl) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Réinitialisation de mot de passe</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #007bff; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background-color: #f9f9f9; }
                    .button { display: inline-block; padding: 12px 24px; background-color: #007bff; color: white; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                    .footer { padding: 20px; text-align: center; font-size: 12px; color: #666; }
                    .warning { background-color: #fff3cd; border: 1px solid #ffeaa7; padding: 15px; border-radius: 5px; margin: 15px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🎓 EduSchedule - IUSJC</h1>
                        <p>Réinitialisation de mot de passe</p>
                    </div>
                    
                    <div class="content">
                        <h2>Bonjour %s,</h2>
                        
                        <p>Vous avez demandé la réinitialisation de votre mot de passe pour votre compte EduSchedule.</p>
                        
                        <p>Cliquez sur le bouton ci-dessous pour créer un nouveau mot de passe :</p>
                        
                        <div style="text-align: center;">
                            <a href="%s" class="button">Réinitialiser mon mot de passe</a>
                        </div>
                        
                        <div class="warning">
                            <strong>⚠️ Important :</strong>
                            <ul>
                                <li>Ce lien est valide pendant 24 heures seulement</li>
                                <li>Si vous n'avez pas demandé cette réinitialisation, ignorez cet email</li>
                                <li>Ne partagez jamais ce lien avec personne</li>
                            </ul>
                        </div>
                        
                        <p>Si le bouton ne fonctionne pas, copiez et collez ce lien dans votre navigateur :</p>
                        <p style="word-break: break-all; background-color: #f1f1f1; padding: 10px; border-radius: 3px;">
                            %s
                        </p>
                    </div>
                    
                    <div class="footer">
                        <p>Cet email a été envoyé automatiquement par EduSchedule - IUSJC</p>
                        <p>Institut Universitaire Saint Jean - Cameroun</p>
                        <p>Si vous avez des questions, contactez l'administrateur système</p>
                    </div>
                </div>
            </body>
            </html>
            """, username, resetUrl, resetUrl);
    }
    
    private String buildPasswordResetConfirmationEmailContent(String username) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Mot de passe modifié</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #28a745; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background-color: #f9f9f9; }
                    .success { background-color: #d4edda; border: 1px solid #c3e6cb; padding: 15px; border-radius: 5px; margin: 15px 0; color: #155724; }
                    .footer { padding: 20px; text-align: center; font-size: 12px; color: #666; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🎓 EduSchedule - IUSJC</h1>
                        <p>Mot de passe modifié avec succès</p>
                    </div>
                    
                    <div class="content">
                        <h2>Bonjour %s,</h2>
                        
                        <div class="success">
                            <strong>✅ Succès !</strong><br>
                            Votre mot de passe a été modifié avec succès.
                        </div>
                        
                        <p>Votre mot de passe EduSchedule a été mis à jour le <strong>%s</strong>.</p>
                        
                        <p>Vous pouvez maintenant vous connecter avec votre nouveau mot de passe.</p>
                        
                        <p><strong>Si vous n'avez pas effectué cette modification :</strong></p>
                        <ul>
                            <li>Contactez immédiatement l'administrateur système</li>
                            <li>Votre compte pourrait être compromis</li>
                        </ul>
                        
                        <p>Pour votre sécurité, nous vous recommandons de :</p>
                        <ul>
                            <li>Utiliser un mot de passe fort et unique</li>
                            <li>Ne jamais partager vos identifiants</li>
                            <li>Vous déconnecter après chaque session</li>
                        </ul>
                    </div>
                    
                    <div class="footer">
                        <p>Cet email a été envoyé automatiquement par EduSchedule - IUSJC</p>
                        <p>Institut Universitaire Saint Jean - Cameroun</p>
                    </div>
                </div>
            </body>
            </html>
            """, username, LocalDateTime.now().toString());
    }
}