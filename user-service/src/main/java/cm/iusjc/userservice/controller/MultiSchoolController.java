package cm.iusjc.userservice.controller;

import cm.iusjc.userservice.dto.TeacherSchoolAssignmentDTO;
import cm.iusjc.userservice.dto.TeacherSchoolAssignmentRequest;
import cm.iusjc.userservice.dto.InterSchoolConflictDTO;
import cm.iusjc.userservice.service.MultiSchoolSchedulingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/multi-school")
@RequiredArgsConstructor
@Slf4j
public class MultiSchoolController {
    
    private final MultiSchoolSchedulingService multiSchoolService;
    
    @PostMapping("/assignments")
    public ResponseEntity<TeacherSchoolAssignmentDTO> createAssignment(@Valid @RequestBody TeacherSchoolAssignmentRequest request) {
        try {
            TeacherSchoolAssignmentDTO assignment = multiSchoolService.createAssignment(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(assignment);
        } catch (Exception e) {
            log.error("Error creating assignment: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/assignments/teacher/{teacherId}")
    public ResponseEntity<List<TeacherSchoolAssignmentDTO>> getTeacherAssignments(@PathVariable Long teacherId) {
        try {
            List<TeacherSchoolAssignmentDTO> assignments = multiSchoolService.getTeacherAssignments(teacherId);
            return ResponseEntity.ok(assignments);
        } catch (Exception e) {
            log.error("Error getting teacher assignments: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/teachers/multi-school")
    public ResponseEntity<List<Long>> getMultiSchoolTeachers() {
        try {
            List<Long> teachers = multiSchoolService.getMultiSchoolTeachers();
            return ResponseEntity.ok(teachers);
        } catch (Exception e) {
            log.error("Error getting multi-school teachers: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/teacher/{teacherId}/is-multi-school")
    public ResponseEntity<Boolean> isMultiSchoolTeacher(@PathVariable Long teacherId) {
        try {
            boolean isMultiSchool = multiSchoolService.isMultiSchoolTeacher(teacherId);
            return ResponseEntity.ok(isMultiSchool);
        } catch (Exception e) {
            log.error("Error checking multi-school status: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/travel-time")
    public ResponseEntity<Integer> calculateTravelTime(@RequestParam Long teacherId,
                                                      @RequestParam Long fromSchoolId,
                                                      @RequestParam Long toSchoolId) {
        try {
            Integer travelTime = multiSchoolService.calculateTravelTime(teacherId, fromSchoolId, toSchoolId);
            return ResponseEntity.ok(travelTime);
        } catch (Exception e) {
            log.error("Error calculating travel time: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/conflicts/check")
    public ResponseEntity<List<InterSchoolConflictDTO>> checkInterSchoolConflicts(@RequestParam Long teacherId,
                                                                                  @RequestParam String startTime,
                                                                                  @RequestParam String endTime,
                                                                                  @RequestParam Long schoolId) {
        try {
            LocalDateTime start = LocalDateTime.parse(startTime);
            LocalDateTime end = LocalDateTime.parse(endTime);
            List<InterSchoolConflictDTO> conflicts = multiSchoolService.checkInterSchoolConflicts(teacherId, start, end, schoolId);
            return ResponseEntity.ok(conflicts);
        } catch (Exception e) {
            log.error("Error checking inter-school conflicts: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/teacher/{teacherId}/total-hours")
    public ResponseEntity<Integer> getTotalWeeklyHours(@PathVariable Long teacherId) {
        try {
            Integer totalHours = multiSchoolService.getTotalWeeklyHours(teacherId);
            return ResponseEntity.ok(totalHours);
        } catch (Exception e) {
            log.error("Error getting total weekly hours: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/teacher/{teacherId}/primary-assignment")
    public ResponseEntity<TeacherSchoolAssignmentDTO> getPrimaryAssignment(@PathVariable Long teacherId) {
        try {
            TeacherSchoolAssignmentDTO primary = multiSchoolService.getPrimaryAssignment(teacherId);
            return ResponseEntity.ok(primary);
        } catch (Exception e) {
            log.error("Error getting primary assignment: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/assignments/{id}")
    public ResponseEntity<TeacherSchoolAssignmentDTO> updateAssignment(@PathVariable Long id,
                                                                      @Valid @RequestBody TeacherSchoolAssignmentRequest request) {
        try {
            TeacherSchoolAssignmentDTO updated = multiSchoolService.updateAssignment(id, request);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error updating assignment: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/assignments/{id}")
    public ResponseEntity<Void> deleteAssignment(@PathVariable Long id) {
        try {
            multiSchoolService.deleteAssignment(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting assignment: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}