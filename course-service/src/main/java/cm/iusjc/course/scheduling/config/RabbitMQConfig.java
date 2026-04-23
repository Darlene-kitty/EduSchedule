package cm.iusjc.course.scheduling.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String USER_EXCHANGE              = "user-exchange";
    public static final String AVAILABILITY_QUEUE         = "availability-changed-queue";
    public static final String AVAILABILITY_ROUTING_KEY   = "availability.changed";

    // Exchange partagé avec le notification-service pour les changements d'emploi du temps
    public static final String SCHEDULE_EXCHANGE          = "schedule-exchange";
    public static final String SCHEDULE_CHANGE_ROUTING_KEY = "schedule.changed";

    @Bean
    public TopicExchange userExchange() {
        return new TopicExchange(USER_EXCHANGE);
    }

    @Bean
    public TopicExchange scheduleExchange() {
        return new TopicExchange(SCHEDULE_EXCHANGE);
    }

    @Bean
    public Queue availabilityChangedQueue() {
        return QueueBuilder.durable(AVAILABILITY_QUEUE).build();
    }

    @Bean
    public Binding availabilityBinding(Queue availabilityChangedQueue, TopicExchange userExchange) {
        return BindingBuilder.bind(availabilityChangedQueue)
                .to(userExchange)
                .with(AVAILABILITY_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new Jackson2JsonMessageConverter(mapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate t = new RabbitTemplate(connectionFactory);
        t.setMessageConverter(jsonMessageConverter());
        return t;
    }
}
