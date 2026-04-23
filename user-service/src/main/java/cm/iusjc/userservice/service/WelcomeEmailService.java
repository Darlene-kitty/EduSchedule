package cm.iusjc.userservice.service;

import cm.iusjc.userservice.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Locale;

@Service
public class WelcomeEmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${app.name:EduSchedule}")
    private String appName;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendWelcomeEmail(User user) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(user.getEmail());
            helper.setSubject("Bienvenue sur " + appName + " !");

            String htmlContent = generateWelcomeEmailContent(user);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Erreur lors de l'envoi de l'email de bienvenue", e);
        }
    }

    public void sendTeacherWelcomeEmail(User user) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(user.getEmail());
            helper.setSubject("Bienvenue dans l'équipe enseignante - " + appName);

            String htmlContent = generateTeacherWelcomeEmailContent(user);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Erreur lors de l'envoi de l'email de bienvenue enseignant", e);
        }
    }

    public void sendAdminWelcomeEmail(User user) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(user.getEmail());
            helper.setSubject("Accès administrateur accordé - " + appName);

            String htmlContent = generateAdminWelcomeEmailContent(user);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Erreur lors de l'envoi de l'email de bienvenue administrateur", e);
        }
    }

    private String generateWelcomeEmailContent(User user) {
        Context context = new Context(Locale.FRENCH);
        context.setVariable("user", user);
        context.setVariable("appName", appName);
        context.setVariable("frontendUrl", frontendUrl);
        context.setVariable("loginUrl", frontendUrl + "/login");
        
        return templateEngine.process("welcome-email", context);
    }

    private String generateTeacherWelcomeEmailContent(User user) {
        Context context = new Context(Locale.FRENCH);
        context.setVariable("user", user);
        context.setVariable("appName", appName);
        context.setVariable("frontendUrl", frontendUrl);
        context.setVariable("loginUrl", frontendUrl + "/login");
        context.setVariable("availabilityUrl", frontendUrl + "/teacher/availability");
        
        return templateEngine.process("teacher-welcome-email", context);
    }

    private String generateAdminWelcomeEmailContent(User user) {
        Context context = new Context(Locale.FRENCH);
        context.setVariable("user", user);
        context.setVariable("appName", appName);
        context.setVariable("frontendUrl", frontendUrl);
        context.setVariable("loginUrl", frontendUrl + "/login");
        context.setVariable("adminUrl", frontendUrl + "/admin");
        
        return templateEngine.process("admin-welcome-email", context);
    }
}