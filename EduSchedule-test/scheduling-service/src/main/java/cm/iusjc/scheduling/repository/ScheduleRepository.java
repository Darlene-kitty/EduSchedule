package cm.iusjc.scheduling.repository;

import cm.iusjc.scheduling.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    
    List<Schedule> findByTeacher(String teacher);
    
    List<Schedule> findByGroupName(String groupName);
    
    List<Schedule> findByRoom(String room);
    
    List<Schedule> findByStatus(String status);
    
    @Query("SELECT s FROM Schedule s WHERE s.startTime >= :startDate AND s.endTime <= :endDate")
    List<Schedule> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                   @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT s FROM Schedule s WHERE s.teacher = :teacher AND s.startTime >= :startDate AND s.endTime <= :endDate")
    List<Schedule> findByTeacherAndDateRange(@Param("teacher") String teacher,
                                             @Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate);
}
