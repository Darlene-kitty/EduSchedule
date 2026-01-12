package cm.iusjc.userservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class WelcomeEmailService {
    
    private final RestTemplate restTemplate;
    
    @Value("${app.notification-service.url:http://localhost:8082}")
    private String notificationServiceUrl;
    
    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;
    
    public void sendWelcomeEmail(String email, String username, String temporaryPassword) {
        try {
            log.info("Sending welcome email to: {}", email);
            
            // Vérifier si le service de notification est disponible
            if (!isNotificationServiceAvailable()) {
                log.warn("Notification service is not available. Skipping welcome email for: {}", email);
                return;
            }
            
            String subject = "Bienvenue sur EduSchedule - Vos identifiants de connexion";
            String message = buildWelcomeMessage(username, temporaryPassword);
            
            Map<String, Object> emailRequest = new HashMap<>();
            emailRequest.put("to", email);
            emailRequest.put("subject", subject);
            emailRequest.put("message", message);
            emailRequest.put("type", "WELCOME");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(emailRequest, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(
                notificationServiceUrl + "/api/v1/notifications/send", 
                request, 
                String.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Welcome email sent successfully to: {}", email);
            } else {
                log.error("Failed to send welcome email to: {}. Status: {}", email, response.getStatusCode());
            }
            
        } catch (Exception e) {
            log.error("Error sending welcome email to: {}. Error: {}", email, e.getMessage());
            // Ne pas faire échouer la création d'utilisateur si l'email échoue
        }
    }
    
    private boolean isNotificationServiceAvailable() {
        try {
            // Test simple de connectivité au service de notification
            restTemplate.getForEntity(notificationServiceUrl + "/actuator/health", String.class);
            return true;
        } catch (Exception e) {
            log.debug("Notification service not available: {}", e.getMessage());
            return false;
        }
    }
    
    private String buildWelcomeMessage(String username, String temporaryPassword) {
        return String.format("""
            Bonjour %s,
            
            Bienvenue sur EduSchedule ! Votre compte a été créé avec succès.
            
            Vos identifiants de connexion :
            • Nom d'utilisateur : %s
            • Mot de passe temporaire : %s
            
            🔗 Connectez-vous ici : %s/login
            
            ⚠️ IMPORTANT : Pour votre sécurité, nous vous recommandons fortement de changer votre mot de passe lors de votre première connexion.
            
            Vous pouvez changer votre mot de passe en utilisant la fonction "Mot de passe oublié" ou en contactant votre administrateur.
            
            Si vous avez des questions, n'hésitez pas à contacter l'équipe support.
            
            Cordialement,
            L'équipe EduSchedule
            """, 
            username, username, temporaryPassword, frontendUrl);
    }
    
    public void sendAccountActivationEmail(String email, String username, String activationToken) {
        try {
            log.info("Sending account activation email to: {}", email);
            
            String subject = "Activez votre compte EduSchedule";
            String message = buildActivationMessage(username, activationToken);
            
            Map<String, Object> emailRequest = new HashMap<>();
            emailRequest.put("to", email);
            emailRequest.put("subject", subject);
            emailRequest.put("message", message);
            emailRequest.put("type", "ACTIVATION");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(emailRequest, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(
                notificationServiceUrl + "/api/v1/notifications/send", 
                request, 
                String.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Activation email sent successfully to: {}", email);
            } else {
                log.error("Failed to send activation email to: {}. Status: {}", email, response.getStatusCode());
            }
            
        } catch (Exception e) {
            log.error("Error sending activation email to: {}. Error: {}", email, e.getMessage(), e);
        }
    }
    
    private String buildActivationMessage(String username, String activationToken) {
        String activationUrl = frontendUrl + "/activate-account?token=" + activationToken;
        
        return String.format("""
            Bonjour %s,
            
            Bienvenue sur EduSchedule ! Votre compte a été créé avec succès.
            
            Pour activer votre compte et définir votre mot de passe, cliquez sur le lien ci-dessous :
            
            🔗 Activer mon compte : %s
            
            Ce lien est valide pendant 24 heures.
            
            Si vous n'avez pas demandé la création de ce compte, vous pouvez ignorer cet email.
            
            Cordialement,
            L'équipe EduSchedule
            """, 
            username, activationUrl);
    }
}