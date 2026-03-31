package cm.iusjc.course.service;

import cm.iusjc.course.dto.CourseDTO;
import cm.iusjc.course.entity.Course;
import cm.iusjc.course.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
public class CourseService {
    
    private final CourseRepository courseRepository;
    
    /**
     * Crée un nouveau cours
     */
    @Transactional
    @CacheEvict(value = "courses", allEntries = true)
    public CourseDTO createCourse(CourseDTO courseDTO) {
        log.info("Creating new course: {}", courseDTO.getName());
        
        // Vérifier si le cours existe déjà
        if (courseRepository.existsByCodeAndSchoolId(courseDTO.getCode(), courseDTO.getSchoolId())) {
            throw new RuntimeException("Course with code '" + courseDTO.getCode() + 
                "' already exists in this school");
        }
        
        Course course = new Course();
        course.setName(courseDTO.getName());
        course.setCode(courseDTO.getCode());
        course.setDescription(courseDTO.getDescription());
        course.setCredits(courseDTO.getCredits());
        course.setHoursPerWeek(courseDTO.getHoursPerWeek());
        course.setDuration(courseDTO.getDuration());
        course.setLevel(courseDTO.getLevel());
        course.setDepartment(courseDTO.getDepartment());
        course.setSemester(courseDTO.getSemester());
        course.setSchoolId(courseDTO.getSchoolId());
        course.setTeacherId(courseDTO.getTeacherId());
        course.setActive(courseDTO.isActive());
        course.setCreatedAt(LocalDateTime.now());
        course.setUpdatedAt(LocalDateTime.now());
        
        Course savedCourse = courseRepository.save(course);
        log.info("Course created successfully with ID: {}", savedCourse.getId());
        
        return convertToDTO(savedCourse);
    }
    
    /**
     * Récupère tous les cours
     */
    @Cacheable(value = "courses")
    public List<CourseDTO> getAllCourses() {
        log.debug("Fetching all courses");
        return courseRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les cours avec pagination
     */
    public Page<CourseDTO> getAllCourses(Pageable pageable) {
        log.debug("Fetching courses with pagination: {}", pageable);
        return courseRepository.findAll(pageable)
                .map(this::convertToDTO);
    }
    
    /**
     * Récupère un cours par ID
     */
    @Cacheable(value = "courses", key = "#id")
    public Optional<CourseDTO> getCourseById(Long id) {
        log.debug("Fetching course by ID: {}", id);
        return courseRepository.findById(id)
                .map(this::convertToDTO);
    }
    
    /**
     * Récupère un cours par code
     */
    @Cacheable(value = "courses", key = "#code")
    public Optional<CourseDTO> getCourseByCode(String code) {
        log.debug("Fetching course by code: {}", code);
        return courseRepository.findByCode(code)
                .map(this::convertToDTO);
    }
    
    /**
     * Récupère les cours actifs
     */
    @Cacheable(value = "activeCourses")
    public List<CourseDTO> getActiveCourses() {
        log.debug("Fetching active courses");
        return courseRepository.findByActiveTrue().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les cours par école
     */
    public List<CourseDTO> getCoursesBySchool(Long schoolId) {
        log.debug("Fetching courses by school ID: {}", schoolId);
        return courseRepository.findBySchoolId(schoolId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les cours par enseignant
     */
    public List<CourseDTO> getCoursesByTeacher(Long teacherId) {
        log.debug("Fetching courses by teacher ID: {}", teacherId);
        return courseRepository.findByTeacherId(teacherId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les cours par département
     */
    public List<CourseDTO> getCoursesByDepartment(String department) {
        log.debug("Fetching courses by department: {}", department);
        return courseRepository.findByDepartment(department).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les cours par niveau
     */
    public List<CourseDTO> getCoursesByLevel(String level) {
        log.debug("Fetching courses by level: {}", level);
        return courseRepository.findByLevel(level).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Met à jour un cours
     */
    @Transactional
    @CacheEvict(value = {"courses", "activeCourses"}, allEntries = true)
    public CourseDTO updateCourse(Long id, CourseDTO courseDTO) {
        log.info("Updating course with ID: {}", id);
        
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found with ID: " + id));
        
        // Vérifier si le nouveau code existe déjà (sauf pour ce cours)
        if (!course.getCode().equals(courseDTO.getCode()) && 
            courseRepository.existsByCodeAndSchoolId(courseDTO.getCode(), courseDTO.getSchoolId())) {
            throw new RuntimeException("Course with code '" + courseDTO.getCode() + 
                "' already exists in this school");
        }
        
        course.setName(courseDTO.getName());
        course.setCode(courseDTO.getCode());
        course.setDescription(courseDTO.getDescription());
        course.setCredits(courseDTO.getCredits());
        course.setHoursPerWeek(courseDTO.getHoursPerWeek());
        course.setDuration(courseDTO.getDuration());
        course.setLevel(courseDTO.getLevel());
        course.setDepartment(courseDTO.getDepartment());
        course.setSemester(courseDTO.getSemester());
        course.setSchoolId(courseDTO.getSchoolId());
        course.setTeacherId(courseDTO.getTeacherId());
        course.setActive(courseDTO.isActive());
        course.setUpdatedAt(LocalDateTime.now());
        
        Course updatedCourse = courseRepository.save(course);
        log.info("Course updated successfully: {}", updatedCourse.getId());
        
        return convertToDTO(updatedCourse);
    }
    
    /**
     * Assigne un enseignant à un cours
     */
    @Transactional
    @CacheEvict(value = {"courses", "activeCourses"}, allEntries = true)
    public CourseDTO assignTeacher(Long courseId, Long teacherId) {
        log.info("Assigning teacher {} to course {}", teacherId, courseId);
        
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with ID: " + courseId));
        
        course.setTeacherId(teacherId);
        course.setUpdatedAt(LocalDateTime.now());
        
        Course updatedCourse = courseRepository.save(course);
        log.info("Teacher assigned successfully to course: {}", updatedCourse.getId());
        
        return convertToDTO(updatedCourse);
    }
    
    /**
     * Active/désactive un cours
     */
    @Transactional
    @CacheEvict(value = {"courses", "activeCourses"}, allEntries = true)
    public CourseDTO toggleCourseStatus(Long id) {
        log.info("Toggling status for course ID: {}", id);
        
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found with ID: " + id));
        
        course.setActive(!course.isActive());
        course.setUpdatedAt(LocalDateTime.now());
        Course updatedCourse = courseRepository.save(course);
        
        log.info("Course status toggled: {} - Active: {}", updatedCourse.getName(), updatedCourse.isActive());
        return convertToDTO(updatedCourse);
    }
    
    /**
     * Supprime un cours (soft delete)
     */
    @Transactional
    @CacheEvict(value = {"courses", "activeCourses"}, allEntries = true)
    public void deleteCourse(Long id) {
        log.info("Deleting course with ID: {}", id);
        
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found with ID: " + id));
        
        // Vérifier si le cours a des groupes ou des emplois du temps associés
        if (hasAssociatedData(id)) {
            throw new RuntimeException("Cannot delete course: it has associated data");
        }
        
        // Soft delete - désactiver le cours
        course.setActive(false);
        course.setUpdatedAt(LocalDateTime.now());
        courseRepository.save(course);
        
        log.info("Course soft deleted: {}", course.getName());
    }
    
    /**
     * Supprime définitivement un cours
     */
    @Transactional
    @CacheEvict(value = {"courses", "activeCourses"}, allEntries = true)
    public void hardDeleteCourse(Long id) {
        log.warn("Hard deleting course with ID: {}", id);
        
        if (hasAssociatedData(id)) {
            throw new RuntimeException("Cannot delete course: it has associated data");
        }
        
        courseRepository.deleteById(id);
        log.warn("Course hard deleted with ID: {}", id);
    }
    
    /**
     * Recherche des cours par nom
     */
    public List<CourseDTO> searchCoursesByName(String name) {
        log.debug("Searching courses by name containing: {}", name);
        return courseRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Recherche des cours par critères multiples
     */
    public List<CourseDTO> searchCourses(String searchTerm) {
        log.debug("Searching courses with term: {}", searchTerm);
        return courseRepository.searchCourses(searchTerm).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtient les statistiques des cours par département
     */
    public List<Object[]> getCourseStatisticsByDepartment() {
        return courseRepository.getCourseCountByDepartment();
    }
    
    /**
     * Obtient les statistiques des cours par niveau
     */
    public List<Object[]> getCourseStatisticsByLevel() {
        return courseRepository.getCourseCountByLevel();
    }
    
    /**
     * Obtient les cours les plus populaires (avec le plus de groupes)
     */
    public List<CourseDTO> getPopularCourses(int limit) {
        return courseRepository.findPopularCourses().stream()
                .limit(limit)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Vérifie si un cours existe
     */
    public boolean existsById(Long id) {
        return courseRepository.existsById(id);
    }
    
    /**
     * Vérifie si un cours existe par code
     */
    public boolean existsByCode(String code) {
        return courseRepository.existsByCode(code);
    }
    
    /**
     * Compte le nombre total de cours
     */
    public long countCourses() {
        return courseRepository.count();
    }
    
    /**
     * Compte le nombre de cours actifs
     */
    public long countActiveCourses() {
        return courseRepository.countByActiveTrue();
    }
    
    /**
     * Compte les cours par école
     */
    public long countCoursesBySchool(Long schoolId) {
        return courseRepository.countBySchoolId(schoolId);
    }
    
    /**
     * Compte les cours par enseignant
     */
    public long countCoursesByTeacher(Long teacherId) {
        return courseRepository.countByTeacherId(teacherId);
    }
    
    /**
     * Vérifie si le cours a des données associées
     */
    private boolean hasAssociatedData(Long courseId) {
        // Cette méthode devrait vérifier dans les autres services
        // Pour l'instant, on retourne false
        return false;
    }
    
    /**
     * Convertit une entité Course en DTO
     */
    private CourseDTO convertToDTO(Course course) {
        CourseDTO dto = new CourseDTO();
        dto.setId(course.getId());
        dto.setName(course.getName());
        dto.setCode(course.getCode());
        dto.setDescription(course.getDescription());
        dto.setCredits(course.getCredits());
        dto.setHoursPerWeek(course.getHoursPerWeek());
        dto.setDuration(course.getDuration());
        dto.setLevel(course.getLevel());
        dto.setDepartment(course.getDepartment());
        dto.setSemester(course.getSemester());
        dto.setSchoolId(course.getSchoolId());
        dto.setTeacherId(course.getTeacherId());
        dto.setActive(course.isActive());
        dto.setCreatedAt(course.getCreatedAt());
        dto.setUpdatedAt(course.getUpdatedAt());
        return dto;
    }
}