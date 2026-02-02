package cm.iusjc.userservice.service;

import cm.iusjc.userservice.entity.TeacherSchoolAssignment;
import cm.iusjc.userservice.repository.TeacherSchoolAssignmentRepository;
import cm.iusjc.userservice.dto.TeacherSchoolAssignmentDTO;
import cm.iusjc.userservice.dto.TeacherSchoolAssignmentRequest;
import cm.iusjc.userservice.dto.InterSchoolConflictDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MultiSchoolSchedulingService {
    
    private final TeacherSchoolAssignmentRepository assignmentRepository;
    private final RestTemplate restTemplate;
    
    @Transactional
    @CacheEvict(value = {"teacherAssignments", "multiSchoolTeachers"}, allEntries = true)
    public TeacherSchoolAssignmentDTO createAssignment(TeacherSchoolAssignmentRequest request) {
        log.info("Creating school assignment for teacher: {} at school: {}", 
                request.getTeacherId(), request.getSchoolId());
        
        TeacherSchoolAssignment assignment = new TeacherSchoolAssignment();
        assignment.setTeacherId(request.getTeacherId());
        assignment.setSchoolId(request.getSchoolId());
        assignment.setWorkingDays(request.getWorkingDays());
        assignment.setStartTime(request.getStartTime());
        assignment.setEndTime(request.getEndTime());
        assignment.setTravelTimeMinutes(request.getTravelTimeMinutes());
        assignment.setMaxHoursPerDay(request.getMaxHoursPerDay());
        assignment.setMaxHoursPerWeek(request.getMaxHoursPerWeek());
        assignment.setPriority(request.getPriority());
        assignment.setContractType(request.getContractType());
        assignment.setEffectiveFrom(request.getEffectiveFrom());
        assignment.setEffectiveTo(request.getEffectiveTo());
        assignment.setNotes(request.getNotes());
        
        TeacherSchoolAssignment saved = assignmentRepository.save(assignment);
        log.info("School assignment created successfully: {}", saved.getId());
        
        return mapToDTO(saved);
    }
    
    @Cacheable(value = "teacherAssignments", key = "#teacherId")
    public List<TeacherSchoolAssignmentDTO> getTeacherAssignments(Long teacherId) {
        return assignmentRepository.findByTeacherIdAndActiveTrue(teacherId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Cacheable(value = "multiSchoolTeachers")
    public List<Long> getMultiSchoolTeachers() {
        return assignmentRepository.findMultiSchoolTeachers(LocalDateTime.now());
    }
    
    public boolean isMultiSchoolTeacher(Long teacherId) {
        Long schoolCount = assignmentRepository.countActiveSchools(teacherId, LocalDateTime.now());
        return schoolCount > 1;
    }
    
    public Integer calculateTravelTime(Long teacherId, Long fromSchoolId, Long toSchoolId) {
        if (fromSchoolId.equals(toSchoolId)) {
            return 0;
        }
        
        Integer travelTime = assignmentRepository.calculateTravelTime(teacherId, fromSchoolId, toSchoolId);
        return travelTime != null ? travelTime : 30; // Défaut 30 minutes
    }
    
    public List<InterSchoolConflictDTO> checkInterSchoolConflicts(Long teacherId, LocalDateTime startTime, LocalDateTime endTime, Long schoolId) {
        log.debug("Checking inter-school conflicts for teacher: {} from {} to {} at school: {}", 
                teacherId, startTime, endTime, schoolId);
        
        List<InterSchoolConflictDTO> conflicts = List.of();
        
        // Récupérer les assignations actives de l'enseignant
        List<TeacherSchoolAssignment> assignments = assignmentRepository.findActiveAssignmentsAt(teacherId, startTime);
        
        for (TeacherSchoolAssignment assignment : assignments) {
            if (!assignment.getSchoolId().equals(schoolId)) {
                // Vérifier s'il y a des cours dans cette autre école au même moment
                List<InterSchoolConflictDTO> schoolConflicts = checkScheduleConflictsAtSchool(
                    teacherId, assignment.getSchoolId(), startTime, endTime);
                
                // Ajouter le temps de déplacement aux conflits
                for (InterSchoolConflictDTO conflict : schoolConflicts) {
                    Integer travelTime = calculateTravelTime(teacherId, assignment.getSchoolId(), schoolId);
                    conflict.setTravelTimeMinutes(travelTime);
                    conflict.setRequiredArrivalTime(startTime.minusMinutes(travelTime));
                }
                
                conflicts.addAll(schoolConflicts);
            }
        }
        
        return conflicts;
    }
    
    private List<InterSchoolConflictDTO> checkScheduleConflictsAtSchool(Long teacherId, Long schoolId, 
                                                                       LocalDateTime startTime, LocalDateTime endTime) {
        try {
            // Appel au service de planification pour vérifier les conflits
            String url = "http://scheduling-service/api/schedules/conflicts/teacher/" + teacherId + 
                        "?schoolId=" + schoolId + 
                        "&startTime=" + startTime + 
                        "&endTime=" + endTime;
            
            InterSchoolConflictDTO[] conflicts = restTemplate.getForObject(url, InterSchoolConflictDTO[].class);
            return conflicts != null ? List.of(conflicts) : List.of();
            
        } catch (Exception e) {
            log.warn("Failed to check schedule conflicts at school {}: {}", schoolId, e.getMessage());
            return List.of();
        }
    }
    
    public Integer getTotalWeeklyHours(Long teacherId) {
        return assignmentRepository.getTotalWeeklyHours(teacherId, LocalDateTime.now());
    }
    
    public TeacherSchoolAssignmentDTO getPrimaryAssignment(Long teacherId) {
        TeacherSchoolAssignment primary = assignmentRepository.findPrimaryAssignment(teacherId, LocalDateTime.now());
        return primary != null ? mapToDTO(primary) : null;
    }
    
    @Transactional
    @CacheEvict(value = {"teacherAssignments", "multiSchoolTeachers"}, allEntries = true)
    public TeacherSchoolAssignmentDTO updateAssignment(Long id, TeacherSchoolAssignmentRequest request) {
        log.info("Updating school assignment: {}", id);
        
        TeacherSchoolAssignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assignment not found: " + id));
        
        assignment.setWorkingDays(request.getWorkingDays());
        assignment.setStartTime(request.getStartTime());
        assignment.setEndTime(request.getEndTime());
        assignment.setTravelTimeMinutes(request.getTravelTimeMinutes());
        assignment.setMaxHoursPerDay(request.getMaxHoursPerDay());
        assignment.setMaxHoursPerWeek(request.getMaxHoursPerWeek());
        assignment.setPriority(request.getPriority());
        assignment.setContractType(request.getContractType());
        assignment.setEffectiveFrom(request.getEffectiveFrom());
        assignment.setEffectiveTo(request.getEffectiveTo());
        assignment.setNotes(request.getNotes());
        
        TeacherSchoolAssignment updated = assignmentRepository.save(assignment);
        log.info("School assignment updated successfully: {}", updated.getId());
        
        return mapToDTO(updated);
    }
    
    @Transactional
    @CacheEvict(value = {"teacherAssignments", "multiSchoolTeachers"}, allEntries = true)
    public void deleteAssignment(Long id) {
        log.info("Deleting school assignment: {}", id);
        
        TeacherSchoolAssignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assignment not found: " + id));
        
        assignment.setActive(false);
        assignmentRepository.save(assignment);
        
        log.info("School assignment deleted successfully: {}", id);
    }
    
    private TeacherSchoolAssignmentDTO mapToDTO(TeacherSchoolAssignment assignment) {
        TeacherSchoolAssignmentDTO dto = new TeacherSchoolAssignmentDTO();
        dto.setId(assignment.getId());
        dto.setTeacherId(assignment.getTeacherId());
        dto.setSchoolId(assignment.getSchoolId());
        dto.setWorkingDays(assignment.getWorkingDays());
        dto.setStartTime(assignment.getStartTime());
        dto.setEndTime(assignment.getEndTime());
        dto.setTravelTimeMinutes(assignment.getTravelTimeMinutes());
        dto.setMaxHoursPerDay(assignment.getMaxHoursPerDay());
        dto.setMaxHoursPerWeek(assignment.getMaxHoursPerWeek());
        dto.setPriority(assignment.getPriority());
        dto.setContractType(assignment.getContractType());
        dto.setEffectiveFrom(assignment.getEffectiveFrom());
        dto.setEffectiveTo(assignment.getEffectiveTo());
        dto.setNotes(assignment.getNotes());
        dto.setActive(assignment.getActive());
        dto.setCreatedAt(assignment.getCreatedAt());
        dto.setUpdatedAt(assignment.getUpdatedAt());
        
        return dto;
    }
}