package cm.iusjc.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WelcomeEmailService {
    
    private final NotificationService notificationService;
    
    /**
     * Envoie un email de bienvenue à un nouvel utilisateur
     */
    public void sendWelcomeEmail(String userEmail, String userName, String userRole) {
        try {
            String subject = "Bienvenue sur EduSchedule - Votre compte a été créé avec succès !";
            String message = buildWelcomeMessage(userName, userRole);
            
            notificationService.createNotification(userEmail, subject, message, "EMAIL");
            
            log.info("Welcome email queued for user: {} ({})", userName, userEmail);
            
        } catch (Exception e) {
            log.error("Failed to send welcome email to user: {} ({})", userName, userEmail, e);
        }
    }
    
    /**
     * Construit le message de bienvenue personnalisé
     */
    private String buildWelcomeMessage(String userName, String userRole) {
        StringBuilder message = new StringBuilder();
        
        message.append("Bonjour ").append(userName).append(",\n\n");
        
        message.append("Bienvenue sur EduSchedule !\n\n");
        
        message.append("Votre compte a été créé avec succès. Vous pouvez maintenant accéder à toutes les fonctionnalités de notre plateforme de gestion d'emplois du temps.\n\n");
        
        // Message personnalisé selon le rôle
        switch (userRole.toUpperCase()) {
            case "TEACHER":
            case "ENSEIGNANT":
                message.append("En tant qu'enseignant, vous pourrez :\n");
                message.append("• Consulter vos emplois du temps\n");
                message.append("• Gérer vos disponibilités\n");
                message.append("• Réserver des salles et équipements\n");
                message.append("• Recevoir des notifications importantes\n");
                message.append("• Synchroniser avec votre calendrier personnel\n");
                break;
                
            case "STUDENT":
            case "ETUDIANT":
                message.append("En tant qu'étudiant, vous pourrez :\n");
                message.append("• Consulter vos emplois du temps\n");
                message.append("• Voir les informations sur vos cours\n");
                message.append("• Recevoir des notifications de changements\n");
                message.append("• Accéder aux ressources pédagogiques\n");
                break;
                
            case "ADMIN":
            case "ADMINISTRATEUR":
                message.append("En tant qu'administrateur, vous avez accès à :\n");
                message.append("• La gestion complète des emplois du temps\n");
                message.append("• La gestion des utilisateurs et des rôles\n");
                message.append("• La gestion des salles et équipements\n");
                message.append("• Les rapports et statistiques\n");
                message.append("• La configuration du système\n");
                break;
                
            default:
                message.append("Vous avez maintenant accès à toutes les fonctionnalités qui correspondent à votre profil.\n");
                break;
        }
        
        message.append("\n");
        message.append("Pour commencer :\n");
        message.append("1. Connectez-vous à votre compte avec vos identifiants\n");
        message.append("2. Complétez votre profil si nécessaire\n");
        message.append("3. Explorez les différentes fonctionnalités disponibles\n");
        message.append("4. N'hésitez pas à consulter l'aide en ligne si vous avez des questions\n\n");
        
        message.append("Quelques conseils pour bien démarrer :\n");
        message.append("• Vérifiez vos paramètres de notification\n");
        message.append("• Configurez vos préférences d'affichage\n");
        message.append("• Explorez le tableau de bord pour avoir une vue d'ensemble\n\n");
        
        message.append("Si vous rencontrez des difficultés ou avez des questions, notre équipe support est là pour vous aider.\n\n");
        
        message.append("Nous vous souhaitons une excellente expérience avec EduSchedule !\n\n");
        
        message.append("Cordialement,\n");
        message.append("L'équipe EduSchedule\n");
        message.append("Institut Universitaire Saint Jean\n\n");
        
        message.append("---\n");
        message.append("Cet email a été envoyé automatiquement. Merci de ne pas y répondre.\n");
        message.append("Pour toute question, contactez-nous à : support@iusjc.cm");
        
        return message.toString();
    }
    
    /**
     * Envoie un email de bienvenue avec des informations supplémentaires
     */
    public void sendWelcomeEmailWithDetails(String userEmail, String userName, String userRole, 
                                          String schoolName, String additionalInfo) {
        try {
            String subject = "Bienvenue sur EduSchedule - " + schoolName;
            String message = buildDetailedWelcomeMessage(userName, userRole, schoolName, additionalInfo);
            
            notificationService.createNotification(userEmail, subject, message, "EMAIL");
            
            log.info("Detailed welcome email queued for user: {} ({}) at {}", userName, userEmail, schoolName);
            
        } catch (Exception e) {
            log.error("Failed to send detailed welcome email to user: {} ({})", userName, userEmail, e);
        }
    }
    
    /**
     * Construit un message de bienvenue détaillé avec informations sur l'école
     */
    private String buildDetailedWelcomeMessage(String userName, String userRole, String schoolName, String additionalInfo) {
        StringBuilder message = new StringBuilder();
        
        message.append("Bonjour ").append(userName).append(",\n\n");
        
        message.append("Bienvenue sur EduSchedule - ").append(schoolName).append(" !\n\n");
        
        message.append("Nous sommes ravis de vous accueillir sur notre plateforme de gestion d'emplois du temps.\n");
        message.append("Votre compte a été créé avec succès et vous fait maintenant partie de la communauté ")
                .append(schoolName).append(".\n\n");
        
        if (additionalInfo != null && !additionalInfo.trim().isEmpty()) {
            message.append("Informations importantes :\n");
            message.append(additionalInfo).append("\n\n");
        }
        
        // Ajouter le message standard
        message.append(buildWelcomeMessage(userName, userRole).substring(
            buildWelcomeMessage(userName, userRole).indexOf("Vous pouvez maintenant")
        ));
        
        return message.toString();
    }
}