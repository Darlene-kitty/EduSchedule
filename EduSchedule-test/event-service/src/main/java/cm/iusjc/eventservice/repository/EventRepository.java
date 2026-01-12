package cm.iusjc.eventservice.repository;

import cm.iusjc.eventservice.entity.Event;
import cm.iusjc.eventservice.entity.EventStatus;
import cm.iusjc.eventservice.entity.EventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    
    // Recherche par organisateur
    Page<Event> findByOrganizerIdOrderByStartDateTimeDesc(Long organizerId, Pageable pageable);
    
    // Recherche par type
    Page<Event> findByTypeOrderByStartDateTimeDesc(EventType type, Pageable pageable);
    
    // Recherche par statut
    Page<Event> findByStatusOrderByStartDateTimeDesc(EventStatus status, Pageable pageable);
    
    // Recherche par ressource
    Page<Event> findByResourceIdOrderByStartDateTimeDesc(Long resourceId, Pageable pageable);
    
    // Recherche par plage de dates
    @Query("SELECT e FROM Event e WHERE e.startDateTime BETWEEN :startDate AND :endDate ORDER BY e.startDateTime")
    List<Event> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                               @Param("endDate") LocalDateTime endDate);
    
    // Vérification de conflits de ressource
    @Query("SELECT e FROM Event e WHERE e.resourceId = :resourceId " +
           "AND e.status IN ('PLANNED', 'CONFIRMED') " +
           "AND ((e.startDateTime <= :endDateTime AND e.endDateTime >= :startDateTime))")
    List<Event> findConflictingEvents(@Param("resourceId") Long resourceId,
                                     @Param("startDateTime") LocalDateTime startDateTime,
                                     @Param("endDateTime") LocalDateTime endDateTime);
    
    // Événements à venir
    @Query("SELECT e FROM Event e WHERE e.startDateTime > :now AND e.status IN ('PLANNED', 'CONFIRMED') ORDER BY e.startDateTime")
    List<Event> findUpcomingEvents(@Param("now") LocalDateTime now, Pageable pageable);
    
    // Événements du jour
    @Query("SELECT e FROM Event e WHERE DATE(e.startDateTime) = DATE(:date) ORDER BY e.startDateTime")
    List<Event> findEventsByDate(@Param("date") LocalDateTime date);
    
    // Statistiques par type
    @Query("SELECT e.type, COUNT(e) FROM Event e GROUP BY e.type")
    List<Object[]> getEventCountByType();
}