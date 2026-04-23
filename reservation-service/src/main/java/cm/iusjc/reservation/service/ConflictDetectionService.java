package cm.iusjc.reservation.service;

import cm.iusjc.reservation.entity.Reservation;
import cm.iusjc.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConflictDetectionService {
    
    private final ReservationRepository reservationRepository;
    
    public List<Reservation> checkConflicts(Long resourceId, LocalDateTime startTime, 
                                          LocalDateTime endTime, Integer setupTime, 
                                          Integer cleanupTime, Long excludeReservationId) {
        
        log.debug("Checking conflicts for resource {} from {} to {}", resourceId, startTime, endTime);
        
        // Calculer les temps effectifs avec setup et cleanup
        LocalDateTime effectiveStart = startTime.minusMinutes(setupTime != null ? setupTime : 0);
        LocalDateTime effectiveEnd = endTime.plusMinutes(cleanupTime != null ? cleanupTime : 0);
        
        // Utiliser la requête optimisée avec setup/cleanup
        List<Reservation> conflicts = reservationRepository.findConflictingReservationsWithSetup(
            resourceId, effectiveStart, effectiveEnd, excludeReservationId);
        
        log.debug("Found {} conflicts", conflicts.size());
        return conflicts;
    }
    
    public boolean hasConflicts(Long resourceId, LocalDateTime startTime, 
                               LocalDateTime endTime, Integer setupTime, 
                               Integer cleanupTime, Long excludeReservationId) {
        return !checkConflicts(resourceId, startTime, endTime, setupTime, cleanupTime, excludeReservationId).isEmpty();
    }
    
    // Vérification de conflits pour plusieurs ressources en parallèle
    public CompletableFuture<List<Reservation>> checkConflictsAsync(List<Long> resourceIds, 
                                                                   LocalDateTime startTime, 
                                                                   LocalDateTime endTime) {
        return CompletableFuture.supplyAsync(() -> {
            log.debug("Checking conflicts for {} resources asynchronously", resourceIds.size());
            
            return reservationRepository.findConflictingReservationsForMultipleResources(
                resourceIds, startTime, endTime);
        });
    }
    
    // Vérification rapide de disponibilité sans détails
    public boolean isResourceAvailable(Long resourceId, LocalDateTime date) {
        Long count = reservationRepository.countReservationsForResourceAndDate(resourceId, date);
        return count == 0;
    }
    
    // Détection de conflits avec analyse de performance
    public ConflictAnalysisResult analyzeConflicts(Long resourceId, LocalDateTime startTime, 
                                                  LocalDateTime endTime, Long excludeReservationId) {
        long startMs = System.currentTimeMillis();
        
        List<Reservation> conflicts = checkConflicts(resourceId, startTime, endTime, 0, 0, excludeReservationId);
        
        long endMs = System.currentTimeMillis();
        long duration = endMs - startMs;
        
        log.debug("Conflict analysis completed in {}ms for resource {}", duration, resourceId);
        
        return ConflictAnalysisResult.builder()
                .conflicts(conflicts)
                .hasConflicts(!conflicts.isEmpty())
                .conflictCount(conflicts.size())
                .analysisTimeMs(duration)
                .resourceId(resourceId)
                .build();
    }
    
    // Classe pour les résultats d'analyse
    @lombok.Builder
    @lombok.Data
    public static class ConflictAnalysisResult {
        private List<Reservation> conflicts;
        private boolean hasConflicts;
        private int conflictCount;
        private long analysisTimeMs;
        private Long resourceId;
    }
}