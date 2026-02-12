package cm.iusjc.calendar.repository;

import cm.iusjc.calendar.entity.CalendarEvent;
import cm.iusjc.calendar.entity.CalendarIntegration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CalendarEventRepository extends JpaRepository<CalendarEvent, Long> {
    
    List<CalendarEvent> findByIntegration(CalendarIntegration integration);
    
    List<CalendarEvent> findByScheduleId(Long scheduleId);
    
    List<CalendarEvent> findByReservationId(Long reservationId);
    
    Optional<CalendarEvent> findByExternalEventIdAndIntegration(String externalEventId, CalendarIntegration integration);
    
    Optional<CalendarEvent> findByIntegrationAndExternalEventId(CalendarIntegration integration, String externalEventId);
    
    List<CalendarEvent> findBySyncStatus(CalendarEvent.EventSyncStatus syncStatus);
    
    @Query("SELECT ce FROM CalendarEvent ce WHERE ce.integration = :integration AND ce.startTime BETWEEN :startTime AND :endTime")
    List<CalendarEvent> findByIntegrationAndTimeRange(@Param("integration") CalendarIntegration integration, 
                                                     @Param("startTime") LocalDateTime startTime, 
                                                     @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT ce FROM CalendarEvent ce WHERE ce.integration.userId = :userId AND ce.startTime BETWEEN :startTime AND :endTime")
    List<CalendarEvent> findByUserIdAndTimeRange(@Param("userId") String userId, 
                                                @Param("startTime") LocalDateTime startTime, 
                                                @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT ce FROM CalendarEvent ce WHERE ce.integration.userId = :userId AND ce.startTime BETWEEN :startTime AND :endTime")
    List<CalendarEvent> findByUserIdAndDateRange(@Param("userId") String userId, 
                                                @Param("startTime") LocalDateTime startTime, 
                                                @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT ce FROM CalendarEvent ce WHERE ce.syncStatus = 'PENDING' OR (ce.syncStatus = 'ERROR' AND ce.lastSyncedAt < :retryAfter)")
    List<CalendarEvent> findEventsNeedingSync(@Param("retryAfter") LocalDateTime retryAfter);
    
    @Query("SELECT ce FROM CalendarEvent ce WHERE ce.integration = :integration AND ce.scheduleId = :scheduleId")
    Optional<CalendarEvent> findByIntegrationAndScheduleId(@Param("integration") CalendarIntegration integration, 
                                                          @Param("scheduleId") Long scheduleId);
    
    @Query("SELECT ce FROM CalendarEvent ce WHERE ce.integration = :integration AND ce.reservationId = :reservationId")
    Optional<CalendarEvent> findByIntegrationAndReservationId(@Param("integration") CalendarIntegration integration, 
                                                             @Param("reservationId") Long reservationId);
    
    @Query("SELECT COUNT(ce) FROM CalendarEvent ce WHERE ce.integration.userId = :userId AND ce.startTime >= :startDate AND ce.startTime < :endDate")
    Long countEventsByUserAndDateRange(@Param("userId") String userId, 
                                      @Param("startDate") LocalDateTime startDate, 
                                      @Param("endDate") LocalDateTime endDate);
}