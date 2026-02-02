package cm.iusjc.scheduling.service;

import cm.iusjc.scheduling.dto.ScheduleDTO;
import cm.iusjc.scheduling.entity.Schedule;
import cm.iusjc.scheduling.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeacherConflictDetectionService {
    
    private final ScheduleRepository scheduleRepository;
    private final RestTemplate restTemplate;
    
    public static class TeacherConflict {
        private String type;
        private String description;
        private Long teacherId;
        private String teacherName;
        private List<ScheduleDTO> conflictingSchedules;
        private String severity;
        private List<String> suggestions;
        
        // Constructors, getters, setters
        public TeacherConflict(String type, String description, Long teacherId, String teacherName) {
            this.type = type;
            this.description = description;
            this.teacherId = teacherId;
            this.teacherName = teacherName;
            this.conflictingSchedules = new ArrayList<>();
            this.suggestions = new ArrayList<>();
        }
        
        // Getters and setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Long getTeacherId() { return teacherId; }
        public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
        public String getTeacherName() { return teacherName; }
        public void setTeacherName(String teacherName) { this.teacherName = teacherName; }
        public List<ScheduleDTO> getConflictingSchedules() { return conflictingSchedules; }
        public void setConflictingSchedules(List<ScheduleDTO> conflictingSchedules) { this.conflictingSchedules = conflictingSchedules; }
        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
        public List<String> getSuggestions() { return suggestions; }
        public void setSuggestions(List<String> suggestions) { this.suggestions = suggestions; }
    }
    
    public static class InterSchoolConflict {
        private String type;
        private String description;
        private Long teacherId;
        private String teacherName;
        private Long fromSchoolId;
        private Long toSchoolId;
        private String fromSchoolName;
        private String toSchoolName;
        private Integer requiredTravelTime;
        private Integer availableTravelTime;
        private List<ScheduleDTO> conflictingSchedules;
        private String severity;
        
        // Constructors, getters, setters
        public InterSchoolConflict(String type, String description, Long teacherId, String teacherName) {
            this.type = type;
            this.description = description;
            this.teacherId = teacherId;
            this.teacherName = teacherName;
            this.conflictingSchedules = new ArrayList<>();
        }
        
        // Getters and setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Long getTeacherId() { return teacherId; }
        public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
        public String getTeacherName() { return teacherName; }
        public void setTeacherName(String teacherName) { this.teacherName = teacherName; }
        public Long getFromSchoolId() { return fromSchoolId; }
        public void setFromSchoolId(Long fromSchoolId) { this.fromSchoolId = fromSchoolId; }
        public Long getToSchoolId() { return toSchoolId; }
        public void setToSchoolId(Long toSchoolId) { this.toSchoolId = toSchoolId; }
        public String getFromSchoolName() { return fromSchoolName; }
        public void setFromSchoolName(String fromSchoolName) { this.fromSchoolName = fromSchoolName; }
        public String getToSchoolName() { return toSchoolName; }
        public void setToSchoolName(String toSchoolName) { this.toSchoolName = toSchoolName; }
        public Integer getRequiredTravelTime() { return requiredTravelTime; }
        public void setRequiredTravelTime(Integer requiredTravelTime) { this.requiredTravelTime = requiredTravelTime; }
        public Integer getAvailableTravelTime() { return availableTravelTime; }
        public void setAvailableTravelTime(Integer availableTravelTime) { this.availableTravelTime = availableTravelTime; }
        public List<ScheduleDTO> getConflictingSchedules() { return conflictingSchedules; }
        public void setConflictingSchedules(List<ScheduleDTO> conflictingSchedules) { this.conflictingSchedules = conflictingSchedules; }
        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
    }
    
    public List<TeacherConflict> detectTeacherConflicts(String teacherName, Schedule newSchedule) {
        List<TeacherConflict> conflicts = new ArrayList<>();
        
        // 1. Vérifier les conflits de temps pour le même enseignant
        List<Schedule> teacherSchedules = scheduleRepository.findByTeacherAndDateRange(
                teacherName, 
                newSchedule.getStartTime().minusHours(1), 
                newSchedule.getEndTime().plusHours(1)
        );
        
        for (Schedule existingSchedule : teacherSchedules) {
            if (existingSchedule.getId().equals(newSchedule.getId())) continue;
            
            if (hasTimeOverlap(existingSchedule, newSchedule)) {
                TeacherConflict conflict = new TeacherConflict(
                        "TIME_OVERLAP",
                        String.format("L'enseignant %s a déjà un cours de %s à %s", 
                                teacherName, 
                                existingSchedule.getStartTime(), 
                                existingSchedule.getEndTime()),
                        extractTeacherId(teacherName),
                        teacherName
                );
                conflict.setSeverity("HIGH");
                conflict.getConflictingSchedules().add(convertToDTO(existingSchedule));
                conflict.getSuggestions().add("Modifier l'horaire du nouveau cours");
                conflict.getSuggestions().add("Assigner un autre enseignant");
                conflicts.add(conflict);
            }
        }
        
        // 2. Vérifier la disponibilité de l'enseignant
        if (!isTeacherAvailable(teacherName, newSchedule)) {
            TeacherConflict conflict = new TeacherConflict(
                    "UNAVAILABLE",
                    String.format("L'enseignant %s n'est pas disponible à ce créneau", teacherName),
                    extractTeacherId(teacherName),
                    teacherName
            );
            conflict.setSeverity("HIGH");
            conflict.getSuggestions().add("Vérifier les disponibilités de l'enseignant");
            conflict.getSuggestions().add("Choisir un autre créneau");
            conflicts.add(conflict);
        }
        
        return conflicts;
    }
    
    public List<InterSchoolConflict> detectInterSchoolConflicts(Schedule schedule) {
        List<InterSchoolConflict> conflicts = new ArrayList<>();
        
        // Récupérer les écoles de l'enseignant
        try {
            String teacherSchoolsUrl = "http://user-service/api/v1/teacher-school-assignments/teacher/" + 
                    extractTeacherId(schedule.getTeacher());
            // Dans un vrai système, on ferait l'appel REST ici
            // List<TeacherSchoolAssignmentDTO> teacherSchools = restTemplate.getForObject(teacherSchoolsUrl, List.class);
            
            // Pour l'instant, simulation
            List<Schedule> sameDaySchedules = scheduleRepository.findByTeacherAndDate(
                    schedule.getTeacher(), 
                    schedule.getStartTime().toLocalDate()
            );
            
            for (Schedule otherSchedule : sameDaySchedules) {
                if (otherSchedule.getId().equals(schedule.getId())) continue;
                
                // Simuler différentes écoles basées sur la salle
                Long schoolId1 = getSchoolIdFromRoom(schedule.getRoom());
                Long schoolId2 = getSchoolIdFromRoom(otherSchedule.getRoom());
                
                if (!schoolId1.equals(schoolId2)) {
                    Integer travelTime = calculateTravelTime(schoolId1, schoolId2);
                    Integer availableTime = calculateAvailableTime(otherSchedule, schedule);
                    
                    if (availableTime < travelTime) {
                        InterSchoolConflict conflict = new InterSchoolConflict(
                                "INSUFFICIENT_TRAVEL_TIME",
                                String.format("Temps de déplacement insuffisant entre les écoles (%d min requis, %d min disponible)", 
                                        travelTime, availableTime),
                                extractTeacherId(schedule.getTeacher()),
                                schedule.getTeacher()
                        );
                        conflict.setFromSchoolId(schoolId1);
                        conflict.setToSchoolId(schoolId2);
                        conflict.setRequiredTravelTime(travelTime);
                        conflict.setAvailableTravelTime(availableTime);
                        conflict.setSeverity(availableTime < travelTime / 2 ? "HIGH" : "MEDIUM");
                        conflict.getConflictingSchedules().add(convertToDTO(otherSchedule));
                        conflicts.add(conflict);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error detecting inter-school conflicts", e);
        }
        
        return conflicts;
    }
    
    private boolean hasTimeOverlap(Schedule schedule1, Schedule schedule2) {
        return schedule1.getStartTime().isBefore(schedule2.getEndTime()) && 
               schedule1.getEndTime().isAfter(schedule2.getStartTime());
    }
    
    private boolean isTeacherAvailable(String teacherName, Schedule schedule) {
        try {
            // Appel au service de disponibilité
            DayOfWeek dayOfWeek = schedule.getStartTime().getDayOfWeek();
            LocalTime startTime = schedule.getStartTime().toLocalTime();
            LocalTime endTime = schedule.getEndTime().toLocalTime();
            
            String availabilityUrl = String.format(
                    "http://user-service/api/v1/teacher-availability/teacher/%d/check?dayOfWeek=%s&startTime=%s&endTime=%s",
                    extractTeacherId(teacherName), dayOfWeek, startTime, endTime
            );
            
            // Dans un vrai système, on ferait l'appel REST ici
            // Boolean isAvailable = restTemplate.getForObject(availabilityUrl, Boolean.class);
            // return isAvailable != null ? isAvailable : false;
            
            // Pour l'instant, simulation basée sur l'heure
            return startTime.isAfter(LocalTime.of(7, 0)) && endTime.isBefore(LocalTime.of(19, 0));
        } catch (Exception e) {
            log.error("Error checking teacher availability", e);
            return false;
        }
    }
    
    private Long extractTeacherId(String teacherName) {
        // Simulation - dans un vrai système, on aurait une table de mapping
        return (long) Math.abs(teacherName.hashCode() % 1000);
    }
    
    private Long getSchoolIdFromRoom(String room) {
        // Simulation basée sur le préfixe de la salle
        if (room.startsWith("A")) return 1L;
        if (room.startsWith("B")) return 2L;
        if (room.startsWith("C")) return 3L;
        return 1L;
    }
    
    private Integer calculateTravelTime(Long fromSchoolId, Long toSchoolId) {
        if (fromSchoolId.equals(toSchoolId)) return 0;
        return Math.abs(fromSchoolId.intValue() - toSchoolId.intValue()) * 15 + 15;
    }
    
    private Integer calculateAvailableTime(Schedule schedule1, Schedule schedule2) {
        LocalDateTime end1 = schedule1.getEndTime();
        LocalDateTime start2 = schedule2.getStartTime();
        
        if (end1.isBefore(start2)) {
            return (int) java.time.Duration.between(end1, start2).toMinutes();
        } else {
            LocalDateTime end2 = schedule2.getEndTime();
            LocalDateTime start1 = schedule1.getStartTime();
            return (int) java.time.Duration.between(end2, start1).toMinutes();
        }
    }
    
    private ScheduleDTO convertToDTO(Schedule schedule) {
        return new ScheduleDTO(
                schedule.getId(),
                schedule.getTitle(),
                schedule.getDescription(),
                schedule.getStartTime(),
                schedule.getEndTime(),
                schedule.getRoom(),
                schedule.getTeacher(),
                schedule.getCourse(),
                schedule.getGroupName(),
                schedule.getStatus(),
                schedule.getCreatedAt(),
                schedule.getUpdatedAt()
        );
    }
}