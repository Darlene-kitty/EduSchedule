package cm.iusjc.eventservice.service;

import cm.iusjc.eventservice.dto.ExamDTO;
import cm.iusjc.eventservice.dto.ExamRequest;
import cm.iusjc.eventservice.dto.SupervisorRequest;
import cm.iusjc.eventservice.entity.Exam;
import cm.iusjc.eventservice.entity.ExamStatus;
import cm.iusjc.eventservice.entity.ExamType;
import cm.iusjc.eventservice.entity.Supervisor;
import cm.iusjc.eventservice.repository.ExamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ExamService {
    
    private final ExamRepository examRepository;
    private final SupervisorService supervisorService;
    private final ConflictDetectionService conflictDetectionService;
    
    /**
     * Crée un nouvel examen
     */
    public ExamDTO createExam(ExamRequest request) {
        log.info("Creating new exam: {}", request.getTitle());
        
        // Vérifier les conflits de ressource
        boolean hasConflict = conflictDetectionService.hasAnyResourceConflict(
            request.getResourceId(), 
            request.getStartDateTime(), 
            request.getEndDateTime()
        );
        
        if (hasConflict) {
            throw new RuntimeException("Resource conflict detected for the requested time slot");
        }
        
        // Créer l'examen
        Exam exam = new Exam();
        exam.setTitle(request.getTitle());
        exam.setDescription(request.getDescription());
        exam.setCourseId(request.getCourseId());
        exam.setType(request.getType());
        exam.setStartDateTime(request.getStartDateTime());
        exam.setEndDateTime(request.getEndDateTime());
        exam.setResourceId(request.getResourceId());
        exam.setTeacherId(request.getTeacherId());
        exam.setMaxStudents(request.getMaxStudents());
        exam.setDurationMinutes(request.getDurationMinutes());
        exam.setInstructions(request.getInstructions());
        exam.setMaterialsAllowed(request.getMaterialsAllowed());
        
        exam = examRepository.save(exam);
        log.info("Exam created with ID: {}", exam.getId());
        
        // Assigner les surveillants si fournis
        if (request.getSupervisors() != null && !request.getSupervisors().isEmpty()) {
            for (SupervisorRequest supervisorRequest : request.getSupervisors()) {
                supervisorService.assignSupervisor(exam.getId(), supervisorRequest);
            }
        }
        
        return convertToDTO(exam);
    }
    
    /**
     * Récupère un examen par ID
     */
    public Optional<ExamDTO> getExamById(Long id) {
        return examRepository.findById(id)
                .map(this::convertToDTO);
    }
    
    /**
     * Récupère tous les examens avec pagination
     */
    public Page<ExamDTO> getAllExams(Pageable pageable) {
        return examRepository.findAll(pageable)
                .map(this::convertToDTO);
    }
    
    /**
     * Récupère les examens par cours
     */
    public Page<ExamDTO> getExamsByCourse(Long courseId, Pageable pageable) {
        return examRepository.findByCourseIdOrderByStartDateTimeDesc(courseId, pageable)
                .map(this::convertToDTO);
    }
    
    /**
     * Récupère les examens par enseignant
     */
    public Page<ExamDTO> getExamsByTeacher(Long teacherId, Pageable pageable) {
        return examRepository.findByTeacherIdOrderByStartDateTimeDesc(teacherId, pageable)
                .map(this::convertToDTO);
    }
    
    /**
     * Récupère les examens par type
     */
    public Page<ExamDTO> getExamsByType(ExamType type, Pageable pageable) {
        return examRepository.findByTypeOrderByStartDateTimeDesc(type, pageable)
                .map(this::convertToDTO);
    }
    
    /**
     * Récupère les examens par ressource
     */
    public Page<ExamDTO> getExamsByResource(Long resourceId, Pageable pageable) {
        return examRepository.findByResourceIdOrderByStartDateTimeDesc(resourceId, pageable)
                .map(this::convertToDTO);
    }
    
    /**
     * Récupère les examens par plage de dates
     */
    public List<ExamDTO> getExamsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return examRepository.findByDateRange(startDate, endDate)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les examens d'aujourd'hui
     */
    public List<ExamDTO> getTodayExams() {
        return examRepository.findTodayExams()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les examens de la semaine
     */
    public List<ExamDTO> getWeekExams(LocalDateTime startOfWeek, LocalDateTime endOfWeek) {
        return examRepository.findWeekExams(startOfWeek, endOfWeek)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Confirme un examen
     */
    public ExamDTO confirmExam(Long examId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Exam not found"));
        
        exam.setStatus(ExamStatus.CONFIRMED);
        exam = examRepository.save(exam);
        
        log.info("Exam {} confirmed", examId);
        return convertToDTO(exam);
    }
    
    /**
     * Démarre un examen
     */
    public ExamDTO startExam(Long examId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Exam not found"));
        
        exam.setStatus(ExamStatus.IN_PROGRESS);
        exam = examRepository.save(exam);
        
        log.info("Exam {} started", examId);
        return convertToDTO(exam);
    }
    
    /**
     * Termine un examen
     */
    public ExamDTO completeExam(Long examId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Exam not found"));
        
        exam.setStatus(ExamStatus.COMPLETED);
        exam = examRepository.save(exam);
        
        log.info("Exam {} completed", examId);
        return convertToDTO(exam);
    }
    
    /**
     * Annule un examen
     */
    public ExamDTO cancelExam(Long examId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Exam not found"));
        
        exam.setStatus(ExamStatus.CANCELLED);
        exam = examRepository.save(exam);
        
        log.info("Exam {} cancelled", examId);
        return convertToDTO(exam);
    }
    
    /**
     * Met à jour un examen
     */
    public ExamDTO updateExam(Long examId, ExamRequest request) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Exam not found"));
        
        // Vérifier les conflits si les dates ou la ressource changent
        if (!exam.getResourceId().equals(request.getResourceId()) ||
            !exam.getStartDateTime().equals(request.getStartDateTime()) ||
            !exam.getEndDateTime().equals(request.getEndDateTime())) {
            
            boolean hasConflict = conflictDetectionService.hasAnyResourceConflict(
                request.getResourceId(), 
                request.getStartDateTime(), 
                request.getEndDateTime(),
                null, // Pas d'événement à exclure
                examId // Exclure l'examen actuel
            );
            
            if (hasConflict) {
                throw new RuntimeException("Resource conflict detected for the requested time slot");
            }
        }
        
        // Mettre à jour les champs
        exam.setTitle(request.getTitle());
        exam.setDescription(request.getDescription());
        exam.setCourseId(request.getCourseId());
        exam.setType(request.getType());
        exam.setStartDateTime(request.getStartDateTime());
        exam.setEndDateTime(request.getEndDateTime());
        exam.setResourceId(request.getResourceId());
        exam.setTeacherId(request.getTeacherId());
        exam.setMaxStudents(request.getMaxStudents());
        exam.setDurationMinutes(request.getDurationMinutes());
        exam.setInstructions(request.getInstructions());
        exam.setMaterialsAllowed(request.getMaterialsAllowed());
        
        exam = examRepository.save(exam);
        log.info("Exam {} updated", examId);
        
        return convertToDTO(exam);
    }
    
    /**
     * Supprime un examen
     */
    public void deleteExam(Long examId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Exam not found"));
        
        // Supprimer aussi les surveillants associés
        supervisorService.removeAllSupervisors(examId);
        
        examRepository.delete(exam);
        log.info("Exam {} deleted", examId);
    }
    
    private ExamDTO convertToDTO(Exam exam) {
        ExamDTO dto = new ExamDTO();
        dto.setId(exam.getId());
        dto.setTitle(exam.getTitle());
        dto.setDescription(exam.getDescription());
        dto.setCourseId(exam.getCourseId());
        dto.setType(exam.getType());
        dto.setStartDateTime(exam.getStartDateTime());
        dto.setEndDateTime(exam.getEndDateTime());
        dto.setResourceId(exam.getResourceId());
        dto.setTeacherId(exam.getTeacherId());
        dto.setMaxStudents(exam.getMaxStudents());
        dto.setDurationMinutes(exam.getDurationMinutes());
        dto.setStatus(exam.getStatus());
        dto.setInstructions(exam.getInstructions());
        dto.setMaterialsAllowed(exam.getMaterialsAllowed());
        dto.setCreatedAt(exam.getCreatedAt());
        dto.setUpdatedAt(exam.getUpdatedAt());
        
        // Charger les surveillants
        dto.setSupervisors(supervisorService.getSupervisorsByExam(exam.getId()));
        
        return dto;
    }
}