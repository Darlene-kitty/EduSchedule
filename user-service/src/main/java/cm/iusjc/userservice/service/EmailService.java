package cm.iusjc.userservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    
    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Value("${app.name:EduSchedule}")
    private String appName;
    
    public void sendEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail, appName + " - IUSJC");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true = HTML content
            
            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
            
        } catch (MailException e) {
            log.error("Failed to send email to: {} - Error: {}", to, e.getMessage());
            throw new RuntimeException("Échec de l'envoi de l'email: " + e.getMessage());
        } catch (MessagingException e) {
            log.error("Messaging exception while sending email to: {} - Error: {}", to, e.getMessage());
            throw new RuntimeException("Erreur de messagerie: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error while sending email to: {} - Error: {}", to, e.getMessage());
            throw new RuntimeException("Erreur inattendue lors de l'envoi de l'email: " + e.getMessage());
        }
    }
    
    public void sendSimpleEmail(String to, String subject, String textContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            
            helper.setFrom(fromEmail, appName + " - IUSJC");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(textContent, false); // false = plain text
            
            mailSender.send(message);
            log.info("Simple email sent successfully to: {}", to);
            
        } catch (Exception e) {
            log.error("Failed to send simple email to: {} - Error: {}", to, e.getMessage());
            throw new RuntimeException("Échec de l'envoi de l'email: " + e.getMessage());
        }
    }
}