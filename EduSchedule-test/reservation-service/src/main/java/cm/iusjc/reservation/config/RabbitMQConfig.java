package cm.iusjc.reservation.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Queues consumed by this service
    @Bean
    public Queue scheduleCreatedQueue() {
        return new Queue("schedule-created", true);
    }

    @Bean
    public Queue scheduleUpdatedQueue() {
        return new Queue("schedule-updated", true);
    }

    @Bean
    public Queue scheduleDeletedQueue() {
        return new Queue("schedule-deleted", true);
    }

    @Bean
    public Queue reservationUpdatedQueue() {
        return new Queue("reservation-updated", true);
    }

    // Queues published to by this service
    @Bean
    public Queue scheduleSyncQueue() {
        return new Queue("schedule-sync", true);
    }

    @Bean
    public Queue reservationNotificationsQueue() {
        return new Queue("reservation-notifications", true);
    }

    @Bean
    public Queue syncErrorsQueue() {
        return new Queue("sync-errors", true);
    }
}
