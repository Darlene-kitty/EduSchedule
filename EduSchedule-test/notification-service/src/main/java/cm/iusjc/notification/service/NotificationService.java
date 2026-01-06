package cm.iusjc.notification.service;

import cm.iusjc.notification.dto.NotificationDTO;
import cm.iusjc.notification.entity.Notification;
import cm.iusjc.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    
    @Transactional
    public NotificationDTO createNotification(String recipient, String subject, String message, String type) {
        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setSubject(subject);
        notification.setMessage(message);
        notification.setType(type);
        notification.setStatus("PENDING");
        
        Notification savedNotification = notificationRepository.save(notification);
        log.info("Notification created: {}", savedNotification.getId());
        
        // Envoyer immédiatement si c'est un email
        if ("EMAIL".equals(type)) {
            sendNotification(savedNotification.getId());
        }
        
        return convertToDTO(savedNotification);
    }
    
    @Transactional
    public void sendNotification(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        
        try {
            if ("EMAIL".equals(notification.getType())) {
                emailService.sendEmail(
                        notification.getRecipient(),
                        notification.getSubject(),
                        notification.getMessage()
                );
            }
            
            notification.setStatus("SENT");
            notification.setSentAt(LocalDateTime.now());
            log.info("Notification sent: {}", id);
            
        } catch (Exception e) {
            notification.setStatus("FAILED");
            log.error("Failed to send notification: {}", id, e);
        }
        
        notificationRepository.save(notification);
    }
    
    public List<NotificationDTO> getAllNotifications() {
        return notificationRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public NotificationDTO getNotificationById(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        return convertToDTO(notification);
    }
    
    public List<NotificationDTO> getNotificationsByRecipient(String recipient) {
        return notificationRepository.findByRecipient(recipient).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<NotificationDTO> getNotificationsByStatus(String status) {
        return notificationRepository.findByStatus(status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    private NotificationDTO convertToDTO(Notification notification) {
        return new NotificationDTO(
                notification.getId(),
                notification.getRecipient(),
                notification.getSubject(),
                notification.getMessage(),
                notification.getType(),
                notification.getStatus(),
                notification.getSentAt(),
                notification.getCreatedAt()
        );
    }
}
