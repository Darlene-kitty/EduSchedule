package cm.iusjc.scheduling.controller;

import cm.iusjc.scheduling.dto.ScheduleDTO;
import cm.iusjc.scheduling.service.ScheduleService;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/schedules")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ScheduleController {
    
    private final ScheduleService scheduleService;
    
    /**
     * Crée un nouveau planning
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> createSchedule(@Valid @RequestBody ScheduleDTO scheduleDTO) {
        try {
            log.info("Creating schedule: {}", scheduleDTO.getTitle());
            ScheduleDTO createdSchedule = scheduleService.createSchedule(scheduleDTO);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "message", "Schedule created successfully",
                "data", createdSchedule
            ));
        } catch (Exception e) {
            log.error("Error creating schedule: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Récupère tous les plannings
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllSchedules() {
        try {
            List<ScheduleDTO> schedules = scheduleService.getAllSchedules();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", schedules,
                "total", schedules.size()
            ));
        } catch (Exception e) {
            log.error("Error fetching schedules: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Récupère les plannings avec pagination
     */
    @GetMapping("/paginated")
    public ResponseEntity<Map<String, Object>> getAllSchedulesPaginated(Pageable pageable) {
        try {
            Page<ScheduleDTO> schedulesPage = scheduleService.getAllSchedules(pageable);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", schedulesPage.getContent(),
                "page", schedulesPage.getNumber(),
                "size", schedulesPage.getSize(),
                "totalElements", schedulesPage.getTotalElements(),
                "totalPages", schedulesPage.getTotalPages()
            ));
        } catch (Exception e) {
            log.error("Error fetching paginated schedules: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Récupère un planning par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getScheduleById(@PathVariable Long id) {
        try {
            return scheduleService.getScheduleById(id)
                    .map(schedule -> ResponseEntity.ok(Map.of(
                        "success", true,
                        "data", schedule
                    )))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error fetching schedule by ID {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Récupère les plannings par statut
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<Map<String, Object>> getSchedulesByStatus(@PathVariable String status) {
        try {
            List<ScheduleDTO> schedules = scheduleService.getSchedulesByStatus(status);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", schedules,
                "total", schedules.size()
            ));
        } catch (Exception e) {
            log.error("Error fetching schedules by status {}: {}", status, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Récupère les plannings par enseignant
     */
    @GetMapping("/teacher/{teacher}")
    public ResponseEntity<Map<String, Object>> getSchedulesByTeacher(@PathVariable String teacher) {
        try {
            List<ScheduleDTO> schedules = scheduleService.getSchedulesByTeacher(teacher);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", schedules,
                "total", schedules.size()
            ));
        } catch (Exception e) {
            log.error("Error fetching schedules by teacher {}: {}", teacher, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Récupère les plannings par cours
     */
    @GetMapping("/course/{course}")
    public ResponseEntity<Map<String, Object>> getSchedulesByCourse(@PathVariable String course) {
        try {
            List<ScheduleDTO> schedules = scheduleService.getSchedulesByCourse(course);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", schedules,
                "total", schedules.size()
            ));
        } catch (Exception e) {
            log.error("Error fetching schedules by course {}: {}", course, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Récupère les plannings par salle
     */
    @GetMapping("/room/{room}")
    public ResponseEntity<Map<String, Object>> getSchedulesByRoom(@PathVariable String room) {
        try {
            List<ScheduleDTO> schedules = scheduleService.getSchedulesByRoom(room);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", schedules,
                "total", schedules.size()
            ));
        } catch (Exception e) {
            log.error("Error fetching schedules by room {}: {}", room, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Récupère les plannings par groupe
     */
    @GetMapping("/group/{groupName}")
    public ResponseEntity<Map<String, Object>> getSchedulesByGroup(@PathVariable String groupName) {
        try {
            List<ScheduleDTO> schedules = scheduleService.getSchedulesByGroup(groupName);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", schedules,
                "total", schedules.size()
            ));
        } catch (Exception e) {
            log.error("Error fetching schedules by group {}: {}", groupName, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Récupère les plannings par période
     */
    @GetMapping("/period")
    public ResponseEntity<Map<String, Object>> getSchedulesByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        try {
            List<ScheduleDTO> schedules = scheduleService.getSchedulesByPeriod(start, end);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", schedules,
                "total", schedules.size()
            ));
        } catch (Exception e) {
            log.error("Error fetching schedules by period: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Récupère les plannings par date
     */
    @GetMapping("/date/{date}")
    public ResponseEntity<Map<String, Object>> getSchedulesByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date) {
        try {
            List<ScheduleDTO> schedules = scheduleService.getSchedulesByDate(date);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", schedules,
                "total", schedules.size()
            ));
        } catch (Exception e) {
            log.error("Error fetching schedules by date: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Récupère les plannings à venir
     */
    @GetMapping("/upcoming")
    public ResponseEntity<Map<String, Object>> getUpcomingSchedules() {
        try {
            List<ScheduleDTO> schedules = scheduleService.getUpcomingSchedules();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", schedules,
                "total", schedules.size()
            ));
        } catch (Exception e) {
            log.error("Error fetching upcoming schedules: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Récupère les plannings en cours
     */
    @GetMapping("/current")
    public ResponseEntity<Map<String, Object>> getCurrentSchedules() {
        try {
            List<ScheduleDTO> schedules = scheduleService.getCurrentSchedules();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", schedules,
                "total", schedules.size()
            ));
        } catch (Exception e) {
            log.error("Error fetching current schedules: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Récupère les plannings de la semaine
     */
    @GetMapping("/weekly")
    public ResponseEntity<Map<String, Object>> getWeeklySchedules(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime weekStart) {
        try {
            List<ScheduleDTO> schedules = scheduleService.getWeeklySchedules(weekStart);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", schedules,
                "total", schedules.size()
            ));
        } catch (Exception e) {
            log.error("Error fetching weekly schedules: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Met à jour un planning
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> updateSchedule(
            @PathVariable Long id, 
            @Valid @RequestBody ScheduleDTO scheduleDTO) {
        try {
            ScheduleDTO updatedSchedule = scheduleService.updateSchedule(id, scheduleDTO);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Schedule updated successfully",
                "data", updatedSchedule
            ));
        } catch (Exception e) {
            log.error("Error updating schedule {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Change le statut d'un planning
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> updateScheduleStatus(
            @PathVariable Long id, 
            @RequestParam String status) {
        try {
            ScheduleDTO updatedSchedule = scheduleService.updateScheduleStatus(id, status);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Schedule status updated successfully",
                "data", updatedSchedule
            ));
        } catch (Exception e) {
            log.error("Error updating schedule status {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Annule un planning
     */
    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> cancelSchedule(@PathVariable Long id) {
        try {
            ScheduleDTO cancelledSchedule = scheduleService.cancelSchedule(id);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Schedule cancelled successfully",
                "data", cancelledSchedule
            ));
        } catch (Exception e) {
            log.error("Error cancelling schedule {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Marque un planning comme terminé
     */
    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> completeSchedule(@PathVariable Long id) {
        try {
            ScheduleDTO completedSchedule = scheduleService.completeSchedule(id);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Schedule completed successfully",
                "data", completedSchedule
            ));
        } catch (Exception e) {
            log.error("Error completing schedule {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Réactive un planning
     */
    @PatchMapping("/{id}/reactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> reactivateSchedule(@PathVariable Long id) {
        try {
            ScheduleDTO reactivatedSchedule = scheduleService.reactivateSchedule(id);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Schedule reactivated successfully",
                "data", reactivatedSchedule
            ));
        } catch (Exception e) {
            log.error("Error reactivating schedule {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Supprime un planning
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteSchedule(@PathVariable Long id) {
        try {
            scheduleService.deleteSchedule(id);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Schedule deleted successfully"
            ));
        } catch (Exception e) {
            log.error("Error deleting schedule {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Recherche des plannings
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchSchedules(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String teacher,
            @RequestParam(required = false) String course,
            @RequestParam(required = false) String room,
            @RequestParam(required = false) String groupName,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String searchTerm) {
        try {
            List<ScheduleDTO> schedules;
            
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                schedules = scheduleService.searchSchedules(searchTerm);
            } else {
                schedules = scheduleService.searchSchedulesWithFilters(title, teacher, course, room, groupName, status);
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", schedules,
                "total", schedules.size()
            ));
        } catch (Exception e) {
            log.error("Error searching schedules: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Vérifie les conflits de salle
     */
    @GetMapping("/conflicts/room")
    public ResponseEntity<Map<String, Object>> checkRoomConflicts(
            @RequestParam String room,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(required = false) Long excludeId) {
        try {
            List<ScheduleDTO> conflicts = scheduleService.checkRoomConflicts(room, startTime, endTime, excludeId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", conflicts,
                "hasConflicts", !conflicts.isEmpty(),
                "total", conflicts.size()
            ));
        } catch (Exception e) {
            log.error("Error checking room conflicts: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Vérifie les conflits d'enseignant
     */
    @GetMapping("/conflicts/teacher")
    public ResponseEntity<Map<String, Object>> checkTeacherConflicts(
            @RequestParam String teacher,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(required = false) Long excludeId) {
        try {
            List<ScheduleDTO> conflicts = scheduleService.checkTeacherConflicts(teacher, startTime, endTime, excludeId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", conflicts,
                "hasConflicts", !conflicts.isEmpty(),
                "total", conflicts.size()
            ));
        } catch (Exception e) {
            log.error("Error checking teacher conflicts: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Obtient les statistiques des plannings
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getScheduleStatistics() {
        try {
            ScheduleService.ScheduleStatistics stats = scheduleService.getScheduleStatistics();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", Map.of(
                    "overview", stats,
                    "statusStats", scheduleService.getScheduleStatisticsByStatus(),
                    "teacherStats", scheduleService.getScheduleStatisticsByTeacher(),
                    "courseStats", scheduleService.getScheduleStatisticsByCourse(),
                    "roomStats", scheduleService.getScheduleStatisticsByRoom()
                )
            ));
        } catch (Exception e) {
            log.error("Error fetching schedule statistics: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
}