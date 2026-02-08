package cm.iusjc.eventservice.service;

import cm.iusjc.eventservice.dto.SupervisorDTO;
import cm.iusjc.eventservice.dto.SupervisorRequest;
import cm.iusjc.eventservice.entity.Supervisor;
import cm.iusjc.eventservice.entity.SupervisorStatus;
import cm.iusjc.eventservice.repository.SupervisorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SupervisorService {
    
    private final SupervisorRepository supervisorRepository;
    
    /**
     * Assigne un surveillant à un examen
     */
    public SupervisorDTO assignSupervisor(Long examId, SupervisorRequest request) {
        log.info("Assigning supervisor {} to exam {}", request.getUserId(), examId);
        
        // Vérifier les conflits de disponibilité
        List<Supervisor> conflicts = supervisorRepository.findConflictingSupervisors(
            request.getUserId(),
            request.getStartTime(),
            request.getEndTime()
        );
        
        if (!conflicts.isEmpty()) {
            log.warn("Supervisor {} has conflicts for the requested time slot", request.getUserId());
            // On peut choisir de lever une exception ou simplement logger un warning
        }
        
        Supervisor supervisor = new Supervisor();
        supervisor.setExamId(examId);
        supervisor.setUserId(request.getUserId());
        supervisor.setRole(request.getRole());
        supervisor.setStartTime(request.getStartTime());
        supervisor.setEndTime(request.getEndTime());
        supervisor.setNotes(request.getNotes());
        supervisor.setStatus(SupervisorStatus.ASSIGNED);
        
        supervisor = supervisorRepository.save(supervisor);
        log.info("Supervisor assigned with ID: {}", supervisor.getId());
        
        return convertToDTO(supervisor);
    }
    
    /**
     * Récupère tous les surveillants d'un examen
     */
    public List<SupervisorDTO> getSupervisorsByExam(Long examId) {
        return supervisorRepository.findByExamIdOrderByRole(examId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Supprime tous les surveillants d'un examen
     */
    public void removeAllSupervisors(Long examId) {
        List<Supervisor> supervisors = supervisorRepository.findByExamIdOrderByRole(examId);
        supervisorRepository.deleteAll(supervisors);
        log.info("Removed {} supervisors from exam {}", supervisors.size(), examId);
    }
    
    /**
     * Supprime un surveillant spécifique
     */
    public void removeSupervisor(Long supervisorId) {
        supervisorRepository.deleteById(supervisorId);
        log.info("Removed supervisor {}", supervisorId);
    }
    
    /**
     * Confirme la présence d'un surveillant
     */
    public SupervisorDTO confirmSupervisor(Long supervisorId) {
        Supervisor supervisor = supervisorRepository.findById(supervisorId)
                .orElseThrow(() -> new RuntimeException("Supervisor not found"));
        
        supervisor.setStatus(SupervisorStatus.CONFIRMED);
        supervisor = supervisorRepository.save(supervisor);
        
        log.info("Supervisor {} confirmed", supervisorId);
        return convertToDTO(supervisor);
    }
    
    /**
     * Marque un surveillant comme présent
     */
    public SupervisorDTO markSupervisorPresent(Long supervisorId) {
        Supervisor supervisor = supervisorRepository.findById(supervisorId)
                .orElseThrow(() -> new RuntimeException("Supervisor not found"));
        
        supervisor.setStatus(SupervisorStatus.PRESENT);
        supervisor = supervisorRepository.save(supervisor);
        
        log.info("Supervisor {} marked as present", supervisorId);
        return convertToDTO(supervisor);
    }
    
    /**
     * Marque un surveillant comme absent
     */
    public SupervisorDTO markSupervisorAbsent(Long supervisorId) {
        Supervisor supervisor = supervisorRepository.findById(supervisorId)
                .orElseThrow(() -> new RuntimeException("Supervisor not found"));
        
        supervisor.setStatus(SupervisorStatus.ABSENT);
        supervisor = supervisorRepository.save(supervisor);
        
        log.info("Supervisor {} marked as absent", supervisorId);
        return convertToDTO(supervisor);
    }
    
    /**
     * Récupère les surveillants d'un utilisateur
     */
    public List<SupervisorDTO> getSupervisorsByUser(Long userId) {
        return supervisorRepository.findByUserIdOrderByStartTime(userId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les surveillants d'aujourd'hui
     */
    public List<SupervisorDTO> getTodaySupervisors() {
        return supervisorRepository.findTodaySupervisors()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    private SupervisorDTO convertToDTO(Supervisor supervisor) {
        SupervisorDTO dto = new SupervisorDTO();
        dto.setId(supervisor.getId());
        dto.setExamId(supervisor.getExamId());
        dto.setUserId(supervisor.getUserId());
        dto.setRole(supervisor.getRole());
        dto.setStartTime(supervisor.getStartTime());
        dto.setEndTime(supervisor.getEndTime());
        dto.setNotes(supervisor.getNotes());
        dto.setStatus(supervisor.getStatus());
        dto.setCreatedAt(supervisor.getCreatedAt());
        dto.setUpdatedAt(supervisor.getUpdatedAt());
        
        // TODO: Enrichir avec les informations utilisateur via un appel au user-service
        
        return dto;
    }
}
