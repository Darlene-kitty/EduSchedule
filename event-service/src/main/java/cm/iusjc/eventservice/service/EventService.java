package cm.iusjc.eventservice.service;

import cm.iusjc.eventservice.dto.EventRequest;
import cm.iusjc.eventservice.entity.Event;
import cm.iusjc.eventservice.entity.EventStatus;
import cm.iusjc.eventservice.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EventService {
    
    private final EventRepository eventRepository;
    private final ConflictDetectionService conflictDetectionService;
    
    /**
     * Crée un nouvel événement
     */
    public Event createEvent(EventRequest request) {
        log.info("Creating new event: {}", request.getTitle());
        
        // Vérifier les conflits de ressource
        List<Event> conflicts = conflictDetectionService.checkEventConflicts(
                request.getResourceId(), 
                request.getStartDateTime(), 
                request.getEndDateTime()
        );
        
        if (!conflicts.isEmpty()) {
            throw new RuntimeException("Resource conflict detected. " + conflicts.size() + " conflicting events found.");
        }
        
        // Valider les dates
        if (request.getEndDateTime().isBefore(request.getStartDateTime())) {
            throw new IllegalArgumentException("End date time must be after start date time");
        }
        
        Event event = new Event();
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setType(request.getType());
        event.setStartDateTime(request.getStartDateTime());
        event.setEndDateTime(request.getEndDateTime());
        event.setResourceId(request.getResourceId());
        event.setOrganizerId(request.getOrganizerId());
        event.setMaxParticipants(request.getMaxParticipants());
        event.setRegistrationRequired(request.getRegistrationRequired());
        event.setRegistrationDeadline(request.getRegistrationDeadline());
        event.setExternalParticipants(request.getExternalParticipants());
        event.setEquipmentNeeded(request.getEquipmentNeeded());
        event.setSpecialRequirements(request.getSpecialRequirements());
        
        Event savedEvent = eventRepository.save(event);
        log.info("Event created successfully with ID: {}", savedEvent.getId());
        
        return savedEvent;
    }
    
    /**
     * Récupère un événement par ID
     */
    public Optional<Event> getEventById(Long id) {
        return eventRepository.findById(id);
    }
    
    /**
     * Récupère tous les événements avec pagination
     */
    public Page<Event> getAllEvents(Pageable pageable) {
        return eventRepository.findAll(pageable);
    }

    /**
     * Récupère tous les événements sous forme de liste (pour le frontend)
     */
    public List<Event> getAllEventsList() {
        return eventRepository.findAll(org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Direction.DESC, "startDateTime"));
    }
    
    /**
     * Récupère les événements par organisateur
     */
    public Page<Event> getEventsByOrganizer(Long organizerId, Pageable pageable) {
        return eventRepository.findByOrganizerIdOrderByStartDateTimeDesc(organizerId, pageable);
    }
    
    /**
     * Récupère les événements par ressource
     */
    public Page<Event> getEventsByResource(Long resourceId, Pageable pageable) {
        return eventRepository.findByResourceIdOrderByStartDateTimeDesc(resourceId, pageable);
    }
    
    /**
     * Récupère les événements par plage de dates
     */
    public List<Event> getEventsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return eventRepository.findByDateRange(startDate, endDate);
    }
    
    /**
     * Récupère les événements à venir
     */
    public List<Event> getUpcomingEvents(Pageable pageable) {
        return eventRepository.findUpcomingEvents(LocalDateTime.now(), pageable);
    }
    
    /**
     * Récupère les événements du jour
     */
    public List<Event> getTodayEvents() {
        return eventRepository.findEventsByDate(LocalDateTime.now());
    }
    
    /**
     * Met à jour un événement
     */
    public Event updateEvent(Long id, EventRequest request) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));
        
        // Vérifier les conflits si les dates ou la ressource changent
        if (!event.getResourceId().equals(request.getResourceId()) ||
            !event.getStartDateTime().equals(request.getStartDateTime()) ||
            !event.getEndDateTime().equals(request.getEndDateTime())) {
            
            List<Event> conflicts = conflictDetectionService.checkEventConflicts(
                    request.getResourceId(), 
                    request.getStartDateTime(), 
                    request.getEndDateTime(),
                    id // Exclure l'événement actuel
            );
            
            if (!conflicts.isEmpty()) {
                throw new RuntimeException("Resource conflict detected. " + conflicts.size() + " conflicting events found.");
            }
        }
        
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setType(request.getType());
        event.setStartDateTime(request.getStartDateTime());
        event.setEndDateTime(request.getEndDateTime());
        event.setResourceId(request.getResourceId());
        event.setMaxParticipants(request.getMaxParticipants());
        event.setRegistrationRequired(request.getRegistrationRequired());
        event.setRegistrationDeadline(request.getRegistrationDeadline());
        event.setExternalParticipants(request.getExternalParticipants());
        event.setEquipmentNeeded(request.getEquipmentNeeded());
        event.setSpecialRequirements(request.getSpecialRequirements());
        
        return eventRepository.save(event);
    }
    
    /**
     * Change le statut d'un événement
     */
    public Event updateEventStatus(Long id, EventStatus status) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));
        
        event.setStatus(status);
        return eventRepository.save(event);
    }
    
    /**
     * Supprime un événement
     */
    public void deleteEvent(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));
        
        // Vérifier si l'événement peut être supprimé
        if (event.getStatus() == EventStatus.IN_PROGRESS) {
            throw new RuntimeException("Cannot delete event in progress");
        }
        
        eventRepository.delete(event);
        log.info("Event deleted successfully: {}", id);
    }
}