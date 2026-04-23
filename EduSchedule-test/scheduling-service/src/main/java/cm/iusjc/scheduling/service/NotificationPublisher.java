package cm.iusjc.scheduling.service;

import cm.iusjc.scheduling.entity.Schedule;
import cm.iusjc.scheduling.dto.NotificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationPublisher {
    
    private final RabbitTemplate rabbitTemplate;
    private static final String EXCHANGE = "schedule-exchange";
    
    public void publishScheduleCreated(Schedule schedule) {
        Map<String, Object> message = new HashMap<>();
        message.put("event", "schedule.created");
        message.put("scheduleId", schedule.getId());
        message.put("title", schedule.getTitle());
        message.put("teacher", schedule.getTeacher());
        message.put("groupName", schedule.getGroupName());
        message.put("startTime", schedule.getStartTime().toString());
        
        rabbitTemplate.convertAndSend(EXCHANGE, "schedule.created", message);
        log.info("Published schedule.created event for schedule: {}", schedule.getId());
    }
    
    public void publishScheduleUpdated(Schedule schedule) {
        Map<String, Object> message = new HashMap<>();
        message.put("event", "schedule.updated");
        message.put("scheduleId", schedule.getId());
        message.put("title", schedule.getTitle());
        
        rabbitTemplate.convertAndSend(EXCHANGE, "schedule.updated", message);
        log.info("Published schedule.updated event for schedule: {}", schedule.getId());
    }
    
    public void publishScheduleDeleted(Long scheduleId) {
        Map<String, Object> message = new HashMap<>();
        message.put("event", "schedule.deleted");
        message.put("scheduleId", scheduleId);
        
        rabbitTemplate.convertAndSend(EXCHANGE, "schedule.deleted", message);
        log.info("Published schedule.deleted event for schedule: {}", scheduleId);
    }
    
    public void publishScheduleCancelled(Long scheduleId, String title, String teacher, String groupName) {
        Map<String, Object> message = new HashMap<>();
        message.put("event", "course.cancelled");
        message.put("scheduleId", scheduleId);
        message.put("title", title);
        message.put("teacher", teacher);
        message.put("groupName", groupName);
        message.put("changeDescription", "Le cours a été annulé par l'administration.");
        message.put("reason", "Annulation administrative");
        
        rabbitTemplate.convertAndSend(EXCHANGE, "schedule.cancelled", message);
        log.info("Published course.cancelled event for schedule: {}", scheduleId);
    }
    
    public void publishRoomChanged(Long scheduleId, String title, String teacher, String oldRoom, String newRoom) {
        Map<String, Object> message = new HashMap<>();
        message.put("event", "room.changed");
        message.put("scheduleId", scheduleId);
        message.put("title", title);
        message.put("teacher", teacher);
        message.put("room", newRoom);
        message.put("oldRoom", oldRoom);
        message.put("changeDescription", "La salle a été changée de " + oldRoom + " vers " + newRoom);
        
        rabbitTemplate.convertAndSend(EXCHANGE, "schedule.room.changed", message);
        log.info("Published room.changed event for schedule: {}", scheduleId);
    }
    
    public void publishNotification(String routingKey, NotificationRequest notificationRequest) {
        Map<String, Object> message = new HashMap<>();
        message.put("event", "notification");
        message.put("userId", notificationRequest.getUserId());
        message.put("title", notificationRequest.getSubject());
        message.put("message", notificationRequest.getContent());
        message.put("type", notificationRequest.getType());
        message.put("priority", notificationRequest.getPriority());
        
        rabbitTemplate.convertAndSend(EXCHANGE, routingKey, message);
        log.info("Published notification event with routing key: {}", routingKey);
    }
}
