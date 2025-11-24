package cm.iusjc.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuration CORS pour l'API Gateway
 * Permet au frontend React de communiquer avec le backend
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        
        // Origines autorisées (frontend)
        corsConfig.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:*",         // Tous les ports localhost
            "http://frontend:*",          // Tous les ports Docker
            "http://127.0.0.1:*"          // Tous les ports 127.0.0.1
        ));
        
        // Méthodes HTTP autorisées
        corsConfig.setAllowedMethods(Arrays.asList(
            "GET", 
            "POST", 
            "PUT", 
            "DELETE", 
            "PATCH", 
            "OPTIONS"
        ));
        
        // Headers autorisés
        corsConfig.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "Accept",
            "Origin",
            "X-Requested-With"
        ));
        
        // Headers exposés au client
        corsConfig.setExposedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type"
        ));
        
        // Autoriser les credentials (cookies, tokens)
        corsConfig.setAllowCredentials(true);
        
        // Durée de cache de la configuration CORS (en secondes)
        corsConfig.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        
        return new CorsWebFilter(source);
    }
}
