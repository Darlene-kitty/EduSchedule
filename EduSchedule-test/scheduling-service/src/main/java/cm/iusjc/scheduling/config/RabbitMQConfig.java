package cm.iusjc.scheduling.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    
    public static final String EXCHANGE = "schedule-exchange";
    public static final String QUEUE = "schedule-notifications";
    public static final String ROUTING_KEY = "schedule.#";
    
    @Bean
    public TopicExchange scheduleExchange() {
        return new TopicExchange(EXCHANGE);
    }
    
    @Bean
    public Queue scheduleQueue() {
        return new Queue(QUEUE, true);
    }
    
    @Bean
    public Binding binding(Queue scheduleQueue, TopicExchange scheduleExchange) {
        return BindingBuilder.bind(scheduleQueue)
                .to(scheduleExchange)
                .with(ROUTING_KEY);
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
