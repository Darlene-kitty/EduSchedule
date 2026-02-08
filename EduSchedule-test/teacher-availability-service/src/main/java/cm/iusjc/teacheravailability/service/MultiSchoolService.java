package cm.iusjc.teacheravailability.service;

import cm.iusjc.teacheravailability.dto.TeacherSchoolAssignmentDTO;
import cm.iusjc.teacheravailability.entity.TeacherSchoolAssignment;
import cm.iusjc.teacheravailability.repository.TeacherSchoolAssignmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class MultiSchoolService {
    
    private static final Logger logger = LoggerFactory.getLogger(MultiSchoolService.class);
    
    @Autowired
    private TeacherSchoolAssignmentRepository assignmentRepository;
    
    @Autowired
    private TeacherSchoolAssignmentMapper mapper;
    
    // Create school assignment
    public TeacherSchoolAssignmentDTO createAssignment(TeacherSchoolAssignmentDTO assignmentDTO) {
        logger.info("Creating school assignment for teacher {} at school {}", 
                   assignmentDTO.getTeacherId(), assignmentDTO.getSchoolId());
        
        validateAssignmentDTO(assignmentDTO);
        
        // Check if assignment already exists
        Optional<TeacherSchoolAssignment> existing = assignmentRepository
            .findByTeacherIdAndSchoolIdAndIsActiveTrue(assignmentDTO.getTeacherId(), assignmentDTO.getSchoolId());
        
        if (existing.isPresent()) {
            throw new RuntimeException("Teacher is already assigned to this school");
        }
        
        TeacherSchoolAssignment assignment = mapper.toEntity(assignmentDTO);
        TeacherSchoolAssignment saved = assignmentRepository.save(assignment);
        
        clearTeacherCache(assignmentDTO.getTeacherId());
        
        return mapper.toDTO(saved);
    }
    
    // Update assignment
    public TeacherSchoolAssignmentDTO updateAssignment(Long id, TeacherSchoolAssignmentDTO assignmentDTO) {
        logger.info("Updating school assignment with ID: {}", id);
        
        TeacherSchoolAssignment existing = assignmentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Assignment not found with ID: " + id));
        
        validateAssignmentDTO(assignmentDTO);
        
        // Update fields
        existing.setWorkingDays(assignmentDTO.getWorkingDays());
        existing.setStartTime(assignmentDTO.getStartTime());
        existing.setEndTime(assignmentDTO.getEndTime());
        existing.setTravelTimeMinutes(assignmentDTO.getTravelTimeMinutes());
        existing.setIsPrimarySchool(assignmentDTO.getIsPrimarySchool());
        existing.setContractType(assignmentDTO.getContractType());
        existing.setMaxHoursPerWeek(assignmentDTO.getMaxHoursPerWeek());
        existing.setIsActive(assignmentDTO.getIsActive());
        
        TeacherSchoolAssignment updated = assignmentRepository.save(existing);
        
        clearTeacherCache(existing.getTeacherId());
        
        return mapper.toDTO(updated);
    }
    
    // Get assignment by ID
    public Optional<TeacherSchoolAssignmentDTO> getAssignmentById(Long id) {
        return assignmentRepository.findById(id)
            .map(mapper::toDTO);
    }
    
    // Get all assignments for a teacher
    @Cacheable(value = "teacherAssignments", key = "#teacherId")
    public List<TeacherSchoolAssignmentDTO> getTeacherAssignments(Long teacherId) {
        logger.info("Getting school assignments for teacher: {}", teacherId);
        
        List<TeacherSchoolAssignment> assignments = assignmentRepository
            .findByTeacherIdAndIsActiveTrue(teacherId);
        
        return assignments.stream()
            .map(mapper::toDTO)
            .collect(Collectors.toList());
    }
    
    // Get all assignments for a school
    public List<TeacherSchoolAssignmentDTO> getSchoolAssignments(Long schoolId) {
        logger.info("Getting teacher assignments for school: {}", schoolId);
        
        List<TeacherSchoolAssignment> assignments = assignmentRepository
            .findBySchoolIdAndIsActiveTrue(schoolId);
        
        return assignments.stream()
            .map(mapper::toDTO)
            .collect(Collectors.toList());
    }
    
    // Get specific assignment
    public Optional<TeacherSchoolAssignmentDTO> getTeacherSchoolAssignment(Long teacherId, Long schoolId) {
        return assignmentRepository
            .findByTeacherIdAndSchoolIdAndIsActiveTrue(teacherId, schoolId)
            .map(mapper::toDTO);
    }
    
    // Check if teacher works at school
    public boolean isTeacherAssignedToSchool(Long teacherId, Long schoolId) {
        return assignmentRepository
            .findByTeacherIdAndSchoolIdAndIsActiveTrue(teacherId, schoolId)
            .isPresent();
    }
    
    // Check if teacher works on specific day at school
    public boolean isTeacherWorkingOnDay(Long teacherId, Long schoolId, DayOfWeek dayOfWeek) {
        Optional<TeacherSchoolAssignment> assignment = assignmentRepository
            .findByTeacherIdAndSchoolIdAndIsActiveTrue(teacherId, schoolId);
        
        return assignment.map(a -> a.isWorkingDay(dayOfWeek)).orElse(false);
    }
    
    // Get teachers working on specific day at school
    public List<TeacherSchoolAssignmentDTO> getTeachersWorkingOnDay(Long schoolId, DayOfWeek dayOfWeek) {
        logger.info("Getting teachers working at school {} on {}", schoolId, dayOfWeek);
        
        List<TeacherSchoolAssignment> assignments = assignmentRepository
            .findBySchoolIdAndWorkingDay(schoolId, dayOfWeek);
        
        return assignments.stream()
            .map(mapper::toDTO)
            .collect(Collectors.toList());
    }
    
    // Get multi-school teachers
    public List<Long> getMultiSchoolTeachers() {
        logger.info("Getting teachers working at multiple schools");
        return assignmentRepository.findMultiSchoolTeachers();
    }
    
    // Count schools for teacher
    public Long countSchoolsForTeacher(Long teacherId) {
        return assignmentRepository.countSchoolsByTeacherId(teacherId);
    }
    
    // Calculate travel time between schools
    public Integer calculateTravelTime(Long fromSchoolId, Long toSchoolId) {
        if (fromSchoolId.equals(toSchoolId)) {
            return 0; // Same school, no travel time
        }
        
        // This is a simplified implementation
        // In a real system, you would:
        // 1. Get school addresses from school service
        // 2. Use a mapping service (Google Maps API, etc.) to calculate actual travel time
        // 3. Consider traffic conditions, transportation mode, etc.
        
        // For now, return a default travel time based on school IDs
        // You could also store this in a separate table
        return getDefaultTravelTime(fromSchoolId, toSchoolId);
    }
    
    // Get primary school for teacher
    public Optional<TeacherSchoolAssignmentDTO> getPrimarySchool(Long teacherId) {
        List<TeacherSchoolAssignment> assignments = assignmentRepository
            .findByTeacherIdAndIsPrimarySchoolTrueAndIsActiveTrue(teacherId);
        
        return assignments.stream()
            .findFirst()
            .map(mapper::toDTO);
    }
    
    // Set primary school
    public void setPrimarySchool(Long teacherId, Long schoolId) {
        logger.info("Setting primary school for teacher {} to school {}", teacherId, schoolId);
        
        // Remove primary flag from all schools for this teacher
        List<TeacherSchoolAssignment> allAssignments = assignmentRepository
            .findByTeacherIdAndIsActiveTrue(teacherId);
        
        allAssignments.forEach(assignment -> assignment.setIsPrimarySchool(false));
        assignmentRepository.saveAll(allAssignments);
        
        // Set primary flag for the specified school
        Optional<TeacherSchoolAssignment> targetAssignment = assignmentRepository
            .findByTeacherIdAndSchoolIdAndIsActiveTrue(teacherId, schoolId);
        
        if (targetAssignment.isPresent()) {
            targetAssignment.get().setIsPrimarySchool(true);
            assignmentRepository.save(targetAssignment.get());
        } else {
            throw new RuntimeException("Teacher is not assigned to the specified school");
        }
        
        clearTeacherCache(teacherId);
    }
    
    // Delete assignment
    @CacheEvict(value = "teacherAssignments", key = "#teacherId")
    public void deleteAssignment(Long id, Long teacherId) {
        logger.info("Deleting school assignment with ID: {}", id);
        
        TeacherSchoolAssignment assignment = assignmentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Assignment not found with ID: " + id));
        
        // Soft delete
        assignment.setIsActive(false);
        assignmentRepository.save(assignment);
    }
    
    // Statistics
    public List<Object[]> getTeacherCountBySchool() {
        return assignmentRepository.countTeachersBySchool();
    }
    
    // Helper methods
    private void validateAssignmentDTO(TeacherSchoolAssignmentDTO dto) {
        if (dto.getTeacherId() == null) {
            throw new IllegalArgumentException("Teacher ID is required");
        }
        if (dto.getSchoolId() == null) {
            throw new IllegalArgumentException("School ID is required");
        }
        if (dto.getWorkingDays() == null || dto.getWorkingDays().isEmpty()) {
            throw new IllegalArgumentException("At least one working day is required");
        }
    }
    
    private Integer getDefaultTravelTime(Long fromSchoolId, Long toSchoolId) {
        // Simple algorithm based on school ID difference
        // In reality, this would be much more sophisticated
        long diff = Math.abs(fromSchoolId - toSchoolId);
        
        if (diff <= 2) return 15; // 15 minutes for nearby schools
        if (diff <= 5) return 30; // 30 minutes for medium distance
        return 45; // 45 minutes for far schools
    }
    
    @CacheEvict(value = "teacherAssignments", key = "#teacherId")
    private void clearTeacherCache(Long teacherId) {
        logger.debug("Clearing assignment cache for teacher: {}", teacherId);
    }
}