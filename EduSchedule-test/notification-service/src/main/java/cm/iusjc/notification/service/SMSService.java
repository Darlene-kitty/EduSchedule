package cm.iusjc.notification.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SMSService {
    
    @Value("${sms.provider.enabled:false}")
    private boolean smsEnabled;
    
    @Value("${sms.provider.api-key:}")
    private String apiKey;
    
    @Value("${sms.provider.sender-id:EduSchedule}")
    private String senderId;
    
    /**
     * Send SMS notification
     */
    public void sendSMS(String phoneNumber, String message) {
        if (!smsEnabled) {
            log.info("SMS service disabled. Would send to {}: {}", phoneNumber, message);
            return;
        }
        
        try {
            // Here you would integrate with your SMS provider (Twilio, AWS SNS, etc.)
            // For now, we'll just log the SMS
            log.info("Sending SMS to {}: {}", phoneNumber, message);
            
            // Example integration with Twilio (commented out)
            /*
            Twilio.init(accountSid, authToken);
            Message twilioMessage = Message.creator(
                new PhoneNumber(phoneNumber),
                new PhoneNumber(fromPhoneNumber),
                message
            ).create();
            
            log.info("SMS sent successfully. SID: {}", twilioMessage.getSid());
            */
            
            // Simulate successful SMS sending
            simulateSMSSending(phoneNumber, message);
            
        } catch (Exception e) {
            log.error("Error sending SMS to {}: {}", phoneNumber, e.getMessage());
            throw new RuntimeException("Failed to send SMS", e);
        }
    }
    
    /**
     * Send bulk SMS notifications
     */
    public void sendBulkSMS(java.util.List<String> phoneNumbers, String message) {
        log.info("Sending bulk SMS to {} recipients", phoneNumbers.size());
        
        for (String phoneNumber : phoneNumbers) {
            try {
                sendSMS(phoneNumber, message);
            } catch (Exception e) {
                log.error("Failed to send SMS to {}: {}", phoneNumber, e.getMessage());
                // Continue with other recipients
            }
        }
    }
    
    /**
     * Validate phone number format
     */
    public boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }
        
        // Basic validation for international format
        String cleaned = phoneNumber.replaceAll("[^0-9+]", "");
        return cleaned.length() >= 10 && cleaned.length() <= 15;
    }
    
    /**
     * Format phone number for SMS sending
     */
    public String formatPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }
        
        // Remove all non-digit characters except +
        String cleaned = phoneNumber.replaceAll("[^0-9+]", "");
        
        // Add country code if missing (assuming Cameroon +237)
        if (!cleaned.startsWith("+")) {
            if (cleaned.startsWith("237")) {
                cleaned = "+" + cleaned;
            } else if (cleaned.length() == 9) {
                cleaned = "+237" + cleaned;
            }
        }
        
        return cleaned;
    }
    
    /**
     * Check SMS service status
     */
    public boolean isServiceAvailable() {
        return smsEnabled && apiKey != null && !apiKey.trim().isEmpty();
    }
    
    /**
     * Get SMS character count and cost estimation
     */
    public SMSInfo getSMSInfo(String message) {
        SMSInfo info = new SMSInfo();
        info.setCharacterCount(message.length());
        info.setMessageCount(calculateMessageCount(message));
        info.setEstimatedCost(info.getMessageCount() * 0.05); // Example: 0.05€ per SMS
        
        return info;
    }
    
    /**
     * Calculate number of SMS messages needed
     */
    private int calculateMessageCount(String message) {
        int length = message.length();
        
        // Standard SMS: 160 characters
        // Unicode SMS: 70 characters
        boolean isUnicode = !message.matches("^[\\x00-\\x7F]*$");
        int maxLength = isUnicode ? 70 : 160;
        
        return (int) Math.ceil((double) length / maxLength);
    }
    
    /**
     * Simulate SMS sending for development/testing
     */
    private void simulateSMSSending(String phoneNumber, String message) {
        // Simulate network delay
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        log.info("✅ SMS simulated successfully to {}", phoneNumber);
    }
    
    /**
     * SMS Information class
     */
    public static class SMSInfo {
        private int characterCount;
        private int messageCount;
        private double estimatedCost;
        
        // Getters and setters
        public int getCharacterCount() { return characterCount; }
        public void setCharacterCount(int characterCount) { this.characterCount = characterCount; }
        
        public int getMessageCount() { return messageCount; }
        public void setMessageCount(int messageCount) { this.messageCount = messageCount; }
        
        public double getEstimatedCost() { return estimatedCost; }
        public void setEstimatedCost(double estimatedCost) { this.estimatedCost = estimatedCost; }
    }
    
    /**
     * Send schedule change SMS notification
     */
    public void sendScheduleChangeSMS(Long scheduleId, Long userId, String message) {
        log.info("Sending schedule change SMS for schedule {} to user {}", scheduleId, userId);
        // In a real implementation, you would fetch the user's phone number from the database
        // For now, we'll just log the message
        String phoneNumber = "+237600000000"; // Placeholder
        sendSMS(phoneNumber, message);
    }
}
