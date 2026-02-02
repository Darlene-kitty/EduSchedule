package cm.iusjc.notification.listener;

import cm.iusjc.notification.service.WelcomeEmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventListener {
    
    private final WelcomeEmailService welcomeEmailService;
    private final ObjectMapper objectMapper;
    
    /**
     * Écoute les événements de création d'utilisateur
     */
    @RabbitListener(queues = "user.created.queue")
    public void handleUserCreated(String message) {
        try {
            log.info("Received user created event: {}", message);
            
            // Parser le message JSON
            Map<String, Object> eventData = objectMapper.readValue(message, Map.class);
            
            String eventType = (String) eventData.get("event");
            if (!"user.created".equals(eventType)) {
                log.warn("Unexpected event type: {}", eventType);
                return;
            }
            
            // Extraire les données utilisateur
            String userEmail = (String) eventData.get("email");
            String userName = (String) eventData.get("name");
            String userRole = (String) eventData.get("role");
            String schoolName = (String) eventData.get("schoolName");
            Long userId = eventData.get("userId") != null ? 
                Long.valueOf(eventData.get("userId").toString()) : null;
            
            if (userEmail == null || userName == null) {
                log.error("Missing required user data in event: email={}, name={}", userEmail, userName);
                return;
            }
            
            // Envoyer l'email de bienvenue
            if (schoolName != null && !schoolName.trim().isEmpty()) {
                welcomeEmailService.sendWelcomeEmailWithDetails(
                    userEmail, userName, userRole != null ? userRole : "USER", 
                    schoolName, null
                );
            } else {
                welcomeEmailService.sendWelcomeEmail(
                    userEmail, userName, userRole != null ? userRole : "USER"
                );
            }
            
            log.info("Welcome email processed for user: {} ({})", userName, userEmail);
            
        } catch (Exception e) {
            log.error("Failed to process user created event: {}", message, e);
        }
    }
    
    /**
     * Écoute les événements de mise à jour d'utilisateur (optionnel)
     */
    @RabbitListener(queues = "user.updated.queue")
    public void handleUserUpdated(String message) {
        try {
            log.info("Received user updated event: {}", message);
            
            Map<String, Object> eventData = objectMapper.readValue(message, Map.class);
            String eventType = (String) eventData.get("event");
            
            if (!"user.updated".equals(eventType)) {
                return;
            }
            
            // Pour l'instant, on ne fait rien pour les mises à jour
            // Mais on pourrait envoyer des notifications de changement de profil
            log.debug("User updated event processed");
            
        } catch (Exception e) {
            log.error("Failed to process user updated event: {}", message, e);
        }
    }
    
    /**
     * Écoute les événements d'activation de compte
     */
    @RabbitListener(queues = "user.activated.queue")
    public void handleUserActivated(String message) {
        try {
            log.info("Received user activated event: {}", message);
            
            Map<String, Object> eventData = objectMapper.readValue(message, Map.class);
            String eventType = (String) eventData.get("event");
            
            if (!"user.activated".equals(eventType)) {
                return;
            }
            
            String userEmail = (String) eventData.get("email");
            String userName = (String) eventData.get("name");
            
            if (userEmail != null && userName != null) {
                // Envoyer une notification d'activation
                // (Ceci pourrait être implémenté plus tard si nécessaire)
                log.info("User activation processed for: {} ({})", userName, userEmail);
            }
            
        } catch (Exception e) {
            log.error("Failed to process user activated event: {}", message, e);
        }
    }
}