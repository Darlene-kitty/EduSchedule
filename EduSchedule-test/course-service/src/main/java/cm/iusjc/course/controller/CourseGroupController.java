package cm.iusjc.course.controller;

import cm.iusjc.course.dto.CourseGroupDTO;
import cm.iusjc.course.dto.CourseGroupRequest;
import cm.iusjc.course.service.CourseGroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/course-groups")
@RequiredArgsConstructor
@Slf4j
public class CourseGroupController {
    
    private final CourseGroupService courseGroupService;
    
    @PostMapping
    public ResponseEntity<CourseGroupDTO> createGroup(@Valid @RequestBody CourseGroupRequest request) {
        log.info("Creating course group: {} for course {}", request.getGroupName(), request.getCourseId());
        CourseGroupDTO group = courseGroupService.createGroup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(group);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<CourseGroupDTO> getGroupById(@PathVariable Long id) {
        CourseGroupDTO group = courseGroupService.getGroupById(id);
        return ResponseEntity.ok(group);
    }
    
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<CourseGroupDTO>> getGroupsByCourse(@PathVariable Long courseId) {
        List<CourseGroupDTO> groups = courseGroupService.getGroupsByCourse(courseId);
        return ResponseEntity.ok(groups);
    }
    
    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<CourseGroupDTO>> getGroupsByTeacher(@PathVariable Long teacherId) {
        List<CourseGroupDTO> groups = courseGroupService.getGroupsByTeacher(teacherId);
        return ResponseEntity.ok(groups);
    }
    
    @GetMapping("/type/{type}")
    public ResponseEntity<List<CourseGroupDTO>> getGroupsByType(@PathVariable String type) {
        List<CourseGroupDTO> groups = courseGroupService.getGroupsByType(type);
        return ResponseEntity.ok(groups);
    }
    
    @GetMapping("/available")
    public ResponseEntity<List<CourseGroupDTO>> getAvailableGroups() {
        List<CourseGroupDTO> groups = courseGroupService.getAvailableGroups();
        return ResponseEntity.ok(groups);
    }
    
    @GetMapping("/available/course/{courseId}")
    public ResponseEntity<List<CourseGroupDTO>> getAvailableGroupsByCourse(@PathVariable Long courseId) {
        List<CourseGroupDTO> groups = courseGroupService.getAvailableGroupsByCourse(courseId);
        return ResponseEntity.ok(groups);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<CourseGroupDTO> updateGroup(
            @PathVariable Long id,
            @Valid @RequestBody CourseGroupRequest request) {
        log.info("Updating course group: {}", id);
        CourseGroupDTO group = courseGroupService.updateGroup(id, request);
        return ResponseEntity.ok(group);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGroup(@PathVariable Long id) {
        log.info("Deleting course group: {}", id);
        courseGroupService.deleteGroup(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{id}/students/add")
    public ResponseEntity<CourseGroupDTO> addStudentToGroup(@PathVariable Long id) {
        log.info("Adding student to group: {}", id);
        CourseGroupDTO group = courseGroupService.addStudentToGroup(id);
        return ResponseEntity.ok(group);
    }
    
    @PostMapping("/{id}/students/remove")
    public ResponseEntity<CourseGroupDTO> removeStudentFromGroup(@PathVariable Long id) {
        log.info("Removing student from group: {}", id);
        CourseGroupDTO group = courseGroupService.removeStudentFromGroup(id);
        return ResponseEntity.ok(group);
    }
}