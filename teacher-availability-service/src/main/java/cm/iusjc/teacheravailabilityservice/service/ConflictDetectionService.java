package cm.iusjc.teacheravailabilityservice.service;

import cm.iusjc.teacheravailabilityservice.entity.TeacherAvailability;
import cm.iusjc.teacheravailabilityservice.entity.TimeSlot;
import cm.iusjc.teacheravailabilityservice.dto.TeacherAvailabilityDTO;
import cm.iusjc.teacheravailabilityservice.dto.TimeSlotDTO;
import cm.iusjc.teacheravailabilityservice.repository.TeacherAvailabilityRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ConflictDetectionService {
    
    @Autowired
    private TeacherAvailabilityRepository availabilityRepository;
    
    @Autowired
    private ScheduleServiceClient scheduleServiceClient;
    
    @Value("${teacher-availability.conflict-detection.buffer-minutes:15}")
    private int bufferMinutes;
    
    @Value("${teacher-availability.conflict-detection.enabled:true}")
    private boolean conflictDetectionEnabled;
    
    /**
     * Détecte les conflits pour une nouvelle disponibilité
     */
    public List<String> detectConflicts(TeacherAvailabilityDTO availabilityDTO) {
        if (!conflictDetectionEnabled) {
            return List.of();
        }
        
        List<String> conflicts = new ArrayList<>();
        
        // 1. Conflits avec les disponibilités existantes
        conflicts.addAll(detectAvailabilityConflicts(availabilityDTO));
        
        // 2. Conflits avec les emplois du temps existants
        conflicts.addAll(detectScheduleConflicts(availabilityDTO));
        
        // 3. Validation des contraintes métier
        conflicts.addAll(validateBusinessConstraints(availabilityDTO));
        
        return conflicts;
    }
    
    /**
     * Détecte les conflits en excluant une disponibilité spécifique (pour les mises à jour)
     */
    public List<String> detectConflictsExcluding(TeacherAvailabilityDTO availabilityDTO, Long excludeId) {
        if (!conflictDetectionEnabled) {
            return List.of();
        }
        
        List<String> conflicts = new ArrayList<>();
        
        // 1. Conflits avec les autres disponibilités (en excluant celle en cours de modification)
        conflicts.addAll(detectAvailabilityConflictsExcluding(availabilityDTO, excludeId));
        
        // 2. Conflits avec les emplois du temps existants
        conflicts.addAll(detectScheduleConflicts(availabilityDTO));
        
        // 3. Validation des contraintes métier
        conflicts.addAll(validateBusinessConstraints(availabilityDTO));
        
        return conflicts;
    }
    
    /**
     * Vérifie si un enseignant est disponible à un moment donné
     */
    public boolean isTeacherAvailableAt(Long teacherId, LocalDateTime startTime, LocalDateTime endTime) {
        LocalDate date = startTime.toLocalDate();
        
        // Récupérer la disponibilité active pour cette date
        var availability = availabilityRepository.findActiveAvailabilityForTeacherOnDate(teacherId, date);
        
        if (availability.isEmpty()) {
            return false;
        }
        
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        LocalTime start = startTime.toLocalTime();
        LocalTime end = endTime.toLocalTime();
        
        // Vérifier si le créneau demandé est couvert par les disponibilités
        return availability.get().getAvailableSlots().stream()
                .anyMatch(slot -> slot.getDayOfWeek().equals(dayOfWeek) &&
                                coversTimeRange(slot, start, end));
    }
    
    /**
     * Détecte les conflits potentiels avec un nouveau créneau d'emploi du temps
     */
    public List<String> detectScheduleConflictForTeacher(Long teacherId, LocalDateTime startTime, LocalDateTime endTime) {
        List<String> conflicts = new ArrayList<>();
        
        if (!isTeacherAvailableAt(teacherId, startTime, endTime)) {
            conflicts.add(String.format("L'enseignant n'est pas disponible le %s de %s à %s", 
                    startTime.toLocalDate(), 
                    startTime.toLocalTime(), 
                    endTime.toLocalTime()));
        }
        
        // Vérifier les conflits avec d'autres emplois du temps
        List<String> scheduleConflicts = scheduleServiceClient.checkTeacherConflicts(teacherId, startTime, endTime);
        conflicts.addAll(scheduleConflicts);
        
        return conflicts;
    }
    
    // Méthodes privées de détection de conflits
    
    private List<String> detectAvailabilityConflicts(TeacherAvailabilityDTO availabilityDTO) {
        List<String> conflicts = new ArrayList<>();
        
        // Récupérer les disponibilités existantes pour cet enseignant
        List<TeacherAvailability> existingAvailabilities = availabilityRepository
                .findActiveAvailabilitiesForTeacherInPeriod(
                        availabilityDTO.getTeacherId(),
                        availabilityDTO.getEffectiveDate(),
                        availabilityDTO.getEndDate() != null ? availabilityDTO.getEndDate() : LocalDate.now().plusYears(1)
                );
        
        for (TeacherAvailability existing : existingAvailabilities) {
            if (periodsOverlap(availabilityDTO, existing)) {
                conflicts.add(String.format("Conflit avec la disponibilité existante du %s au %s", 
                        existing.getEffectiveDate(), 
                        existing.getEndDate() != null ? existing.getEndDate() : "indéfini"));
                
                // Détailler les conflits de créneaux
                conflicts.addAll(detectTimeSlotConflicts(availabilityDTO.getAvailableSlots(), existing.getAvailableSlots()));
            }
        }
        
        return conflicts;
    }
    
    private List<String> detectAvailabilityConflictsExcluding(TeacherAvailabilityDTO availabilityDTO, Long excludeId) {
        List<String> conflicts = new ArrayList<>();
        
        // Récupérer les disponibilités existantes pour cet enseignant (en excluant celle spécifiée)
        List<TeacherAvailability> existingAvailabilities = availabilityRepository
                .findActiveAvailabilitiesForTeacherInPeriod(
                        availabilityDTO.getTeacherId(),
                        availabilityDTO.getEffectiveDate(),
                        availabilityDTO.getEndDate() != null ? availabilityDTO.getEndDate() : LocalDate.now().plusYears(1)
                ).stream()
                .filter(availability -> !availability.getId().equals(excludeId))
                .collect(Collectors.toList());
        
        for (TeacherAvailability existing : existingAvailabilities) {
            if (periodsOverlap(availabilityDTO, existing)) {
                conflicts.add(String.format("Conflit avec la disponibilité existante du %s au %s", 
                        existing.getEffectiveDate(), 
                        existing.getEndDate() != null ? existing.getEndDate() : "indéfini"));
                
                // Détailler les conflits de créneaux
                conflicts.addAll(detectTimeSlotConflicts(availabilityDTO.getAvailableSlots(), existing.getAvailableSlots()));
            }
        }
        
        return conflicts;
    }
    
    private List<String> detectScheduleConflicts(TeacherAvailabilityDTO availabilityDTO) {
        List<String> conflicts = new ArrayList<>();
        
        // Vérifier les conflits avec les emplois du temps existants
        try {
            List<String> scheduleConflicts = scheduleServiceClient.checkAvailabilityConflicts(
                    availabilityDTO.getTeacherId(),
                    availabilityDTO.getEffectiveDate(),
                    availabilityDTO.getEndDate()
            );
            conflicts.addAll(scheduleConflicts);
        } catch (Exception e) {
            // Si le service de planification n'est pas disponible, on continue sans cette vérification
            conflicts.add("Impossible de vérifier les conflits avec les emplois du temps existants");
        }
        
        return conflicts;
    }
    
    private List<String> validateBusinessConstraints(TeacherAvailabilityDTO availabilityDTO) {
        List<String> violations = new ArrayList<>();
        
        // Vérifier les heures maximales par semaine
        if (availabilityDTO.getAvailableSlots() != null) {
            int totalWeeklyMinutes = availabilityDTO.getAvailableSlots().stream()
                    .mapToInt(slot -> (int) slot.getDurationMinutes())
                    .sum();
            
            int totalWeeklyHours = totalWeeklyMinutes / 60;
            
            if (availabilityDTO.getMaxHoursPerWeek() != null && totalWeeklyHours > availabilityDTO.getMaxHoursPerWeek()) {
                violations.add(String.format("Total hebdomadaire (%d heures) dépasse la limite fixée (%d heures)", 
                        totalWeeklyHours, availabilityDTO.getMaxHoursPerWeek()));
            }
            
            // Vérifier les heures maximales par jour
            if (availabilityDTO.getMaxHoursPerDay() != null) {
                for (DayOfWeek day : DayOfWeek.values()) {
                    int dailyMinutes = availabilityDTO.getAvailableSlots().stream()
                            .filter(slot -> slot.getDayOfWeek().equals(day))
                            .mapToInt(slot -> (int) slot.getDurationMinutes())
                            .sum();
                    
                    int dailyHours = dailyMinutes / 60;
                    
                    if (dailyHours > availabilityDTO.getMaxHoursPerDay()) {
                        violations.add(String.format("Total du %s (%d heures) dépasse la limite quotidienne (%d heures)", 
                                day, dailyHours, availabilityDTO.getMaxHoursPerDay()));
                    }
                }
            }
        }
        
        return violations;
    }
    
    private List<String> detectTimeSlotConflicts(List<TimeSlotDTO> newSlots, List<TimeSlot> existingSlots) {
        List<String> conflicts = new ArrayList<>();
        
        if (newSlots == null || existingSlots == null) {
            return conflicts;
        }
        
        for (TimeSlotDTO newSlot : newSlots) {
            for (TimeSlot existingSlot : existingSlots) {
                if (timeSlotsOverlap(newSlot, existingSlot)) {
                    conflicts.add(String.format("Conflit de créneau: %s %s-%s avec %s %s-%s", 
                            newSlot.getDayOfWeek(), newSlot.getStartTime(), newSlot.getEndTime(),
                            existingSlot.getDayOfWeek(), existingSlot.getStartTime(), existingSlot.getEndTime()));
                }
            }
        }
        
        return conflicts;
    }
    
    // Méthodes utilitaires
    
    private boolean periodsOverlap(TeacherAvailabilityDTO dto, TeacherAvailability entity) {
        LocalDate dtoStart = dto.getEffectiveDate();
        LocalDate dtoEnd = dto.getEndDate() != null ? dto.getEndDate() : LocalDate.MAX;
        
        LocalDate entityStart = entity.getEffectiveDate();
        LocalDate entityEnd = entity.getEndDate() != null ? entity.getEndDate() : LocalDate.MAX;
        
        return dtoStart.isBefore(entityEnd) && dtoEnd.isAfter(entityStart);
    }
    
    private boolean timeSlotsOverlap(TimeSlotDTO dto, TimeSlot entity) {
        if (!dto.getDayOfWeek().equals(entity.getDayOfWeek())) {
            return false;
        }
        
        // Ajouter un buffer pour éviter les créneaux trop proches
        LocalTime dtoStart = dto.getStartTime().minusMinutes(bufferMinutes);
        LocalTime dtoEnd = dto.getEndTime().plusMinutes(bufferMinutes);
        
        return dtoStart.isBefore(entity.getEndTime()) && dtoEnd.isAfter(entity.getStartTime());
    }
    
    private boolean coversTimeRange(TimeSlot slot, LocalTime start, LocalTime end) {
        return !start.isBefore(slot.getStartTime()) && !end.isAfter(slot.getEndTime());
    }
}