package cm.iusjc.userservice.controller;

import cm.iusjc.userservice.dto.TeacherAvailabilityDTO;
import cm.iusjc.userservice.dto.TeacherAvailabilityRequest;
import cm.iusjc.userservice.dto.TimeSlotDTO;
import cm.iusjc.userservice.service.TeacherAvailabilityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/teacher-availability")
@RequiredArgsConstructor
@Slf4j
public class TeacherAvailabilityController {
    
    private final TeacherAvailabilityService availabilityService;
    
    @GetMapping
    public ResponseEntity<List<TeacherAvailabilityDTO>> getAllAvailabilities() {
        try {
            List<TeacherAvailabilityDTO> availabilities = availabilityService.getAllAvailabilities();
            return ResponseEntity.ok(availabilities);
        } catch (Exception e) {
            log.error("Error getting all availabilities: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping
    public ResponseEntity<TeacherAvailabilityDTO> createAvailability(@Valid @RequestBody TeacherAvailabilityRequest request) {
        try {
            TeacherAvailabilityDTO availability = availabilityService.createAvailability(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(availability);
        } catch (Exception e) {
            log.error("Error creating availability: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<TeacherAvailabilityDTO>> getTeacherAvailabilities(@PathVariable Long teacherId) {
        try {
            List<TeacherAvailabilityDTO> availabilities = availabilityService.getTeacherAvailabilities(teacherId);
            return ResponseEntity.ok(availabilities);
        } catch (Exception e) {
            log.error("Error getting teacher availabilities: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/teacher/{teacherId}/slots/{dayOfWeek}")
    public ResponseEntity<List<TimeSlotDTO>> getAvailableSlots(@PathVariable Long teacherId, 
                                                              @PathVariable DayOfWeek dayOfWeek) {
        try {
            List<TimeSlotDTO> slots = availabilityService.getAvailableSlots(teacherId, dayOfWeek);
            return ResponseEntity.ok(slots);
        } catch (Exception e) {
            log.error("Error getting available slots: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/teacher/{teacherId}/check")
    public ResponseEntity<Boolean> checkAvailability(@PathVariable Long teacherId,
                                                    @RequestParam String startDateTime,
                                                    @RequestParam String endDateTime) {
        try {
            LocalDateTime start = LocalDateTime.parse(startDateTime);
            LocalDateTime end = LocalDateTime.parse(endDateTime);
            boolean available = availabilityService.isTeacherAvailable(teacherId, start, end);
            return ResponseEntity.ok(available);
        } catch (Exception e) {
            log.error("Error checking availability: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/teacher/{teacherId}/preferred")
    public ResponseEntity<List<TeacherAvailabilityDTO>> getPreferredSlots(@PathVariable Long teacherId) {
        try {
            List<TeacherAvailabilityDTO> preferred = availabilityService.getPreferredSlots(teacherId);
            return ResponseEntity.ok(preferred);
        } catch (Exception e) {
            log.error("Error getting preferred slots: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/teacher/{teacherId}/total-hours")
    public ResponseEntity<Long> getTotalAvailableHours(@PathVariable Long teacherId) {
        try {
            Long totalHours = availabilityService.getTotalAvailableHours(teacherId);
            return ResponseEntity.ok(totalHours);
        } catch (Exception e) {
            log.error("Error getting total hours: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<TeacherAvailabilityDTO> updateAvailability(@PathVariable Long id,
                                                                    @Valid @RequestBody TeacherAvailabilityRequest request) {
        try {
            TeacherAvailabilityDTO updated = availabilityService.updateAvailability(id, request);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error updating availability: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAvailability(@PathVariable Long id) {
        try {
            availabilityService.deleteAvailability(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting availability: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/teacher/{teacherId}/default")
    public ResponseEntity<Void> setDefaultAvailability(@PathVariable Long teacherId,
                                                      @RequestParam Long schoolId) {
        try {
            availabilityService.setDefaultAvailability(teacherId, schoolId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error setting default availability: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}