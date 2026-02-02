package cm.iusjc.calendar.controller;

import cm.iusjc.calendar.dto.CalendarEventDto;
import cm.iusjc.calendar.dto.CalendarIntegrationDto;
import cm.iusjc.calendar.dto.WeeklyScheduleDto;
import cm.iusjc.calendar.service.CalendarIntegrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CalendarController {
    
    private final CalendarIntegrationService calendarIntegrationService;
    
    /**
     * Créer une nouvelle intégration calendrier
     */
    @PostMapping("/integrations")
    public ResponseEntity<CalendarIntegrationDto> createIntegration(@RequestBody CalendarIntegrationDto integrationDto) {
        log.info("Création d'une intégration calendrier pour l'utilisateur: {}", integrationDto.getUserId());
        CalendarIntegrationDto created = calendarIntegrationService.createIntegration(integrationDto);
        return ResponseEntity.ok(created);
    }
    
    /**
     * Obtenir les intégrations d'un utilisateur
     */
    @GetMapping("/integrations/{userId}")
    public ResponseEntity<List<CalendarIntegrationDto>> getUserIntegrations(@PathVariable String userId) {
        log.info("Récupération des intégrations calendrier pour l'utilisateur: {}", userId);
        List<CalendarIntegrationDto> integrations = calendarIntegrationService.getUserIntegrations(userId);
        return ResponseEntity.ok(integrations);
    }
    
    /**
     * Obtenir l'emploi du temps hebdomadaire
     */
    @GetMapping("/schedule/weekly/{userId}")
    public ResponseEntity<WeeklyScheduleDto> getWeeklySchedule(
            @PathVariable String userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        
        log.info("Récupération de l'emploi du temps hebdomadaire pour l'utilisateur: {} semaine du: {}", userId, weekStart);
        WeeklyScheduleDto weeklySchedule = calendarIntegrationService.getWeeklySchedule(userId, weekStart);
        return ResponseEntity.ok(weeklySchedule);
    }
    
    /**
     * Obtenir l'emploi du temps hebdomadaire pour la semaine courante
     */
    @GetMapping("/schedule/weekly/{userId}/current")
    public ResponseEntity<WeeklyScheduleDto> getCurrentWeeklySchedule(@PathVariable String userId) {
        log.info("Récupération de l'emploi du temps hebdomadaire courant pour l'utilisateur: {}", userId);
        LocalDate currentWeekStart = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
        WeeklyScheduleDto weeklySchedule = calendarIntegrationService.getWeeklySchedule(userId, currentWeekStart);
        return ResponseEntity.ok(weeklySchedule);
    }
    
    /**
     * Synchroniser les calendriers d'un utilisateur
     */
    @PostMapping("/sync/{userId}")
    public ResponseEntity<String> syncCalendars(@PathVariable String userId) {
        log.info("Synchronisation des calendriers pour l'utilisateur: {}", userId);
        try {
            calendarIntegrationService.syncCalendarEvents(userId);
            return ResponseEntity.ok("Synchronisation terminée avec succès");
        } catch (Exception e) {
            log.error("Erreur lors de la synchronisation: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Erreur lors de la synchronisation: " + e.getMessage());
        }
    }
    
    /**
     * Exporter un événement vers les calendriers externes
     */
    @PostMapping("/events/export")
    public ResponseEntity<String> exportEvent(@RequestBody CalendarEventDto eventDto) {
        log.info("Export de l'événement: {} vers les calendriers externes", eventDto.getTitle());
        try {
            calendarIntegrationService.exportEventToExternalCalendars(eventDto);
            return ResponseEntity.ok("Événement exporté avec succès");
        } catch (Exception e) {
            log.error("Erreur lors de l'export: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Erreur lors de l'export: " + e.getMessage());
        }
    }
    
    /**
     * Obtenir l'emploi du temps pour une école
     */
    @GetMapping("/schedule/school/{schoolId}/weekly")
    public ResponseEntity<WeeklyScheduleDto> getSchoolWeeklySchedule(
            @PathVariable String schoolId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        
        log.info("Récupération de l'emploi du temps hebdomadaire pour l'école: {} semaine du: {}", schoolId, weekStart);
        // Cette méthode devra être implémentée pour récupérer tous les événements de l'école
        WeeklyScheduleDto weeklySchedule = calendarIntegrationService.getWeeklySchedule("school:" + schoolId, weekStart);
        return ResponseEntity.ok(weeklySchedule);
    }
    
    /**
     * Obtenir l'emploi du temps pour une salle
     */
    @GetMapping("/schedule/room/{roomId}/weekly")
    public ResponseEntity<WeeklyScheduleDto> getRoomWeeklySchedule(
            @PathVariable String roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        
        log.info("Récupération de l'emploi du temps hebdomadaire pour la salle: {} semaine du: {}", roomId, weekStart);
        // Cette méthode devra être implémentée pour récupérer tous les événements de la salle
        WeeklyScheduleDto weeklySchedule = calendarIntegrationService.getWeeklySchedule("room:" + roomId, weekStart);
        return ResponseEntity.ok(weeklySchedule);
    }
    
    /**
     * Obtenir l'emploi du temps pour un enseignant
     */
    @GetMapping("/schedule/teacher/{teacherId}/weekly")
    public ResponseEntity<WeeklyScheduleDto> getTeacherWeeklySchedule(
            @PathVariable String teacherId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        
        log.info("Récupération de l'emploi du temps hebdomadaire pour l'enseignant: {} semaine du: {}", teacherId, weekStart);
        WeeklyScheduleDto weeklySchedule = calendarIntegrationService.getWeeklySchedule("teacher:" + teacherId, weekStart);
        return ResponseEntity.ok(weeklySchedule);
    }
}