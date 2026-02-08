package cm.iusjc.teacheravailability.controller;

import cm.iusjc.teacheravailability.dto.TeacherSchoolAssignmentDTO;
import cm.iusjc.teacheravailability.service.MultiSchoolService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/multi-school")
@CrossOrigin(origins = "*")
public class MultiSchoolController {
    
    private static final Logger logger = LoggerFactory.getLogger(MultiSchoolController.class);
    
    @Autowired
    private MultiSchoolService multiSchoolService;
    
    // Create school assignment
    @PostMapping("/assignments")
    public ResponseEntity<TeacherSchoolAssignmentDTO> createAssignment(
            @Valid @RequestBody TeacherSchoolAssignmentDTO assignmentDTO) {
        logger.info("Creating school assignment for teacher {} at school {}", 
                   assignmentDTO.getTeacherId(), assignmentDTO.getSchoolId());
        
        try {
            TeacherSchoolAssignmentDTO created = multiSchoolService.createAssignment(assignmentDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            logger.error("Error creating assignment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    // Update assignment
    @PutMapping("/assignments/{id}")
    public ResponseEntity<TeacherSchoolAssignmentDTO> updateAssignment(
            @PathVariable Long id,
            @Valid @RequestBody TeacherSchoolAssignmentDTO assignmentDTO) {
        logger.info("Updating school assignment with ID: {}", id);
        
        try {
            TeacherSchoolAssignmentDTO updated = multiSchoolService.updateAssignment(id, assignmentDTO);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            logger.error("Error updating assignment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    // Get assignment by ID
    @GetMapping("/assignments/{id}")
    public ResponseEntity<TeacherSchoolAssignmentDTO> getAssignmentById(@PathVariable Long id) {
        logger.info("Getting assignment with ID: {}", id);
        
        Optional<TeacherSchoolAssignmentDTO> assignment = multiSchoolService.getAssignmentById(id);
        return assignment.map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build());
    }
    
    // Get all assignments for a teacher
    @GetMapping("/teacher/{teacherId}/assignments")
    public ResponseEntity<List<TeacherSchoolAssignmentDTO>> getTeacherAssignments(
            @PathVariable Long teacherId) {
        logger.info("Getting school assignments for teacher: {}", teacherId);
        
        List<TeacherSchoolAssignmentDTO> assignments = multiSchoolService.getTeacherAssignments(teacherId);
        return ResponseEntity.ok(assignments);
    }
    
    // Get all assignments for a school
    @GetMapping("/school/{schoolId}/assignments")
    public ResponseEntity<List<TeacherSchoolAssignmentDTO>> getSchoolAssignments(
            @PathVariable Long schoolId) {
        logger.info("Getting teacher assignments for school: {}", schoolId);
        
        List<TeacherSchoolAssignmentDTO> assignments = multiSchoolService.getSchoolAssignments(schoolId);
        return ResponseEntity.ok(assignments);
    }
    
    // Get specific teacher-school assignment
    @GetMapping("/teacher/{teacherId}/school/{schoolId}")
    public ResponseEntity<TeacherSchoolAssignmentDTO> getTeacherSchoolAssignment(
            @PathVariable Long teacherId,
            @PathVariable Long schoolId) {
        logger.info("Getting assignment for teacher {} at school {}", teacherId, schoolId);
        
        Optional<TeacherSchoolAssignmentDTO> assignment = 
            multiSchoolService.getTeacherSchoolAssignment(teacherId, schoolId);
        return assignment.map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build());
    }
    
    // Check if teacher is assigned to school
    @GetMapping("/teacher/{teacherId}/school/{schoolId}/check")
    public ResponseEntity<Boolean> isTeacherAssignedToSchool(
            @PathVariable Long teacherId,
            @PathVariable Long schoolId) {
        logger.info("Checking if teacher {} is assigned to school {}", teacherId, schoolId);
        
        boolean isAssigned = multiSchoolService.isTeacherAssignedToSchool(teacherId, schoolId);
        return ResponseEntity.ok(isAssigned);
    }
    
    // Check if teacher works on specific day at school
    @GetMapping("/teacher/{teacherId}/school/{schoolId}/day/{dayOfWeek}")
    public ResponseEntity<Boolean> isTeacherWorkingOnDay(
            @PathVariable Long teacherId,
            @PathVariable Long schoolId,
            @PathVariable DayOfWeek dayOfWeek) {
        logger.info("Checking if teacher {} works at school {} on {}", teacherId, schoolId, dayOfWeek);
        
        boolean isWorking = multiSchoolService.isTeacherWorkingOnDay(teacherId, schoolId, dayOfWeek);
        return ResponseEntity.ok(isWorking);
    }
    
    // Get teachers working on specific day at school
    @GetMapping("/school/{schoolId}/day/{dayOfWeek}/teachers")
    public ResponseEntity<List<TeacherSchoolAssignmentDTO>> getTeachersWorkingOnDay(
            @PathVariable Long schoolId,
            @PathVariable DayOfWeek dayOfWeek) {
        logger.info("Getting teachers working at school {} on {}", schoolId, dayOfWeek);
        
        List<TeacherSchoolAssignmentDTO> teachers = 
            multiSchoolService.getTeachersWorkingOnDay(schoolId, dayOfWeek);
        return ResponseEntity.ok(teachers);
    }
    
    // Get multi-school teachers
    @GetMapping("/multi-school-teachers")
    public ResponseEntity<List<Long>> getMultiSchoolTeachers() {
        logger.info("Getting teachers working at multiple schools");
        
        List<Long> teacherIds = multiSchoolService.getMultiSchoolTeachers();
        return ResponseEntity.ok(teacherIds);
    }
    
    // Count schools for teacher
    @GetMapping("/teacher/{teacherId}/school-count")
    public ResponseEntity<Long> countSchoolsForTeacher(@PathVariable Long teacherId) {
        logger.info("Counting schools for teacher: {}", teacherId);
        
        Long count = multiSchoolService.countSchoolsForTeacher(teacherId);
        return ResponseEntity.ok(count);
    }
    
    // Calculate travel time between schools
    @GetMapping("/travel-time")
    public ResponseEntity<Integer> calculateTravelTime(
            @RequestParam Long fromSchoolId,
            @RequestParam Long toSchoolId) {
        logger.info("Calculating travel time from school {} to school {}", fromSchoolId, toSchoolId);
        
        Integer travelTime = multiSchoolService.calculateTravelTime(fromSchoolId, toSchoolId);
        return ResponseEntity.ok(travelTime);
    }
    
    // Get primary school for teacher
    @GetMapping("/teacher/{teacherId}/primary-school")
    public ResponseEntity<TeacherSchoolAssignmentDTO> getPrimarySchool(@PathVariable Long teacherId) {
        logger.info("Getting primary school for teacher: {}", teacherId);
        
        Optional<TeacherSchoolAssignmentDTO> primarySchool = multiSchoolService.getPrimarySchool(teacherId);
        return primarySchool.map(ResponseEntity::ok)
                           .orElse(ResponseEntity.notFound().build());
    }
    
    // Set primary school
    @PostMapping("/teacher/{teacherId}/primary-school/{schoolId}")
    public ResponseEntity<Void> setPrimarySchool(
            @PathVariable Long teacherId,
            @PathVariable Long schoolId) {
        logger.info("Setting primary school for teacher {} to school {}", teacherId, schoolId);
        
        try {
            multiSchoolService.setPrimarySchool(teacherId, schoolId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error setting primary school: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    // Delete assignment
    @DeleteMapping("/assignments/{id}/teacher/{teacherId}")
    public ResponseEntity<Void> deleteAssignment(
            @PathVariable Long id,
            @PathVariable Long teacherId) {
        logger.info("Deleting school assignment with ID: {}", id);
        
        try {
            multiSchoolService.deleteAssignment(id, teacherId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Error deleting assignment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    // Get statistics
    @GetMapping("/stats/teachers-by-school")
    public ResponseEntity<List<Object[]>> getTeacherCountBySchool() {
        logger.info("Getting teacher count statistics by school");
        
        List<Object[]> stats = multiSchoolService.getTeacherCountBySchool();
        return ResponseEntity.ok(stats);
    }
    
    // Health check
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Multi-School Service is running");
    }
}