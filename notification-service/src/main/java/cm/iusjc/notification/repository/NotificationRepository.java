package cm.iusjc.notification.repository;

import cm.iusjc.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    List<Notification> findByRecipient(String recipient);
    
    List<Notification> findByStatus(String status);
    
    List<Notification> findByType(String type);
    
    List<Notification> findByRecipientAndStatus(String recipient, String status);
}
