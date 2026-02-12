package cm.iusjc.teacheravailability.service;

import cm.iusjc.teacheravailability.dto.*;
import cm.iusjc.teacheravailability.entity.AvailabilityType;
import cm.iusjc.teacheravailability.entity.TeacherAvailability;
import cm.iusjc.teacheravailability.repository.TeacherAvailabilityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class TeacherAvailabilityService {
    
    private static final Logger logger = LoggerFactory.getLogger(TeacherAvailabilityService.class);
    
    @Autowired
    private TeacherAvailabilityRepository availabilityRepository;
    
    @Autowired
    private ConflictDetectionService conflictDetectionService;
    
    @Autowired
    private TeacherAvailabilityMapper mapper;
    
    // Create availability
    public TeacherAvailabilityDTO createAvailability(TeacherAvailabilityDTO availabilityDTO) {
        logger.info("Creating availability for teacher {} on {}", 
                   availabilityDTO.getTeacherId(), availabilityDTO.getDayOfWeek());
        
        // Validate input
        validateAvailabilityDTO(availabilityDTO);
        
        // Check for conflicts
        ConflictDetectionDTO conflictCheck = conflictDetectionService.checkConflicts(
            availabilityDTO.getTeacherId(),
            availabilityDTO.getDayOfWeek(),
            availabilityDTO.getStartTime(),
            availabilityDTO.getEndTime(),
            availabilityDTO.getSchoolId()
        );
        
        if (conflictCheck.isHasConflicts()) {
            logger.warn("Conflicts detected for teacher {} availability: {}", 
                       availabilityDTO.getTeacherId(), conflictCheck.getMessage());
            // You might want to throw an exception or handle conflicts differently
        }
        
        // Convert DTO to entity
        TeacherAvailability availability = mapper.toEntity(availabilityDTO);
        
        // Save
        TeacherAvailability saved = availabilityRepository.save(availability);
        
        // Clear cache
        clearTeacherCache(availabilityDTO.getTeacherId());
        
        logger.info("Created availability with ID: {}", saved.getId());
        return mapper.toDTO(saved);
    }
    
    // Update availability
    public TeacherAvailabilityDTO updateAvailability(Long id, TeacherAvailabilityDTO availabilityDTO) {
        logger.info("Updating availability with ID: {}", id);
        
        TeacherAvailability existing = availabilityRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Availability not found with ID: " + id));
        
        // Validate input
        validateAvailabilityDTO(availabilityDTO);
        
        // Update fields
        existing.setDayOfWeek(availabilityDTO.getDayOfWeek());
        existing.setStartTime(availabilityDTO.getStartTime());
        existing.setEndTime(availabilityDTO.getEndTime());
        existing.setAvailabilityType(availabilityDTO.getAvailabilityType());
        existing.setSpecificDate(availabilityDTO.getSpecificDate());
        existing.setIsRecurring(availabilityDTO.getIsRecurring());
        existing.setPriorityLevel(availabilityDTO.getPriorityLevel());
        existing.setNotes(availabilityDTO.getNotes());
        existing.setIsActive(availabilityDTO.getIsActive());
        
        TeacherAvailability updated = availabilityRepository.save(existing);
        
        // Clear cache
        clearTeacherCache(existing.getTeacherId());
        
        return mapper.toDTO(updated);
    }
    
    // Get availability by ID
    public Optional<TeacherAvailabilityDTO> getAvailabilityById(Long id) {
        return availabilityRepository.findById(id)
            .map(mapper::toDTO);
    }
    
    // Get all availabilities for a teacher
    @Cacheable(value = "teacherAvailabilities", key = "#teacherId")
    public List<TeacherAvailabilityDTO> getTeacherAvailabilities(Long teacherId) {
        logger.info("Getting availabilities for teacher: {}", teacherId);
        
        List<TeacherAvailability> availabilities = availabilityRepository
            .findByTeacherIdAndIsActiveTrue(teacherId);
        
        return availabilities.stream()
            .map(mapper::toDTO)
            .collect(Collectors.toList());
    }
    
    // Get availabilities for a teacher and school
    public List<TeacherAvailabilityDTO> getTeacherSchoolAvailabilities(Long teacherId, Long schoolId) {
        logger.info("Getting availabilities for teacher {} at school {}", teacherId, schoolId);
        
        List<TeacherAvailability> availabilities = availabilityRepository
            .findByTeacherIdAndSchoolIdAndIsActiveTrue(teacherId, schoolId);
        
        return availabilities.stream()
            .map(mapper::toDTO)
            .collect(Collectors.toList());
    }
    
    // Get availabilities for a specific day
    public List<TeacherAvailabilityDTO> getTeacherDayAvailabilities(Long teacherId, DayOfWeek dayOfWeek) {
        logger.info("Getting availabilities for teacher {} on {}", teacherId, dayOfWeek);
        
        List<TeacherAvailability> availabilities = availabilityRepository
            .findByTeacherIdAndDayOfWeekAndIsActiveTrue(teacherId, dayOfWeek);
        
        return availabilities.stream()
            .map(mapper::toDTO)
            .collect(Collectors.toList());
    }
    
    // Check if teacher is available at specific time
    public boolean isTeacherAvailable(Long teacherId, DayOfWeek dayOfWeek, LocalTime time) {
        logger.debug("Checking if teacher {} is available on {} at {}", teacherId, dayOfWeek, time);
        
        List<TeacherAvailability> availableSlots = availabilityRepository
            .findAvailableAt(teacherId, dayOfWeek, time);
        
        return !availableSlots.isEmpty();
    }
    
    // Get available time slots for a teacher on a specific day
    public List<AvailabilitySlotDTO> getAvailableSlots(Long teacherId, DayOfWeek dayOfWeek) {
        logger.info("Getting available slots for teacher {} on {}", teacherId, dayOfWeek);
        
        List<TeacherAvailability> availabilities = availabilityRepository
            .findByTeacherIdAndDayOfWeekAndAvailabilityTypeAndIsActiveTrue(
                teacherId, dayOfWeek, AvailabilityType.AVAILABLE);
        
        return availabilities.stream()
            .map(this::convertToSlotDTO)
            .collect(Collectors.toList());
    }
    
    // Get preferred time slots
    public List<AvailabilitySlotDTO> getPreferredSlots(Long teacherId, DayOfWeek dayOfWeek) {
        logger.info("Getting preferred slots for teacher {} on {}", teacherId, dayOfWeek);
        
        List<TeacherAvailability> availabilities = availabilityRepository
            .findByTeacherIdAndDayOfWeekAndAvailabilityTypeAndIsActiveTrue(
                teacherId, dayOfWeek, AvailabilityType.PREFERRED);
        
        return availabilities.stream()
            .map(this::convertToSlotDTO)
            .collect(Collectors.toList());
    }
    
    // Delete availability
    @CacheEvict(value = "teacherAvailabilities", key = "#teacherId")
    public void deleteAvailability(Long id, Long teacherId) {
        logger.info("Deleting availability with ID: {}", id);
        
        TeacherAvailability availability = availabilityRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Availability not found with ID: " + id));
        
        // Soft delete
        availability.setIsActive(false);
        availabilityRepository.save(availability);
        
        // Or hard delete
        // availabilityRepository.deleteById(id);
    }
    
    // Bulk operations
    public List<TeacherAvailabilityDTO> createBulkAvailabilities(List<TeacherAvailabilityDTO> availabilities) {
        logger.info("Creating {} availabilities in bulk", availabilities.size());
        
        List<TeacherAvailability> entities = availabilities.stream()
            .map(dto -> {
                validateAvailabilityDTO(dto);
                return mapper.toEntity(dto);
            })
            .collect(Collectors.toList());
        
        List<TeacherAvailability> saved = availabilityRepository.saveAll(entities);
        
        // Clear cache for all affected teachers
        availabilities.stream()
            .map(TeacherAvailabilityDTO::getTeacherId)
            .distinct()
            .forEach(this::clearTeacherCache);
        
        return saved.stream()
            .map(mapper::toDTO)
            .collect(Collectors.toList());
    }
    
    // Statistics
    public Long getAvailabilityCount(Long teacherId) {
        return availabilityRepository.countByTeacherId(teacherId);
    }
    
    public List<Object[]> getAvailabilityStatsByDay(Long teacherId) {
        return availabilityRepository.countByTeacherIdGroupByDay(teacherId);
    }
    
    // Helper methods
    private void validateAvailabilityDTO(TeacherAvailabilityDTO dto) {
        if (dto.getTeacherId() == null) {
            throw new IllegalArgumentException("Teacher ID is required");
        }
        if (dto.getDayOfWeek() == null) {
            throw new IllegalArgumentException("Day of week is required");
        }
        if (!dto.isValidTimeRange()) {
            throw new IllegalArgumentException("Invalid time range: start time must be before end time");
        }
        if (dto.getAvailabilityType() == null) {
            throw new IllegalArgumentException("Availability type is required");
        }
    }
    
    private AvailabilitySlotDTO convertToSlotDTO(TeacherAvailability availability) {
        AvailabilitySlotDTO slot = new AvailabilitySlotDTO();
        slot.setDayOfWeek(availability.getDayOfWeek());
        slot.setStartTime(availability.getStartTime());
        slot.setEndTime(availability.getEndTime());
        slot.setAvailabilityType(availability.getAvailabilityType());
        slot.setPriorityLevel(availability.getPriorityLevel());
        slot.setNotes(availability.getNotes());
        slot.setSchoolId(availability.getSchoolId());
        return slot;
    }
    
    @CacheEvict(value = "teacherAvailabilities", key = "#teacherId")
    private void clearTeacherCache(Long teacherId) {
        logger.debug("Clearing cache for teacher: {}", teacherId);
    }
}