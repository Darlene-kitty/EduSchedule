package cm.iusjc.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * Configuration pour charger les variables d'environnement depuis le fichier .env
 * Cette classe charge automatiquement le fichier .env au démarrage de l'application
 */
@Configuration
public class DotEnvConfig {

    @PostConstruct
    public void loadDotEnv() {
        try {
            // Charger le fichier .env depuis la racine du projet
            Dotenv dotenv = Dotenv.configure()
                    .directory("./")  // Chercher dans le répertoire racine
                    .ignoreIfMalformed()
                    .ignoreIfMissing()
                    .load();

            // Définir toutes les variables d'environnement comme propriétés système
            dotenv.entries().forEach(entry -> {
                String key = entry.getKey();
                String value = entry.getValue();
                
                // Ne pas écraser les variables d'environnement existantes
                if (System.getProperty(key) == null && System.getenv(key) == null) {
                    System.setProperty(key, value);
                }
            });

            System.out.println("✅ Fichier .env chargé avec succès (" + dotenv.entries().size() + " variables)");
            
        } catch (Exception e) {
            System.err.println("⚠️ Erreur lors du chargement du fichier .env: " + e.getMessage());
            System.err.println("L'application continuera avec les variables d'environnement système");
        }
    }
}