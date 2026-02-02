package cm.iusjc.scheduling.service;

import cm.iusjc.scheduling.dto.ScheduleDTO;
import cm.iusjc.scheduling.dto.ScheduleRequest;
import cm.iusjc.scheduling.entity.Schedule;
import cm.iusjc.scheduling.repository.ScheduleRepository;
import cm.iusjc.scheduling.event.ScheduleChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher eventPublisher;
    
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
        
        // Publier événement pour notifications automatiques
        eventPublisher.publishEvent(new ScheduleChangedEvent(this, savedSchedule, 
            ScheduleChangedEvent.ChangeType.CREATED, getCurrentUserId()));
        
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
        Schedule oldSchedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));
        
        // Créer une copie de l'ancien emploi du temps pour les notifications
        Schedule oldScheduleCopy = new Schedule();
        oldScheduleCopy.setId(oldSchedule.getId());
        oldScheduleCopy.setTitle(oldSchedule.getTitle());
        oldScheduleCopy.setDescription(oldSchedule.getDescription());
        oldScheduleCopy.setStartTime(oldSchedule.getStartTime());
        oldScheduleCopy.setEndTime(oldSchedule.getEndTime());
        oldScheduleCopy.setRoom(oldSchedule.getRoom());
        oldScheduleCopy.setTeacher(oldSchedule.getTeacher());
        oldScheduleCopy.setCourse(oldSchedule.getCourse());
        oldScheduleCopy.setGroupName(oldSchedule.getGroupName());
        oldScheduleCopy.setStatus(oldSchedule.getStatus());
        
        // Mettre à jour
        oldSchedule.setTitle(request.getTitle());
        oldSchedule.setDescription(request.getDescription());
        oldSchedule.setStartTime(request.getStartTime());
        oldSchedule.setEndTime(request.getEndTime());
        oldSchedule.setRoom(request.getRoom());
        oldSchedule.setTeacher(request.getTeacher());
        oldSchedule.setCourse(request.getCourse());
        oldSchedule.setGroupName(request.getGroupName());
        
        Schedule updatedSchedule = scheduleRepository.save(oldSchedule);
        
        // Publier événement
        notificationPublisher.publishScheduleUpdated(updatedSchedule);
        
        // Publier événement pour notifications automatiques
        eventPublisher.publishEvent(new ScheduleChangedEvent(this, updatedSchedule, oldScheduleCopy,
            ScheduleChangedEvent.ChangeType.UPDATED, getCurrentUserId()));
        
        log.info("Schedule updated: {}", updatedSchedule.getId());
        return convertToDTO(updatedSchedule);
    }
    
    @Transactional
    @CacheEvict(value = "schedules", key = "#id")
    public void deleteSchedule(Long id) {
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));
        
        // Marquer comme annulé au lieu de supprimer
        schedule.setStatus("CANCELLED");
        scheduleRepository.save(schedule);
        
        // Publier événement
        notificationPublisher.publishScheduleDeleted(id);
        
        // Publier événement pour notifications automatiques
        eventPublisher.publishEvent(new ScheduleChangedEvent(this, schedule,
            ScheduleChangedEvent.ChangeType.CANCELLED, getCurrentUserId()));
        
        log.info("Schedule cancelled: {}", id);
    }
    
    @Transactional
    @CacheEvict(value = "schedules", key = "#id")
    public void cancelSchedule(Long id, String reason) {
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));
        
        schedule.setStatus("CANCELLED");
        if (reason != null) {
            schedule.setDescription(schedule.getDescription() + " [ANNULÉ: " + reason + "]");
        }
        scheduleRepository.save(schedule);
        
        // Publier événement pour notifications automatiques
        eventPublisher.publishEvent(new ScheduleChangedEvent(this, schedule,
            ScheduleChangedEvent.ChangeType.CANCELLED, getCurrentUserId()));
        
        log.info("Schedule cancelled with reason: {} - {}", id, reason);
    }
    
    private Long getCurrentUserId() {
        // TODO: Récupérer l'ID de l'utilisateur connecté depuis le contexte de sécurité
        // Pour l'instant, retourner un ID par défaut
        return 1L;
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
