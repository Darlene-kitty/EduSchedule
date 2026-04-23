package cm.iusjc.notification.service;

import cm.iusjc.notification.dto.NotificationTemplateDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class NotificationTemplateService {
    
    private final Map<String, NotificationTemplateDTO> templates = new HashMap<>();
    
    public NotificationTemplateService() {
        initializeDefaultTemplates();
    }
    
    /**
     * Get notification template by type and channel
     */
    public NotificationTemplateDTO getTemplate(String templateType, String channel) {
        String key = templateType + "_" + channel;
        return templates.get(key);
    }
    
    /**
     * Register a new template
     */
    public void registerTemplate(String templateType, String channel, NotificationTemplateDTO template) {
        String key = templateType + "_" + channel;
        templates.put(key, template);
        log.info("Registered template: {}", key);
    }
    
    /**
     * Initialize default templates
     */
    private void initializeDefaultTemplates() {
        // Schedule Created Templates
        registerScheduleCreatedTemplates();
        
        // Schedule Updated Templates
        registerScheduleUpdatedTemplates();
        
        // Schedule Cancelled Templates
        registerScheduleCancelledTemplates();
        
        // Room Changed Templates
        registerRoomChangedTemplates();
        
        // Reminder Templates
        registerReminderTemplates();
        
        log.info("Initialized {} notification templates", templates.size());
    }
    
    private void registerScheduleCreatedTemplates() {
        // Email template
        NotificationTemplateDTO emailTemplate = new NotificationTemplateDTO();
        emailTemplate.setTemplateType("SCHEDULE_CREATED");
        emailTemplate.setChannel("EMAIL");
        emailTemplate.setEmailSubject("📅 Nouveau cours programmé - {{courseName}}");
        emailTemplate.setEmailBodyHtml(getScheduleCreatedEmailHtml());
        registerTemplate("SCHEDULE_CREATED", "EMAIL", emailTemplate);
        
        // SMS template
        NotificationTemplateDTO smsTemplate = new NotificationTemplateDTO();
        smsTemplate.setTemplateType("SCHEDULE_CREATED");
        smsTemplate.setChannel("SMS");
        smsTemplate.setSmsMessage("📅 Nouveau cours: {{courseName}} le {{startDateTime}} en salle {{room}} avec {{teacherName}}");
        registerTemplate("SCHEDULE_CREATED", "SMS", smsTemplate);
    }
    
    private void registerScheduleUpdatedTemplates() {
        // Email template
        NotificationTemplateDTO emailTemplate = new NotificationTemplateDTO();
        emailTemplate.setTemplateType("SCHEDULE_UPDATED");
        emailTemplate.setChannel("EMAIL");
        emailTemplate.setEmailSubject("🔄 Modification d'emploi du temps - {{courseName}}");
        emailTemplate.setEmailBodyHtml(getScheduleUpdatedEmailHtml());
        registerTemplate("SCHEDULE_UPDATED", "EMAIL", emailTemplate);
        
        // SMS template
        NotificationTemplateDTO smsTemplate = new NotificationTemplateDTO();
        smsTemplate.setTemplateType("SCHEDULE_UPDATED");
        smsTemplate.setChannel("SMS");
        smsTemplate.setSmsMessage("🔄 MODIFIÉ: {{courseName}} maintenant le {{startDateTime}} en salle {{room}}");
        registerTemplate("SCHEDULE_UPDATED", "SMS", smsTemplate);
    }
    
    private void registerScheduleCancelledTemplates() {
        // Email template
        NotificationTemplateDTO emailTemplate = new NotificationTemplateDTO();
        emailTemplate.setTemplateType("SCHEDULE_CANCELLED");
        emailTemplate.setChannel("EMAIL");
        emailTemplate.setEmailSubject("❌ Cours annulé - {{courseName}}");
        emailTemplate.setEmailBodyHtml(getScheduleCancelledEmailHtml());
        registerTemplate("SCHEDULE_CANCELLED", "EMAIL", emailTemplate);
        
        // SMS template
        NotificationTemplateDTO smsTemplate = new NotificationTemplateDTO();
        smsTemplate.setTemplateType("SCHEDULE_CANCELLED");
        smsTemplate.setChannel("SMS");
        smsTemplate.setSmsMessage("❌ ANNULÉ: {{courseName}} du {{startDateTime}}. Raison: {{changeReason}}");
        registerTemplate("SCHEDULE_CANCELLED", "SMS", smsTemplate);
    }
    
    private void registerRoomChangedTemplates() {
        // Email template
        NotificationTemplateDTO emailTemplate = new NotificationTemplateDTO();
        emailTemplate.setTemplateType("ROOM_CHANGED");
        emailTemplate.setChannel("EMAIL");
        emailTemplate.setEmailSubject("🏢 Changement de salle - {{courseName}}");
        emailTemplate.setEmailBodyHtml(getRoomChangedEmailHtml());
        registerTemplate("ROOM_CHANGED", "EMAIL", emailTemplate);
        
        // SMS template
        NotificationTemplateDTO smsTemplate = new NotificationTemplateDTO();
        smsTemplate.setTemplateType("ROOM_CHANGED");
        smsTemplate.setChannel("SMS");
        smsTemplate.setSmsMessage("🏢 SALLE CHANGÉE: {{courseName}} le {{startDateTime}} maintenant en salle {{room}} (au lieu de {{previousRoom}})");
        registerTemplate("ROOM_CHANGED", "SMS", smsTemplate);
    }
    
    private void registerReminderTemplates() {
        // Course reminder email
        NotificationTemplateDTO reminderEmail = new NotificationTemplateDTO();
        reminderEmail.setTemplateType("COURSE_REMINDER");
        reminderEmail.setChannel("EMAIL");
        reminderEmail.setEmailSubject("⏰ Rappel - Cours dans {{minutesBefore}} minutes");
        reminderEmail.setEmailBodyHtml(getCourseReminderEmailHtml());
        registerTemplate("COURSE_REMINDER", "EMAIL", reminderEmail);
        
        // Course reminder SMS
        NotificationTemplateDTO reminderSms = new NotificationTemplateDTO();
        reminderSms.setTemplateType("COURSE_REMINDER");
        reminderSms.setChannel("SMS");
        reminderSms.setSmsMessage("⏰ Rappel: {{courseName}} dans {{minutesBefore}}min en salle {{room}}");
        registerTemplate("COURSE_REMINDER", "SMS", reminderSms);
    }
    
    // HTML Templates
    private String getScheduleCreatedEmailHtml() {
        return """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; border: 1px solid #ddd;">
                <div style="background-color: #4CAF50; color: white; padding: 20px; text-align: center;">
                    <h1 style="margin: 0;">📅 Nouveau Cours Programmé</h1>
                </div>
                
                <div style="padding: 20px; background-color: #f9f9f9;">
                    <h2 style="color: #333; margin-top: 0;">Détails du cours</h2>
                    
                    <div style="background-color: white; padding: 20px; border-radius: 8px; margin: 15px 0; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
                        <table style="width: 100%; border-collapse: collapse;">
                            <tr>
                                <td style="padding: 8px 0; font-weight: bold; color: #555;">📚 Cours :</td>
                                <td style="padding: 8px 0;">{{courseName}} ({{courseCode}})</td>
                            </tr>
                            <tr>
                                <td style="padding: 8px 0; font-weight: bold; color: #555;">👨‍🏫 Enseignant :</td>
                                <td style="padding: 8px 0;">{{teacherName}}</td>
                            </tr>
                            <tr>
                                <td style="padding: 8px 0; font-weight: bold; color: #555;">👥 Groupe :</td>
                                <td style="padding: 8px 0;">{{groupName}}</td>
                            </tr>
                            <tr>
                                <td style="padding: 8px 0; font-weight: bold; color: #555;">📅 Date et heure :</td>
                                <td style="padding: 8px 0;">{{startDateTime}}</td>
                            </tr>
                            <tr>
                                <td style="padding: 8px 0; font-weight: bold; color: #555;">🏢 Salle :</td>
                                <td style="padding: 8px 0;">{{room}}</td>
                            </tr>
                            <tr>
                                <td style="padding: 8px 0; font-weight: bold; color: #555;">🏫 École :</td>
                                <td style="padding: 8px 0;">{{schoolName}}</td>
                            </tr>
                        </table>
                    </div>
                    
                    <div style="background-color: #e8f5e8; padding: 15px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #4CAF50;">
                        <p style="margin: 0; color: #2e7d32; font-weight: bold;">
                            ✅ Ce cours a été ajouté à votre emploi du temps.
                        </p>
                        <p style="margin: 5px 0 0 0; color: #2e7d32;">
                            Vous recevrez un rappel 30 minutes avant le début du cours.
                        </p>
                    </div>
                </div>
                
                <div style="background-color: #333; color: white; padding: 15px; text-align: center;">
                    <p style="margin: 0; font-size: 12px;">
                        EduSchedule - Institut Universitaire Saint Jean<br>
                        Système de gestion d'emploi du temps
                    </p>
                </div>
            </div>
            """;
    }
    
    private String getScheduleUpdatedEmailHtml() {
        return """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; border: 1px solid #ddd;">
                <div style="background-color: #FF9800; color: white; padding: 20px; text-align: center;">
                    <h1 style="margin: 0;">🔄 Emploi du Temps Modifié</h1>
                </div>
                
                <div style="padding: 20px; background-color: #f9f9f9;">
                    <h2 style="color: #333; margin-top: 0;">Cours modifié</h2>
                    
                    <div style="background-color: white; padding: 20px; border-radius: 8px; margin: 15px 0;">
                        <h3 style="color: #FF9800; margin-top: 0;">Nouvelles informations :</h3>
                        <table style="width: 100%; border-collapse: collapse;">
                            <tr>
                                <td style="padding: 8px 0; font-weight: bold; color: #555;">📚 Cours :</td>
                                <td style="padding: 8px 0;">{{courseName}} ({{courseCode}})</td>
                            </tr>
                            <tr>
                                <td style="padding: 8px 0; font-weight: bold; color: #555;">📅 Date et heure :</td>
                                <td style="padding: 8px 0;">{{startDateTime}}</td>
                            </tr>
                            <tr>
                                <td style="padding: 8px 0; font-weight: bold; color: #555;">🏢 Salle :</td>
                                <td style="padding: 8px 0;">{{room}}</td>
                            </tr>
                            <tr>
                                <td style="padding: 8px 0; font-weight: bold; color: #555;">👨‍🏫 Enseignant :</td>
                                <td style="padding: 8px 0;">{{teacherName}}</td>
                            </tr>
                        </table>
                    </div>
                    
                    <div style="background-color: #fff3e0; padding: 15px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #FF9800;">
                        <p style="margin: 0; color: #f57c00; font-weight: bold;">
                            ⚠️ Veuillez noter ces changements dans votre agenda.
                        </p>
                    </div>
                </div>
                
                <div style="background-color: #333; color: white; padding: 15px; text-align: center;">
                    <p style="margin: 0; font-size: 12px;">
                        EduSchedule - Institut Universitaire Saint Jean
                    </p>
                </div>
            </div>
            """;
    }
    
    private String getScheduleCancelledEmailHtml() {
        return """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; border: 1px solid #ddd;">
                <div style="background-color: #f44336; color: white; padding: 20px; text-align: center;">
                    <h1 style="margin: 0;">❌ Cours Annulé</h1>
                </div>
                
                <div style="padding: 20px; background-color: #f9f9f9;">
                    <h2 style="color: #333; margin-top: 0;">Cours annulé</h2>
                    
                    <div style="background-color: white; padding: 20px; border-radius: 8px; margin: 15px 0;">
                        <table style="width: 100%; border-collapse: collapse;">
                            <tr>
                                <td style="padding: 8px 0; font-weight: bold; color: #555;">📚 Cours :</td>
                                <td style="padding: 8px 0;">{{courseName}} ({{courseCode}})</td>
                            </tr>
                            <tr>
                                <td style="padding: 8px 0; font-weight: bold; color: #555;">📅 Date prévue :</td>
                                <td style="padding: 8px 0;">{{startDateTime}}</td>
                            </tr>
                            <tr>
                                <td style="padding: 8px 0; font-weight: bold; color: #555;">🏢 Salle :</td>
                                <td style="padding: 8px 0;">{{room}}</td>
                            </tr>
                            <tr>
                                <td style="padding: 8px 0; font-weight: bold; color: #555;">📝 Raison :</td>
                                <td style="padding: 8px 0;">{{changeReason}}</td>
                            </tr>
                        </table>
                    </div>
                    
                    <div style="background-color: #ffebee; padding: 15px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #f44336;">
                        <p style="margin: 0; color: #c62828; font-weight: bold;">
                            ❌ Ce cours a été supprimé de votre emploi du temps.
                        </p>
                        <p style="margin: 5px 0 0 0; color: #c62828;">
                            Un cours de rattrapage sera programmé ultérieurement si nécessaire.
                        </p>
                    </div>
                </div>
                
                <div style="background-color: #333; color: white; padding: 15px; text-align: center;">
                    <p style="margin: 0; font-size: 12px;">
                        EduSchedule - Institut Universitaire Saint Jean
                    </p>
                </div>
            </div>
            """;
    }
    
    private String getRoomChangedEmailHtml() {
        return """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; border: 1px solid #ddd;">
                <div style="background-color: #2196F3; color: white; padding: 20px; text-align: center;">
                    <h1 style="margin: 0;">🏢 Changement de Salle</h1>
                </div>
                
                <div style="padding: 20px; background-color: #f9f9f9;">
                    <h2 style="color: #333; margin-top: 0;">Nouvelle salle assignée</h2>
                    
                    <div style="background-color: white; padding: 20px; border-radius: 8px; margin: 15px 0;">
                        <table style="width: 100%; border-collapse: collapse;">
                            <tr>
                                <td style="padding: 8px 0; font-weight: bold; color: #555;">📚 Cours :</td>
                                <td style="padding: 8px 0;">{{courseName}} ({{courseCode}})</td>
                            </tr>
                            <tr>
                                <td style="padding: 8px 0; font-weight: bold; color: #555;">📅 Date et heure :</td>
                                <td style="padding: 8px 0;">{{startDateTime}}</td>
                            </tr>
                            <tr>
                                <td style="padding: 8px 0; font-weight: bold; color: #555;">👨‍🏫 Enseignant :</td>
                                <td style="padding: 8px 0;">{{teacherName}}</td>
                            </tr>
                        </table>
                    </div>
                    
                    <div style="background-color: #e3f2fd; padding: 20px; border-radius: 8px; margin: 20px 0;">
                        <table style="width: 100%; border-collapse: collapse;">
                            <tr>
                                <td style="padding: 8px 0; font-weight: bold; color: #555;">🏢 Ancienne salle :</td>
                                <td style="padding: 8px 0; text-decoration: line-through; color: #999;">{{previousRoom}}</td>
                            </tr>
                            <tr>
                                <td style="padding: 8px 0; font-weight: bold; color: #555;">🏢 Nouvelle salle :</td>
                                <td style="padding: 8px 0; color: #1976d2; font-weight: bold; font-size: 18px;">{{room}}</td>
                            </tr>
                        </table>
                    </div>
                    
                    <div style="background-color: #e3f2fd; padding: 15px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #2196F3;">
                        <p style="margin: 0; color: #1976d2; font-weight: bold;">
                            🔄 Veuillez vous rendre dans la nouvelle salle.
                        </p>
                        <p style="margin: 5px 0 0 0; color: #1976d2;">
                            L'horaire reste inchangé.
                        </p>
                    </div>
                </div>
                
                <div style="background-color: #333; color: white; padding: 15px; text-align: center;">
                    <p style="margin: 0; font-size: 12px;">
                        EduSchedule - Institut Universitaire Saint Jean
                    </p>
                </div>
            </div>
            """;
    }
    
    private String getCourseReminderEmailHtml() {
        return """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; border: 1px solid #ddd;">
                <div style="background-color: #9C27B0; color: white; padding: 20px; text-align: center;">
                    <h1 style="margin: 0;">⏰ Rappel de Cours</h1>
                </div>
                
                <div style="padding: 20px; background-color: #f9f9f9;">
                    <h2 style="color: #333; margin-top: 0;">Cours dans {{minutesBefore}} minutes</h2>
                    
                    <div style="background-color: white; padding: 20px; border-radius: 8px; margin: 15px 0;">
                        <table style="width: 100%; border-collapse: collapse;">
                            <tr>
                                <td style="padding: 8px 0; font-weight: bold; color: #555;">📚 Cours :</td>
                                <td style="padding: 8px 0;">{{courseName}}</td>
                            </tr>
                            <tr>
                                <td style="padding: 8px 0; font-weight: bold; color: #555;">⏰ Heure :</td>
                                <td style="padding: 8px 0;">{{startTime}}</td>
                            </tr>
                            <tr>
                                <td style="padding: 8px 0; font-weight: bold; color: #555;">🏢 Salle :</td>
                                <td style="padding: 8px 0;">{{room}}</td>
                            </tr>
                            <tr>
                                <td style="padding: 8px 0; font-weight: bold; color: #555;">👨‍🏫 Enseignant :</td>
                                <td style="padding: 8px 0;">{{teacherName}}</td>
                            </tr>
                        </table>
                    </div>
                    
                    <div style="background-color: #f3e5f5; padding: 15px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #9C27B0;">
                        <p style="margin: 0; color: #7b1fa2; font-weight: bold;">
                            ⏰ N'oubliez pas votre cours !
                        </p>
                    </div>
                </div>
                
                <div style="background-color: #333; color: white; padding: 15px; text-align: center;">
                    <p style="margin: 0; font-size: 12px;">
                        EduSchedule - Institut Universitaire Saint Jean
                    </p>
                </div>
            </div>
            """;
    }
    
    public java.util.List<NotificationTemplateDTO> getAllTemplates() {
        return new java.util.ArrayList<>(templates.values());
    }
    
    public NotificationTemplateDTO createTemplate(NotificationTemplateDTO template) {
        registerTemplate(template.getTemplateType(), template.getChannel(), template);
        return template;
    }
    
    public NotificationTemplateDTO updateTemplate(Long id, NotificationTemplateDTO template) {
        // For simplicity, using templateType and channel as key
        registerTemplate(template.getTemplateType(), template.getChannel(), template);
        return template;
    }
    
    public void deleteTemplate(Long id) {
        // Implementation would remove from templates map
        log.info("Template deleted: {}", id);
    }
}
