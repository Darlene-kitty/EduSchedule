package cm.iusjc.teacheravailabilityservice.controller;

import cm.iusjc.teacheravailabilityservice.dto.TeacherAvailabilityDTO;
import cm.iusjc.teacheravailabilityservice.dto.TimeSlotDTO;
import cm.iusjc.teacheravailabilityservice.service.TeacherAvailabilityService;
import cm.iusjc.teacheravailabilityservice.service.ConflictDetectionService;
import cm.iusjc.teacheravailabilityservice.exception.AvailabilityNotFoundException;
import cm.iusjc.teacheravailabilityservice.exception.ConflictException;
import cm.iusjc.teacheravailabilityservice.exception.ValidationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/teacher-availability")
@Validated
@CrossOrigin(origins = "*")
public class TeacherAvailabilityController {
    
    @Autowired
    private TeacherAvailabilityService availabilityService;
    
    @Autowired
    private ConflictDetectionService conflictDetectionService;
    
    // CRUD Operations
    
    @PostMapping
    public ResponseEntity<TeacherAvailabilityDTO> createAvailability(@Valid @RequestBody TeacherAvailabilityDTO availabilityDTO) {
        try {
            TeacherAvailabilityDTO created = availabilityService.createAvailability(availabilityDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (ConflictException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (ValidationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TeacherAvailabilityDTO> getAvailabilityById(@PathVariable @Positive Long id) {
        try {
            TeacherAvailabilityDTO availability = availabilityService.getAvailabilityById(id);
            return ResponseEntity.ok(availability);
        } catch (AvailabilityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<TeacherAvailabilityDTO> updateAvailability(
            @PathVariable @Positive Long id, 
            @Valid @RequestBody TeacherAvailabilityDTO availabilityDTO) {
        try {
            availabilityDTO.setId(id);
            TeacherAvailabilityDTO updated = availabilityService.updateAvailability(availabilityDTO);
            return ResponseEntity.ok(updated);
        } catch (AvailabilityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (ConflictException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (ValidationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAvailability(@PathVariable @Positive Long id) {
        try {
            availabilityService.deleteAvailability(id);
            return ResponseEntity.noContent().build();
        } catch (AvailabilityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Recherche par enseignant
    
    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<TeacherAvailabilityDTO>> getAvailabilitiesByTeacher(@PathVariable @Positive Long teacherId) {
        List<TeacherAvailabilityDTO> availabilities = availabilityService.getAvailabilitiesByTeacher(teacherId);
        return ResponseEntity.ok(availabilities);
    }
    
    @GetMapping("/teacher/{teacherId}/active")
    public ResponseEntity<TeacherAvailabilityDTO> getActiveAvailabilityForTeacher(
            @PathVariable @Positive Long teacherId,
            @RequestParam(required = false) LocalDate date) {
        
        LocalDate searchDate = date != null ? date : LocalDate.now();
        
        return availabilityService.getActiveAvailabilityForTeacherOnDate(teacherId, searchDate)
                .map(availability -> ResponseEntity.ok(availability))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/teacher/{teacherId}/period")
    public ResponseEntity<List<TeacherAvailabilityDTO>> getAvailabilitiesInPeriod(
            @PathVariable @Positive Long teacherId,
            @RequestParam @NotNull LocalDate startDate,
            @RequestParam @NotNull LocalDate endDate) {
        
        List<TeacherAvailabilityDTO> availabilities = availabilityService.getAvailabilitiesInPeriod(teacherId, startDate, endDate);
        return ResponseEntity.ok(availabilities);
    }
    
    // Vérification de disponibilité
    
    @GetMapping("/teacher/{teacherId}/check")
    public ResponseEntity<Map<String, Object>> checkTeacherAvailability(
            @PathVariable @Positive Long teacherId,
            @RequestParam @NotNull LocalDateTime startTime,
            @RequestParam @NotNull LocalDateTime endTime) {
        
        boolean isAvailable = availabilityService.isTeacherAvailable(teacherId, startTime, endTime);
        
        Map<String, Object> response = new HashMap<>();
        response.put("teacherId", teacherId);
        response.put("startTime", startTime);
        response.put("endTime", endTime);
        response.put("isAvailable", isAvailable);
        response.put("checkedAt", LocalDateTime.now());
        
        if (!isAvailable) {
            List<String> conflicts = conflictDetectionService.detectScheduleConflictForTeacher(teacherId, startTime, endTime);
            response.put("conflicts", conflicts);
        }
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/teacher/{teacherId}/slots")
    public ResponseEntity<List<TimeSlotDTO>> getAvailableSlots(
            @PathVariable @Positive Long teacherId,
            @RequestParam @NotNull LocalDate date) {
        
        List<TimeSlotDTO> slots = availabilityService.getAvailableSlots(teacherId, date);
        return ResponseEntity.ok(slots);
    }
    
    // Gestion des créneaux
    
    @PostMapping("/{availabilityId}/slots")
    public ResponseEntity<TeacherAvailabilityDTO> addTimeSlot(
            @PathVariable @Positive Long availabilityId,
            @Valid @RequestBody TimeSlotDTO timeSlotDTO) {
        try {
            TeacherAvailabilityDTO updated = availabilityService.addTimeSlot(availabilityId, timeSlotDTO);
            return ResponseEntity.ok(updated);
        } catch (AvailabilityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (ConflictException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
    
    @DeleteMapping("/{availabilityId}/slots/{timeSlotId}")
    public ResponseEntity<TeacherAvailabilityDTO> removeTimeSlot(
            @PathVariable @Positive Long availabilityId,
            @PathVariable @Positive Long timeSlotId) {
        try {
            TeacherAvailabilityDTO updated = availabilityService.removeTimeSlot(availabilityId, timeSlotId);
            return ResponseEntity.ok(updated);
        } catch (AvailabilityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Détection de conflits
    
    @PostMapping("/conflicts/check")
    public ResponseEntity<Map<String, Object>> checkConflicts(@Valid @RequestBody TeacherAvailabilityDTO availabilityDTO) {
        List<String> conflicts = conflictDetectionService.detectConflicts(availabilityDTO);
        
        Map<String, Object> response = new HashMap<>();
        response.put("hasConflicts", !conflicts.isEmpty());
        response.put("conflicts", conflicts);
        response.put("checkedAt", LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }
    
    // Statistiques
    
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalActiveAvailabilities", availabilityService.countActiveAvailabilities());
        stats.put("teachersWithAvailabilities", availabilityService.countTeachersWithAvailabilities());
        stats.put("generatedAt", LocalDateTime.now());
        
        return ResponseEntity.ok(stats);
    }
    
    // Health check
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "teacher-availability-service");
        health.put("timestamp", LocalDateTime.now().toString());
        
        return ResponseEntity.ok(health);
    }
    
    // Exception handlers
    
    @ExceptionHandler(AvailabilityNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(AvailabilityNotFoundException e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "NOT_FOUND");
        error.put("message", e.getMessage());
        error.put("timestamp", LocalDateTime.now().toString());
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Map<String, String>> handleConflict(ConflictException e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "CONFLICT");
        error.put("message", e.getMessage());
        error.put("timestamp", LocalDateTime.now().toString());
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, String>> handleValidation(ValidationException e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "VALIDATION_ERROR");
        error.put("message", e.getMessage());
        error.put("timestamp", LocalDateTime.now().toString());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneral(Exception e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "INTERNAL_ERROR");
        error.put("message", "Une erreur interne s'est produite");
        error.put("timestamp", LocalDateTime.now().toString());
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}