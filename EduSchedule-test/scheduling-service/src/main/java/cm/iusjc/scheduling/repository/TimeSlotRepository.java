package cm.iusjc.scheduling.repository;

import cm.iusjc.scheduling.entity.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {
    
    // Recherche par jour de la semaine
    List<TimeSlot> findByDayOfWeek(String dayOfWeek);
    List<TimeSlot> findByDayOfWeekOrderByStartTimeAsc(String dayOfWeek);
    long countByDayOfWeek(String dayOfWeek);
    
    // Recherche par planning
    List<TimeSlot> findByScheduleId(Long scheduleId);
    List<TimeSlot> findByScheduleIdOrderByDayOfWeekAscStartTimeAsc(Long scheduleId);
    long countByScheduleId(Long scheduleId);
    
    // Recherche par heure
    List<TimeSlot> findByStartTime(LocalTime startTime);
    List<TimeSlot> findByEndTime(LocalTime endTime);
    List<TimeSlot> findByStartTimeBetween(LocalTime start, LocalTime end);
    List<TimeSlot> findByEndTimeBetween(LocalTime start, LocalTime end);
    
    // Recherche par plage horaire
    @Query("SELECT ts FROM TimeSlot ts WHERE ts.startTime >= :start AND ts.endTime <= :end")
    List<TimeSlot> findTimeSlotsInRange(@Param("start") LocalTime start, @Param("end") LocalTime end);
    
    // Conflits de créneaux
    @Query("SELECT ts FROM TimeSlot ts WHERE ts.dayOfWeek = :dayOfWeek AND " +
           "((ts.startTime <= :startTime AND ts.endTime > :startTime) OR " +
           "(ts.startTime < :endTime AND ts.endTime >= :endTime) OR " +
           "(ts.startTime >= :startTime AND ts.endTime <= :endTime)) AND " +
           "ts.id != :excludeId")
    List<TimeSlot> findConflictingTimeSlots(@Param("dayOfWeek") String dayOfWeek,
                                          @Param("startTime") LocalTime startTime,
                                          @Param("endTime") LocalTime endTime,
                                          @Param("excludeId") Long excludeId);
    
    // Recherche avancée
    @Query("SELECT ts FROM TimeSlot ts WHERE " +
           "(:dayOfWeek IS NULL OR ts.dayOfWeek = :dayOfWeek) AND " +
           "(:scheduleId IS NULL OR ts.schedule.id = :scheduleId) AND " +
           "(:startTime IS NULL OR ts.startTime >= :startTime) AND " +
           "(:endTime IS NULL OR ts.endTime <= :endTime)")
    List<TimeSlot> findTimeSlotsWithFilters(@Param("dayOfWeek") String dayOfWeek,
                                          @Param("scheduleId") Long scheduleId,
                                          @Param("startTime") LocalTime startTime,
                                          @Param("endTime") LocalTime endTime);
    
    // Statistiques
    @Query("SELECT ts.dayOfWeek, COUNT(ts) FROM TimeSlot ts GROUP BY ts.dayOfWeek ORDER BY ts.dayOfWeek")
    List<Object[]> getTimeSlotCountByDayOfWeek();
    
    @Query("SELECT HOUR(ts.startTime), COUNT(ts) FROM TimeSlot ts GROUP BY HOUR(ts.startTime) ORDER BY HOUR(ts.startTime)")
    List<Object[]> getTimeSlotCountByStartHour();
    
    @Query("SELECT AVG(TIMESTAMPDIFF(MINUTE, ts.startTime, ts.endTime)) FROM TimeSlot ts")
    Double getAverageTimeSlotDuration();
    
    // Créneaux par période de la journée
    @Query("SELECT ts FROM TimeSlot ts WHERE ts.startTime >= :morningStart AND ts.startTime < :morningEnd")
    List<TimeSlot> findMorningTimeSlots(@Param("morningStart") LocalTime morningStart, @Param("morningEnd") LocalTime morningEnd);
    
    @Query("SELECT ts FROM TimeSlot ts WHERE ts.startTime >= :afternoonStart AND ts.startTime < :afternoonEnd")
    List<TimeSlot> findAfternoonTimeSlots(@Param("afternoonStart") LocalTime afternoonStart, @Param("afternoonEnd") LocalTime afternoonEnd);
    
    @Query("SELECT ts FROM TimeSlot ts WHERE ts.startTime >= :eveningStart AND ts.startTime < :eveningEnd")
    List<TimeSlot> findEveningTimeSlots(@Param("eveningStart") LocalTime eveningStart, @Param("eveningEnd") LocalTime eveningEnd);
    
    // Créneaux disponibles (sans planning associé)
    @Query("SELECT ts FROM TimeSlot ts WHERE ts.schedule IS NULL")
    List<TimeSlot> findAvailableTimeSlots();
    
    // Créneaux occupés (avec planning associé)
    @Query("SELECT ts FROM TimeSlot ts WHERE ts.schedule IS NOT NULL")
    List<TimeSlot> findOccupiedTimeSlots();
    
    // Créneaux les plus longs
    @Query("SELECT ts FROM TimeSlot ts ORDER BY TIMESTAMPDIFF(MINUTE, ts.startTime, ts.endTime) DESC")
    List<TimeSlot> findLongestTimeSlots();
    
    // Créneaux les plus courts
    @Query("SELECT ts FROM TimeSlot ts ORDER BY TIMESTAMPDIFF(MINUTE, ts.startTime, ts.endTime) ASC")
    List<TimeSlot> findShortestTimeSlots();
    
    // Vérification d'existence
    boolean existsByDayOfWeekAndStartTimeAndEndTime(String dayOfWeek, LocalTime startTime, LocalTime endTime);
    
    // Suppression par planning
    void deleteByScheduleId(Long scheduleId);
}