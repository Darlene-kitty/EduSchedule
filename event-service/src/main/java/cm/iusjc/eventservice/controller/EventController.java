package cm.iusjc.eventservice.controller;

import cm.iusjc.eventservice.dto.EventRequest;
import cm.iusjc.eventservice.entity.Event;
import cm.iusjc.eventservice.entity.EventStatus;
import cm.iusjc.eventservice.service.ConflictDetectionService;
import cm.iusjc.eventservice.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
@Slf4j
public class EventController {
    
    private final EventService eventService;
    private final ConflictDetectionService conflictDetectionService;
    
    /**
     * Crée un nouvel événement
     */
    @PostMapping
    public ResponseEntity<Event> createEvent(@Valid @RequestBody EventRequest request) {
        try {
            Event event = eventService.createEvent(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(event);
        } catch (RuntimeException e) {
            log.error("Error creating event: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Récupère un événement par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Event> getEvent(@PathVariable Long id) {
        Optional<Event> event = eventService.getEventById(id);
        return event.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Récupère tous les événements (liste complète pour le frontend)
     */
    @GetMapping
    public ResponseEntity<List<Event>> getAllEvents() {
        List<Event> events = eventService.getAllEventsList();
        return ResponseEntity.ok(events);
    }
    
    /**
     * Récupère les événements par organisateur
     */
    @GetMapping("/organizer/{organizerId}")
    public ResponseEntity<Page<Event>> getEventsByOrganizer(
            @PathVariable Long organizerId,
            Pageable pageable) {
        Page<Event> events = eventService.getEventsByOrganizer(organizerId, pageable);
        return ResponseEntity.ok(events);
    }
    
    /**
     * Récupère les événements par ressource
     */
    @GetMapping("/resource/{resourceId}")
    public ResponseEntity<Page<Event>> getEventsByResource(
            @PathVariable Long resourceId,
            Pageable pageable) {
        Page<Event> events = eventService.getEventsByResource(resourceId, pageable);
        return ResponseEntity.ok(events);
    }
    
    /**
     * Récupère les événements par plage de dates
     */
    @GetMapping("/date-range")
    public ResponseEntity<List<Event>> getEventsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<Event> events = eventService.getEventsByDateRange(startDate, endDate);
        return ResponseEntity.ok(events);
    }
    
    /**
     * Récupère les événements à venir
     */
    @GetMapping("/upcoming")
    public ResponseEntity<List<Event>> getUpcomingEvents(Pageable pageable) {
        List<Event> events = eventService.getUpcomingEvents(pageable);
        return ResponseEntity.ok(events);
    }
    
    /**
     * Récupère les événements du jour
     */
    @GetMapping("/today")
    public ResponseEntity<List<Event>> getTodayEvents() {
        List<Event> events = eventService.getTodayEvents();
        return ResponseEntity.ok(events);
    }
    
    /**
     * Met à jour un événement
     */
    @PutMapping("/{id}")
    public ResponseEntity<Event> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody EventRequest request) {
        try {
            Event event = eventService.updateEvent(id, request);
            return ResponseEntity.ok(event);
        } catch (RuntimeException e) {
            log.error("Error updating event {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Change le statut d'un événement
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<Event> updateEventStatus(
            @PathVariable Long id,
            @RequestParam EventStatus status) {
        try {
            Event event = eventService.updateEventStatus(id, status);
            return ResponseEntity.ok(event);
        } catch (RuntimeException e) {
            log.error("Error updating event status {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Supprime un événement
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        try {
            eventService.deleteEvent(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("Error deleting event {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Vérifie les conflits pour une ressource
     */
    @GetMapping("/conflicts/check")
    public ResponseEntity<List<Object>> checkConflicts(
            @RequestParam Long resourceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDateTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDateTime) {
        
        List<Object> conflicts = conflictDetectionService.checkAllConflicts(resourceId, startDateTime, endDateTime);
        return ResponseEntity.ok(conflicts);
    }
    
    /**
     * Vérifie la disponibilité d'une ressource
     */
    @GetMapping("/availability/check")
    public ResponseEntity<Boolean> checkAvailability(
            @RequestParam Long resourceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDateTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDateTime) {
        
        boolean available = conflictDetectionService.isResourceAvailable(resourceId, startDateTime, endDateTime);
        return ResponseEntity.ok(available);
    }
    
    /**
     * Trouve les créneaux disponibles
     */
    @GetMapping("/availability/slots")
    public ResponseEntity<List<ConflictDetectionService.TimeSlot>> findAvailableSlots(
            @RequestParam Long resourceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date,
            @RequestParam(defaultValue = "60") int durationMinutes) {
        
        List<ConflictDetectionService.TimeSlot> slots = conflictDetectionService.findAvailableSlots(resourceId, date, durationMinutes);
        return ResponseEntity.ok(slots);
    }
    
    /**
     * Endpoint de test
     */
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Event Service is running!");
    }
}