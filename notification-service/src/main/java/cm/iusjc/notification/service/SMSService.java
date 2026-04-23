package cm.iusjc.notification.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.List;

@Service
@Slf4j
public class SMSService {

    @Value("${sms.twilio.enabled:false}")
    private boolean smsEnabled;

    @Value("${sms.twilio.account-sid:}")
    private String accountSid;

    @Value("${sms.twilio.auth-token:}")
    private String authToken;

    @Value("${sms.twilio.from-number:}")
    private String fromNumber;

    @Value("${sms.twilio.sender-id:EduSchedule}")
    private String senderId;

    /** Initialise le SDK Twilio au démarrage si les credentials sont présents. */
    @PostConstruct
    public void init() {
        if (smsEnabled && isConfigured()) {
            Twilio.init(accountSid, authToken);
            log.info("Twilio SMS service initialized (from: {})", fromNumber);
        } else if (smsEnabled) {
            log.warn("SMS enabled but Twilio credentials are missing — SMS will be logged only.");
        } else {
            log.info("SMS service disabled (sms.twilio.enabled=false).");
        }
    }

    // ── Envoi simple ──────────────────────────────────────────────────────────

    /**
     * Envoie un SMS. Si Twilio n'est pas configuré, logue le message sans erreur.
     */
    public void sendSMS(String phoneNumber, String messageText) {
        if (!smsEnabled) {
            log.info("[SMS-DISABLED] To={} | {}", phoneNumber, messageText);
            return;
        }

        String formatted = formatPhoneNumber(phoneNumber);
        if (!isValidPhoneNumber(formatted)) {
            log.warn("Numéro invalide, SMS ignoré : {}", phoneNumber);
            return;
        }

        if (!isConfigured()) {
            log.warn("[SMS-NO-CREDENTIALS] To={} | {}", formatted, messageText);
            return;
        }

        try {
            Message msg = Message.creator(
                    new PhoneNumber(formatted),
                    new PhoneNumber(fromNumber),
                    messageText
            ).create();

            log.info("SMS envoyé à {} — SID: {}", formatted, msg.getSid());
        } catch (Exception e) {
            log.error("Échec envoi SMS à {} : {}", formatted, e.getMessage());
            // Ne pas propager l'exception pour ne pas bloquer le flux principal
        }
    }

    /** Envoi en masse — continue même si un destinataire échoue. */
    public void sendBulkSMS(List<String> phoneNumbers, String messageText) {
        log.info("Envoi SMS en masse à {} destinataires", phoneNumbers.size());
        for (String number : phoneNumbers) {
            sendSMS(number, messageText);
        }
    }

    // ── Notifications métier ──────────────────────────────────────────────────

    public void sendScheduleChangeSMS(String phoneNumber, String courseName, String changeDescription) {
        String text = String.format(
            "[EduSchedule] Modification cours : %s\n%s",
            courseName, changeDescription
        );
        sendSMS(phoneNumber, text);
    }

    public void sendReservationSMS(String phoneNumber, String title, String status) {
        String text = String.format(
            "[EduSchedule] Réservation \"%s\" : %s",
            title, status
        );
        sendSMS(phoneNumber, text);
    }

    public void sendReminderSMS(String phoneNumber, String courseName, int minutesBefore) {
        String text = String.format(
            "[EduSchedule] Rappel : \"%s\" commence dans %d min.",
            courseName, minutesBefore
        );
        sendSMS(phoneNumber, text);
    }

    // ── Utilitaires ───────────────────────────────────────────────────────────

    public boolean isServiceAvailable() {
        return smsEnabled && isConfigured();
    }

    public boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) return false;
        String cleaned = phoneNumber.replaceAll("[^0-9+]", "");
        return cleaned.length() >= 10 && cleaned.length() <= 15;
    }

    /**
     * Normalise le numéro au format international.
     * Ajoute +237 (Cameroun) si aucun indicatif n'est présent.
     */
    public String formatPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) return null;
        String cleaned = phoneNumber.replaceAll("[^0-9+]", "");
        if (!cleaned.startsWith("+")) {
            if (cleaned.startsWith("237")) {
                cleaned = "+" + cleaned;
            } else if (cleaned.length() == 9) {
                cleaned = "+237" + cleaned;
            }
        }
        return cleaned;
    }

    public SMSInfo getSMSInfo(String message) {
        SMSInfo info = new SMSInfo();
        info.setCharacterCount(message.length());
        info.setMessageCount(calculateMessageCount(message));
        info.setEstimatedCost(info.getMessageCount() * 0.05);
        return info;
    }

    private boolean isConfigured() {
        return accountSid != null && !accountSid.isBlank()
            && authToken  != null && !authToken.isBlank()
            && fromNumber != null && !fromNumber.isBlank();
    }

    private int calculateMessageCount(String message) {
        boolean isUnicode = !message.matches("^[\\x00-\\x7F]*$");
        int maxLength = isUnicode ? 70 : 160;
        return (int) Math.ceil((double) message.length() / maxLength);
    }

    // ── DTO interne ───────────────────────────────────────────────────────────

    public static class SMSInfo {
        private int characterCount;
        private int messageCount;
        private double estimatedCost;

        public int getCharacterCount()              { return characterCount; }
        public void setCharacterCount(int v)        { this.characterCount = v; }
        public int getMessageCount()                { return messageCount; }
        public void setMessageCount(int v)          { this.messageCount = v; }
        public double getEstimatedCost()            { return estimatedCost; }
        public void setEstimatedCost(double v)      { this.estimatedCost = v; }
    }
}
