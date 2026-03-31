package cm.iusjc.reservation.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    /** Exchange partagé avec le notification-service */
    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange("notification-exchange", true, false);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    // Queues consumed by this service
    @Bean public Queue scheduleCreatedQueue()       { return new Queue("schedule-created", true); }
    @Bean public Queue scheduleUpdatedQueue()       { return new Queue("schedule-updated", true); }
    @Bean public Queue scheduleDeletedQueue()       { return new Queue("schedule-deleted", true); }
    @Bean public Queue reservationUpdatedQueue()    { return new Queue("reservation-updated", true); }

    // Queues published to by this service
    @Bean public Queue scheduleSyncQueue()              { return new Queue("schedule-sync", true); }
    @Bean public Queue reservationNotificationsQueue()  { return new Queue("reservation-notifications", true); }
    @Bean public Queue syncErrorsQueue()                { return new Queue("sync-errors", true); }
}
