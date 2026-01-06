package cm.iusjc.scheduling.repository;

import cm.iusjc.scheduling.entity.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {
    
    List<TimeSlot> findByScheduleId(Long scheduleId);
    
    List<TimeSlot> findByDayOfWeek(String dayOfWeek);
}
