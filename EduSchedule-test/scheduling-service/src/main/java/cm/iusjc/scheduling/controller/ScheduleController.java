package cm.iusjc.scheduling.controller;

import cm.iusjc.scheduling.dto.ScheduleDTO;
import cm.iusjc.scheduling.dto.ScheduleRequest;
import cm.iusjc.scheduling.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleController {
    
    private final ScheduleService scheduleService;
    
    @PostMapping
    public ResponseEntity<ScheduleDTO> createSchedule(@RequestBody ScheduleRequest request) {
        ScheduleDTO schedule = scheduleService.createSchedule(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(schedule);
    }
    
    @GetMapping
    public ResponseEntity<List<ScheduleDTO>> getAllSchedules() {
        return ResponseEntity.ok(scheduleService.getAllSchedules());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ScheduleDTO> getScheduleById(@PathVariable Long id) {
        return ResponseEntity.ok(scheduleService.getScheduleById(id));
    }
    
    @GetMapping("/teacher/{teacher}")
    public ResponseEntity<List<ScheduleDTO>> getSchedulesByTeacher(@PathVariable String teacher) {
        return ResponseEntity.ok(scheduleService.getSchedulesByTeacher(teacher));
    }
    
    @GetMapping("/group/{groupName}")
    public ResponseEntity<List<ScheduleDTO>> getSchedulesByGroup(@PathVariable String groupName) {
        return ResponseEntity.ok(scheduleService.getSchedulesByGroup(groupName));
    }
    
    @GetMapping("/room/{room}")
    public ResponseEntity<List<ScheduleDTO>> getSchedulesByRoom(@PathVariable String room) {
        return ResponseEntity.ok(scheduleService.getSchedulesByRoom(room));
    }
    
    @GetMapping("/range")
    public ResponseEntity<List<ScheduleDTO>> getSchedulesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(scheduleService.getSchedulesByDateRange(startDate, endDate));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ScheduleDTO> updateSchedule(@PathVariable Long id, @RequestBody ScheduleRequest request) {
        return ResponseEntity.ok(scheduleService.updateSchedule(id, request));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long id) {
        scheduleService.deleteSchedule(id);
        return ResponseEntity.noContent().build();
    }
}
