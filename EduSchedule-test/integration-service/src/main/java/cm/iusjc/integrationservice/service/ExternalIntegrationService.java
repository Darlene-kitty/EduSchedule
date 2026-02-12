package cm.iusjc.integrationservice.service;

import cm.iusjc.integrationservice.dto.*;
import cm.iusjc.integrationservice.entity.IntegrationConfig;
import cm.iusjc.integrationservice.repository.IntegrationConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class ExternalIntegrationService {

    @Autowired
    private IntegrationConfigRepository configRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private OutlookIntegrationService outlookService;

    @Autowired
    private MoodleIntegrationService moodleService;

    @Autowired
    private TeamsIntegrationService teamsService;

    @Autowired
    private GoogleCalendarService googleCalendarService;

    public IntegrationResponse syncWithOutlook(OutlookSyncRequest request) {
        try {
            IntegrationConfig config = getIntegrationConfig("OUTLOOK");
            if (config == null || !config.isEnabled()) {
                return IntegrationResponse.builder()
                        .success(false)
                        .message("Intégration Outlook non configurée ou désactivée")
                        .build();
            }

            // Authentification OAuth2
            String accessToken = outlookService.getAccessToken(config);
            
            // Synchronisation selon la direction configurée
            SyncResult result = new SyncResult();
            
            if ("BIDIRECTIONAL".equals(config.getSyncDirection()) || 
                "TO_OUTLOOK".equals(config.getSyncDirection())) {
                
                // Exporter les événements vers Outlook
                List<OutlookEvent> exportedEvents = outlookService.exportEvents(
                        request.getEvents(), accessToken);
                result.setExportedCount(exportedEvents.size());
            }

            if ("BIDIRECTIONAL".equals(config.getSyncDirection()) || 
                "FROM_OUTLOOK".equals(config.getSyncDirection())) {
                
                // Importer les événements depuis Outlook
                List<ScheduleEvent> importedEvents = outlookService.importEvents(
                        request.getDateRange(), accessToken);
                result.setImportedCount(importedEvents.size());
            }

            // Enregistrer le résultat de synchronisation
            saveSyncResult("OUTLOOK", result);

            return IntegrationResponse.builder()
                    .success(true)
                    .integration("OUTLOOK")
                    .syncResult(result)
                    .message("Synchronisation Outlook réussie")
                    .timestamp(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            return IntegrationResponse.builder()
                    .success(false)
                    .message("Erreur lors de la synchronisation Outlook: " + e.getMessage())
                    .build();
        }
    }

    public IntegrationResponse syncWithMoodle(MoodleSyncRequest request) {
        try {
            IntegrationConfig config = getIntegrationConfig("MOODLE");
            if (config == null || !config.isEnabled()) {
                return IntegrationResponse.builder()
                        .success(false)
                        .message("Intégration Moodle non configurée")
                        .build();
            }

            // Authentification avec token API Moodle
            String apiToken = config.getApiToken();
            
            SyncResult result = new SyncResult();

            // Importer les cours depuis Moodle
            if (request.isSyncCourses()) {
                List<Course> courses = moodleService.importCourses(apiToken);
                result.setCoursesImported(courses.size());
                
                // Créer les emplois du temps correspondants
                List<Schedule> schedules = moodleService.createSchedulesFromCourses(courses);
                result.setSchedulesCreated(schedules.size());
            }

            // Importer les étudiants
            if (request.isSyncStudents()) {
                List<Student> students = moodleService.importStudents(apiToken);
                result.setStudentsImported(students.size());
            }

            // Importer les enseignants
            if (request.isSyncTeachers()) {
                List<Teacher> teachers = moodleService.importTeachers(apiToken);
                result.setTeachersImported(teachers.size());
            }

            // Synchroniser les notes et présences
            if (request.isSyncGrades()) {
                moodleService.syncGrades(apiToken, request.getCourseIds());
            }

            saveSyncResult("MOODLE", result);

            return IntegrationResponse.builder()
                    .success(true)
                    .integration("MOODLE")
                    .syncResult(result)
                    .message("Synchronisation Moodle réussie")
                    .timestamp(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            return IntegrationResponse.builder()
                    .success(false)
                    .message("Erreur lors de la synchronisation Moodle: " + e.getMessage())
                    .build();
        }
    }

    public IntegrationResponse createTeamsMeeting(TeamsMeetingRequest request) {
        try {
            IntegrationConfig config = getIntegrationConfig("TEAMS");
            if (config == null || !config.isEnabled()) {
                return IntegrationResponse.builder()
                        .success(false)
                        .message("Intégration Teams non configurée")
                        .build();
            }

            // Créer la réunion Teams
            TeamsMeeting meeting = teamsService.createMeeting(request, config);

            // Mettre à jour l'emploi du temps avec le lien de la réunion
            updateScheduleWithMeetingLink(request.getScheduleId(), meeting.getJoinUrl());

            // Envoyer les invitations
            if (request.isSendInvitations()) {
                teamsService.sendInvitations(meeting, request.getAttendees());
            }

            return IntegrationResponse.builder()
                    .success(true)
                    .integration("TEAMS")
                    .data(Map.of(
                            "meetingId", meeting.getId(),
                            "joinUrl", meeting.getJoinUrl(),
                            "dialInNumber", meeting.getDialInNumber()
                    ))
                    .message("Réunion Teams créée avec succès")
                    .timestamp(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            return IntegrationResponse.builder()
                    .success(false)
                    .message("Erreur lors de la création de la réunion Teams: " + e.getMessage())
                    .build();
        }
    }

    public IntegrationResponse syncWithGoogleCalendar(GoogleCalendarSyncRequest request) {
        try {
            IntegrationConfig config = getIntegrationConfig("GOOGLE_CALENDAR");
            if (config == null || !config.isEnabled()) {
                return IntegrationResponse.builder()
                        .success(false)
                        .message("Intégration Google Calendar non configurée")
                        .build();
            }

            // Authentification OAuth2 Google
            String accessToken = googleCalendarService.getAccessToken(config);
            
            SyncResult result = new SyncResult();

            // Export vers Google Calendar
            if ("BIDIRECTIONAL".equals(config.getSyncDirection()) || 
                "TO_GOOGLE".equals(config.getSyncDirection())) {
                
                List<GoogleCalendarEvent> exportedEvents = googleCalendarService.exportEvents(
                        request.getEvents(), accessToken);
                result.setExportedCount(exportedEvents.size());
            }

            // Import depuis Google Calendar
            if ("BIDIRECTIONAL".equals(config.getSyncDirection()) || 
                "FROM_GOOGLE".equals(config.getSyncDirection())) {
                
                List<ScheduleEvent> importedEvents = googleCalendarService.importEvents(
                        request.getCalendarId(), request.getDateRange(), accessToken);
                result.setImportedCount(importedEvents.size());
            }

            saveSyncResult("GOOGLE_CALENDAR", result);

            return IntegrationResponse.builder()
                    .success(true)
                    .integration("GOOGLE_CALENDAR")
                    .syncResult(result)
                    .message("Synchronisation Google Calendar réussie")
                    .timestamp(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            return IntegrationResponse.builder()
                    .success(false)
                    .message("Erreur lors de la synchronisation Google Calendar: " + e.getMessage())
                    .build();
        }
    }

    public IntegrationResponse syncWithActiveDirectory(ADSyncRequest request) {
        try {
            IntegrationConfig config = getIntegrationConfig("ACTIVE_DIRECTORY");
            if (config == null || !config.isEnabled()) {
                return IntegrationResponse.builder()
                        .success(false)
                        .message("Intégration Active Directory non configurée")
                        .build();
            }

            // Connexion LDAP
            LdapConnection connection = createLdapConnection(config);
            
            SyncResult result = new SyncResult();

            // Synchroniser les utilisateurs
            if (request.isSyncUsers()) {
                List<User> users = syncUsersFromAD(connection, request.getOuFilter());
                result.setUsersImported(users.size());
            }

            // Synchroniser les groupes
            if (request.isSyncGroups()) {
                List<Group> groups = syncGroupsFromAD(connection, request.getGroupFilter());
                result.setGroupsImported(groups.size());
            }

            // Configurer l'authentification SSO
            if (request.isConfigureSSO()) {
                configureSSOSettings(config);
            }

            connection.close();
            saveSyncResult("ACTIVE_DIRECTORY", result);

            return IntegrationResponse.builder()
                    .success(true)
                    .integration("ACTIVE_DIRECTORY")
                    .syncResult(result)
                    .message("Synchronisation Active Directory réussie")
                    .timestamp(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            return IntegrationResponse.builder()
                    .success(false)
                    .message("Erreur lors de la synchronisation AD: " + e.getMessage())
                    .build();
        }
    }

    public IntegrationStatusResponse getIntegrationStatus() {
        try {
            List<IntegrationConfig> configs = configRepository.findAll();
            
            List<IntegrationStatus> statuses = configs.stream()
                    .map(this::checkIntegrationStatus)
                    .collect(java.util.stream.Collectors.toList());

            return IntegrationStatusResponse.builder()
                    .success(true)
                    .integrations(statuses)
                    .totalIntegrations(statuses.size())
                    .activeIntegrations(statuses.stream()
                            .mapToInt(s -> s.isActive() ? 1 : 0).sum())
                    .lastUpdated(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            return IntegrationStatusResponse.builder()
                    .success(false)
                    .message("Erreur lors de la vérification du statut: " + e.getMessage())
                    .build();
        }
    }

    public CompletableFuture<IntegrationResponse> scheduleAutoSync(String integrationType) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                IntegrationConfig config = getIntegrationConfig(integrationType);
                if (config == null || !config.isAutoSyncEnabled()) {
                    return IntegrationResponse.builder()
                            .success(false)
                            .message("Synchronisation automatique non configurée pour " + integrationType)
                            .build();
                }

                // Exécuter la synchronisation selon le type
                switch (integrationType) {
                    case "OUTLOOK":
                        return syncWithOutlook(createDefaultOutlookSyncRequest());
                    case "MOODLE":
                        return syncWithMoodle(createDefaultMoodleSyncRequest());
                    case "GOOGLE_CALENDAR":
                        return syncWithGoogleCalendar(createDefaultGoogleSyncRequest());
                    default:
                        return IntegrationResponse.builder()
                                .success(false)
                                .message("Type d'intégration non supporté: " + integrationType)
                                .build();
                }

            } catch (Exception e) {
                return IntegrationResponse.builder()
                        .success(false)
                        .message("Erreur lors de la synchronisation automatique: " + e.getMessage())
                        .build();
            }
        });
    }

    private IntegrationConfig getIntegrationConfig(String type) {
        return configRepository.findByType(type).orElse(null);
    }

    private void saveSyncResult(String integrationType, SyncResult result) {
        // Sauvegarder le résultat de synchronisation pour audit
        // Implémentation de l'audit des synchronisations
    }

    private IntegrationStatus checkIntegrationStatus(IntegrationConfig config) {
        boolean isActive = false;
        String lastError = null;
        LocalDateTime lastSync = null;

        try {
            // Tester la connectivité selon le type d'intégration
            switch (config.getType()) {
                case "OUTLOOK":
                    isActive = outlookService.testConnection(config);
                    break;
                case "MOODLE":
                    isActive = moodleService.testConnection(config);
                    break;
                case "TEAMS":
                    isActive = teamsService.testConnection(config);
                    break;
                case "GOOGLE_CALENDAR":
                    isActive = googleCalendarService.testConnection(config);
                    break;
            }

            lastSync = config.getLastSyncDate();

        } catch (Exception e) {
            isActive = false;
            lastError = e.getMessage();
        }

        return IntegrationStatus.builder()
                .type(config.getType())
                .name(config.getName())
                .active(isActive)
                .enabled(config.isEnabled())
                .lastSync(lastSync)
                .lastError(lastError)
                .syncDirection(config.getSyncDirection())
                .autoSync(config.isAutoSyncEnabled())
                .build();
    }

    private void updateScheduleWithMeetingLink(Long scheduleId, String meetingLink) {
        try {
            String url = "http://localhost:8086/api/schedules/" + scheduleId + "/meeting-link";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            Map<String, String> body = Map.of("meetingLink", meetingLink);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
            
            restTemplate.put(url, request);
        } catch (Exception e) {
            // Log l'erreur mais ne pas faire échouer l'intégration
            System.err.println("Erreur lors de la mise à jour du lien de réunion: " + e.getMessage());
        }
    }

    private LdapConnection createLdapConnection(IntegrationConfig config) {
        // Créer une connexion LDAP avec les paramètres de configuration
        return new LdapConnection(
                config.getServerUrl(),
                config.getUsername(),
                config.getPassword()
        );
    }

    private List<User> syncUsersFromAD(LdapConnection connection, String ouFilter) {
        // Synchroniser les utilisateurs depuis Active Directory
        return new ArrayList<>(); // Implémentation simplifiée
    }

    private List<Group> syncGroupsFromAD(LdapConnection connection, String groupFilter) {
        // Synchroniser les groupes depuis Active Directory
        return new ArrayList<>(); // Implémentation simplifiée
    }

    private void configureSSOSettings(IntegrationConfig config) {
        // Configurer les paramètres SSO
        // Implémentation de la configuration SSO
    }

    private OutlookSyncRequest createDefaultOutlookSyncRequest() {
        return OutlookSyncRequest.builder()
                .dateRange(DateRange.builder()
                        .start(LocalDateTime.now())
                        .end(LocalDateTime.now().plusDays(30))
                        .build())
                .build();
    }

    private MoodleSyncRequest createDefaultMoodleSyncRequest() {
        return MoodleSyncRequest.builder()
                .syncCourses(true)
                .syncStudents(true)
                .syncTeachers(true)
                .syncGrades(false)
                .build();
    }

    private GoogleCalendarSyncRequest createDefaultGoogleSyncRequest() {
        return GoogleCalendarSyncRequest.builder()
                .calendarId("primary")
                .dateRange(DateRange.builder()
                        .start(LocalDateTime.now())
                        .end(LocalDateTime.now().plusDays(30))
                        .build())
                .build();
    }
}