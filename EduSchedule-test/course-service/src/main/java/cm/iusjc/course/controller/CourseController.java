package cm.iusjc.course.controller;

import cm.iusjc.course.dto.CourseDTO;
import cm.iusjc.course.service.CourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CourseController {
    
    private final CourseService courseService;
    
    /**
     * Crée un nouveau cours
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createCourse(@Valid @RequestBody CourseDTO courseDTO) {
        try {
            log.info("Creating course: {}", courseDTO.getName());
            CourseDTO createdCourse = courseService.createCourse(courseDTO);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "message", "Course created successfully",
                "data", createdCourse
            ));
        } catch (Exception e) {
            log.error("Error creating course: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Récupère tous les cours
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllCourses() {
        try {
            List<CourseDTO> courses = courseService.getAllCourses();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", courses,
                "total", courses.size()
            ));
        } catch (Exception e) {
            log.error("Error fetching courses: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Récupère les cours avec pagination
     */
    @GetMapping("/paginated")
    public ResponseEntity<Map<String, Object>> getAllCoursesPaginated(Pageable pageable) {
        try {
            Page<CourseDTO> coursesPage = courseService.getAllCourses(pageable);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", coursesPage.getContent(),
                "page", coursesPage.getNumber(),
                "size", coursesPage.getSize(),
                "totalElements", coursesPage.getTotalElements(),
                "totalPages", coursesPage.getTotalPages()
            ));
        } catch (Exception e) {
            log.error("Error fetching paginated courses: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Récupère un cours par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getCourseById(@PathVariable Long id) {
        try {
            return courseService.getCourseById(id)
                    .map(course -> ResponseEntity.ok(Map.of(
                        "success", true,
                        "data", course
                    )))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error fetching course by ID {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Récupère un cours par code
     */
    @GetMapping("/by-code/{code}")
    public ResponseEntity<Map<String, Object>> getCourseByCode(@PathVariable String code) {
        try {
            return courseService.getCourseByCode(code)
                    .map(course -> ResponseEntity.ok(Map.of(
                        "success", true,
                        "data", course
                    )))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error fetching course by code {}: {}", code, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Récupère les cours actifs
     */
    @GetMapping("/active")
    public ResponseEntity<Map<String, Object>> getActiveCourses() {
        try {
            List<CourseDTO> activeCourses = courseService.getActiveCourses();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", activeCourses,
                "total", activeCourses.size()
            ));
        } catch (Exception e) {
            log.error("Error fetching active courses: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Récupère les cours par école
     */
    @GetMapping("/school/{schoolId}")
    public ResponseEntity<Map<String, Object>> getCoursesBySchool(@PathVariable Long schoolId) {
        try {
            List<CourseDTO> courses = courseService.getCoursesBySchool(schoolId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", courses,
                "total", courses.size()
            ));
        } catch (Exception e) {
            log.error("Error fetching courses by school {}: {}", schoolId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Récupère les cours par enseignant
     */
    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<Map<String, Object>> getCoursesByTeacher(@PathVariable Long teacherId) {
        try {
            List<CourseDTO> courses = courseService.getCoursesByTeacher(teacherId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", courses,
                "total", courses.size()
            ));
        } catch (Exception e) {
            log.error("Error fetching courses by teacher {}: {}", teacherId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Récupère les cours par département
     */
    @GetMapping("/department/{department}")
    public ResponseEntity<Map<String, Object>> getCoursesByDepartment(@PathVariable String department) {
        try {
            List<CourseDTO> courses = courseService.getCoursesByDepartment(department);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", courses,
                "total", courses.size()
            ));
        } catch (Exception e) {
            log.error("Error fetching courses by department {}: {}", department, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Met à jour un cours
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateCourse(
            @PathVariable Long id, 
            @Valid @RequestBody CourseDTO courseDTO) {
        try {
            CourseDTO updatedCourse = courseService.updateCourse(id, courseDTO);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Course updated successfully",
                "data", updatedCourse
            ));
        } catch (Exception e) {
            log.error("Error updating course {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Assigne un enseignant à un cours
     */
    @PatchMapping("/{courseId}/assign-teacher/{teacherId}")
    public ResponseEntity<Map<String, Object>> assignTeacher(
            @PathVariable Long courseId, 
            @PathVariable Long teacherId) {
        try {
            CourseDTO updatedCourse = courseService.assignTeacher(courseId, teacherId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Teacher assigned successfully",
                "data", updatedCourse
            ));
        } catch (Exception e) {
            log.error("Error assigning teacher {} to course {}: {}", teacherId, courseId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Active/désactive un cours
     */
    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<Map<String, Object>> toggleCourseStatus(@PathVariable Long id) {
        try {
            CourseDTO updatedCourse = courseService.toggleCourseStatus(id);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Course status updated successfully",
                "data", updatedCourse
            ));
        } catch (Exception e) {
            log.error("Error toggling course status {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Supprime un cours (soft delete)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteCourse(@PathVariable Long id) {
        try {
            courseService.deleteCourse(id);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Course deleted successfully"
            ));
        } catch (Exception e) {
            log.error("Error deleting course {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Supprime définitivement un cours
     */
    @DeleteMapping("/{id}/hard")
    public ResponseEntity<Map<String, Object>> hardDeleteCourse(@PathVariable Long id) {
        try {
            courseService.hardDeleteCourse(id);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Course permanently deleted"
            ));
        } catch (Exception e) {
            log.error("Error hard deleting course {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Recherche des cours par nom
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchCourses(@RequestParam String name) {
        try {
            List<CourseDTO> courses = courseService.searchCoursesByName(name);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", courses,
                "total", courses.size()
            ));
        } catch (Exception e) {
            log.error("Error searching courses: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Obtient les statistiques des cours
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getCourseStatistics() {
        try {
            long totalCourses = courseService.countCourses();
            long activeCourses = courseService.countActiveCourses();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", Map.of(
                    "totalCourses", totalCourses,
                    "activeCourses", activeCourses,
                    "inactiveCourses", totalCourses - activeCourses,
                    "departmentStats", courseService.getCourseStatisticsByDepartment(),
                    "levelStats", courseService.getCourseStatisticsByLevel()
                )
            ));
        } catch (Exception e) {
            log.error("Error fetching course statistics: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
}