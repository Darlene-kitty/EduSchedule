package cm.iusjc.calendar.repository;

import cm.iusjc.calendar.entity.CalendarIntegration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CalendarIntegrationRepository extends JpaRepository<CalendarIntegration, Long> {
    
    List<CalendarIntegration> findByUserId(String userId);
    
    List<CalendarIntegration> findByUserIdAndEnabled(String userId, Boolean enabled);
    
    List<CalendarIntegration> findByUserIdAndEnabledTrue(String userId);
    
    List<CalendarIntegration> findByUserIdAndSyncEnabledTrue(String userId);
    
    Optional<CalendarIntegration> findByUserIdAndProvider(String userId, CalendarIntegration.CalendarProvider provider);
    
    List<CalendarIntegration> findByProvider(CalendarIntegration.CalendarProvider provider);
    
    List<CalendarIntegration> findByEnabledAndSyncEnabled(Boolean enabled, Boolean syncEnabled);
    
    List<CalendarIntegration> findBySyncStatus(CalendarIntegration.SyncStatus syncStatus);
    
    @Query("SELECT ci FROM CalendarIntegration ci WHERE ci.enabled = true AND ci.syncEnabled = true AND (ci.lastSyncAt IS NULL OR ci.lastSyncAt < :since)")
    List<CalendarIntegration> findIntegrationsNeedingSync(@Param("since") LocalDateTime since);
    
    @Query("SELECT ci FROM CalendarIntegration ci WHERE ci.tokenExpiresAt IS NOT NULL AND ci.tokenExpiresAt < :expiryTime")
    List<CalendarIntegration> findIntegrationsWithExpiringTokens(@Param("expiryTime") LocalDateTime expiryTime);
    
    @Query("SELECT COUNT(ci) FROM CalendarIntegration ci WHERE ci.userId = :userId AND ci.enabled = true")
    Long countActiveIntegrationsByUser(@Param("userId") String userId);
}