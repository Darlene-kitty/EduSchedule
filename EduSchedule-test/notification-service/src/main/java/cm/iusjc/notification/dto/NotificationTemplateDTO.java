package cm.iusjc.notification.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationTemplateDTO {
    
    private String templateType; // SCHEDULE_CREATED, SCHEDULE_UPDATED, etc.
    private String channel; // EMAIL, SMS, PUSH
    private String language = "FR"; // FR, EN
    
    // Email specific
    private String emailSubject;
    private String emailBodyHtml;
    private String emailBodyText;
    
    // SMS specific
    private String smsMessage;
    
    // Push notification specific
    private String pushTitle;
    private String pushBody;
    private Map<String, Object> pushData;
    
    // Template variables
    private Map<String, Object> variables;
    
    // Styling
    private String cssStyles;
    private String logoUrl;
    private String footerText;
    
    // Helper methods
    public String getProcessedEmailSubject() {
        return processTemplate(emailSubject, variables);
    }
    
    public String getProcessedEmailBody() {
        return processTemplate(emailBodyHtml != null ? emailBodyHtml : emailBodyText, variables);
    }
    
    public String getProcessedSmsMessage() {
        return processTemplate(smsMessage, variables);
    }
    
    public String getProcessedPushTitle() {
        return processTemplate(pushTitle, variables);
    }
    
    public String getProcessedPushBody() {
        return processTemplate(pushBody, variables);
    }
    
    private String processTemplate(String template, Map<String, Object> vars) {
        if (template == null || vars == null) {
            return template;
        }
        
        String processed = template;
        for (Map.Entry<String, Object> entry : vars.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            processed = processed.replace(placeholder, value);
        }
        
        return processed;
    }
}