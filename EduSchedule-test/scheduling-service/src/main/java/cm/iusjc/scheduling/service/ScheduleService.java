package cm.iusjc.scheduling.service;

import cm.iusjc.scheduling.dto.ScheduleDTO;
import cm.iusjc.scheduling.entity.Schedule;
import cm.iusjc.scheduling.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleService {
    
    private final ScheduleRepository scheduleRepository;
    
    /**
     * Crée un nouveau planning
     */
    @Transactional
    @CacheEvict(value = "schedules", allEntries = true)
    public ScheduleDTO createSchedule(ScheduleDTO scheduleDTO) {
        log.info("Creating new schedule: {}", scheduleDTO.getTitle());
        
        // Validation des dates
        validateScheduleTimes(scheduleDTO.getStartTime(), scheduleDTO.getEndTime());
        
        // Vérifier les conflits
        checkForConflicts(scheduleDTO, null);
        
        Schedule schedule = new Schedule();
        schedule.setTitle(scheduleDTO.getTitle());
        schedule.setDescription(scheduleDTO.getDescription());
        schedule.setStartTime(scheduleDTO.getStartTime());
        schedule.setEndTime(scheduleDTO.getEndTime());
        schedule.setRoom(scheduleDTO.getRoom());
        schedule.setTeacher(scheduleDTO.getTeacher());
        schedule.setCourse(scheduleDTO.getCourse());
        schedule.setGroupName(scheduleDTO.getGroupName());
        schedule.setStatus(scheduleDTO.getStatus() != null ? scheduleDTO.getStatus() : "ACTIVE");
        schedule.setCreatedAt(LocalDateTime.now());
        schedule.setUpdatedAt(LocalDateTime.now());
        
        Schedule savedSchedule = scheduleRepository.save(schedule);
        log.info("Schedule created successfully with ID: {}", savedSchedule.getId());
        
        return convertToDTO(savedSchedule);
    }
    
    /**
     * Récupère tous les plannings
     */
    @Cacheable(value = "schedules")
    public List<ScheduleDTO> getAllSchedules() {
        log.debug("Fetching all schedules");
        return scheduleRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les plannings avec pagination
     */
    public Page<ScheduleDTO> getAllSchedules(Pageable pageable) {
        log.debug("Fetching schedules with pagination: {}", pageable);
        return scheduleRepository.findAll(pageable)
                .map(this::convertToDTO);
    }
    
    /**
     * Récupère un planning par ID
     */
    @Cacheable(value = "schedules", key = "#id")
    public Optional<ScheduleDTO> getScheduleById(Long id) {
        log.debug("Fetching schedule by ID: {}", id);
        return scheduleRepository.findById(id)
                .map(this::convertToDTO);
    }
    
    /**
     * Récupère un planning par titre
     */
    @Cacheable(value = "schedules", key = "#title")
    public Optional<ScheduleDTO> getScheduleByTitle(String title) {
        log.debug("Fetching schedule by title: {}", title);
        return scheduleRepository.findByTitle(title)
                .map(this::convertToDTO);
    }
    
    /**
     * Récupère les plannings par statut
     */
    public List<ScheduleDTO> getSchedulesByStatus(String status) {
        log.debug("Fetching schedules by status: {}", status);
        return scheduleRepository.findByStatusOrderByStartTimeAsc(status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les plannings par enseignant
     */
    public List<ScheduleDTO> getSchedulesByTeacher(String teacher) {
        log.debug("Fetching schedules by teacher: {}", teacher);
        return scheduleRepository.findByTeacherOrderByStartTimeAsc(teacher).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les plannings par cours
     */
    public List<ScheduleDTO> getSchedulesByCourse(String course) {
        log.debug("Fetching schedules by course: {}", course);
        return scheduleRepository.findByCourseOrderByStartTimeAsc(course).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les plannings par salle
     */
    public List<ScheduleDTO> getSchedulesByRoom(String room) {
        log.debug("Fetching schedules by room: {}", room);
        return scheduleRepository.findByRoomOrderByStartTimeAsc(room).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les plannings par groupe
     */
    public List<ScheduleDTO> getSchedulesByGroup(String groupName) {
        log.debug("Fetching schedules by group: {}", groupName);
        return scheduleRepository.findByGroupNameOrderByStartTimeAsc(groupName).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les plannings par période
     */
    public List<ScheduleDTO> getSchedulesByPeriod(LocalDateTime start, LocalDateTime end) {
        log.debug("Fetching schedules between {} and {}", start, end);
        return scheduleRepository.findSchedulesInPeriod(start, end).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les plannings par date
     */
    public List<ScheduleDTO> getSchedulesByDate(LocalDateTime date) {
        log.debug("Fetching schedules for date: {}", date);
        return scheduleRepository.findByDateOrderByStartTime(date).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les plannings à venir
     */
    @Cacheable(value = "upcomingSchedules")
    public List<ScheduleDTO> getUpcomingSchedules() {
        log.debug("Fetching upcoming schedules");
        return scheduleRepository.findUpcomingSchedules(LocalDateTime.now()).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les plannings en cours
     */
    public List<ScheduleDTO> getCurrentSchedules() {
        log.debug("Fetching current schedules");
        return scheduleRepository.findCurrentSchedules(LocalDateTime.now()).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les plannings de la semaine
     */
    public List<ScheduleDTO> getWeeklySchedules(LocalDateTime weekStart) {
        log.debug("Fetching weekly schedules starting from: {}", weekStart);
        LocalDateTime weekEnd = weekStart.plusWeeks(1);
        return scheduleRepository.findWeeklySchedules(weekStart, weekEnd).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Met à jour un planning
     */
    @Transactional
    @CacheEvict(value = {"schedules", "upcomingSchedules"}, allEntries = true)
    public ScheduleDTO updateSchedule(Long id, ScheduleDTO scheduleDTO) {
        log.info("Updating schedule with ID: {}", id);
        
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Schedule not found with ID: " + id));
        
        // Validation des dates
        validateScheduleTimes(scheduleDTO.getStartTime(), scheduleDTO.getEndTime());
        
        // Vérifier les conflits (exclure le planning actuel)
        checkForConflicts(scheduleDTO, id);
        
        schedule.setTitle(scheduleDTO.getTitle());
        schedule.setDescription(scheduleDTO.getDescription());
        schedule.setStartTime(scheduleDTO.getStartTime());
        schedule.setEndTime(scheduleDTO.getEndTime());
        schedule.setRoom(scheduleDTO.getRoom());
        schedule.setTeacher(scheduleDTO.getTeacher());
        schedule.setCourse(scheduleDTO.getCourse());
        schedule.setGroupName(scheduleDTO.getGroupName());
        schedule.setStatus(scheduleDTO.getStatus());
        schedule.setUpdatedAt(LocalDateTime.now());
        
        Schedule updatedSchedule = scheduleRepository.save(schedule);
        log.info("Schedule updated successfully: {}", updatedSchedule.getId());
        
        return convertToDTO(updatedSchedule);
    }
    
    /**
     * Change le statut d'un planning
     */
    @Transactional
    @CacheEvict(value = {"schedules", "upcomingSchedules"}, allEntries = true)
    public ScheduleDTO updateScheduleStatus(Long id, String status) {
        log.info("Updating schedule status for ID: {} to {}", id, status);
        
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Schedule not found with ID: " + id));
        
        schedule.setStatus(status);
        schedule.setUpdatedAt(LocalDateTime.now());
        
        Schedule updatedSchedule = scheduleRepository.save(schedule);
        log.info("Schedule status updated: {} - Status: {}", updatedSchedule.getTitle(), updatedSchedule.getStatus());
        
        return convertToDTO(updatedSchedule);
    }
    
    /**
     * Annule un planning
     */
    @Transactional
    @CacheEvict(value = {"schedules", "upcomingSchedules"}, allEntries = true)
    public ScheduleDTO cancelSchedule(Long id) {
        return updateScheduleStatus(id, "CANCELLED");
    }
    
    /**
     * Marque un planning comme terminé
     */
    @Transactional
    @CacheEvict(value = {"schedules", "upcomingSchedules"}, allEntries = true)
    public ScheduleDTO completeSchedule(Long id) {
        return updateScheduleStatus(id, "COMPLETED");
    }
    
    /**
     * Réactive un planning
     */
    @Transactional
    @CacheEvict(value = {"schedules", "upcomingSchedules"}, allEntries = true)
    public ScheduleDTO reactivateSchedule(Long id) {
        return updateScheduleStatus(id, "ACTIVE");
    }
    
    /**
     * Supprime un planning
     */
    @Transactional
    @CacheEvict(value = {"schedules", "upcomingSchedules"}, allEntries = true)
    public void deleteSchedule(Long id) {
        log.info("Deleting schedule with ID: {}", id);
        
        if (!scheduleRepository.existsById(id)) {
            throw new RuntimeException("Schedule not found with ID: " + id);
        }
        
        scheduleRepository.deleteById(id);
        log.info("Schedule deleted: {}", id);
    }
    
    /**
     * Recherche des plannings par titre
     */
    public List<ScheduleDTO> searchSchedulesByTitle(String title) {
        log.debug("Searching schedules by title containing: {}", title);
        return scheduleRepository.findByTitleContainingIgnoreCase(title).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Recherche avancée de plannings
     */
    public List<ScheduleDTO> searchSchedulesWithFilters(String title, String teacher, String course, 
            String room, String groupName, String status) {
        log.debug("Searching schedules with advanced filters");
        return scheduleRepository.findSchedulesWithFilters(title, teacher, course, room, groupName, status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Recherche textuelle globale
     */
    public List<ScheduleDTO> searchSchedules(String searchTerm) {
        log.debug("Searching schedules with term: {}", searchTerm);
        return scheduleRepository.searchSchedules(searchTerm).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Vérifie les conflits de salle
     */
    public List<ScheduleDTO> checkRoomConflicts(String room, LocalDateTime startTime, LocalDateTime endTime, Long excludeId) {
        Long excludeIdSafe = excludeId != null ? excludeId : -1L;
        return scheduleRepository.findRoomConflicts(room, startTime, endTime, excludeIdSafe).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Vérifie les conflits d'enseignant
     */
    public List<ScheduleDTO> checkTeacherConflicts(String teacher, LocalDateTime startTime, LocalDateTime endTime, Long excludeId) {
        Long excludeIdSafe = excludeId != null ? excludeId : -1L;
        return scheduleRepository.findTeacherConflicts(teacher, startTime, endTime, excludeIdSafe).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtient les statistiques des plannings
     */
    public ScheduleStatistics getScheduleStatistics() {
        long totalSchedules = scheduleRepository.count();
        long activeSchedules = scheduleRepository.countByStatus("ACTIVE");
        long completedSchedules = scheduleRepository.countByStatus("COMPLETED");
        long cancelledSchedules = scheduleRepository.countByStatus("CANCELLED");
        
        return ScheduleStatistics.builder()
                .totalSchedules(totalSchedules)
                .activeSchedules(activeSchedules)
                .completedSchedules(completedSchedules)
                .cancelledSchedules(cancelledSchedules)
                .build();
    }
    
    /**
     * Obtient les statistiques par statut
     */
    public List<Object[]> getScheduleStatisticsByStatus() {
        return scheduleRepository.getScheduleCountByStatus();
    }
    
    /**
     * Obtient les statistiques par enseignant
     */
    public List<Object[]> getScheduleStatisticsByTeacher() {
        return scheduleRepository.getScheduleCountByTeacher();
    }
    
    /**
     * Obtient les statistiques par cours
     */
    public List<Object[]> getScheduleStatisticsByCourse() {
        return scheduleRepository.getScheduleCountByCourse();
    }
    
    /**
     * Obtient les statistiques par salle
     */
    public List<Object[]> getScheduleStatisticsByRoom() {
        return scheduleRepository.getScheduleCountByRoom();
    }
    
    /**
     * Vérifie si un planning existe
     */
    public boolean existsById(Long id) {
        return scheduleRepository.existsById(id);
    }
    
    /**
     * Vérifie si un planning existe par titre
     */
    public boolean existsByTitle(String title) {
        return scheduleRepository.existsByTitle(title);
    }
    
    /**
     * Compte le nombre total de plannings
     */
    public long countSchedules() {
        return scheduleRepository.count();
    }
    
    /**
     * Compte les plannings par statut
     */
    public long countSchedulesByStatus(String status) {
        return scheduleRepository.countByStatus(status);
    }
    
    /**
     * Validation des heures de planning
     */
    private void validateScheduleTimes(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            throw new RuntimeException("Start time and end time are required");
        }
        
        if (startTime.isAfter(endTime)) {
            throw new RuntimeException("Start time must be before end time");
        }
        
        if (startTime.isBefore(LocalDateTime.now().minus(1, ChronoUnit.HOURS))) {
            throw new RuntimeException("Cannot create schedule in the past");
        }
        
        long durationMinutes = ChronoUnit.MINUTES.between(startTime, endTime);
        if (durationMinutes < 15) {
            throw new RuntimeException("Schedule duration must be at least 15 minutes");
        }
        
        if (durationMinutes > 480) { // 8 heures
            throw new RuntimeException("Schedule duration cannot exceed 8 hours");
        }
    }
    
    /**
     * Vérifie les conflits avant création/modification
     */
    private void checkForConflicts(ScheduleDTO scheduleDTO, Long excludeId) {
        if (scheduleDTO.getRoom() != null && !scheduleDTO.getRoom().trim().isEmpty()) {
            List<ScheduleDTO> roomConflicts = checkRoomConflicts(
                scheduleDTO.getRoom(), 
                scheduleDTO.getStartTime(), 
                scheduleDTO.getEndTime(), 
                excludeId
            );
            if (!roomConflicts.isEmpty()) {
                throw new RuntimeException("Room conflict detected: " + scheduleDTO.getRoom() + 
                    " is already booked during this time");
            }
        }
        
        if (scheduleDTO.getTeacher() != null && !scheduleDTO.getTeacher().trim().isEmpty()) {
            List<ScheduleDTO> teacherConflicts = checkTeacherConflicts(
                scheduleDTO.getTeacher(), 
                scheduleDTO.getStartTime(), 
                scheduleDTO.getEndTime(), 
                excludeId
            );
            if (!teacherConflicts.isEmpty()) {
                throw new RuntimeException("Teacher conflict detected: " + scheduleDTO.getTeacher() + 
                    " is already scheduled during this time");
            }
        }
    }
    
    /**
     * Convertit une entité Schedule en DTO
     */
    private ScheduleDTO convertToDTO(Schedule schedule) {
        ScheduleDTO dto = new ScheduleDTO();
        dto.setId(schedule.getId());
        dto.setTitle(schedule.getTitle());
        dto.setDescription(schedule.getDescription());
        dto.setStartTime(schedule.getStartTime());
        dto.setEndTime(schedule.getEndTime());
        dto.setRoom(schedule.getRoom());
        dto.setTeacher(schedule.getTeacher());
        dto.setCourse(schedule.getCourse());
        dto.setGroupName(schedule.getGroupName());
        dto.setStatus(schedule.getStatus());
        dto.setCreatedAt(schedule.getCreatedAt());
        dto.setUpdatedAt(schedule.getUpdatedAt());
        return dto;
    }
    
    /**
     * Classe pour les statistiques des plannings
     */
    @lombok.Builder
    @lombok.Data
    public static class ScheduleStatistics {
        private long totalSchedules;
        private long activeSchedules;
        private long completedSchedules;
        private long cancelledSchedules;
    }
}