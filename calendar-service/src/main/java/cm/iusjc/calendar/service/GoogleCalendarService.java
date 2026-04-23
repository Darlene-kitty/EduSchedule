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
public class GoogleCalendarService {
    
    private final CalendarEventRepository eventRepository;
    private final RestTemplate restTemplate;
    
    @Value("${calendar.google.api.url:https://www.googleapis.com/calendar/v3}")
    private String googleApiUrl;
    
    /**
     * Synchroniser les événements avec Google Calendar
     */
    public void syncEvents(CalendarIntegration integration) {
        log.info("Synchronisation avec Google Calendar pour l'utilisateur: {}", integration.getUserId());
        
        try {
            // Vérifier et renouveler le token si nécessaire
            if (isTokenExpired(integration)) {
                refreshAccessToken(integration);
            }
            
            // Récupérer les événements depuis Google Calendar
            List<Map<String, Object>> googleEvents = fetchGoogleEvents(integration);
            
            // Synchroniser chaque événement
            for (Map<String, Object> googleEvent : googleEvents) {
                syncGoogleEvent(integration, googleEvent);
            }
            
            log.info("Synchronisation Google Calendar terminée pour l'utilisateur: {}", integration.getUserId());
            
        } catch (Exception e) {
            log.error("Erreur lors de la synchronisation Google Calendar: {}", e.getMessage());
            throw new RuntimeException("Erreur de synchronisation Google Calendar", e);
        }
    }
    
    /**
     * Créer un événement dans Google Calendar
     */
    public String createEvent(CalendarIntegration integration, CalendarEvent event) {
        log.info("Création d'un événement dans Google Calendar: {}", event.getTitle());
        
        try {
            if (isTokenExpired(integration)) {
                refreshAccessToken(integration);
            }
            
            Map<String, Object> eventData = buildGoogleEventData(event);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(integration.getAccessToken());
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(eventData, headers);
            
            String url = String.format("%s/calendars/%s/events", googleApiUrl, integration.getCalendarId());
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return (String) response.getBody().get("id");
            }
            
            throw new RuntimeException("Échec de création de l'événement Google Calendar");
            
        } catch (Exception e) {
            log.error("Erreur lors de la création de l'événement Google Calendar: {}", e.getMessage());
            throw new RuntimeException("Erreur de création Google Calendar", e);
        }
    }
    
    /**
     * Effectuer la synchronisation initiale
     */
    public void performInitialSync(CalendarIntegration integration) {
        log.info("Synchronisation initiale Google Calendar pour l'utilisateur: {}", integration.getUserId());
        
        // Récupérer les événements des 30 derniers jours et 90 prochains jours
        LocalDateTime startTime = LocalDateTime.now().minusDays(30);
        LocalDateTime endTime = LocalDateTime.now().plusDays(90);
        
        try {
            List<Map<String, Object>> googleEvents = fetchGoogleEventsInRange(integration, startTime, endTime);
            
            for (Map<String, Object> googleEvent : googleEvents) {
                syncGoogleEvent(integration, googleEvent);
            }
            
            integration.setSyncStatus(CalendarIntegration.SyncStatus.SUCCESS);
            integration.setLastSyncAt(LocalDateTime.now());
            
        } catch (Exception e) {
            log.error("Erreur lors de la synchronisation initiale Google Calendar: {}", e.getMessage());
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
        log.info("Renouvellement du token Google Calendar pour l'utilisateur: {}", integration.getUserId());
        
        try {
            Map<String, String> refreshData = new HashMap<>();
            refreshData.put("client_id", getGoogleClientId());
            refreshData.put("client_secret", getGoogleClientSecret());
            refreshData.put("refresh_token", integration.getRefreshToken());
            refreshData.put("grant_type", "refresh_token");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            HttpEntity<Map<String, String>> request = new HttpEntity<>(refreshData, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://oauth2.googleapis.com/token", request, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> tokenData = response.getBody();
                integration.setAccessToken((String) tokenData.get("access_token"));
                
                if (tokenData.containsKey("expires_in")) {
                    int expiresIn = (Integer) tokenData.get("expires_in");
                    integration.setTokenExpiresAt(LocalDateTime.now().plusSeconds(expiresIn));
                }
            }
            
        } catch (Exception e) {
            log.error("Erreur lors du renouvellement du token Google: {}", e.getMessage());
            throw new RuntimeException("Erreur de renouvellement de token", e);
        }
    }
    
    /**
     * Récupérer les événements depuis Google Calendar
     */
    private List<Map<String, Object>> fetchGoogleEvents(CalendarIntegration integration) {
        return fetchGoogleEventsInRange(integration, 
            LocalDateTime.now().minusDays(7), 
            LocalDateTime.now().plusDays(30));
    }
    
    /**
     * Récupérer les événements Google Calendar dans une plage de dates
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> fetchGoogleEventsInRange(
            CalendarIntegration integration, LocalDateTime startTime, LocalDateTime endTime) {
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(integration.getAccessToken());
            
            String timeMin = startTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z";
            String timeMax = endTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z";
            
            String url = String.format("%s/calendars/%s/events?timeMin=%s&timeMax=%s&singleEvents=true&orderBy=startTime",
                googleApiUrl, integration.getCalendarId(), timeMin, timeMax);
            
            HttpEntity<?> request = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return (List<Map<String, Object>>) response.getBody().get("items");
            }
            
            return List.of();
            
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des événements Google: {}", e.getMessage());
            throw new RuntimeException("Erreur de récupération des événements", e);
        }
    }
    
    /**
     * Synchroniser un événement Google
     */
    private void syncGoogleEvent(CalendarIntegration integration, Map<String, Object> googleEvent) {
        try {
            String googleEventId = (String) googleEvent.get("id");
            
            // Vérifier si l'événement existe déjà
            CalendarEvent existingEvent = eventRepository
                .findByIntegrationAndExternalEventId(integration, googleEventId)
                .orElse(null);
            
            if (existingEvent == null) {
                // Créer un nouvel événement
                CalendarEvent newEvent = createEventFromGoogleData(integration, googleEvent);
                eventRepository.save(newEvent);
                log.debug("Nouvel événement créé depuis Google Calendar: {}", newEvent.getTitle());
            } else {
                // Mettre à jour l'événement existant
                updateEventFromGoogleData(existingEvent, googleEvent);
                eventRepository.save(existingEvent);
                log.debug("Événement mis à jour depuis Google Calendar: {}", existingEvent.getTitle());
            }
            
        } catch (Exception e) {
            log.error("Erreur lors de la synchronisation de l'événement Google: {}", e.getMessage());
        }
    }
    
    /**
     * Créer un événement depuis les données Google
     */
    @SuppressWarnings("unchecked")
    private CalendarEvent createEventFromGoogleData(CalendarIntegration integration, Map<String, Object> googleEvent) {
        CalendarEvent event = new CalendarEvent();
        event.setIntegration(integration);
        event.setExternalEventId((String) googleEvent.get("id"));
        event.setTitle((String) googleEvent.get("summary"));
        event.setDescription((String) googleEvent.get("description"));
        
        // Gérer les dates/heures
        Map<String, Object> start = (Map<String, Object>) googleEvent.get("start");
        Map<String, Object> end = (Map<String, Object>) googleEvent.get("end");
        
        if (start != null && end != null) {
            event.setStartTime(parseGoogleDateTime(start));
            event.setEndTime(parseGoogleDateTime(end));
            event.setIsAllDay(start.containsKey("date"));
        }
        
        event.setLocation((String) googleEvent.get("location"));
        event.setSyncStatus(CalendarEvent.EventSyncStatus.SYNCED);
        event.setLastSyncedAt(LocalDateTime.now());
        
        return event;
    }
    
    /**
     * Mettre à jour un événement depuis les données Google
     */
    @SuppressWarnings("unchecked")
    private void updateEventFromGoogleData(CalendarEvent event, Map<String, Object> googleEvent) {
        event.setTitle((String) googleEvent.get("summary"));
        event.setDescription((String) googleEvent.get("description"));
        
        Map<String, Object> start = (Map<String, Object>) googleEvent.get("start");
        Map<String, Object> end = (Map<String, Object>) googleEvent.get("end");
        
        if (start != null && end != null) {
            event.setStartTime(parseGoogleDateTime(start));
            event.setEndTime(parseGoogleDateTime(end));
            event.setIsAllDay(start.containsKey("date"));
        }
        
        event.setLocation((String) googleEvent.get("location"));
        event.setSyncStatus(CalendarEvent.EventSyncStatus.SYNCED);
        event.setLastSyncedAt(LocalDateTime.now());
    }
    
    /**
     * Construire les données d'événement pour Google Calendar
     */
    private Map<String, Object> buildGoogleEventData(CalendarEvent event) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("summary", event.getTitle());
        eventData.put("description", event.getDescription());
        eventData.put("location", event.getLocation());
        
        Map<String, Object> start = new HashMap<>();
        Map<String, Object> end = new HashMap<>();
        
        if (event.getIsAllDay()) {
            start.put("date", event.getStartTime().toLocalDate().toString());
            end.put("date", event.getEndTime().toLocalDate().toString());
        } else {
            start.put("dateTime", event.getStartTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            end.put("dateTime", event.getEndTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        
        eventData.put("start", start);
        eventData.put("end", end);
        
        return eventData;
    }
    
    /**
     * Parser une date/heure Google Calendar
     */
    @SuppressWarnings("unchecked")
    private LocalDateTime parseGoogleDateTime(Map<String, Object> dateTimeData) {
        if (dateTimeData.containsKey("dateTime")) {
            String dateTimeStr = (String) dateTimeData.get("dateTime");
            return LocalDateTime.parse(dateTimeStr.substring(0, 19));
        } else if (dateTimeData.containsKey("date")) {
            String dateStr = (String) dateTimeData.get("date");
            return LocalDateTime.parse(dateStr + "T00:00:00");
        }
        return LocalDateTime.now();
    }
    
    private String getGoogleClientId() {
        // À récupérer depuis la configuration
        return System.getenv("GOOGLE_CLIENT_ID");
    }
    
    private String getGoogleClientSecret() {
        // À récupérer depuis la configuration
        return System.getenv("GOOGLE_CLIENT_SECRET");
    }
}