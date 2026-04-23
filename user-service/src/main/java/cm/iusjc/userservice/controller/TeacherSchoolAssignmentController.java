package cm.iusjc.userservice.controller;

import cm.iusjc.userservice.dto.TeacherSchoolAssignmentDTO;
import cm.iusjc.userservice.service.TeacherSchoolAssignmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/teacher-school-assignments")
@RequiredArgsConstructor
@Slf4j
public class TeacherSchoolAssignmentController {
    
    private final TeacherSchoolAssignmentService assignmentService;
    
    @PostMapping
    public ResponseEntity<TeacherSchoolAssignmentDTO> createAssignment(@Valid @RequestBody TeacherSchoolAssignmentDTO dto) {
        log.info("Creating school assignment for teacher {}", dto.getTeacherId());
        TeacherSchoolAssignmentDTO created = assignmentService.createAssignment(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<TeacherSchoolAssignmentDTO>> getTeacherSchools(@PathVariable Long teacherId) {
        List<TeacherSchoolAssignmentDTO> schools = assignmentService.getTeacherSchools(teacherId);
        return ResponseEntity.ok(schools);
    }
    
    @GetMapping("/school/{schoolId}")
    public ResponseEntity<List<TeacherSchoolAssignmentDTO>> getSchoolTeachers(@PathVariable Long schoolId) {
        List<TeacherSchoolAssignmentDTO> teachers = assignmentService.getSchoolTeachers(schoolId);
        return ResponseEntity.ok(teachers);
    }
    
    @GetMapping("/teacher/{teacherId}/school/{schoolId}")
    public ResponseEntity<TeacherSchoolAssignmentDTO> getTeacherSchoolAssignment(
            @PathVariable Long teacherId,
            @PathVariable Long schoolId) {
        Optional<TeacherSchoolAssignmentDTO> assignment = assignmentService.getTeacherSchoolAssignment(teacherId, schoolId);
        return assignment.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/teacher/{teacherId}/day/{dayOfWeek}")
    public ResponseEntity<List<TeacherSchoolAssignmentDTO>> getTeacherSchoolsForDay(
            @PathVariable Long teacherId,
            @PathVariable DayOfWeek dayOfWeek) {
        List<TeacherSchoolAssignmentDTO> schools = assignmentService.getTeacherSchoolsForDay(teacherId, dayOfWeek);
        return ResponseEntity.ok(schools);
    }
    
    @GetMapping("/teacher/{teacherId}/primary")
    public ResponseEntity<TeacherSchoolAssignmentDTO> getPrimarySchool(@PathVariable Long teacherId) {
        Optional<TeacherSchoolAssignmentDTO> primarySchool = assignmentService.getPrimarySchool(teacherId);
        return primarySchool.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/teacher/{teacherId}/school/{schoolId}/check")
    public ResponseEntity<Boolean> checkTeacherSchoolAssignment(
            @PathVariable Long teacherId,
            @PathVariable Long schoolId) {
        boolean isAssigned = assignmentService.isTeacherAssignedToSchool(teacherId, schoolId);
        return ResponseEntity.ok(isAssigned);
    }
    
    @GetMapping("/travel-time")
    public ResponseEntity<Integer> calculateTravelTime(
            @RequestParam Long fromSchoolId,
            @RequestParam Long toSchoolId) {
        Integer travelTime = assignmentService.calculateTravelTime(fromSchoolId, toSchoolId);
        return ResponseEntity.ok(travelTime);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<TeacherSchoolAssignmentDTO> updateAssignment(
            @PathVariable Long id,
            @Valid @RequestBody TeacherSchoolAssignmentDTO dto) {
        TeacherSchoolAssignmentDTO updated = assignmentService.updateAssignment(id, dto);
        return ResponseEntity.ok(updated);
    }
    
    @PutMapping("/teacher/{teacherId}/primary-school/{schoolId}")
    public ResponseEntity<Void> setPrimarySchool(
            @PathVariable Long teacherId,
            @PathVariable Long schoolId) {
        assignmentService.setPrimarySchool(teacherId, schoolId);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAssignment(@PathVariable Long id) {
        assignmentService.deleteAssignment(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Teacher School Assignment Service is running");
    }
}