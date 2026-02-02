package cm.iusjc.userservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserEventPublisher {
    
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    
    private static final String USER_EXCHANGE = "user-exchange";
    
    /**
     * Publie un événement de création d'utilisateur
     */
    public void publishUserCreated(Long userId, String username, String email, String role, String schoolName) {
        try {
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("event", "user.created");
            eventData.put("userId", userId);
            eventData.put("name", username);
            eventData.put("email", email);
            eventData.put("role", role);
            eventData.put("schoolName", schoolName);
            eventData.put("timestamp", System.currentTimeMillis());
            
            String message = objectMapper.writeValueAsString(eventData);
            
            rabbitTemplate.convertAndSend(USER_EXCHANGE, "user.created", message);
            
            log.info("Published user.created event for user: {} ({})", username, email);
            
        } catch (Exception e) {
            log.error("Failed to publish user.created event for user: {} ({})", username, email, e);
        }
    }
    
    /**
     * Publie un événement de mise à jour d'utilisateur
     */
    public void publishUserUpdated(Long userId, String username, String email, String role) {
        try {
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("event", "user.updated");
            eventData.put("userId", userId);
            eventData.put("name", username);
            eventData.put("email", email);
            eventData.put("role", role);
            eventData.put("timestamp", System.currentTimeMillis());
            
            String message = objectMapper.writeValueAsString(eventData);
            
            rabbitTemplate.convertAndSend(USER_EXCHANGE, "user.updated", message);
            
            log.info("Published user.updated event for user: {} ({})", username, email);
            
        } catch (Exception e) {
            log.error("Failed to publish user.updated event for user: {} ({})", username, email, e);
        }
    }
    
    /**
     * Publie un événement d'activation d'utilisateur
     */
    public void publishUserActivated(Long userId, String username, String email) {
        try {
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("event", "user.activated");
            eventData.put("userId", userId);
            eventData.put("name", username);
            eventData.put("email", email);
            eventData.put("timestamp", System.currentTimeMillis());
            
            String message = objectMapper.writeValueAsString(eventData);
            
            rabbitTemplate.convertAndSend(USER_EXCHANGE, "user.activated", message);
            
            log.info("Published user.activated event for user: {} ({})", username, email);
            
        } catch (Exception e) {
            log.error("Failed to publish user.activated event for user: {} ({})", username, email, e);
        }
    }
}