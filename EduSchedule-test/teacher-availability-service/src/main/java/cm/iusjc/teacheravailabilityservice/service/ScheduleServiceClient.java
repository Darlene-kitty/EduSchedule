package cm.iusjc.teacheravailabilityservice.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@FeignClient(name = "scheduling-service", fallback = ScheduleServiceClient.ScheduleServiceFallback.class)
public interface ScheduleServiceClient {
    
    @GetMapping("/api/schedules/teacher/{teacherId}/conflicts")
    List<String> checkTeacherConflicts(
            @PathVariable("teacherId") Long teacherId,
            @RequestParam("startTime") LocalDateTime startTime,
            @RequestParam("endTime") LocalDateTime endTime
    );
    
    @GetMapping("/api/schedules/teacher/{teacherId}/availability-conflicts")
    List<String> checkAvailabilityConflicts(
            @PathVariable("teacherId") Long teacherId,
            @RequestParam("startDate") LocalDate startDate,
            @RequestParam("endDate") LocalDate endDate
    );
    
    @GetMapping("/api/schedules/teacher/{teacherId}/upcoming")
    List<ScheduleDTO> getUpcomingSchedules(@PathVariable("teacherId") Long teacherId);
    
    @Component
    static class ScheduleServiceFallback implements ScheduleServiceClient {
        
        @Override
        public List<String> checkTeacherConflicts(Long teacherId, LocalDateTime startTime, LocalDateTime endTime) {
            return List.of(); // Pas de conflits détectés en cas d'erreur
        }
        
        @Override
        public List<String> checkAvailabilityConflicts(Long teacherId, LocalDate startDate, LocalDate endDate) {
            return List.of(); // Pas de conflits détectés en cas d'erreur
        }
        
        @Override
        public List<ScheduleDTO> getUpcomingSchedules(Long teacherId) {
            return List.of(); // Liste vide en cas d'erreur
        }
    }
    
    // DTO pour les emplois du temps
    static class ScheduleDTO {
        private Long id;
        private String title;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String room;
        private String course;
        
        // Constructors
        public ScheduleDTO() {}
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
        
        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
        
        public String getRoom() { return room; }
        public void setRoom(String room) { this.room = room; }
        
        public String getCourse() { return course; }
        public void setCourse(String course) { this.course = course; }
    }
}