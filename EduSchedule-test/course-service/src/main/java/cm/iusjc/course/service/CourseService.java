package cm.iusjc.course.service;

import cm.iusjc.course.dto.*;
import cm.iusjc.course.entity.Course;
import cm.iusjc.course.entity.CourseGroup;
import cm.iusjc.course.repository.CourseRepository;
import cm.iusjc.course.repository.CourseGroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseService {
    
    private final CourseRepository courseRepository;
    private final CourseGroupRepository courseGroupRepository;
    private final UserServiceClient userServiceClient;
    
    @Transactional
    public CourseDTO createCourse(CourseRequest request) {
        log.info("Creating new course: {}", request.getCode());
        
        // Vérifier si le code existe déjà
        if (courseRepository.existsByCode(request.getCode())) {
            throw new RuntimeException("Course code already exists: " + request.getCode());
        }
        
        Course course = new Course();
        course.setName(request.getName());
        course.setCode(request.getCode());
        course.setDescription(request.getDescription());
        course.setCredits(request.getCredits());
        course.setDuration(request.getDuration());
        course.setDepartment(request.getDepartment());
        course.setLevel(request.getLevel());
        course.setSemester(request.getSemester());
        course.setTeacherId(request.getTeacherId());
        course.setMaxStudents(request.getMaxStudents());
        course.setActive(true);
        
        Course savedCourse = courseRepository.save(course);
        log.info("Course created successfully: {}", savedCourse.getCode());
        
        return mapToDTO(savedCourse);
    }
    
    public CourseDTO getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + id));
        return mapToDTO(course);
    }
    
    public CourseDTO getCourseByCode(String code) {
        Course course = courseRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Course not found with code: " + code));
        return mapToDTO(course);
    }
    
    public List<CourseDTO> getAllCourses() {
        return courseRepository.findByActiveTrue()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    public Page<CourseDTO> getCoursesWithFilters(String department, String level, 
                                                String semester, Long teacherId, 
                                                Pageable pageable) {
        return courseRepository.findCoursesWithFilters(department, level, semester, teacherId, pageable)
                .map(this::mapToDTO);
    }
    
    public List<CourseDTO> getCoursesByDepartment(String department) {
        return courseRepository.findByDepartmentAndActiveTrue(department)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    public List<CourseDTO> getCoursesByTeacher(Long teacherId) {
        return courseRepository.findByTeacherIdAndActiveTrue(teacherId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public CourseDTO updateCourse(Long id, CourseRequest request) {
        log.info("Updating course: {}", id);
        
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + id));
        
        // Vérifier si le nouveau code existe déjà (sauf pour ce cours)
        if (!course.getCode().equals(request.getCode()) && 
            courseRepository.existsByCode(request.getCode())) {
            throw new RuntimeException("Course code already exists: " + request.getCode());
        }
        
        course.setName(request.getName());
        course.setCode(request.getCode());
        course.setDescription(request.getDescription());
        course.setCredits(request.getCredits());
        course.setDuration(request.getDuration());
        course.setDepartment(request.getDepartment());
        course.setLevel(request.getLevel());
        course.setSemester(request.getSemester());
        course.setTeacherId(request.getTeacherId());
        course.setMaxStudents(request.getMaxStudents());
        
        Course updatedCourse = courseRepository.save(course);
        log.info("Course updated successfully: {}", updatedCourse.getCode());
        
        return mapToDTO(updatedCourse);
    }
    
    @Transactional
    public void deleteCourse(Long id) {
        log.info("Deleting course: {}", id);
        
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + id));
        
        // Soft delete
        course.setActive(false);
        courseRepository.save(course);
        
        // Désactiver aussi tous les groupes associés
        List<CourseGroup> groups = courseGroupRepository.findByCourseIdAndActiveTrue(id);
        groups.forEach(group -> group.setActive(false));
        courseGroupRepository.saveAll(groups);
        
        log.info("Course deleted successfully: {}", id);
    }
    
    public List<CourseDTO> searchCourses(String query) {
        return courseRepository.findByNameContainingAndActiveTrue(query)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    private CourseDTO mapToDTO(Course course) {
        CourseDTO dto = new CourseDTO();
        dto.setId(course.getId());
        dto.setName(course.getName());
        dto.setCode(course.getCode());
        dto.setDescription(course.getDescription());
        dto.setCredits(course.getCredits());
        dto.setDuration(course.getDuration());
        dto.setDepartment(course.getDepartment());
        dto.setLevel(course.getLevel());
        dto.setSemester(course.getSemester());
        dto.setTeacherId(course.getTeacherId());
        dto.setMaxStudents(course.getMaxStudents());
        dto.setActive(course.getActive());
        dto.setCreatedAt(course.getCreatedAt());
        dto.setUpdatedAt(course.getUpdatedAt());
        
        // Récupérer le nom de l'enseignant
        if (course.getTeacherId() != null) {
            try {
                dto.setTeacherName(userServiceClient.getUserName(course.getTeacherId()));
            } catch (Exception e) {
                log.warn("Could not fetch teacher name for ID: {}", course.getTeacherId());
                dto.setTeacherName("Enseignant inconnu");
            }
        }
        
        // Récupérer les groupes associés
        List<CourseGroup> groups = courseGroupRepository.findByCourseIdAndActiveTrue(course.getId());
        dto.setGroups(groups.stream().map(this::mapGroupToDTO).collect(Collectors.toList()));
        
        return dto;
    }
    
    private CourseGroupDTO mapGroupToDTO(CourseGroup group) {
        CourseGroupDTO dto = new CourseGroupDTO();
        dto.setId(group.getId());
        dto.setCourseId(group.getCourseId());
        dto.setGroupName(group.getGroupName());
        dto.setType(group.getType());
        dto.setMaxStudents(group.getMaxStudents());
        dto.setCurrentStudents(group.getCurrentStudents());
        dto.setTeacherId(group.getTeacherId());
        dto.setActive(group.getActive());
        dto.setCreatedAt(group.getCreatedAt());
        dto.setUpdatedAt(group.getUpdatedAt());
        
        // Récupérer le nom de l'enseignant du groupe
        if (group.getTeacherId() != null) {
            try {
                dto.setTeacherName(userServiceClient.getUserName(group.getTeacherId()));
            } catch (Exception e) {
                log.warn("Could not fetch teacher name for group ID: {}", group.getTeacherId());
                dto.setTeacherName("Enseignant inconnu");
            }
        }
        
        return dto;
    }
}