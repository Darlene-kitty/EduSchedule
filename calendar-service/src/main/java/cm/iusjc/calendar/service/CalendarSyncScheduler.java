package cm.iusjc.calendar.service;

import cm.iusjc.calendar.entity.CalendarIntegration;
import cm.iusjc.calendar.repository.CalendarIntegrationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CalendarSyncScheduler {
    
    private final CalendarIntegrationRepository integrationRepository;
    private final CalendarIntegrationService calendarIntegrationService;
    
    @Value("${calendar.sync.interval:300000}") // 5 minutes par défaut
    private long syncInterval;
    
    /**
     * Synchronisation automatique toutes les 5 minutes
     */
    @Scheduled(fixedDelayString = "${calendar.sync.interval:300000}")
    public void performScheduledSync() {
        log.info("Démarrage de la synchronisation automatique des calendriers");
        
        try {
            // Récupérer les intégrations qui ont besoin d'être synchronisées
            LocalDateTime syncThreshold = LocalDateTime.now().minusSeconds(syncInterval / 1000);
            List<CalendarIntegration> integrationsToSync = integrationRepository
                .findIntegrationsNeedingSync(syncThreshold);
            
            log.info("Nombre d'intégrations à synchroniser: {}", integrationsToSync.size());
            
            for (CalendarIntegration integration : integrationsToSync) {
                try {
                    log.debug("Synchronisation de l'intégration {} pour l'utilisateur {}", 
                        integration.getId(), integration.getUserId());
                    
                    calendarIntegrationService.syncCalendarEvents(integration.getUserId());
                    
                } catch (Exception e) {
                    log.error("Erreur lors de la synchronisation de l'intégration {}: {}", 
                        integration.getId(), e.getMessage());
                }
            }
            
            log.info("Synchronisation automatique terminée");
            
        } catch (Exception e) {
            log.error("Erreur lors de la synchronisation automatique: {}", e.getMessage());
        }
    }
    
    /**
     * Nettoyage des tokens expirés toutes les heures
     */
    @Scheduled(fixedRate = 3600000) // 1 heure
    public void cleanupExpiredTokens() {
        log.info("Nettoyage des tokens expirés");
        
        try {
            LocalDateTime now = LocalDateTime.now();
            List<CalendarIntegration> expiredIntegrations = integrationRepository
                .findIntegrationsWithExpiringTokens(now);
            
            log.info("Nombre d'intégrations avec tokens expirés: {}", expiredIntegrations.size());
            
            for (CalendarIntegration integration : expiredIntegrations) {
                try {
                    // Tenter de renouveler le token
                    if (integration.getRefreshToken() != null) {
                        log.debug("Tentative de renouvellement du token pour l'intégration {}", 
                            integration.getId());
                        
                        // Le renouvellement sera géré par les services spécifiques lors de la prochaine sync
                        integration.setSyncStatus(CalendarIntegration.SyncStatus.PENDING);
                        integrationRepository.save(integration);
                    } else {
                        // Désactiver l'intégration si pas de refresh token
                        log.warn("Désactivation de l'intégration {} - pas de refresh token", 
                            integration.getId());
                        integration.setEnabled(false);
                        integration.setSyncStatus(CalendarIntegration.SyncStatus.ERROR);
                        integration.setSyncError("Token expiré sans refresh token");
                        integrationRepository.save(integration);
                    }
                    
                } catch (Exception e) {
                    log.error("Erreur lors du traitement du token expiré pour l'intégration {}: {}", 
                        integration.getId(), e.getMessage());
                }
            }
            
        } catch (Exception e) {
            log.error("Erreur lors du nettoyage des tokens expirés: {}", e.getMessage());
        }
    }
    
    /**
     * Statistiques de synchronisation toutes les 6 heures
     */
    @Scheduled(fixedRate = 21600000) // 6 heures
    public void logSyncStatistics() {
        try {
            long totalIntegrations = integrationRepository.count();
            long activeIntegrations = integrationRepository.findByEnabledAndSyncEnabled(true, true).size();
            long errorIntegrations = integrationRepository.findBySyncStatus(CalendarIntegration.SyncStatus.ERROR).size();
            
            log.info("Statistiques de synchronisation - Total: {}, Actives: {}, Erreurs: {}", 
                totalIntegrations, activeIntegrations, errorIntegrations);
                
        } catch (Exception e) {
            log.error("Erreur lors de la génération des statistiques: {}", e.getMessage());
        }
    }
}