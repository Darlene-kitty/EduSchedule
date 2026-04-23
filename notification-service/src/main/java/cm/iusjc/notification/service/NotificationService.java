package cm.iusjc.notification.service;

import cm.iusjc.notification.dto.NotificationDTO;
import cm.iusjc.notification.entity.Notification;
import cm.iusjc.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    
    /**
     * Crée une nouvelle notification
     */
    @Transactional
    @CacheEvict(value = "notifications", allEntries = true)
    public NotificationDTO createNotification(NotificationDTO notificationDTO) {
        log.info("Creating new notification for user: {}", notificationDTO.getUserId());
        
        Notification notification = new Notification();
        notification.setUserId(notificationDTO.getUserId());
        notification.setRecipient(notificationDTO.getRecipient());
        notification.setSubject(notificationDTO.getSubject());
        notification.setTitle(notificationDTO.getTitle());
        notification.setMessage(notificationDTO.getMessage());
        notification.setType(notificationDTO.getType());
        notification.setPriority(notificationDTO.getPriority());
        notification.setChannel(notificationDTO.getChannel());
        notification.setRelatedEntityType(notificationDTO.getRelatedEntityType());
        notification.setRelatedEntityId(notificationDTO.getRelatedEntityId());
        notification.setScheduledFor(notificationDTO.getScheduledFor());
        notification.setRead(false);
        notification.setSent(false);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setUpdatedAt(LocalDateTime.now());
        
        Notification savedNotification = notificationRepository.save(notification);
        log.info("Notification created successfully with ID: {}", savedNotification.getId());
        
        return convertToDTO(savedNotification);
    }
    
    /**
     * Crée une nouvelle notification (méthode de compatibilité)
     */
    @Transactional
    @CacheEvict(value = "notifications", allEntries = true)
    public NotificationDTO createNotification(String recipient, String subject, String message, String type) {
        log.info("Creating new notification for recipient: {}", recipient);
        
        NotificationDTO dto = new NotificationDTO();
        dto.setRecipient(recipient);
        dto.setRecipientEmail(recipient);
        dto.setSubject(subject);
        dto.setTitle(subject);
        dto.setMessage(message);
        dto.setType(type);
        dto.setChannel(type);
        dto.setPriority("NORMAL");
        
        return createNotification(dto);
    }
    
    /**
     * Crée une notification pour plusieurs utilisateurs
     */
    @Transactional
    @CacheEvict(value = "notifications", allEntries = true)
    public List<NotificationDTO> createBulkNotifications(List<Long> userIds, NotificationDTO notificationTemplate) {
        log.info("Creating bulk notifications for {} users", userIds.size());
        
        List<Notification> notifications = userIds.stream()
                .map(userId -> {
                    Notification notification = new Notification();
                    notification.setUserId(userId);
                    notification.setRecipient(notificationTemplate.getRecipient());
                    notification.setSubject(notificationTemplate.getSubject());
                    notification.setTitle(notificationTemplate.getTitle());
                    notification.setMessage(notificationTemplate.getMessage());
                    notification.setType(notificationTemplate.getType());
                    notification.setPriority(notificationTemplate.getPriority());
                    notification.setChannel(notificationTemplate.getChannel());
                    notification.setRelatedEntityType(notificationTemplate.getRelatedEntityType());
                    notification.setRelatedEntityId(notificationTemplate.getRelatedEntityId());
                    notification.setScheduledFor(notificationTemplate.getScheduledFor());
                    notification.setRead(false);
                    notification.setSent(false);
                    notification.setCreatedAt(LocalDateTime.now());
                    notification.setUpdatedAt(LocalDateTime.now());
                    return notification;
                })
                .collect(Collectors.toList());
        
        List<Notification> savedNotifications = notificationRepository.saveAll(notifications);
        log.info("Bulk notifications created successfully: {} notifications", savedNotifications.size());
        
        return savedNotifications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère toutes les notifications
     */
    @Cacheable(value = "notifications")
    public List<NotificationDTO> getAllNotifications() {
        log.debug("Fetching all notifications");
        return notificationRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les notifications avec pagination
     */
    public Page<NotificationDTO> getAllNotifications(Pageable pageable) {
        log.debug("Fetching notifications with pagination: {}", pageable);
        return notificationRepository.findAll(pageable)
                .map(this::convertToDTO);
    }
    
    /**
     * Récupère les notifications d'un utilisateur
     */
    @Cacheable(value = "notifications", key = "'user_' + #userId")
    public List<NotificationDTO> getNotificationsByUserId(Long userId) {
        log.debug("Fetching notifications for user: {}", userId);
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère une notification par ID
     */
    @Cacheable(value = "notifications", key = "#id")
    public Optional<NotificationDTO> getNotificationById(Long id) {
        log.debug("Fetching notification by ID: {}", id);
        return notificationRepository.findById(id)
                .map(this::convertToDTO);
    }
    
    /**
     * Récupère les notifications d'un utilisateur
     */
    public List<NotificationDTO> getNotificationsByUser(Long userId) {
        log.debug("Fetching notifications for user: {}", userId);
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les notifications d'un utilisateur avec pagination
     */
    public Page<NotificationDTO> getNotificationsByUser(Long userId, Pageable pageable) {
        log.debug("Fetching notifications for user {} with pagination: {}", userId, pageable);
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::convertToDTO);
    }
    
    /**
     * Récupère les notifications non lues d'un utilisateur
     */
    public List<NotificationDTO> getUnreadNotificationsByUser(Long userId) {
        log.debug("Fetching unread notifications for user: {}", userId);
        return notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les notifications par type
     */
    public List<NotificationDTO> getNotificationsByType(String type) {
        log.debug("Fetching notifications by type: {}", type);
        return notificationRepository.findByType(type).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les notifications par priorité
     */
    public List<NotificationDTO> getNotificationsByPriority(String priority) {
        log.debug("Fetching notifications by priority: {}", priority);
        return notificationRepository.findByPriority(priority).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les notifications non envoyées
     */
    public List<NotificationDTO> getPendingNotifications() {
        log.debug("Fetching pending notifications");
        return notificationRepository.findBySentFalseAndScheduledForLessThanEqual(LocalDateTime.now()).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les notifications programmées
     */
    public List<NotificationDTO> getScheduledNotifications() {
        log.debug("Fetching scheduled notifications");
        return notificationRepository.findBySentFalseAndScheduledForGreaterThan(LocalDateTime.now()).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Met à jour une notification
     */
    @Transactional
    @CacheEvict(value = "notifications", allEntries = true)
    public NotificationDTO updateNotification(Long id, NotificationDTO notificationDTO) {
        log.info("Updating notification with ID: {}", id);
        
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found with ID: " + id));
        
        notification.setTitle(notificationDTO.getTitle());
        notification.setMessage(notificationDTO.getMessage());
        notification.setType(notificationDTO.getType());
        notification.setPriority(notificationDTO.getPriority());
        notification.setChannel(notificationDTO.getChannel());
        notification.setRelatedEntityType(notificationDTO.getRelatedEntityType());
        notification.setRelatedEntityId(notificationDTO.getRelatedEntityId());
        notification.setScheduledFor(notificationDTO.getScheduledFor());
        notification.setUpdatedAt(LocalDateTime.now());
        
        Notification updatedNotification = notificationRepository.save(notification);
        log.info("Notification updated successfully: {}", updatedNotification.getId());
        
        return convertToDTO(updatedNotification);
    }
    
    /**
     * Marque une notification comme lue
     */
    @Transactional
    @CacheEvict(value = "notifications", allEntries = true)
    public NotificationDTO markAsRead(Long id) {
        log.info("Marking notification as read: {}", id);
        
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found with ID: " + id));
        
        notification.setRead(true);
        notification.setReadAt(LocalDateTime.now());
        notification.setUpdatedAt(LocalDateTime.now());
        
        Notification updatedNotification = notificationRepository.save(notification);
        log.info("Notification marked as read: {}", updatedNotification.getId());
        
        return convertToDTO(updatedNotification);
    }
    
    /**
     * Marque toutes les notifications d'un utilisateur comme lues
     */
    @Transactional
    @CacheEvict(value = "notifications", allEntries = true)
    public void markAllAsReadForUser(Long userId) {
        log.info("Marking all notifications as read for user: {}", userId);
        
        List<Notification> unreadNotifications = notificationRepository.findByUserIdAndReadFalse(userId);
        LocalDateTime now = LocalDateTime.now();
        
        unreadNotifications.forEach(notification -> {
            notification.setRead(true);
            notification.setReadAt(now);
            notification.setUpdatedAt(now);
        });
        
        notificationRepository.saveAll(unreadNotifications);
        log.info("Marked {} notifications as read for user: {}", unreadNotifications.size(), userId);
    }
    
    /**
     * Marque une notification comme envoyée
     */
    @Transactional
    @CacheEvict(value = "notifications", allEntries = true)
    public NotificationDTO markAsSent(Long id) {
        log.info("Marking notification as sent: {}", id);
        
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found with ID: " + id));
        
        notification.setSent(true);
        notification.setSentAt(LocalDateTime.now());
        notification.setUpdatedAt(LocalDateTime.now());
        
        Notification updatedNotification = notificationRepository.save(notification);
        log.info("Notification marked as sent: {}", updatedNotification.getId());
        
        return convertToDTO(updatedNotification);
    }
    
    /**
     * Supprime une notification
     */
    @Transactional
    @CacheEvict(value = "notifications", allEntries = true)
    public void deleteNotification(Long id) {
        log.info("Deleting notification with ID: {}", id);
        
        if (!notificationRepository.existsById(id)) {
            throw new RuntimeException("Notification not found with ID: " + id);
        }
        
        notificationRepository.deleteById(id);
        log.info("Notification deleted: {}", id);
    }
    
    /**
     * Supprime toutes les notifications lues d'un utilisateur
     */
    @Transactional
    @CacheEvict(value = "notifications", allEntries = true)
    public void deleteReadNotificationsForUser(Long userId) {
        log.info("Deleting read notifications for user: {}", userId);
        
        List<Notification> readNotifications = notificationRepository.findByUserIdAndReadTrue(userId);
        notificationRepository.deleteAll(readNotifications);
        
        log.info("Deleted {} read notifications for user: {}", readNotifications.size(), userId);
    }
    
    /**
     * Supprime les anciennes notifications
     */
    @Transactional
    @CacheEvict(value = "notifications", allEntries = true)
    public void deleteOldNotifications(int daysOld) {
        log.info("Deleting notifications older than {} days", daysOld);
        
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        List<Notification> oldNotifications = notificationRepository.findByCreatedAtBefore(cutoffDate);
        notificationRepository.deleteAll(oldNotifications);
        
        log.info("Deleted {} old notifications", oldNotifications.size());
    }
    
    /**
     * Compte les notifications non lues d'un utilisateur
     */
    public long countUnreadNotificationsForUser(Long userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }
    
    /**
     * Compte les notifications par type
     */
    public long countNotificationsByType(String type) {
        return notificationRepository.countByType(type);
    }
    
    /**
     * Compte les notifications par priorité
     */
    public long countNotificationsByPriority(String priority) {
        return notificationRepository.countByPriority(priority);
    }
    
    /**
     * Obtient les statistiques des notifications
     */
    public NotificationStatistics getNotificationStatistics() {
        long totalNotifications = notificationRepository.count();
        long unreadNotifications = notificationRepository.countByReadFalse();
        long sentNotifications = notificationRepository.countBySentTrue();
        long pendingNotifications = notificationRepository.countBySentFalse();
        
        return NotificationStatistics.builder()
                .totalNotifications(totalNotifications)
                .unreadNotifications(unreadNotifications)
                .sentNotifications(sentNotifications)
                .pendingNotifications(pendingNotifications)
                .build();
    }
    
    /**
     * Recherche des notifications par contenu
     */
    public List<NotificationDTO> searchNotifications(String searchTerm) {
        log.debug("Searching notifications with term: {}", searchTerm);
        return notificationRepository.searchNotifications(searchTerm).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Vérifie si une notification existe
     */
    public boolean existsById(Long id) {
        return notificationRepository.existsById(id);
    }
    
    /**
     * Convertit une entité Notification en DTO
     */
    private NotificationDTO convertToDTO(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setUserId(notification.getUserId());
        dto.setRecipient(notification.getRecipient());
        dto.setRecipientEmail(notification.getRecipient());
        dto.setSubject(notification.getSubject());
        dto.setTitle(notification.getTitle());
        dto.setMessage(notification.getMessage());
        dto.setType(notification.getType());
        dto.setPriority(notification.getPriority());
        dto.setChannel(notification.getChannel());
        dto.setRelatedEntityType(notification.getRelatedEntityType());
        dto.setRelatedEntityId(notification.getRelatedEntityId());
        dto.setRead(notification.isRead());
        dto.setReadAt(notification.getReadAt());
        dto.setSent(notification.isSent());
        dto.setSentAt(notification.getSentAt());
        dto.setScheduledFor(notification.getScheduledFor());
        dto.setCreatedAt(notification.getCreatedAt());
        dto.setUpdatedAt(notification.getUpdatedAt());
        return dto;
    }
    
    /**
     * Classe pour les statistiques des notifications
     */
    @lombok.Builder
    @lombok.Data
    public static class NotificationStatistics {
        private long totalNotifications;
        private long unreadNotifications;
        private long sentNotifications;
        private long pendingNotifications;
    }
}