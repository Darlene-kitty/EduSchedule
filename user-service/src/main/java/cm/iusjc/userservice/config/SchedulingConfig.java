package cm.iusjc.userservice.config;

import cm.iusjc.userservice.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class SchedulingConfig {
    
    private final PasswordResetService passwordResetService;
    
    // Nettoyer les tokens expirés tous les jours à 2h du matin
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupExpiredTokens() {
        log.info("Starting cleanup of expired password reset tokens");
        passwordResetService.cleanupExpiredTokens();
        log.info("Cleanup of expired password reset tokens completed");
    }
}