package cm.iusjc.scheduling.service;

import cm.iusjc.scheduling.dto.ScheduleDTO;
import cm.iusjc.scheduling.dto.ScheduleRequest;
import cm.iusjc.scheduling.entity.Schedule;
import cm.iusjc.scheduling.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleService {
    
    private final ScheduleRepository scheduleRepository;
    private final NotificationPublisher notificationPublisher;
    
    @Transactional
    @CacheEvict(value = "schedules", allEntries = true)
    public ScheduleDTO createSchedule(ScheduleRequest request) {
        Schedule schedule = new Schedule();
        schedule.setTitle(request.getTitle());
        schedule.setDescription(request.getDescription());
        schedule.setStartTime(request.getStartTime());
        schedule.setEndTime(request.getEndTime());
        schedule.setRoom(request.getRoom());
        schedule.setTeacher(request.getTeacher());
        schedule.setCourse(request.getCourse());
        schedule.setGroupName(request.getGroupName());
        schedule.setStatus("ACTIVE");
        
        Schedule savedSchedule = scheduleRepository.save(schedule);
        
        // Publier événement vers RabbitMQ
        notificationPublisher.publishScheduleCreated(savedSchedule);
        
        log.info("Schedule created: {}", savedSchedule.getId());
        return convertToDTO(savedSchedule);
    }
    
    @Cacheable(value = "schedules")
    public List<ScheduleDTO> getAllSchedules() {
        return scheduleRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Cacheable(value = "schedules", key = "#id")
    public ScheduleDTO getScheduleById(Long id) {
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));
        return convertToDTO(schedule);
    }
    
    public List<ScheduleDTO> getSchedulesByTeacher(String teacher) {
        return scheduleRepository.findByTeacher(teacher).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<ScheduleDTO> getSchedulesByGroup(String groupName) {
        return scheduleRepository.findByGroupName(groupName).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<ScheduleDTO> getSchedulesByRoom(String room) {
        return scheduleRepository.findByRoom(room).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<ScheduleDTO> getSchedulesByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return scheduleRepository.findByDateRange(startDate, endDate).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional
    @CacheEvict(value = "schedules", key = "#id")
    public ScheduleDTO updateSchedule(Long id, ScheduleRequest request) {
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));
        
        schedule.setTitle(request.getTitle());
        schedule.setDescription(request.getDescription());
        schedule.setStartTime(request.getStartTime());
        schedule.setEndTime(request.getEndTime());
        schedule.setRoom(request.getRoom());
        schedule.setTeacher(request.getTeacher());
        schedule.setCourse(request.getCourse());
        schedule.setGroupName(request.getGroupName());
        
        Schedule updatedSchedule = scheduleRepository.save(schedule);
        
        // Publier événement
        notificationPublisher.publishScheduleUpdated(updatedSchedule);
        
        log.info("Schedule updated: {}", updatedSchedule.getId());
        return convertToDTO(updatedSchedule);
    }
    
    @Transactional
    @CacheEvict(value = "schedules", key = "#id")
    public void deleteSchedule(Long id) {
        if (!scheduleRepository.existsById(id)) {
            throw new RuntimeException("Schedule not found");
        }
        scheduleRepository.deleteById(id);
        
        // Publier événement
        notificationPublisher.publishScheduleDeleted(id);
        
        log.info("Schedule deleted: {}", id);
    }
    
    private ScheduleDTO convertToDTO(Schedule schedule) {
        return new ScheduleDTO(
                schedule.getId(),
                schedule.getTitle(),
                schedule.getDescription(),
                schedule.getStartTime(),
                schedule.getEndTime(),
                schedule.getRoom(),
                schedule.getTeacher(),
                schedule.getCourse(),
                schedule.getGroupName(),
                schedule.getStatus(),
                schedule.getCreatedAt(),
                schedule.getUpdatedAt()
        );
    }
}
