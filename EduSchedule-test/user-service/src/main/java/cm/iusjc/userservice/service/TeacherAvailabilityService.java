package cm.iusjc.userservice.service;

import cm.iusjc.userservice.entity.TeacherAvailability;
import cm.iusjc.userservice.entity.AvailabilityType;
import cm.iusjc.userservice.repository.TeacherAvailabilityRepository;
import cm.iusjc.userservice.dto.TeacherAvailabilityDTO;
import cm.iusjc.userservice.dto.TeacherAvailabilityRequest;
import cm.iusjc.userservice.dto.TimeSlotDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeacherAvailabilityService {
    
    private final TeacherAvailabilityRepository availabilityRepository;
    
    @Transactional
    @CacheEvict(value = {"teacherAvailability", "availableSlots"}, key = "#request.teacherId")
    public TeacherAvailabilityDTO createAvailability(TeacherAvailabilityRequest request) {
        log.info("Creating availability for teacher: {}", request.getTeacherId());
        
        // Vérifier les conflits
        List<TeacherAvailability> conflicts = availabilityRepository.findConflictingAvailabilities(
            request.getTeacherId(),
            request.getDayOfWeek(),
            request.getStartTime(),
            request.getEndTime(),
            null
        );
        
        if (!conflicts.isEmpty()) {
            throw new RuntimeException("Conflict detected with existing availability");
        }
        
        TeacherAvailability availability = new TeacherAvailability();
        availability.setTeacherId(request.getTeacherId());
        availability.setSchoolId(request.getSchoolId());
        availability.setDayOfWeek(request.getDayOfWeek());
        availability.setStartTime(request.getStartTime());
        availability.setEndTime(request.getEndTime());
        availability.setAvailabilityType(request.getAvailabilityType());
        availability.setRecurring(request.getRecurring());
        availability.setSpecificDate(request.getSpecificDate());
        availability.setPriority(request.getPriority());
        availability.setNotes(request.getNotes());
        
        TeacherAvailability saved = availabilityRepository.save(availability);
        log.info("Availability created successfully: {}", saved.getId());
        
        return mapToDTO(saved);
    }
    
    public List<TeacherAvailabilityDTO> getAllAvailabilities() {
        return availabilityRepository.findAll()
                .stream()
                .filter(a -> Boolean.TRUE.equals(a.getActive()))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "teacherAvailability", key = "#teacherId")
    public List<TeacherAvailabilityDTO> getTeacherAvailabilities(Long teacherId) {
        return availabilityRepository.findByTeacherIdAndActiveTrue(teacherId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Cacheable(value = "availableSlots", key = "#teacherId + '_' + #dayOfWeek")
    public List<TimeSlotDTO> getAvailableSlots(Long teacherId, DayOfWeek dayOfWeek) {
        List<TeacherAvailability> availabilities = availabilityRepository
                .findByTeacherIdAndDayOfWeekAndActiveTrue(teacherId, dayOfWeek);
        
        return availabilities.stream()
                .filter(a -> a.getAvailabilityType() == AvailabilityType.AVAILABLE)
                .map(a -> new TimeSlotDTO(a.getStartTime(), a.getEndTime(), a.getPriority()))
                .collect(Collectors.toList());
    }
    
    public boolean isTeacherAvailable(Long teacherId, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
        List<TeacherAvailability> availabilities = availabilityRepository
                .findAvailableAt(teacherId, dayOfWeek, startTime);
        
        return availabilities.stream()
                .anyMatch(a -> a.isAvailableAt(startTime) && a.isAvailableAt(endTime));
    }
    
    public boolean isTeacherAvailable(Long teacherId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        DayOfWeek dayOfWeek = startDateTime.getDayOfWeek();
        LocalTime startTime = startDateTime.toLocalTime();
        LocalTime endTime = endDateTime.toLocalTime();
        
        return isTeacherAvailable(teacherId, dayOfWeek, startTime, endTime);
    }
    
    @Transactional
    @CacheEvict(value = {"teacherAvailability", "availableSlots"}, key = "#id")
    public TeacherAvailabilityDTO updateAvailability(Long id, TeacherAvailabilityRequest request) {
        log.info("Updating availability: {}", id);
        
        TeacherAvailability availability = availabilityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Availability not found: " + id));
        
        // Vérifier les conflits (en excluant cette disponibilité)
        List<TeacherAvailability> conflicts = availabilityRepository.findConflictingAvailabilities(
            request.getTeacherId(),
            request.getDayOfWeek(),
            request.getStartTime(),
            request.getEndTime(),
            id
        );
        
        if (!conflicts.isEmpty()) {
            throw new RuntimeException("Conflict detected with existing availability");
        }
        
        availability.setSchoolId(request.getSchoolId());
        availability.setDayOfWeek(request.getDayOfWeek());
        availability.setStartTime(request.getStartTime());
        availability.setEndTime(request.getEndTime());
        availability.setAvailabilityType(request.getAvailabilityType());
        availability.setRecurring(request.getRecurring());
        availability.setSpecificDate(request.getSpecificDate());
        availability.setPriority(request.getPriority());
        availability.setNotes(request.getNotes());
        
        TeacherAvailability updated = availabilityRepository.save(availability);
        log.info("Availability updated successfully: {}", updated.getId());
        
        return mapToDTO(updated);
    }
    
    @Transactional
    @CacheEvict(value = {"teacherAvailability", "availableSlots"}, allEntries = true)
    public void deleteAvailability(Long id) {
        log.info("Deleting availability: {}", id);
        
        TeacherAvailability availability = availabilityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Availability not found: " + id));
        
        availability.setActive(false);
        availabilityRepository.save(availability);
        
        log.info("Availability deleted successfully: {}", id);
    }
    
    public List<TeacherAvailabilityDTO> getPreferredSlots(Long teacherId) {
        return availabilityRepository.findByTeacherIdAndAvailabilityTypeAndActiveTrue(
                teacherId, AvailabilityType.PREFERRED)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    public Long getTotalAvailableHours(Long teacherId) {
        return availabilityRepository.getTotalAvailableHoursPerWeek(teacherId);
    }
    
    @Transactional
    @CacheEvict(value = {"teacherAvailability", "availableSlots"}, key = "#teacherId")
    public void setDefaultAvailability(Long teacherId, Long schoolId) {
        log.info("Setting default availability for teacher: {} at school: {}", teacherId, schoolId);
        
        // Créer des disponibilités par défaut (Lundi-Vendredi 8h-18h)
        for (DayOfWeek day : List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, 
                                    DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
            TeacherAvailability availability = new TeacherAvailability();
            availability.setTeacherId(teacherId);
            availability.setSchoolId(schoolId);
            availability.setDayOfWeek(day);
            availability.setStartTime(LocalTime.of(8, 0));
            availability.setEndTime(LocalTime.of(18, 0));
            availability.setAvailabilityType(AvailabilityType.AVAILABLE);
            availability.setRecurring(true);
            availability.setPriority(2); // Acceptable
            availability.setNotes("Disponibilité par défaut");
            
            availabilityRepository.save(availability);
        }
        
        log.info("Default availability set for teacher: {}", teacherId);
    }
    
    private TeacherAvailabilityDTO mapToDTO(TeacherAvailability availability) {
        TeacherAvailabilityDTO dto = new TeacherAvailabilityDTO();
        dto.setId(availability.getId());
        dto.setTeacherId(availability.getTeacherId());
        dto.setSchoolId(availability.getSchoolId());
        dto.setDayOfWeek(availability.getDayOfWeek());
        dto.setStartTime(availability.getStartTime());
        dto.setEndTime(availability.getEndTime());
        dto.setAvailabilityType(availability.getAvailabilityType());
        dto.setRecurring(availability.getRecurring());
        dto.setSpecificDate(availability.getSpecificDate());
        dto.setPriority(availability.getPriority());
        dto.setNotes(availability.getNotes());
        dto.setActive(availability.getActive());
        dto.setCreatedAt(availability.getCreatedAt());
        dto.setUpdatedAt(availability.getUpdatedAt());
        
        return dto;
    }
}