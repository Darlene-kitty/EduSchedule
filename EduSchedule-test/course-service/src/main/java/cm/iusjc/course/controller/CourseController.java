package cm.iusjc.course.controller;

import cm.iusjc.course.dto.CourseDTO;
import cm.iusjc.course.dto.CourseRequest;
import cm.iusjc.course.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
@Slf4j
public class CourseController {
    
    private final CourseService courseService;
    
    @PostMapping
    public ResponseEntity<CourseDTO> createCourse(@Valid @RequestBody CourseRequest request) {
        log.info("Creating course: {}", request.getCode());
        CourseDTO course = courseService.createCourse(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(course);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<CourseDTO> getCourseById(@PathVariable Long id) {
        CourseDTO course = courseService.getCourseById(id);
        return ResponseEntity.ok(course);
    }
    
    @GetMapping("/code/{code}")
    public ResponseEntity<CourseDTO> getCourseByCode(@PathVariable String code) {
        CourseDTO course = courseService.getCourseByCode(code);
        return ResponseEntity.ok(course);
    }
    
    @GetMapping
    public ResponseEntity<List<CourseDTO>> getAllCourses(
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) Long teacherId) {
        
        if (department != null || level != null || semester != null || teacherId != null) {
            // Si des filtres sont fournis, utiliser la recherche paginée avec Pageable.unpaged()
            Page<CourseDTO> coursePage = courseService.getCoursesWithFilters(
                    department, level, semester, teacherId, Pageable.unpaged());
            return ResponseEntity.ok(coursePage.getContent());
        } else {
            List<CourseDTO> courses = courseService.getAllCourses();
            return ResponseEntity.ok(courses);
        }
    }
    
    @GetMapping("/paginated")
    public ResponseEntity<Page<CourseDTO>> getCoursesWithPagination(
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) Long teacherId,
            Pageable pageable) {
        
        Page<CourseDTO> courses = courseService.getCoursesWithFilters(
                department, level, semester, teacherId, pageable);
        return ResponseEntity.ok(courses);
    }
    
    @GetMapping("/department/{department}")
    public ResponseEntity<List<CourseDTO>> getCoursesByDepartment(@PathVariable String department) {
        List<CourseDTO> courses = courseService.getCoursesByDepartment(department);
        return ResponseEntity.ok(courses);
    }
    
    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<CourseDTO>> getCoursesByTeacher(@PathVariable Long teacherId) {
        List<CourseDTO> courses = courseService.getCoursesByTeacher(teacherId);
        return ResponseEntity.ok(courses);
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<CourseDTO>> searchCourses(@RequestParam String query) {
        List<CourseDTO> courses = courseService.searchCourses(query);
        return ResponseEntity.ok(courses);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<CourseDTO> updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody CourseRequest request) {
        log.info("Updating course: {}", id);
        CourseDTO course = courseService.updateCourse(id, request);
        return ResponseEntity.ok(course);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        log.info("Deleting course: {}", id);
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Course Service is running");
    }
}