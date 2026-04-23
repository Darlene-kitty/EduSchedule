package cm.iusjc.scheduling.controller;

import cm.iusjc.scheduling.dto.TimeSlotDTO;
import cm.iusjc.scheduling.service.TimeSlotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/timeslots")
@RequiredArgsConstructor
@Slf4j
public class TimeSlotController {
    
    private final TimeSlotService timeSlotService;
    
    /**
     * Crée un nouveau créneau horaire
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> createTimeSlot(@Valid @RequestBody TimeSlotDTO timeSlotDTO) {
        try {
            log.info("Creating time slot: {} {} - {}", 
                timeSlotDTO.getDayOfWeek(), timeSlotDTO.getStartTime(), timeSlotDTO.getEndTime());
            TimeSlotDTO createdTimeSlot = timeSlotService.createTimeSlot(timeSlotDTO);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "message", "Time slot created successfully",
                "data", createdTimeSlot
            ));
        } catch (Exception e) {
            log.error("Error creating time slot: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Récupère tous les créneaux horaires
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllTimeSlots() {
        try {
            List<TimeSlotDTO> timeSlots = timeSlotService.getAllTimeSlots();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", timeSlots,
                "total", timeSlots.size()
            ));
        } catch (Exception e) {
            log.error("Error fetching time slots: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Récupère les créneaux horaires avec pagination
     */
    @GetMapping("/paginated")
    public ResponseEntity<Map<String, Object>> getAllTimeSlotsPaginated(Pageable pageable) {
        try {
            Page<TimeSlotDTO> timeSlotsPage = timeSlotService.getAllTimeSlots(pageable);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", timeSlotsPage.getContent(),
                "page", timeSlotsPage.getNumber(),
                "size", timeSlotsPage.getSize(),
                "totalElements", timeSlotsPage.getTotalElements(),
                "totalPages", timeSlotsPage.getTotalPages()
            ));
        } catch (Exception e) {
            log.error("Error fetching paginated time slots: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Récupère un créneau horaire par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getTimeSlotById(@PathVariable Long id) {
        try {
            return timeSlotService.getTimeSlotById(id)
                    .map(timeSlot -> ResponseEntity.ok(Map.of(
                        "success", true,
                        "data", timeSlot
                    )))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error fetching time slot by ID {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Récupère les créneaux horaires par jour de la semaine
     */
    @GetMapping("/day/{dayOfWeek}")
    public ResponseEntity<Map<String, Object>> getTimeSlotsByDayOfWeek(@PathVariable String dayOfWeek) {
        try {
            List<TimeSlotDTO> timeSlots = timeSlotService.getTimeSlotsByDayOfWeek(dayOfWeek);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", timeSlots,
                "total", timeSlots.size()
            ));
        } catch (Exception e) {
            log.error("Error fetching time slots by day {}: {}", dayOfWeek, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Récupère les créneaux horaires par planning
     */
    @GetMapping("/schedule/{scheduleId}")
    public ResponseEntity<Map<String, Object>> getTimeSlotsBySchedule(@PathVariable Long scheduleId) {
        try {
            List<TimeSlotDTO> timeSlots = timeSlotService.getTimeSlotsBySchedule(scheduleId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", timeSlots,
                "total", timeSlots.size()
            ));
        } catch (Exception e) {
            log.error("Error fetching time slots by schedule {}: {}", scheduleId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Récupère les créneaux horaires dans une plage horaire
     */
    @GetMapping("/range")
    public ResponseEntity<Map<String, Object>> getTimeSlotsInRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime end) {
        try {
            List<TimeSlotDTO> timeSlots = timeSlotService.getTimeSlotsInRange(start, end);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", timeSlots,
                "total", timeSlots.size()
            ));
        } catch (Exception e) {
            log.error("Error fetching time slots in range: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Récupère les créneaux horaires du matin
     */
    @GetMapping("/morning")
    public ResponseEntity<Map<String, Object>> getMorningTimeSlots() {
        try {
            List<TimeSlotDTO> timeSlots = timeSlotService.getMorningTimeSlots();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", timeSlots,
                "total", timeSlots.size()
            ));
        } catch (Exception e) {
            log.error("Error fetching morning time slots: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Récupère les créneaux horaires de l'après-midi
     */
    @GetMapping("/afternoon")
    public ResponseEntity<Map<String, Object>> getAfternoonTimeSlots() {
        try {
            List<TimeSlotDTO> timeSlots = timeSlotService.getAfternoonTimeSlots();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", timeSlots,
                "total", timeSlots.size()
            ));
        } catch (Exception e) {
            log.error("Error fetching afternoon time slots: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Récupère les créneaux horaires du soir
     */
    @GetMapping("/evening")
    public ResponseEntity<Map<String, Object>> getEveningTimeSlots() {
        try {
            List<TimeSlotDTO> timeSlots = timeSlotService.getEveningTimeSlots();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", timeSlots,
                "total", timeSlots.size()
            ));
        } catch (Exception e) {
            log.error("Error fetching evening time slots: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Récupère les créneaux horaires disponibles
     */
    @GetMapping("/available")
    public ResponseEntity<Map<String, Object>> getAvailableTimeSlots() {
        try {
            List<TimeSlotDTO> timeSlots = timeSlotService.getAvailableTimeSlots();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", timeSlots,
                "total", timeSlots.size()
            ));
        } catch (Exception e) {
            log.error("Error fetching available time slots: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Récupère les créneaux horaires occupés
     */
    @GetMapping("/occupied")
    public ResponseEntity<Map<String, Object>> getOccupiedTimeSlots() {
        try {
            List<TimeSlotDTO> timeSlots = timeSlotService.getOccupiedTimeSlots();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", timeSlots,
                "total", timeSlots.size()
            ));
        } catch (Exception e) {
            log.error("Error fetching occupied time slots: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Met à jour un créneau horaire
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> updateTimeSlot(
            @PathVariable Long id, 
            @Valid @RequestBody TimeSlotDTO timeSlotDTO) {
        try {
            TimeSlotDTO updatedTimeSlot = timeSlotService.updateTimeSlot(id, timeSlotDTO);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Time slot updated successfully",
                "data", updatedTimeSlot
            ));
        } catch (Exception e) {
            log.error("Error updating time slot {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Associe un créneau horaire à un planning
     */
    @PatchMapping("/{timeSlotId}/assign/{scheduleId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> assignToSchedule(
            @PathVariable Long timeSlotId, 
            @PathVariable Long scheduleId) {
        try {
            TimeSlotDTO updatedTimeSlot = timeSlotService.assignToSchedule(timeSlotId, scheduleId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Time slot assigned to schedule successfully",
                "data", updatedTimeSlot
            ));
        } catch (Exception e) {
            log.error("Error assigning time slot {} to schedule {}: {}", timeSlotId, scheduleId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Libère un créneau horaire d'un planning
     */
    @PatchMapping("/{timeSlotId}/unassign")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> unassignFromSchedule(@PathVariable Long timeSlotId) {
        try {
            TimeSlotDTO updatedTimeSlot = timeSlotService.unassignFromSchedule(timeSlotId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Time slot unassigned from schedule successfully",
                "data", updatedTimeSlot
            ));
        } catch (Exception e) {
            log.error("Error unassigning time slot {}: {}", timeSlotId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Supprime un créneau horaire
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteTimeSlot(@PathVariable Long id) {
        try {
            timeSlotService.deleteTimeSlot(id);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Time slot deleted successfully"
            ));
        } catch (Exception e) {
            log.error("Error deleting time slot {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Supprime tous les créneaux horaires d'un planning
     */
    @DeleteMapping("/schedule/{scheduleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteTimeSlotsBySchedule(@PathVariable Long scheduleId) {
        try {
            timeSlotService.deleteTimeSlotsBySchedule(scheduleId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Time slots deleted for schedule successfully"
            ));
        } catch (Exception e) {
            log.error("Error deleting time slots for schedule {}: {}", scheduleId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Recherche avancée de créneaux horaires
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchTimeSlots(
            @RequestParam(required = false) String dayOfWeek,
            @RequestParam(required = false) Long scheduleId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime) {
        try {
            List<TimeSlotDTO> timeSlots = timeSlotService.searchTimeSlotsWithFilters(dayOfWeek, scheduleId, startTime, endTime);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", timeSlots,
                "total", timeSlots.size()
            ));
        } catch (Exception e) {
            log.error("Error searching time slots: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Vérifie les conflits de créneaux horaires
     */
    @GetMapping("/conflicts")
    public ResponseEntity<Map<String, Object>> checkConflictingTimeSlots(
            @RequestParam String dayOfWeek,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime,
            @RequestParam(required = false) Long excludeId) {
        try {
            List<TimeSlotDTO> conflicts = timeSlotService.checkConflictingTimeSlots(dayOfWeek, startTime, endTime, excludeId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", conflicts,
                "hasConflicts", !conflicts.isEmpty(),
                "total", conflicts.size()
            ));
        } catch (Exception e) {
            log.error("Error checking time slot conflicts: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Obtient les statistiques des créneaux horaires
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getTimeSlotStatistics() {
        try {
            TimeSlotService.TimeSlotStatistics stats = timeSlotService.getTimeSlotStatistics();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", Map.of(
                    "overview", stats,
                    "dayStats", timeSlotService.getTimeSlotStatisticsByDayOfWeek(),
                    "hourStats", timeSlotService.getTimeSlotStatisticsByStartHour()
                )
            ));
        } catch (Exception e) {
            log.error("Error fetching time slot statistics: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Vérifie si un créneau horaire existe
     */
    @GetMapping("/exists")
    public ResponseEntity<Map<String, Object>> checkTimeSlotExists(
            @RequestParam String dayOfWeek,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime) {
        try {
            boolean exists = timeSlotService.existsByDayAndTime(dayOfWeek, startTime, endTime);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", Map.of("exists", exists)
            ));
        } catch (Exception e) {
            log.error("Error checking time slot existence: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
}