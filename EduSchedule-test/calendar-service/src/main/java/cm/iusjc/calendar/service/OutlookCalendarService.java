package cm.iusjc.calendar.service;

import cm.iusjc.calendar.entity.CalendarEvent;
import cm.iusjc.calendar.entity.CalendarIntegration;
import cm.iusjc.calendar.repository.CalendarEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutlookCalendarService {
    
    private final CalendarEventRepository eventRepository;
    private final RestTemplate restTemplate;
    
    @Value("${calendar.outlook.api.url:https://graph.microsoft.com/v1.0}")
    private String outlookApiUrl;
    
    /**
     * Synchroniser les événements avec Outlook Calendar
     */
    public void syncEvents(CalendarIntegration integration) {
        log.info("Synchronisation avec Outlook Calendar pour l'utilisateur: {}", integration.getUserId());
        
        try {
            // Vérifier et renouveler le token si nécessaire
            if (isTokenExpired(integration)) {
                refreshAccessToken(integration);
            }
            
            // Récupérer les événements depuis Outlook Calendar
            List<Map<String, Object>> outlookEvents = fetchOutlookEvents(integration);
            
            // Synchroniser chaque événement
            for (Map<String, Object> outlookEvent : outlookEvents) {
                syncOutlookEvent(integration, outlookEvent);
            }
            
            log.info("Synchronisation Outlook Calendar terminée pour l'utilisateur: {}", integration.getUserId());
            
        } catch (Exception e) {
            log.error("Erreur lors de la synchronisation Outlook Calendar: {}", e.getMessage());
            throw new RuntimeException("Erreur de synchronisation Outlook Calendar", e);
        }
    }
    
    /**
     * Créer un événement dans Outlook Calendar
     */
    public String createEvent(CalendarIntegration integration, CalendarEvent event) {
        log.info("Création d'un événement dans Outlook Calendar: {}", event.getTitle());
        
        try {
            if (isTokenExpired(integration)) {
                refreshAccessToken(integration);
            }
            
            Map<String, Object> eventData = buildOutlookEventData(event);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(integration.getAccessToken());
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(eventData, headers);
            
            String url = String.format("%s/me/calendars/%s/events", outlookApiUrl, integration.getCalendarId());
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            
            if (response.getStatusCode() == HttpStatus.CREATED && response.getBody() != null) {
                return (String) response.getBody().get("id");
            }
            
            throw new RuntimeException("Échec de création de l'événement Outlook Calendar");
            
        } catch (Exception e) {
            log.error("Erreur lors de la création de l'événement Outlook Calendar: {}", e.getMessage());
            throw new RuntimeException("Erreur de création Outlook Calendar", e);
        }
    }
    
    /**
     * Effectuer la synchronisation initiale
     */
    public void performInitialSync(CalendarIntegration integration) {
        log.info("Synchronisation initiale Outlook Calendar pour l'utilisateur: {}", integration.getUserId());
        
        // Récupérer les événements des 30 derniers jours et 90 prochains jours
        LocalDateTime startTime = LocalDateTime.now().minusDays(30);
        LocalDateTime endTime = LocalDateTime.now().plusDays(90);
        
        try {
            List<Map<String, Object>> outlookEvents = fetchOutlookEventsInRange(integration, startTime, endTime);
            
            for (Map<String, Object> outlookEvent : outlookEvents) {
                syncOutlookEvent(integration, outlookEvent);
            }
            
            integration.setSyncStatus(CalendarIntegration.SyncStatus.SUCCESS);
            integration.setLastSyncAt(LocalDateTime.now());
            
        } catch (Exception e) {
            log.error("Erreur lors de la synchronisation initiale Outlook Calendar: {}", e.getMessage());
            throw new RuntimeException("Erreur de synchronisation initiale", e);
        }
    }
    
    /**
     * Vérifier si le token est expiré
     */
    private boolean isTokenExpired(CalendarIntegration integration) {
        return integration.getTokenExpiresAt() != null && 
               integration.getTokenExpiresAt().isBefore(LocalDateTime.now().plusMinutes(5));
    }
    
    /**
     * Renouveler le token d'accès
     */
    private void refreshAccessToken(CalendarIntegration integration) {
        log.info("Renouvellement du token Outlook Calendar pour l'utilisateur: {}", integration.getUserId());
        
        try {
            Map<String, String> refreshData = new HashMap<>();
            refreshData.put("client_id", getOutlookClientId());
            refreshData.put("client_secret", getOutlookClientSecret());
            refreshData.put("refresh_token", integration.getRefreshToken());
            refreshData.put("grant_type", "refresh_token");
            refreshData.put("scope", "https://graph.microsoft.com/Calendars.ReadWrite");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            HttpEntity<Map<String, String>> request = new HttpEntity<>(refreshData, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://login.microsoftonline.com/common/oauth2/v2.0/token", request, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> tokenData = response.getBody();
                integration.setAccessToken((String) tokenData.get("access_token"));
                
                if (tokenData.containsKey("expires_in")) {
                    int expiresIn = (Integer) tokenData.get("expires_in");
                    integration.setTokenExpiresAt(LocalDateTime.now().plusSeconds(expiresIn));
                }
            }
            
        } catch (Exception e) {
            log.error("Erreur lors du renouvellement du token Outlook: {}", e.getMessage());
            throw new RuntimeException("Erreur de renouvellement de token", e);
        }
    }
    
    /**
     * Récupérer les événements depuis Outlook Calendar
     */
    private List<Map<String, Object>> fetchOutlookEvents(CalendarIntegration integration) {
        return fetchOutlookEventsInRange(integration, 
            LocalDateTime.now().minusDays(7), 
            LocalDateTime.now().plusDays(30));
    }
    
    /**
     * Récupérer les événements Outlook Calendar dans une plage de dates
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> fetchOutlookEventsInRange(
            CalendarIntegration integration, LocalDateTime startTime, LocalDateTime endTime) {
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(integration.getAccessToken());
            
            String startTimeStr = startTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            String endTimeStr = endTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            
            String filter = String.format("start/dateTime ge '%s' and end/dateTime le '%s'", startTimeStr, endTimeStr);
            String url = String.format("%s/me/calendars/%s/events?$filter=%s&$orderby=start/dateTime",
                outlookApiUrl, integration.getCalendarId(), filter);
            
            HttpEntity<?> request = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return (List<Map<String, Object>>) response.getBody().get("value");
            }
            
            return List.of();
            
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des événements Outlook: {}", e.getMessage());
            throw new RuntimeException("Erreur de récupération des événements", e);
        }
    }
    
    /**
     * Synchroniser un événement Outlook
     */
    private void syncOutlookEvent(CalendarIntegration integration, Map<String, Object> outlookEvent) {
        try {
            String outlookEventId = (String) outlookEvent.get("id");
            
            // Vérifier si l'événement existe déjà
            CalendarEvent existingEvent = eventRepository
                .findByIntegrationAndExternalEventId(integration, outlookEventId)
                .orElse(null);
            
            if (existingEvent == null) {
                // Créer un nouvel événement
                CalendarEvent newEvent = createEventFromOutlookData(integration, outlookEvent);
                eventRepository.save(newEvent);
                log.debug("Nouvel événement créé depuis Outlook Calendar: {}", newEvent.getTitle());
            } else {
                // Mettre à jour l'événement existant
                updateEventFromOutlookData(existingEvent, outlookEvent);
                eventRepository.save(existingEvent);
                log.debug("Événement mis à jour depuis Outlook Calendar: {}", existingEvent.getTitle());
            }
            
        } catch (Exception e) {
            log.error("Erreur lors de la synchronisation de l'événement Outlook: {}", e.getMessage());
        }
    }
    
    /**
     * Créer un événement depuis les données Outlook
     */
    @SuppressWarnings("unchecked")
    private CalendarEvent createEventFromOutlookData(CalendarIntegration integration, Map<String, Object> outlookEvent) {
        CalendarEvent event = new CalendarEvent();
        event.setIntegration(integration);
        event.setExternalEventId((String) outlookEvent.get("id"));
        event.setTitle((String) outlookEvent.get("subject"));
        
        Map<String, Object> body = (Map<String, Object>) outlookEvent.get("body");
        if (body != null) {
            event.setDescription((String) body.get("content"));
        }
        
        // Gérer les dates/heures
        Map<String, Object> start = (Map<String, Object>) outlookEvent.get("start");
        Map<String, Object> end = (Map<String, Object>) outlookEvent.get("end");
        
        if (start != null && end != null) {
            event.setStartTime(parseOutlookDateTime(start));
            event.setEndTime(parseOutlookDateTime(end));
            event.setIsAllDay((Boolean) outlookEvent.get("isAllDay"));
        }
        
        Map<String, Object> location = (Map<String, Object>) outlookEvent.get("location");
        if (location != null) {
            event.setLocation((String) location.get("displayName"));
        }
        
        event.setSyncStatus(CalendarEvent.EventSyncStatus.SYNCED);
        event.setLastSyncedAt(LocalDateTime.now());
        
        return event;
    }
    
    /**
     * Mettre à jour un événement depuis les données Outlook
     */
    @SuppressWarnings("unchecked")
    private void updateEventFromOutlookData(CalendarEvent event, Map<String, Object> outlookEvent) {
        event.setTitle((String) outlookEvent.get("subject"));
        
        Map<String, Object> body = (Map<String, Object>) outlookEvent.get("body");
        if (body != null) {
            event.setDescription((String) body.get("content"));
        }
        
        Map<String, Object> start = (Map<String, Object>) outlookEvent.get("start");
        Map<String, Object> end = (Map<String, Object>) outlookEvent.get("end");
        
        if (start != null && end != null) {
            event.setStartTime(parseOutlookDateTime(start));
            event.setEndTime(parseOutlookDateTime(end));
            event.setIsAllDay((Boolean) outlookEvent.get("isAllDay"));
        }
        
        Map<String, Object> location = (Map<String, Object>) outlookEvent.get("location");
        if (location != null) {
            event.setLocation((String) location.get("displayName"));
        }
        
        event.setSyncStatus(CalendarEvent.EventSyncStatus.SYNCED);
        event.setLastSyncedAt(LocalDateTime.now());
    }
    
    /**
     * Construire les données d'événement pour Outlook Calendar
     */
    private Map<String, Object> buildOutlookEventData(CalendarEvent event) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("subject", event.getTitle());
        
        Map<String, Object> body = new HashMap<>();
        body.put("contentType", "HTML");
        body.put("content", event.getDescription());
        eventData.put("body", body);
        
        Map<String, Object> start = new HashMap<>();
        Map<String, Object> end = new HashMap<>();
        
        start.put("dateTime", event.getStartTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        start.put("timeZone", "UTC");
        end.put("dateTime", event.getEndTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        end.put("timeZone", "UTC");
        
        eventData.put("start", start);
        eventData.put("end", end);
        eventData.put("isAllDay", event.getIsAllDay());
        
        if (event.getLocation() != null) {
            Map<String, Object> location = new HashMap<>();
            location.put("displayName", event.getLocation());
            eventData.put("location", location);
        }
        
        return eventData;
    }
    
    /**
     * Parser une date/heure Outlook Calendar
     */
    @SuppressWarnings("unchecked")
    private LocalDateTime parseOutlookDateTime(Map<String, Object> dateTimeData) {
        String dateTimeStr = (String) dateTimeData.get("dateTime");
        if (dateTimeStr != null) {
            // Outlook retourne les dates au format ISO avec timezone
            if (dateTimeStr.contains("T")) {
                return LocalDateTime.parse(dateTimeStr.substring(0, 19));
            }
        }
        return LocalDateTime.now();
    }
    
    private String getOutlookClientId() {
        // À récupérer depuis la configuration
        return System.getenv("OUTLOOK_CLIENT_ID");
    }
    
    private String getOutlookClientSecret() {
        // À récupérer depuis la configuration
        return System.getenv("OUTLOOK_CLIENT_SECRET");
    }
}