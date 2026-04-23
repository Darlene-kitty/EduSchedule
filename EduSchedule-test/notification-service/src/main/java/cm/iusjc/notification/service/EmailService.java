package cm.iusjc.notification.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;

/**
 * Service d'envoi d'emails via SMTP (Spring Mail).
 *
 * Si MAIL_USERNAME ou MAIL_PASSWORD ne sont pas renseignés dans .env,
 * les envois sont loggés sans lever d'exception (mode dégradé).
 */
@Service
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${spring.mail.from:noreply@iusjc.cm}")
    private String fromAddress;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // ── Envoi texte brut ──────────────────────────────────────────────────────

    public void sendEmail(String to, String subject, String text) {
        if (!isConfigured()) {
            log.warn("[EMAIL-NO-CREDENTIALS] To={} | Subject={}", to, subject);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            message.setFrom(fromAddress);

            mailSender.send(message);
            log.info("Email envoyé à : {}", to);
        } catch (Exception e) {
            log.error("Échec envoi email à {} : {}", to, e.getMessage());
            // Ne pas propager — une notification email ratée ne doit pas bloquer le flux
        }
    }

    // ── Envoi HTML ────────────────────────────────────────────────────────────

    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        if (!isConfigured()) {
            log.warn("[EMAIL-NO-CREDENTIALS] To={} | Subject={}", to, subject);
            return;
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            helper.setFrom(fromAddress);

            mailSender.send(message);
            log.info("Email HTML envoyé à : {}", to);
        } catch (Exception e) {
            log.error("Échec envoi email HTML à {} : {}", to, e.getMessage());
        }
    }

    // ── Emails métier ─────────────────────────────────────────────────────────

    /** Notification de changement d'emploi du temps. */
    public void sendScheduleChangeEmail(String to, String courseName, String changeDescription) {
        String subject = "📅 Modification de cours : " + courseName;
        String body = String.format(
            "Bonjour,\n\n" +
            "Une modification a été apportée à votre emploi du temps :\n\n" +
            "📚 Cours : %s\n" +
            "📝 Détail : %s\n\n" +
            "Consultez l'application EduSchedule pour plus d'informations.\n\n" +
            "Cordialement,\nL'équipe EduSchedule",
            courseName, changeDescription
        );
        sendEmail(to, subject, body);
    }

    /** Notification de réservation. */
    public void sendReservationEmail(String to, String title, String status, String details) {
        String emoji = "CONFIRMED".equals(status) ? "✅" : "REJECTED".equals(status) ? "❌" : "🔔";
        String subject = emoji + " Réservation : " + title;
        String body = String.format(
            "Bonjour,\n\n" +
            "Votre réservation a été mise à jour :\n\n" +
            "📋 Titre : %s\n" +
            "📌 Statut : %s\n" +
            "%s\n\n" +
            "Cordialement,\nL'équipe EduSchedule",
            title, status, details != null ? "ℹ️ " + details : ""
        );
        sendEmail(to, subject, body);
    }

    /** Rappel avant un cours. */
    public void sendReminderEmail(String to, String courseName, String room, String startTime, int minutesBefore) {
        String subject = "⏰ Rappel : " + courseName + " dans " + minutesBefore + " min";
        String body = String.format(
            "Bonjour,\n\n" +
            "Rappel : votre cours commence bientôt.\n\n" +
            "📚 Cours : %s\n" +
            "🏫 Salle : %s\n" +
            "🕐 Heure : %s\n\n" +
            "Cordialement,\nL'équipe EduSchedule",
            courseName, room, startTime
        );
        sendEmail(to, subject, body);
    }

    // ── Utilitaire ────────────────────────────────────────────────────────────

    /** Retourne true si les credentials SMTP sont renseignés. */
    public boolean isConfigured() {
        return mailUsername != null && !mailUsername.isBlank();
    }
}
