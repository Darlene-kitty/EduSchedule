package cm.iusjc.teacheravailabilityservice.service;

import cm.iusjc.teacheravailabilityservice.entity.TeacherAvailability;
import cm.iusjc.teacheravailabilityservice.entity.TeacherAvailability.AvailabilityStatus;
import cm.iusjc.teacheravailabilityservice.entity.TimeSlot;
import cm.iusjc.teacheravailabilityservice.dto.TeacherAvailabilityDTO;
import cm.iusjc.teacheravailabilityservice.dto.TimeSlotDTO;
import cm.iusjc.teacheravailabilityservice.repository.TeacherAvailabilityRepository;
import cm.iusjc.teacheravailabilityservice.exception.AvailabilityNotFoundException;
import cm.iusjc.teacheravailabilityservice.exception.ConflictException;
import cm.iusjc.teacheravailabilityservice.exception.ValidationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class TeacherAvailabilityService {
    
    @Autowired
    private TeacherAvailabilityRepository availabilityRepository;
    
    @Autowired
    private ConflictDetectionService conflictDetectionService;
    
    @Autowired
    private UserServiceClient userServiceClient;
    
    @Autowired
    private NotificationService notificationService;
    
    // CRUD Operations
    
    @Transactional
    public TeacherAvailabilityDTO createAvailability(TeacherAvailabilityDTO availabilityDTO) {
        validateAvailabilityDTO(availabilityDTO);
        
        // Vérifier les conflits
        List<String> conflicts = conflictDetectionService.detectConflicts(availabilityDTO);
        if (!conflicts.isEmpty()) {
            throw new ConflictException("Conflits détectés: " + String.join(", ", conflicts));
        }
        
        // Récupérer le nom de l'enseignant
        String teacherName = userServiceClient.getUserName(availabilityDTO.getTeacherId());
        
        TeacherAvailability availability = convertToEntity(availabilityDTO);
        availability.setTeacherName(teacherName);
        availability.setCreatedAt(LocalDateTime.now());
        
        TeacherAvailability saved = availabilityRepository.save(availability);
        
        // Notification
        notificationService.notifyAvailabilityCreated(saved);
        
        return convertToDTO(saved);
    }
    
    @Cacheable(value = "teacher-availability", key = "#id")
    @Transactional(readOnly = true)
    public TeacherAvailabilityDTO getAvailabilityById(Long id) {
        TeacherAvailability availability = availabilityRepository.findById(id)
                .orElseThrow(() -> new AvailabilityNotFoundException("Disponibilité non trouvée: " + id));
        
        TeacherAvailabilityDTO dto = convertToDTO(availability);
        dto.calculateTotalWeeklyHours();
        
        return dto;
    }
    
    @CachePut(value = "teacher-availability", key = "#availabilityDTO.id")
    @Transactional
    public TeacherAvailabilityDTO updateAvailability(TeacherAvailabilityDTO availabilityDTO) {
        validateAvailabilityDTO(availabilityDTO);
        
        TeacherAvailability existing = availabilityRepository.findById(availabilityDTO.getId())
                .orElseThrow(() -> new AvailabilityNotFoundException("Disponibilité non trouvée: " + availabilityDTO.getId()));
        
        // Vérifier les conflits (en excluant la disponibilité actuelle)
        List<String> conflicts = conflictDetectionService.detectConflictsExcluding(availabilityDTO, availabilityDTO.getId());
        if (!conflicts.isEmpty()) {
            throw new ConflictException("Conflits détectés: " + String.join(", ", conflicts));
        }
        
        // Mettre à jour les champs
        updateEntityFromDTO(existing, availabilityDTO);
        existing.setUpdatedAt(LocalDateTime.now());
        
        TeacherAvailability saved = availabilityRepository.save(existing);
        
        // Notification
        notificationService.notifyAvailabilityUpdated(saved);
        
        return convertToDTO(saved);
    }
    
    @CacheEvict(value = "teacher-availability", key = "#id")
    @Transactional
    public void deleteAvailability(Long id) {
        TeacherAvailability availability = availabilityRepository.findById(id)
                .orElseThrow(() -> new AvailabilityNotFoundException("Disponibilité non trouvée: " + id));
        
        // Notification avant suppression
        notificationService.notifyAvailabilityDeleted(availability);
        
        availabilityRepository.delete(availability);
    }
    
    // Recherche et requêtes
    
    @Cacheable(value = "teacher-availabilities", key = "#teacherId")
    @Transactional(readOnly = true)
    public List<TeacherAvailabilityDTO> getAvailabilitiesByTeacher(Long teacherId) {
        List<TeacherAvailability> availabilities = availabilityRepository.findByTeacherIdAndStatus(teacherId, AvailabilityStatus.ACTIVE);
        
        return availabilities.stream()
                .map(this::convertToDTO)
                .peek(dto -> dto.calculateTotalWeeklyHours())
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Optional<TeacherAvailabilityDTO> getActiveAvailabilityForTeacherOnDate(Long teacherId, LocalDate date) {
        Optional<TeacherAvailability> availability = availabilityRepository.findActiveAvailabilityForTeacherOnDate(teacherId, date);
        
        return availability.map(this::convertToDTO);
    }
    
    @Transactional(readOnly = true)
    public List<TeacherAvailabilityDTO> getAvailabilitiesInPeriod(Long teacherId, LocalDate startDate, LocalDate endDate) {
        List<TeacherAvailability> availabilities = availabilityRepository.findActiveAvailabilitiesForTeacherInPeriod(teacherId, startDate, endDate);
        
        return availabilities.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    // Vérification de disponibilité
    
    @Transactional(readOnly = true)
    public boolean isTeacherAvailable(Long teacherId, LocalDateTime startTime, LocalDateTime endTime) {
        LocalDate date = startTime.toLocalDate();
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        
        Optional<TeacherAvailability> availability = availabilityRepository.findActiveAvailabilityForTeacherOnDate(teacherId, date);
        
        if (availability.isEmpty()) {
            return false;
        }
        
        // Vérifier si le créneau demandé correspond aux disponibilités
        return availability.get().getAvailableSlots().stream()
                .anyMatch(slot -> slot.getDayOfWeek().equals(dayOfWeek) &&
                                !startTime.toLocalTime().isBefore(slot.getStartTime()) &&
                                !endTime.toLocalTime().isAfter(slot.getEndTime()));
    }
    
    @Transactional(readOnly = true)
    public List<TimeSlotDTO> getAvailableSlots(Long teacherId, LocalDate date) {
        Optional<TeacherAvailability> availability = availabilityRepository.findActiveAvailabilityForTeacherOnDate(teacherId, date);
        
        if (availability.isEmpty()) {
            return List.of();
        }
        
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        
        return availability.get().getAvailableSlots().stream()
                .filter(slot -> slot.getDayOfWeek().equals(dayOfWeek))
                .map(this::convertTimeSlotToDTO)
                .collect(Collectors.toList());
    }
    
    // Gestion des créneaux
    
    @Transactional
    public TeacherAvailabilityDTO addTimeSlot(Long availabilityId, TimeSlotDTO timeSlotDTO) {
        TeacherAvailability availability = availabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new AvailabilityNotFoundException("Disponibilité non trouvée: " + availabilityId));
        
        TimeSlot timeSlot = convertTimeSlotToEntity(timeSlotDTO);
        
        // Vérifier les conflits
        if (availability.hasConflictWith(timeSlot)) {
            throw new ConflictException("Le créneau entre en conflit avec les créneaux existants");
        }
        
        availability.addTimeSlot(timeSlot);
        availability.setUpdatedAt(LocalDateTime.now());
        
        TeacherAvailability saved = availabilityRepository.save(availability);
        
        return convertToDTO(saved);
    }
    
    @Transactional
    public TeacherAvailabilityDTO removeTimeSlot(Long availabilityId, Long timeSlotId) {
        TeacherAvailability availability = availabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new AvailabilityNotFoundException("Disponibilité non trouvée: " + availabilityId));
        
        TimeSlot toRemove = availability.getAvailableSlots().stream()
                .filter(slot -> slot.getId().equals(timeSlotId))
                .findFirst()
                .orElseThrow(() -> new AvailabilityNotFoundException("Créneau non trouvé: " + timeSlotId));
        
        availability.removeTimeSlot(toRemove);
        availability.setUpdatedAt(LocalDateTime.now());
        
        TeacherAvailability saved = availabilityRepository.save(availability);
        
        return convertToDTO(saved);
    }
    
    // Statistiques
    
    @Transactional(readOnly = true)
    public long countActiveAvailabilities() {
        return availabilityRepository.countActiveAvailabilities();
    }
    
    @Transactional(readOnly = true)
    public long countTeachersWithAvailabilities() {
        return availabilityRepository.countTeachersWithActiveAvailabilities();
    }
    
    // Utilitaires de conversion
    
    private TeacherAvailability convertToEntity(TeacherAvailabilityDTO dto) {
        TeacherAvailability entity = new TeacherAvailability();
        updateEntityFromDTO(entity, dto);
        return entity;
    }
    
    private void updateEntityFromDTO(TeacherAvailability entity, TeacherAvailabilityDTO dto) {
        entity.setTeacherId(dto.getTeacherId());
        entity.setTeacherName(dto.getTeacherName());
        entity.setEffectiveDate(dto.getEffectiveDate());
        entity.setEndDate(dto.getEndDate());
        entity.setStatus(dto.getStatus());
        entity.setNotes(dto.getNotes());
        entity.setMaxHoursPerDay(dto.getMaxHoursPerDay());
        entity.setMaxHoursPerWeek(dto.getMaxHoursPerWeek());
        entity.setCreatedBy(dto.getCreatedBy());
        
        if (dto.getAvailableSlots() != null) {
            List<TimeSlot> slots = dto.getAvailableSlots().stream()
                    .map(this::convertTimeSlotToEntity)
                    .collect(Collectors.toList());
            entity.setAvailableSlots(slots);
        }
    }
    
    private TeacherAvailabilityDTO convertToDTO(TeacherAvailability entity) {
        TeacherAvailabilityDTO dto = new TeacherAvailabilityDTO();
        dto.setId(entity.getId());
        dto.setTeacherId(entity.getTeacherId());
        dto.setTeacherName(entity.getTeacherName());
        dto.setEffectiveDate(entity.getEffectiveDate());
        dto.setEndDate(entity.getEndDate());
        dto.setStatus(entity.getStatus());
        dto.setNotes(entity.getNotes());
        dto.setMaxHoursPerDay(entity.getMaxHoursPerDay());
        dto.setMaxHoursPerWeek(entity.getMaxHoursPerWeek());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setCreatedBy(entity.getCreatedBy());
        
        if (entity.getAvailableSlots() != null) {
            List<TimeSlotDTO> slots = entity.getAvailableSlots().stream()
                    .map(this::convertTimeSlotToDTO)
                    .collect(Collectors.toList());
            dto.setAvailableSlots(slots);
        }
        
        return dto;
    }
    
    private TimeSlot convertTimeSlotToEntity(TimeSlotDTO dto) {
        TimeSlot entity = new TimeSlot();
        entity.setId(dto.getId());
        entity.setStartTime(dto.getStartTime());
        entity.setEndTime(dto.getEndTime());
        entity.setDayOfWeek(dto.getDayOfWeek());
        entity.setIsRecurring(dto.getIsRecurring());
        return entity;
    }
    
    private TimeSlotDTO convertTimeSlotToDTO(TimeSlot entity) {
        TimeSlotDTO dto = new TimeSlotDTO();
        dto.setId(entity.getId());
        dto.setStartTime(entity.getStartTime());
        dto.setEndTime(entity.getEndTime());
        dto.setDayOfWeek(entity.getDayOfWeek());
        dto.setIsRecurring(entity.getIsRecurring());
        return dto;
    }
    
    // Validation
    
    private void validateAvailabilityDTO(TeacherAvailabilityDTO dto) {
        if (!dto.isValid()) {
            throw new ValidationException("Données de disponibilité invalides");
        }
        
        if (dto.getAvailableSlots() != null) {
            for (TimeSlotDTO slot : dto.getAvailableSlots()) {
                if (!slot.isValid()) {
                    throw new ValidationException("Créneau invalide: " + slot);
                }
            }
        }
    }
}