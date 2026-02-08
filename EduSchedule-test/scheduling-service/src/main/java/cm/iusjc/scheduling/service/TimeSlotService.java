package cm.iusjc.scheduling.service;

import cm.iusjc.scheduling.dto.TimeSlotDTO;
import cm.iusjc.scheduling.entity.Schedule;
import cm.iusjc.scheduling.entity.TimeSlot;
import cm.iusjc.scheduling.repository.ScheduleRepository;
import cm.iusjc.scheduling.repository.TimeSlotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TimeSlotService {
    
    private final TimeSlotRepository timeSlotRepository;
    private final ScheduleRepository scheduleRepository;
    
    /**
     * Crée un nouveau créneau horaire
     */
    @Transactional
    @CacheEvict(value = "timeSlots", allEntries = true)
    public TimeSlotDTO createTimeSlot(TimeSlotDTO timeSlotDTO) {
        log.info("Creating new time slot: {} {} - {}", 
            timeSlotDTO.getDayOfWeek(), timeSlotDTO.getStartTime(), timeSlotDTO.getEndTime());
        
        // Validation des heures
        validateTimeSlot(timeSlotDTO.getStartTime(), timeSlotDTO.getEndTime());
        
        // Vérifier les conflits
        checkForConflicts(timeSlotDTO, null);
        
        TimeSlot timeSlot = new TimeSlot();
        timeSlot.setDayOfWeek(timeSlotDTO.getDayOfWeek());
        timeSlot.setStartTime(timeSlotDTO.getStartTime());
        timeSlot.setEndTime(timeSlotDTO.getEndTime());
        
        // Associer au planning si spécifié
        if (timeSlotDTO.getScheduleId() != null) {
            Schedule schedule = scheduleRepository.findById(timeSlotDTO.getScheduleId())
                    .orElseThrow(() -> new RuntimeException("Schedule not found with ID: " + timeSlotDTO.getScheduleId()));
            timeSlot.setSchedule(schedule);
        }
        
        TimeSlot savedTimeSlot = timeSlotRepository.save(timeSlot);
        log.info("Time slot created successfully with ID: {}", savedTimeSlot.getId());
        
        return convertToDTO(savedTimeSlot);
    }
    
    /**
     * Récupère tous les créneaux horaires
     */
    @Cacheable(value = "timeSlots")
    public List<TimeSlotDTO> getAllTimeSlots() {
        log.debug("Fetching all time slots");
        return timeSlotRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les créneaux horaires avec pagination
     */
    public Page<TimeSlotDTO> getAllTimeSlots(Pageable pageable) {
        log.debug("Fetching time slots with pagination: {}", pageable);
        return timeSlotRepository.findAll(pageable)
                .map(this::convertToDTO);
    }
    
    /**
     * Récupère un créneau horaire par ID
     */
    @Cacheable(value = "timeSlots", key = "#id")
    public Optional<TimeSlotDTO> getTimeSlotById(Long id) {
        log.debug("Fetching time slot by ID: {}", id);
        return timeSlotRepository.findById(id)
                .map(this::convertToDTO);
    }
    
    /**
     * Récupère les créneaux horaires par jour de la semaine
     */
    public List<TimeSlotDTO> getTimeSlotsByDayOfWeek(String dayOfWeek) {
        log.debug("Fetching time slots by day of week: {}", dayOfWeek);
        return timeSlotRepository.findByDayOfWeekOrderByStartTimeAsc(dayOfWeek).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les créneaux horaires par planning
     */
    public List<TimeSlotDTO> getTimeSlotsBySchedule(Long scheduleId) {
        log.debug("Fetching time slots by schedule: {}", scheduleId);
        return timeSlotRepository.findByScheduleIdOrderByDayOfWeekAscStartTimeAsc(scheduleId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les créneaux horaires par heure de début
     */
    public List<TimeSlotDTO> getTimeSlotsByStartTime(LocalTime startTime) {
        log.debug("Fetching time slots by start time: {}", startTime);
        return timeSlotRepository.findByStartTime(startTime).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les créneaux horaires dans une plage horaire
     */
    public List<TimeSlotDTO> getTimeSlotsInRange(LocalTime start, LocalTime end) {
        log.debug("Fetching time slots in range: {} - {}", start, end);
        return timeSlotRepository.findTimeSlotsInRange(start, end).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les créneaux horaires du matin
     */
    public List<TimeSlotDTO> getMorningTimeSlots() {
        log.debug("Fetching morning time slots");
        LocalTime morningStart = LocalTime.of(6, 0);
        LocalTime morningEnd = LocalTime.of(12, 0);
        return timeSlotRepository.findMorningTimeSlots(morningStart, morningEnd).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les créneaux horaires de l'après-midi
     */
    public List<TimeSlotDTO> getAfternoonTimeSlots() {
        log.debug("Fetching afternoon time slots");
        LocalTime afternoonStart = LocalTime.of(12, 0);
        LocalTime afternoonEnd = LocalTime.of(18, 0);
        return timeSlotRepository.findAfternoonTimeSlots(afternoonStart, afternoonEnd).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les créneaux horaires du soir
     */
    public List<TimeSlotDTO> getEveningTimeSlots() {
        log.debug("Fetching evening time slots");
        LocalTime eveningStart = LocalTime.of(18, 0);
        LocalTime eveningEnd = LocalTime.of(23, 59);
        return timeSlotRepository.findEveningTimeSlots(eveningStart, eveningEnd).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les créneaux horaires disponibles
     */
    @Cacheable(value = "availableTimeSlots")
    public List<TimeSlotDTO> getAvailableTimeSlots() {
        log.debug("Fetching available time slots");
        return timeSlotRepository.findAvailableTimeSlots().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les créneaux horaires occupés
     */
    public List<TimeSlotDTO> getOccupiedTimeSlots() {
        log.debug("Fetching occupied time slots");
        return timeSlotRepository.findOccupiedTimeSlots().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Met à jour un créneau horaire
     */
    @Transactional
    @CacheEvict(value = {"timeSlots", "availableTimeSlots"}, allEntries = true)
    public TimeSlotDTO updateTimeSlot(Long id, TimeSlotDTO timeSlotDTO) {
        log.info("Updating time slot with ID: {}", id);
        
        TimeSlot timeSlot = timeSlotRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Time slot not found with ID: " + id));
        
        // Validation des heures
        validateTimeSlot(timeSlotDTO.getStartTime(), timeSlotDTO.getEndTime());
        
        // Vérifier les conflits (exclure le créneau actuel)
        checkForConflicts(timeSlotDTO, id);
        
        timeSlot.setDayOfWeek(timeSlotDTO.getDayOfWeek());
        timeSlot.setStartTime(timeSlotDTO.getStartTime());
        timeSlot.setEndTime(timeSlotDTO.getEndTime());
        
        // Mettre à jour l'association au planning
        if (timeSlotDTO.getScheduleId() != null) {
            Schedule schedule = scheduleRepository.findById(timeSlotDTO.getScheduleId())
                    .orElseThrow(() -> new RuntimeException("Schedule not found with ID: " + timeSlotDTO.getScheduleId()));
            timeSlot.setSchedule(schedule);
        } else {
            timeSlot.setSchedule(null);
        }
        
        TimeSlot updatedTimeSlot = timeSlotRepository.save(timeSlot);
        log.info("Time slot updated successfully: {}", updatedTimeSlot.getId());
        
        return convertToDTO(updatedTimeSlot);
    }
    
    /**
     * Associe un créneau horaire à un planning
     */
    @Transactional
    @CacheEvict(value = {"timeSlots", "availableTimeSlots"}, allEntries = true)
    public TimeSlotDTO assignToSchedule(Long timeSlotId, Long scheduleId) {
        log.info("Assigning time slot {} to schedule {}", timeSlotId, scheduleId);
        
        TimeSlot timeSlot = timeSlotRepository.findById(timeSlotId)
                .orElseThrow(() -> new RuntimeException("Time slot not found with ID: " + timeSlotId));
        
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Schedule not found with ID: " + scheduleId));
        
        timeSlot.setSchedule(schedule);
        TimeSlot updatedTimeSlot = timeSlotRepository.save(timeSlot);
        
        log.info("Time slot assigned successfully to schedule: {}", updatedTimeSlot.getId());
        return convertToDTO(updatedTimeSlot);
    }
    
    /**
     * Libère un créneau horaire d'un planning
     */
    @Transactional
    @CacheEvict(value = {"timeSlots", "availableTimeSlots"}, allEntries = true)
    public TimeSlotDTO unassignFromSchedule(Long timeSlotId) {
        log.info("Unassigning time slot {} from schedule", timeSlotId);
        
        TimeSlot timeSlot = timeSlotRepository.findById(timeSlotId)
                .orElseThrow(() -> new RuntimeException("Time slot not found with ID: " + timeSlotId));
        
        timeSlot.setSchedule(null);
        TimeSlot updatedTimeSlot = timeSlotRepository.save(timeSlot);
        
        log.info("Time slot unassigned successfully: {}", updatedTimeSlot.getId());
        return convertToDTO(updatedTimeSlot);
    }
    
    /**
     * Supprime un créneau horaire
     */
    @Transactional
    @CacheEvict(value = {"timeSlots", "availableTimeSlots"}, allEntries = true)
    public void deleteTimeSlot(Long id) {
        log.info("Deleting time slot with ID: {}", id);
        
        if (!timeSlotRepository.existsById(id)) {
            throw new RuntimeException("Time slot not found with ID: " + id);
        }
        
        timeSlotRepository.deleteById(id);
        log.info("Time slot deleted: {}", id);
    }
    
    /**
     * Supprime tous les créneaux horaires d'un planning
     */
    @Transactional
    @CacheEvict(value = {"timeSlots", "availableTimeSlots"}, allEntries = true)
    public void deleteTimeSlotsBySchedule(Long scheduleId) {
        log.info("Deleting time slots for schedule: {}", scheduleId);
        
        timeSlotRepository.deleteByScheduleId(scheduleId);
        log.info("Time slots deleted for schedule: {}", scheduleId);
    }
    
    /**
     * Recherche avancée de créneaux horaires
     */
    public List<TimeSlotDTO> searchTimeSlotsWithFilters(String dayOfWeek, Long scheduleId, 
            LocalTime startTime, LocalTime endTime) {
        log.debug("Searching time slots with advanced filters");
        return timeSlotRepository.findTimeSlotsWithFilters(dayOfWeek, scheduleId, startTime, endTime).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Vérifie les conflits de créneaux horaires
     */
    public List<TimeSlotDTO> checkConflictingTimeSlots(String dayOfWeek, LocalTime startTime, LocalTime endTime, Long excludeId) {
        Long excludeIdSafe = excludeId != null ? excludeId : -1L;
        return timeSlotRepository.findConflictingTimeSlots(dayOfWeek, startTime, endTime, excludeIdSafe).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtient les statistiques des créneaux horaires
     */
    public TimeSlotStatistics getTimeSlotStatistics() {
        long totalTimeSlots = timeSlotRepository.count();
        long availableTimeSlots = timeSlotRepository.findAvailableTimeSlots().size();
        long occupiedTimeSlots = timeSlotRepository.findOccupiedTimeSlots().size();
        Double averageDuration = timeSlotRepository.getAverageTimeSlotDuration();
        
        return TimeSlotStatistics.builder()
                .totalTimeSlots(totalTimeSlots)
                .availableTimeSlots(availableTimeSlots)
                .occupiedTimeSlots(occupiedTimeSlots)
                .averageDurationMinutes(averageDuration != null ? averageDuration : 0.0)
                .build();
    }
    
    /**
     * Obtient les statistiques par jour de la semaine
     */
    public List<Object[]> getTimeSlotStatisticsByDayOfWeek() {
        return timeSlotRepository.getTimeSlotCountByDayOfWeek();
    }
    
    /**
     * Obtient les statistiques par heure de début
     */
    public List<Object[]> getTimeSlotStatisticsByStartHour() {
        return timeSlotRepository.getTimeSlotCountByStartHour();
    }
    
    /**
     * Vérifie si un créneau horaire existe
     */
    public boolean existsById(Long id) {
        return timeSlotRepository.existsById(id);
    }
    
    /**
     * Vérifie si un créneau horaire existe par caractéristiques
     */
    public boolean existsByDayAndTime(String dayOfWeek, LocalTime startTime, LocalTime endTime) {
        return timeSlotRepository.existsByDayOfWeekAndStartTimeAndEndTime(dayOfWeek, startTime, endTime);
    }
    
    /**
     * Compte le nombre total de créneaux horaires
     */
    public long countTimeSlots() {
        return timeSlotRepository.count();
    }
    
    /**
     * Compte les créneaux horaires par jour de la semaine
     */
    public long countTimeSlotsByDayOfWeek(String dayOfWeek) {
        return timeSlotRepository.countByDayOfWeek(dayOfWeek);
    }
    
    /**
     * Validation des heures de créneau
     */
    private void validateTimeSlot(LocalTime startTime, LocalTime endTime) {
        if (startTime == null || endTime == null) {
            throw new RuntimeException("Start time and end time are required");
        }
        
        if (startTime.isAfter(endTime) || startTime.equals(endTime)) {
            throw new RuntimeException("Start time must be before end time");
        }
        
        long durationMinutes = java.time.Duration.between(startTime, endTime).toMinutes();
        if (durationMinutes < 15) {
            throw new RuntimeException("Time slot duration must be at least 15 minutes");
        }
        
        if (durationMinutes > 480) { // 8 heures
            throw new RuntimeException("Time slot duration cannot exceed 8 hours");
        }
    }
    
    /**
     * Vérifie les conflits avant création/modification
     */
    private void checkForConflicts(TimeSlotDTO timeSlotDTO, Long excludeId) {
        List<TimeSlotDTO> conflicts = checkConflictingTimeSlots(
            timeSlotDTO.getDayOfWeek(), 
            timeSlotDTO.getStartTime(), 
            timeSlotDTO.getEndTime(), 
            excludeId
        );
        if (!conflicts.isEmpty()) {
            throw new RuntimeException("Time slot conflict detected: overlapping time slot exists for " + 
                timeSlotDTO.getDayOfWeek() + " " + timeSlotDTO.getStartTime() + "-" + timeSlotDTO.getEndTime());
        }
    }
    
    /**
     * Convertit une entité TimeSlot en DTO
     */
    private TimeSlotDTO convertToDTO(TimeSlot timeSlot) {
        TimeSlotDTO dto = new TimeSlotDTO();
        dto.setId(timeSlot.getId());
        dto.setDayOfWeek(timeSlot.getDayOfWeek());
        dto.setStartTime(timeSlot.getStartTime());
        dto.setEndTime(timeSlot.getEndTime());
        dto.setScheduleId(timeSlot.getSchedule() != null ? timeSlot.getSchedule().getId() : null);
        return dto;
    }
    
    /**
     * Classe pour les statistiques des créneaux horaires
     */
    @lombok.Builder
    @lombok.Data
    public static class TimeSlotStatistics {
        private long totalTimeSlots;
        private long availableTimeSlots;
        private long occupiedTimeSlots;
        private double averageDurationMinutes;
    }
}