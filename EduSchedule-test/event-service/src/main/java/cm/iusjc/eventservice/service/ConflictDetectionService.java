package cm.iusjc.eventservice.service;

import cm.iusjc.eventservice.entity.Event;
import cm.iusjc.eventservice.entity.Exam;
import cm.iusjc.eventservice.repository.EventRepository;
import cm.iusjc.eventservice.repository.ExamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConflictDetectionService {
    
    private final EventRepository eventRepository;
    private final ExamRepository examRepository;
    
    /**
     * Vérifie les conflits pour un événement
     */
    public List<Event> checkEventConflicts(Long resourceId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return checkEventConflicts(resourceId, startDateTime, endDateTime, null);
    }
    
    /**
     * Vérifie les conflits pour un événement en excluant un ID spécifique
     */
    public List<Event> checkEventConflicts(Long resourceId, LocalDateTime startDateTime, LocalDateTime endDateTime, Long excludeEventId) {
        List<Event> conflicts = eventRepository.findConflictingEvents(resourceId, startDateTime, endDateTime);
        
        // Exclure l'événement spécifié (pour les mises à jour)
        if (excludeEventId != null) {
            conflicts.removeIf(event -> event.getId().equals(excludeEventId));
        }
        
        return conflicts;
    }
    
    /**
     * Vérifie les conflits pour un examen
     */
    public List<Exam> checkExamConflicts(Long resourceId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return checkExamConflicts(resourceId, startDateTime, endDateTime, null);
    }
    
    /**
     * Vérifie les conflits pour un examen en excluant un ID spécifique
     */
    public List<Exam> checkExamConflicts(Long resourceId, LocalDateTime startDateTime, LocalDateTime endDateTime, Long excludeExamId) {
        List<Exam> conflicts = examRepository.findConflictingExams(resourceId, startDateTime, endDateTime);
        
        // Exclure l'examen spécifié (pour les mises à jour)
        if (excludeExamId != null) {
            conflicts.removeIf(exam -> exam.getId().equals(excludeExamId));
        }
        
        return conflicts;
    }
    
    /**
     * Vérifie tous les conflits (événements + examens) pour une ressource
     */
    public List<Object> checkAllConflicts(Long resourceId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        List<Object> allConflicts = new ArrayList<>();
        
        // Ajouter les conflits d'événements
        List<Event> eventConflicts = checkEventConflicts(resourceId, startDateTime, endDateTime);
        allConflicts.addAll(eventConflicts);
        
        // Ajouter les conflits d'examens
        List<Exam> examConflicts = checkExamConflicts(resourceId, startDateTime, endDateTime);
        allConflicts.addAll(examConflicts);
        
        return allConflicts;
    }
    
    /**
     * Vérifie si une ressource est disponible
     */
    public boolean isResourceAvailable(Long resourceId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        List<Object> conflicts = checkAllConflicts(resourceId, startDateTime, endDateTime);
        return conflicts.isEmpty();
    }
    
    /**
     * Vérifie s'il y a un conflit de ressource (retourne true si conflit)
     */
    public boolean hasAnyResourceConflict(Long resourceId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return !isResourceAvailable(resourceId, startDateTime, endDateTime);
    }
    
    /**
     * Vérifie s'il y a un conflit de ressource en excluant un examen spécifique
     */
    public boolean hasAnyResourceConflict(Long resourceId, LocalDateTime startDateTime, LocalDateTime endDateTime, Long excludeEventId, Long excludeExamId) {
        List<Object> allConflicts = new ArrayList<>();
        
        // Ajouter les conflits d'événements
        List<Event> eventConflicts = checkEventConflicts(resourceId, startDateTime, endDateTime, excludeEventId);
        allConflicts.addAll(eventConflicts);
        
        // Ajouter les conflits d'examens
        List<Exam> examConflicts = checkExamConflicts(resourceId, startDateTime, endDateTime, excludeExamId);
        allConflicts.addAll(examConflicts);
        
        return !allConflicts.isEmpty();
    }
    
    /**
     * Trouve les créneaux libres pour une ressource sur une journée
     */
    public List<TimeSlot> findAvailableSlots(Long resourceId, LocalDateTime date, int slotDurationMinutes) {
        LocalDateTime startOfDay = date.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusMinutes(1);
        
        // Récupérer tous les événements et examens du jour
        List<Event> events = eventRepository.findEventsByDate(date);
        List<Exam> exams = examRepository.findExamsByDate(date);
        
        // Filtrer par ressource
        events = events.stream()
                .filter(event -> event.getResourceId().equals(resourceId))
                .toList();
        exams = exams.stream()
                .filter(exam -> exam.getResourceId().equals(resourceId))
                .toList();
        
        // Créer la liste des créneaux occupés
        List<TimeSlot> occupiedSlots = new ArrayList<>();
        
        for (Event event : events) {
            occupiedSlots.add(new TimeSlot(event.getStartDateTime(), event.getEndDateTime()));
        }
        
        for (Exam exam : exams) {
            occupiedSlots.add(new TimeSlot(exam.getStartDateTime(), exam.getEndDateTime()));
        }
        
        // Trier les créneaux occupés
        occupiedSlots.sort((a, b) -> a.getStart().compareTo(b.getStart()));
        
        // Trouver les créneaux libres
        List<TimeSlot> availableSlots = new ArrayList<>();
        LocalDateTime currentTime = startOfDay.withHour(8); // Commencer à 8h
        LocalDateTime endTime = startOfDay.withHour(18); // Finir à 18h
        
        for (TimeSlot occupied : occupiedSlots) {
            if (currentTime.isBefore(occupied.getStart())) {
                // Il y a un créneau libre avant cet événement
                if (currentTime.plusMinutes(slotDurationMinutes).isBefore(occupied.getStart()) ||
                    currentTime.plusMinutes(slotDurationMinutes).equals(occupied.getStart())) {
                    availableSlots.add(new TimeSlot(currentTime, occupied.getStart()));
                }
            }
            currentTime = occupied.getEnd();
        }
        
        // Vérifier s'il y a un créneau libre après le dernier événement
        if (currentTime.isBefore(endTime)) {
            if (currentTime.plusMinutes(slotDurationMinutes).isBefore(endTime) ||
                currentTime.plusMinutes(slotDurationMinutes).equals(endTime)) {
                availableSlots.add(new TimeSlot(currentTime, endTime));
            }
        }
        
        return availableSlots;
    }
    
    /**
     * Classe interne pour représenter un créneau horaire
     */
    public static class TimeSlot {
        private LocalDateTime start;
        private LocalDateTime end;
        
        public TimeSlot(LocalDateTime start, LocalDateTime end) {
            this.start = start;
            this.end = end;
        }
        
        public LocalDateTime getStart() { return start; }
        public LocalDateTime getEnd() { return end; }
    }
}