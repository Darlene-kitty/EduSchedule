package cm.iusjc.notification.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuration STOMP/WebSocket.
 *
 * Connexion : ws://notification-service:8087/ws  (SockJS fallback)
 * Topics publiés par le serveur :
 *   /topic/notifications          — toutes les notifications
 *   /topic/notifications/{userId} — notifications d'un utilisateur
 *   /topic/reservations           — événements de réservation
 *   /topic/schedule               — changements d'emploi du temps
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Broker simple en mémoire pour les topics
        registry.enableSimpleBroker("/topic", "/queue");
        // Préfixe pour les messages envoyés par les clients vers le serveur
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}
