package cm.iusjc.teacheravailability.controller;

import cm.iusjc.teacheravailability.dto.*;
import cm.iusjc.teacheravailability.entity.AvailabilityType;
import cm.iusjc.teacheravailability.service.ConflictDetectionService;
import cm.iusjc.teacheravailability.service.TeacherAvailabilityService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/teacher-availability")
public class TeacherAvailabilityController {
    
    private static final Logger logger = LoggerFactory.getLogger(TeacherAvailabilityController.class);
    
    @Autowired
    private TeacherAvailabilityService availabilityService;
    
    @Autowired
    private ConflictDetectionService conflictDetectionService;
    
    // Create availability
    @PostMapping
    public ResponseEntity<TeacherAvailabilityDTO> createAvailability(
            @Valid @RequestBody TeacherAvailabilityDTO availabilityDTO) {
        logger.info("Creating availability for teacher {}", availabilityDTO.getTeacherId());
        
        try {
            TeacherAvailabilityDTO created = availabilityService.createAvailability(availabilityDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            logger.error("Error creating availability: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    // Update availability
    @PutMapping("/{id}")
    public ResponseEntity<TeacherAvailabilityDTO> updateAvailability(
            @PathVariable Long id,
            @Valid @RequestBody TeacherAvailabilityDTO availabilityDTO) {
        logger.info("Updating availability with ID: {}", id);
        
        try {
            TeacherAvailabilityDTO updated = availabilityService.updateAvailability(id, availabilityDTO);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            logger.error("Error updating availability: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    // Get availability by ID
    @GetMapping("/{id}")
    public ResponseEntity<TeacherAvailabilityDTO> getAvailabilityById(@PathVariable Long id) {
        logger.info("Getting availability with ID: {}", id);
        
        Optional<TeacherAvailabilityDTO> availability = availabilityService.getAvailabilityById(id);
        return availability.map(ResponseEntity::ok)
                          .orElse(ResponseEntity.notFound().build());
    }
    
    // Get all availabilities for a teacher
    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<TeacherAvailabilityDTO>> getTeacherAvailabilities(
            @PathVariable Long teacherId) {
        logger.info("Getting availabilities for teacher: {}", teacherId);
        
        List<TeacherAvailabilityDTO> availabilities = availabilityService.getTeacherAvailabilities(teacherId);
        return ResponseEntity.ok(availabilities);
    }
    
    // Get availabilities for a teacher at a specific school
    @GetMapping("/teacher/{teacherId}/school/{schoolId}")
    public ResponseEntity<List<TeacherAvailabilityDTO>> getTeacherSchoolAvailabilities(
            @PathVariable Long teacherId,
            @PathVariable Long schoolId) {
        logger.info("Getting availabilities for teacher {} at school {}", teacherId, schoolId);
        
        List<TeacherAvailabilityDTO> availabilities = 
            availabilityService.getTeacherSchoolAvailabilities(teacherId, schoolId);
        return ResponseEntity.ok(availabilities);
    }
    
    // Get availabilities for a teacher on a specific day
    @GetMapping("/teacher/{teacherId}/day/{dayOfWeek}")
    public ResponseEntity<List<TeacherAvailabilityDTO>> getTeacherDayAvailabilities(
            @PathVariable Long teacherId,
            @PathVariable DayOfWeek dayOfWeek) {
        logger.info("Getting availabilities for teacher {} on {}", teacherId, dayOfWeek);
        
        List<TeacherAvailabilityDTO> availabilities = 
            availabilityService.getTeacherDayAvailabilities(teacherId, dayOfWeek);
        return ResponseEntity.ok(availabilities);
    }
    
    // Check if teacher is available at specific time
    @GetMapping("/teacher/{teacherId}/check")
    public ResponseEntity<Boolean> isTeacherAvailable(
            @PathVariable Long teacherId,
            @RequestParam DayOfWeek dayOfWeek,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time) {
        logger.info("Checking if teacher {} is available on {} at {}", teacherId, dayOfWeek, time);
        
        boolean isAvailable = availabilityService.isTeacherAvailable(teacherId, dayOfWeek, time);
        return ResponseEntity.ok(isAvailable);
    }
    
    // Get available slots for a teacher on a specific day
    @GetMapping("/teacher/{teacherId}/slots/{dayOfWeek}")
    public ResponseEntity<List<AvailabilitySlotDTO>> getAvailableSlots(
            @PathVariable Long teacherId,
            @PathVariable DayOfWeek dayOfWeek) {
        logger.info("Getting available slots for teacher {} on {}", teacherId, dayOfWeek);
        
        List<AvailabilitySlotDTO> slots = availabilityService.getAvailableSlots(teacherId, dayOfWeek);
        return ResponseEntity.ok(slots);
    }
    
    // Get preferred slots for a teacher on a specific day
    @GetMapping("/teacher/{teacherId}/preferred/{dayOfWeek}")
    public ResponseEntity<List<AvailabilitySlotDTO>> getPreferredSlots(
            @PathVariable Long teacherId,
            @PathVariable DayOfWeek dayOfWeek) {
        logger.info("Getting preferred slots for teacher {} on {}", teacherId, dayOfWeek);
        
        List<AvailabilitySlotDTO> slots = availabilityService.getPreferredSlots(teacherId, dayOfWeek);
        return ResponseEntity.ok(slots);
    }
    
    // Check for conflicts
    @PostMapping("/check-conflicts")
    public ResponseEntity<ConflictDetectionDTO> checkConflicts(
            @RequestParam Long teacherId,
            @RequestParam DayOfWeek dayOfWeek,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime,
            @RequestParam(required = false) Long schoolId) {
        logger.info("Checking conflicts for teacher {} on {} from {} to {}", 
                   teacherId, dayOfWeek, startTime, endTime);
        
        ConflictDetectionDTO conflicts = conflictDetectionService.checkConflicts(
            teacherId, dayOfWeek, startTime, endTime, schoolId);
        return ResponseEntity.ok(conflicts);
    }
    
    // Get all conflicts for a teacher on a specific day
    @GetMapping("/teacher/{teacherId}/conflicts/{dayOfWeek}")
    public ResponseEntity<List<ConflictDetectionDTO>> getDayConflicts(
            @PathVariable Long teacherId,
            @PathVariable DayOfWeek dayOfWeek) {
        logger.info("Getting conflicts for teacher {} on {}", teacherId, dayOfWeek);
        
        List<ConflictDetectionDTO> conflicts = conflictDetectionService.getDayConflicts(teacherId, dayOfWeek);
        return ResponseEntity.ok(conflicts);
    }
    
    // Create bulk availabilities
    @PostMapping("/bulk")
    public ResponseEntity<List<TeacherAvailabilityDTO>> createBulkAvailabilities(
            @Valid @RequestBody List<TeacherAvailabilityDTO> availabilities) {
        logger.info("Creating {} availabilities in bulk", availabilities.size());
        
        try {
            List<TeacherAvailabilityDTO> created = availabilityService.createBulkAvailabilities(availabilities);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            logger.error("Error creating bulk availabilities: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    // Delete availability
    @DeleteMapping("/{id}/teacher/{teacherId}")
    public ResponseEntity<Void> deleteAvailability(
            @PathVariable Long id,
            @PathVariable Long teacherId) {
        logger.info("Deleting availability with ID: {}", id);
        
        try {
            availabilityService.deleteAvailability(id, teacherId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Error deleting availability: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    // Get availability statistics
    @GetMapping("/teacher/{teacherId}/stats")
    public ResponseEntity<Object> getAvailabilityStats(@PathVariable Long teacherId) {
        logger.info("Getting availability statistics for teacher: {}", teacherId);
        
        Long count = availabilityService.getAvailabilityCount(teacherId);
        List<Object[]> statsByDay = availabilityService.getAvailabilityStatsByDay(teacherId);
        
        return ResponseEntity.ok(new Object() {
            public final Long totalAvailabilities = count;
            public final List<Object[]> availabilitiesByDay = statsByDay;
        });
    }
    
    // Health check
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Teacher Availability Service is running");
    }
}