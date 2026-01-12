package cm.iusjc.reservation.service;

import cm.iusjc.reservation.entity.Reservation;
import cm.iusjc.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

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
        
        List<Reservation> conflicts = reservationRepository.findConflictingReservations(
            resourceId, effectiveStart, effectiveEnd, excludeReservationId);
        
        log.debug("Found {} conflicts", conflicts.size());
        return conflicts;
    }
    
    public boolean hasConflicts(Long resourceId, LocalDateTime startTime, 
                               LocalDateTime endTime, Integer setupTime, 
                               Integer cleanupTime, Long excludeReservationId) {
        return !checkConflicts(resourceId, startTime, endTime, setupTime, cleanupTime, excludeReservationId).isEmpty();
    }
}