package cm.iusjc.calendar.service;

import cm.iusjc.calendar.entity.CalendarEvent;
import cm.iusjc.calendar.entity.CalendarIntegration;
import cm.iusjc.calendar.repository.CalendarEventRepository;
import cm.iusjc.calendar.repository.CalendarIntegrationRepository;
import cm.iusjc.calendar.dto.CalendarEventDto;
import cm.iusjc.calendar.dto.CalendarIntegrationDto;
import cm.iusjc.calendar.dto.WeeklyScheduleDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CalendarIntegrationService {
    
    private final CalendarIntegrationRepository integrationRepository;
    private final CalendarEventRepository eventRepository;
    private final GoogleCalendarService googleCalendarService;
    private final OutlookCalendarService outlookCalendarService;
    
    /**
     * Créer une nouvelle intégration calendrier
     */
    @Transactional
    public CalendarIntegrationDto createIntegration(CalendarIntegrationDto dto) {
        log.info("Création d'une nouvelle intégration calendrier pour l'utilisateur: {}", dto.getUserId());
        
        CalendarIntegration integration = new CalendarIntegration();
        integration.setUserId(dto.getUserId());
        integration.setProvider(dto.getProvider());
        integration.setCalendarId(dto.getCalendarId());
        integration.setAccessToken(dto.getAccessToken());
        integration.setRefreshToken(dto.getRefreshToken());
        integration.setTokenExpiresAt(dto.getTokenExpiresAt());
        integration.setEnabled(dto.getEnabled());
        integration.setSyncEnabled(dto.getSyncEnabled());
        integration.setSyncDirection(dto.getSyncDirection());
        
        CalendarIntegration saved = integrationRepository.save(integration);
        
        // Démarrer la synchronisation initiale
        if (saved.getSyncEnabled()) {
            performInitialSync(saved);
        }
        
        return convertToDto(saved);
    }
    
    /**
     * Obtenir les intégrations d'un utilisateur
     */
    public List<CalendarIntegrationDto> getUserIntegrations(String userId) {
        return integrationRepository.findByUserIdAndEnabledTrue(userId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtenir l'emploi du temps hebdomadaire pour un utilisateur
     */
    public WeeklyScheduleDto getWeeklySchedule(String userId, LocalDate weekStart) {
        log.info("Récupération de l'emploi du temps hebdomadaire pour l'utilisateur: {} semaine du: {}", userId, weekStart);
        
        // Calculer les dates de début et fin de semaine
        LocalDate monday = weekStart.with(DayOfWeek.MONDAY);
        LocalDate sunday = monday.plusDays(6);
        
        LocalDateTime startDateTime = monday.atStartOfDay();
        LocalDateTime endDateTime = sunday.atTime(23, 59, 59);
        
        // Récupérer les événements de la semaine
        List<CalendarEvent> events = eventRepository.findByUserIdAndDateRange(userId, startDateTime, endDateTime);
        
        WeeklyScheduleDto weeklySchedule = new WeeklyScheduleDto();
        weeklySchedule.setUserId(userId);
        weeklySchedule.setWeekStart(monday);
        weeklySchedule.setWeekEnd(sunday);
        
        // Organiser les événements par jour
        for (int i = 0; i < 7; i++) {
            LocalDate currentDay = monday.plusDays(i);
            List<CalendarEventDto> dayEvents = events.stream()
                    .filter(event -> event.getStartTime().toLocalDate().equals(currentDay))
                    .map(this::convertEventToDto)
                    .sorted((e1, e2) -> e1.getStartTime().compareTo(e2.getStartTime()))
                    .collect(Collectors.toList());
            
            weeklySchedule.getDailySchedules().put(currentDay.getDayOfWeek(), dayEvents);
        }
        
        return weeklySchedule;
    }
    
    /**
     * Synchroniser les événements avec les calendriers externes
     */
    @Transactional
    public void syncCalendarEvents(String userId) {
        log.info("Synchronisation des événements calendrier pour l'utilisateur: {}", userId);
        
        List<CalendarIntegration> integrations = integrationRepository.findByUserIdAndSyncEnabledTrue(userId);
        
        for (CalendarIntegration integration : integrations) {
            try {
                integration.setSyncStatus(CalendarIntegration.SyncStatus.SYNCING);
                integrationRepository.save(integration);
                
                switch (integration.getProvider()) {
                    case GOOGLE:
                        googleCalendarService.syncEvents(integration);
                        break;
                    case OUTLOOK:
                        outlookCalendarService.syncEvents(integration);
                        break;
                    default:
                        log.warn("Provider non supporté: {}", integration.getProvider());
                }
                
                integration.setSyncStatus(CalendarIntegration.SyncStatus.SUCCESS);
                integration.setLastSyncAt(LocalDateTime.now());
                integration.setSyncError(null);
                
            } catch (Exception e) {
                log.error("Erreur lors de la synchronisation pour l'intégration {}: {}", integration.getId(), e.getMessage());
                integration.setSyncStatus(CalendarIntegration.SyncStatus.ERROR);
                integration.setSyncError(e.getMessage());
            } finally {
                integrationRepository.save(integration);
            }
        }
    }
    
    /**
     * Exporter un événement vers les calendriers externes
     */
    @Transactional
    public void exportEventToExternalCalendars(CalendarEventDto eventDto) {
        log.info("Export de l'événement {} vers les calendriers externes", eventDto.getTitle());
        
        List<CalendarIntegration> integrations = integrationRepository.findByUserIdAndSyncEnabledTrue(eventDto.getUserId());
        
        for (CalendarIntegration integration : integrations) {
            if (integration.getSyncDirection() == CalendarIntegration.SyncDirection.IMPORT_ONLY) {
                continue;
            }
            
            try {
                CalendarEvent event = convertDtoToEvent(eventDto);
                event.setIntegration(integration);
                event.setSyncStatus(CalendarEvent.EventSyncStatus.PENDING);
                
                CalendarEvent savedEvent = eventRepository.save(event);
                
                switch (integration.getProvider()) {
                    case GOOGLE:
                        String googleEventId = googleCalendarService.createEvent(integration, savedEvent);
                        savedEvent.setExternalEventId(googleEventId);
                        break;
                    case OUTLOOK:
                        String outlookEventId = outlookCalendarService.createEvent(integration, savedEvent);
                        savedEvent.setExternalEventId(outlookEventId);
                        break;
                }
                
                savedEvent.setSyncStatus(CalendarEvent.EventSyncStatus.SYNCED);
                savedEvent.setLastSyncedAt(LocalDateTime.now());
                eventRepository.save(savedEvent);
                
            } catch (Exception e) {
                log.error("Erreur lors de l'export vers {}: {}", integration.getProvider(), e.getMessage());
            }
        }
    }
    
    /**
     * Effectuer la synchronisation initiale
     */
    private void performInitialSync(CalendarIntegration integration) {
        try {
            log.info("Synchronisation initiale pour l'intégration {}", integration.getId());
            
            switch (integration.getProvider()) {
                case GOOGLE:
                    googleCalendarService.performInitialSync(integration);
                    break;
                case OUTLOOK:
                    outlookCalendarService.performInitialSync(integration);
                    break;
            }
            
        } catch (Exception e) {
            log.error("Erreur lors de la synchronisation initiale: {}", e.getMessage());
            integration.setSyncStatus(CalendarIntegration.SyncStatus.ERROR);
            integration.setSyncError(e.getMessage());
            integrationRepository.save(integration);
        }
    }
    
    /**
     * Convertir une entité en DTO
     */
    private CalendarIntegrationDto convertToDto(CalendarIntegration integration) {
        CalendarIntegrationDto dto = new CalendarIntegrationDto();
        dto.setId(integration.getId());
        dto.setUserId(integration.getUserId());
        dto.setProvider(integration.getProvider());
        dto.setCalendarId(integration.getCalendarId());
        dto.setEnabled(integration.getEnabled());
        dto.setSyncEnabled(integration.getSyncEnabled());
        dto.setSyncDirection(integration.getSyncDirection());
        dto.setLastSyncAt(integration.getLastSyncAt());
        dto.setSyncStatus(integration.getSyncStatus());
        dto.setCreatedAt(integration.getCreatedAt());
        return dto;
    }
    
    /**
     * Convertir un événement en DTO
     */
    private CalendarEventDto convertEventToDto(CalendarEvent event) {
        CalendarEventDto dto = new CalendarEventDto();
        dto.setId(event.getId());
        dto.setTitle(event.getTitle());
        dto.setDescription(event.getDescription());
        dto.setStartTime(event.getStartTime());
        dto.setEndTime(event.getEndTime());
        dto.setLocation(event.getLocation());
        dto.setIsAllDay(event.getIsAllDay());
        dto.setSyncStatus(event.getSyncStatus());
        return dto;
    }
    
    /**
     * Convertir un DTO en entité événement
     */
    private CalendarEvent convertDtoToEvent(CalendarEventDto dto) {
        CalendarEvent event = new CalendarEvent();
        event.setTitle(dto.getTitle());
        event.setDescription(dto.getDescription());
        event.setStartTime(dto.getStartTime());
        event.setEndTime(dto.getEndTime());
        event.setLocation(dto.getLocation());
        event.setIsAllDay(dto.getIsAllDay());
        event.setAttendees(dto.getAttendees());
        return event;
    }
}