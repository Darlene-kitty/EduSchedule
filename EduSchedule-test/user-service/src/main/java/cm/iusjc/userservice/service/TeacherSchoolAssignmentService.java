package cm.iusjc.userservice.service;

import cm.iusjc.userservice.dto.TeacherSchoolAssignmentDTO;
import cm.iusjc.userservice.entity.TeacherSchoolAssignment;
import cm.iusjc.userservice.repository.TeacherSchoolAssignmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeacherSchoolAssignmentService {
    
    private final TeacherSchoolAssignmentRepository assignmentRepository;
    
    @Transactional
    public TeacherSchoolAssignmentDTO createAssignment(TeacherSchoolAssignmentDTO dto) {
        log.info("Creating school assignment for teacher {} at school {}", dto.getTeacherId(), dto.getSchoolId());
        
        // Vérifier si c'est la première école (école principale)
        Long schoolCount = assignmentRepository.countActiveSchoolsByTeacherId(dto.getTeacherId());
        if (schoolCount == 0) {
            dto.setIsPrimarySchool(true);
        }
        
        TeacherSchoolAssignment assignment = mapToEntity(dto);
        TeacherSchoolAssignment saved = assignmentRepository.save(assignment);
        
        return mapToDTO(saved);
    }
    
    public List<TeacherSchoolAssignmentDTO> getTeacherSchools(Long teacherId) {
        return assignmentRepository.findByTeacherIdAndIsActiveTrue(teacherId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    public List<TeacherSchoolAssignmentDTO> getSchoolTeachers(Long schoolId) {
        return assignmentRepository.findBySchoolIdAndIsActiveTrue(schoolId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    public Optional<TeacherSchoolAssignmentDTO> getTeacherSchoolAssignment(Long teacherId, Long schoolId) {
        return assignmentRepository.findByTeacherIdAndSchoolIdAndIsActiveTrue(teacherId, schoolId)
                .map(this::mapToDTO);
    }
    
    public List<TeacherSchoolAssignmentDTO> getTeacherSchoolsForDay(Long teacherId, DayOfWeek dayOfWeek) {
        return assignmentRepository.findByTeacherIdAndWorkingDay(teacherId, dayOfWeek)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    public Optional<TeacherSchoolAssignmentDTO> getPrimarySchool(Long teacherId) {
        return assignmentRepository.findPrimarySchoolByTeacherId(teacherId)
                .map(this::mapToDTO);
    }
    
    public boolean isTeacherAssignedToSchool(Long teacherId, Long schoolId) {
        return assignmentRepository.findByTeacherIdAndSchoolIdAndIsActiveTrue(teacherId, schoolId)
                .isPresent();
    }
    
    public Integer calculateTravelTime(Long fromSchoolId, Long toSchoolId) {
        if (fromSchoolId.equals(toSchoolId)) {
            return 0;
        }
        
        // Logique simple - dans un vrai système, cela pourrait utiliser une API de géolocalisation
        // Pour l'instant, on retourne un temps fixe basé sur la différence d'ID
        return Math.abs(fromSchoolId.intValue() - toSchoolId.intValue()) * 15 + 15; // 15-60 minutes
    }
    
    @Transactional
    public TeacherSchoolAssignmentDTO updateAssignment(Long id, TeacherSchoolAssignmentDTO dto) {
        TeacherSchoolAssignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assignment not found: " + id));
        
        assignment.setSchoolName(dto.getSchoolName());
        assignment.setSchoolAddress(dto.getSchoolAddress());
        assignment.setWorkingDays(dto.getWorkingDays());
        assignment.setStartTime(dto.getStartTime());
        assignment.setEndTime(dto.getEndTime());
        assignment.setTravelTimeMinutes(dto.getTravelTimeMinutes());
        assignment.setIsPrimarySchool(dto.getIsPrimarySchool());
        assignment.setIsActive(dto.getIsActive());
        
        TeacherSchoolAssignment updated = assignmentRepository.save(assignment);
        log.info("Updated assignment {} for teacher {}", id, dto.getTeacherId());
        
        return mapToDTO(updated);
    }
    
    @Transactional
    public void deleteAssignment(Long id) {
        TeacherSchoolAssignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assignment not found: " + id));
        
        assignment.setIsActive(false);
        assignmentRepository.save(assignment);
        
        log.info("Deactivated assignment {}", id);
    }
    
    @Transactional
    public void setPrimarySchool(Long teacherId, Long schoolId) {
        // Désactiver l'école principale actuelle
        Optional<TeacherSchoolAssignment> currentPrimary = assignmentRepository.findPrimarySchoolByTeacherId(teacherId);
        currentPrimary.ifPresent(assignment -> {
            assignment.setIsPrimarySchool(false);
            assignmentRepository.save(assignment);
        });
        
        // Définir la nouvelle école principale
        Optional<TeacherSchoolAssignment> newPrimary = assignmentRepository.findByTeacherIdAndSchoolIdAndIsActiveTrue(teacherId, schoolId);
        newPrimary.ifPresent(assignment -> {
            assignment.setIsPrimarySchool(true);
            assignmentRepository.save(assignment);
        });
        
        log.info("Set primary school {} for teacher {}", schoolId, teacherId);
    }
    
    private TeacherSchoolAssignment mapToEntity(TeacherSchoolAssignmentDTO dto) {
        TeacherSchoolAssignment assignment = new TeacherSchoolAssignment();
        assignment.setId(dto.getId());
        assignment.setTeacherId(dto.getTeacherId());
        assignment.setSchoolId(dto.getSchoolId());
        assignment.setSchoolName(dto.getSchoolName());
        assignment.setSchoolAddress(dto.getSchoolAddress());
        assignment.setWorkingDays(dto.getWorkingDays());
        assignment.setStartTime(dto.getStartTime());
        assignment.setEndTime(dto.getEndTime());
        assignment.setTravelTimeMinutes(dto.getTravelTimeMinutes() != null ? dto.getTravelTimeMinutes() : 30);
        assignment.setIsPrimarySchool(dto.getIsPrimarySchool() != null ? dto.getIsPrimarySchool() : false);
        assignment.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        return assignment;
    }
    
    private TeacherSchoolAssignmentDTO mapToDTO(TeacherSchoolAssignment assignment) {
        TeacherSchoolAssignmentDTO dto = new TeacherSchoolAssignmentDTO();
        dto.setId(assignment.getId());
        dto.setTeacherId(assignment.getTeacherId());
        dto.setSchoolId(assignment.getSchoolId());
        dto.setSchoolName(assignment.getSchoolName());
        dto.setSchoolAddress(assignment.getSchoolAddress());
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
        dto.setIsPrimarySchool(assignment.getIsPrimarySchool());
        dto.setIsActive(assignment.getIsActive());
        dto.setCreatedAt(assignment.getCreatedAt());
        dto.setUpdatedAt(assignment.getUpdatedAt());
        return dto;
    }
}