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
    
    // Configuration pour les événements utilisateur
    public static final String USER_EXCHANGE = "user-exchange";
    public static final String USER_CREATED_QUEUE = "user.created.queue";
    public static final String USER_UPDATED_QUEUE = "user.updated.queue";
    public static final String USER_ACTIVATED_QUEUE = "user.activated.queue";
    public static final String USER_CREATED_ROUTING_KEY = "user.created";
    public static final String USER_UPDATED_ROUTING_KEY = "user.updated";
    public static final String USER_ACTIVATED_ROUTING_KEY = "user.activated";
    
    // Nouvelles queues pour les fonctionnalités étendues
    public static final String RESERVATION_NOTIFICATIONS_QUEUE = "reservation-notifications";
    public static final String REMINDER_NOTIFICATIONS_QUEUE = "reminder-notifications";
    public static final String CALENDAR_SYNC_QUEUE = "calendar-sync";
    
    // Nouveaux exchanges
    public static final String CALENDAR_EXCHANGE = "calendar-exchange";
    
    // Nouveaux routing keys
    public static final String RESERVATION_ROUTING_KEY = "reservation.#";
    public static final String REMINDER_ROUTING_KEY = "reminder.#";
    public static final String CALENDAR_SYNC_ROUTING_KEY = "calendar.sync.#";
    
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
    
    // Configuration pour les événements utilisateur
    @Bean
    public TopicExchange userExchange() {
        return new TopicExchange(USER_EXCHANGE);
    }
    
    @Bean
    public Queue userCreatedQueue() {
        return new Queue(USER_CREATED_QUEUE, true);
    }
    
    @Bean
    public Queue userUpdatedQueue() {
        return new Queue(USER_UPDATED_QUEUE, true);
    }
    
    @Bean
    public Queue userActivatedQueue() {
        return new Queue(USER_ACTIVATED_QUEUE, true);
    }
    
    @Bean
    public Binding userCreatedBinding(@Qualifier("userCreatedQueue") Queue userCreatedQueue, 
                                    @Qualifier("userExchange") TopicExchange userExchange) {
        return BindingBuilder.bind(userCreatedQueue)
                .to(userExchange)
                .with(USER_CREATED_ROUTING_KEY);
    }
    
    @Bean
    public Binding userUpdatedBinding(@Qualifier("userUpdatedQueue") Queue userUpdatedQueue, 
                                    @Qualifier("userExchange") TopicExchange userExchange) {
        return BindingBuilder.bind(userUpdatedQueue)
                .to(userExchange)
                .with(USER_UPDATED_ROUTING_KEY);
    }
    
    @Bean
    public Binding userActivatedBinding(@Qualifier("userActivatedQueue") Queue userActivatedQueue, 
                                      @Qualifier("userExchange") TopicExchange userExchange) {
        return BindingBuilder.bind(userActivatedQueue)
                .to(userExchange)
                .with(USER_ACTIVATED_ROUTING_KEY);
    }
    
    // Nouvelles configurations pour les réservations
    @Bean
    public Queue reservationNotificationsQueue() {
        return new Queue(RESERVATION_NOTIFICATIONS_QUEUE, true);
    }
    
    @Bean
    public Binding reservationNotificationsBinding(@Qualifier("reservationNotificationsQueue") Queue reservationQueue, 
                                                  @Qualifier("notificationExchange") TopicExchange notificationExchange) {
        return BindingBuilder.bind(reservationQueue)
                .to(notificationExchange)
                .with(RESERVATION_ROUTING_KEY);
    }
    
    // Configuration pour les rappels
    @Bean
    public Queue reminderNotificationsQueue() {
        return new Queue(REMINDER_NOTIFICATIONS_QUEUE, true);
    }
    
    @Bean
    public Binding reminderNotificationsBinding(@Qualifier("reminderNotificationsQueue") Queue reminderQueue, 
                                               @Qualifier("notificationExchange") TopicExchange notificationExchange) {
        return BindingBuilder.bind(reminderQueue)
                .to(notificationExchange)
                .with(REMINDER_ROUTING_KEY);
    }
    
    // Configuration pour la synchronisation calendrier
    @Bean
    public TopicExchange calendarExchange() {
        return new TopicExchange(CALENDAR_EXCHANGE);
    }
    
    @Bean
    public Queue calendarSyncQueue() {
        return new Queue(CALENDAR_SYNC_QUEUE, true);
    }
    
    @Bean
    public Binding calendarSyncBinding(@Qualifier("calendarSyncQueue") Queue calendarQueue, 
                                      @Qualifier("calendarExchange") TopicExchange calendarExchange) {
        return BindingBuilder.bind(calendarQueue)
                .to(calendarExchange)
                .with(CALENDAR_SYNC_ROUTING_KEY);
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