package cm.iusjc.notification.repository;

import cm.iusjc.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    List<Notification> findByRecipient(String recipient);
    
    List<Notification> findByStatus(String status);
    
    List<Notification> findByType(String type);
    
    List<Notification> findByRecipientAndStatus(String recipient, String status);
    
    // Nouveaux méthodes pour les rappels
    List<Notification> findByStatusAndScheduledForLessThanEqual(String status, LocalDateTime scheduledFor);
    
    List<Notification> findByRecipientAndStatusOrderByScheduledForAsc(String recipient, String status);
    
    List<Notification> findByEventTypeAndEventId(String eventType, Long eventId);
    
    List<Notification> findByPriorityAndStatus(String priority, String status);
    
    @Query("SELECT n FROM Notification n WHERE n.status = 'SCHEDULED' AND n.scheduledFor BETWEEN :start AND :end")
    List<Notification> findScheduledNotificationsBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT n FROM Notification n WHERE n.recipient = :recipient AND n.eventType = :eventType AND n.status IN ('PENDING', 'SCHEDULED')")
    List<Notification> findPendingNotificationsByRecipientAndEventType(@Param("recipient") String recipient, @Param("eventType") String eventType);
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.status = 'FAILED' AND n.createdAt >= :since")
    Long countFailedNotificationsSince(@Param("since") LocalDateTime since);
}
