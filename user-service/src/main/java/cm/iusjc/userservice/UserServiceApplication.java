package cm.iusjc.userservice;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
public class UserServiceApplication {
    
    static {
        // Charger le fichier .env avant le démarrage de Spring Boot
        loadDotEnv();
    }
    
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
    
    private static void loadDotEnv() {
        try {
            Dotenv dotenv = Dotenv.configure()
                    .directory("../")  // Remonter au répertoire parent (racine du projet)
                    .ignoreIfMalformed()
                    .ignoreIfMissing()
                    .load();

            // Définir les variables comme propriétés système
            dotenv.entries().forEach(entry -> {
                String key = entry.getKey();
                String value = entry.getValue();
                
                // Ne pas écraser les variables existantes
                if (System.getProperty(key) == null && System.getenv(key) == null) {
                    System.setProperty(key, value);
                }
            });

            System.out.println("✅ [USER-SERVICE] Fichier .env chargé (" + dotenv.entries().size() + " variables)");
            
        } catch (Exception e) {
            System.err.println("⚠️ [USER-SERVICE] Erreur chargement .env: " + e.getMessage());
        }
    }
}