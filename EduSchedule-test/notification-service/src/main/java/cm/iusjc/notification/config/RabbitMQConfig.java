package cm.iusjc.notification.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    
    public static final String EXCHANGE = "notification-exchange";
    public static final String QUEUE = "notification-queue";
    public static final String ROUTING_KEY = "notification.#";
    
    // Configuration pour les schedules
    public static final String SCHEDULE_EXCHANGE = "schedule-exchange";
    public static final String SCHEDULE_QUEUE = "schedule-notifications";
    public static final String SCHEDULE_ROUTING_KEY = "schedule.#";
    
    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(EXCHANGE);
    }
    
    @Bean
    public Queue notificationQueue() {
        return new Queue(QUEUE, true);
    }
    
    @Bean
    public Binding notificationBinding(@Qualifier("notificationQueue") Queue notificationQueue, 
                                     @Qualifier("notificationExchange") TopicExchange notificationExchange) {
        return BindingBuilder.bind(notificationQueue)
                .to(notificationExchange)
                .with(ROUTING_KEY);
    }
    
    // Configuration pour les schedules
    @Bean
    public TopicExchange scheduleExchange() {
        return new TopicExchange(SCHEDULE_EXCHANGE);
    }
    
    @Bean
    public Queue scheduleQueue() {
        return new Queue(SCHEDULE_QUEUE, true);
    }
    
    @Bean
    public Binding scheduleBinding(@Qualifier("scheduleQueue") Queue scheduleQueue, 
                                 @Qualifier("scheduleExchange") TopicExchange scheduleExchange) {
        return BindingBuilder.bind(scheduleQueue)
                .to(scheduleExchange)
                .with(SCHEDULE_ROUTING_KEY);
    }
    
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
