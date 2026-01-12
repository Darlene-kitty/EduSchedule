package cm.iusjc.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;

/**
 * Configuration spécifique au développement
 * Recharge le fichier .env et affiche les variables importantes
 */
@Configuration
@Profile({"dev", "development"})
public class DevelopmentConfig {

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🔧 MODE DÉVELOPPEMENT - Variables d'environnement");
        System.out.println("=".repeat(60));
        
        // Afficher les variables importantes
        printEnvVar("SPRING_DATASOURCE_URL", "URL de la base de données");
        printEnvVar("SPRING_DATASOURCE_USERNAME", "Utilisateur DB");
        printEnvVar("JWT_SECRET", "Clé JWT", true);
        printEnvVar("MAIL_HOST", "Serveur SMTP");
        printEnvVar("MAIL_USERNAME", "Email SMTP");
        printEnvVar("EUREKA_CLIENT_SERVICEURL_DEFAULTZONE", "Eureka Server");
        printEnvVar("SPRING_PROFILES_ACTIVE", "Profil actif");
        
        System.out.println("=".repeat(60));
        System.out.println("✅ Application prête !");
        System.out.println("=".repeat(60) + "\n");
    }
    
    private void printEnvVar(String key, String description) {
        printEnvVar(key, description, false);
    }
    
    private void printEnvVar(String key, String description, boolean mask) {
        String value = System.getProperty(key);
        if (value == null) {
            value = System.getenv(key);
        }
        
        if (value != null) {
            String displayValue = mask ? maskValue(value) : value;
            System.out.printf("  %-35s: %s%n", description, displayValue);
        } else {
            System.out.printf("  %-35s: ❌ NON DÉFINIE%n", description);
        }
    }
    
    private String maskValue(String value) {
        if (value == null || value.length() <= 8) {
            return "***";
        }
        return value.substring(0, 4) + "***" + value.substring(value.length() - 4);
    }
}