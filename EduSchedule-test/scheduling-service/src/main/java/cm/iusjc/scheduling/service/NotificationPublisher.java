package cm.iusjc.scheduling.service;

import cm.iusjc.scheduling.entity.Schedule;
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
}
