package cm.iusjc.reservation.service;

import cm.iusjc.reservation.dto.RoomSuggestionDTO;
import cm.iusjc.reservation.dto.OptimizationCriteriaDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomOptimizationService {
    
    private final RestTemplate restTemplate;
    private final ConflictDetectionService conflictDetectionService;
    
    @Cacheable(value = "roomSuggestions", key = "#criteria.hashCode()")
    public List<RoomSuggestionDTO> suggestOptimalRooms(OptimizationCriteriaDTO criteria) {
        log.info("Finding optimal rooms for criteria: {}", criteria);
        
        try {
            // Appel au room-service pour obtenir les salles disponibles
            String url = "http://room-service/api/rooms/search" +
                        "?type=" + criteria.getRoomType() +
                        "&minCapacity=" + criteria.getMinCapacity() +
                        "&schoolId=" + criteria.getSchoolId() +
                        "&hasProjector=" + criteria.isRequiresProjector() +
                        "&hasComputer=" + criteria.isRequiresComputer();
            
            RoomSuggestionDTO[] rooms = restTemplate.getForObject(url, RoomSuggestionDTO[].class);
            
            if (rooms == null || rooms.length == 0) {
                return List.of();
            }
            
            // Filtrer les salles disponibles (sans conflit)
            List<RoomSuggestionDTO> availableRooms = List.of(rooms).stream()
                    .filter(room -> isRoomAvailable(room.getId(), criteria.getStartTime(), criteria.getEndTime()))
                    .collect(Collectors.toList());
            
            // Calculer le score d'optimisation pour chaque salle
            return availableRooms.stream()
                    .map(room -> calculateOptimizationScore(room, criteria))
                    .sorted((r1, r2) -> Double.compare(r2.getOptimizationScore(), r1.getOptimizationScore()))
                    .limit(5) // Top 5 suggestions
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.error("Error finding optimal rooms: {}", e.getMessage());
            return List.of();
        }
    }
    
    private boolean isRoomAvailable(Long roomId, LocalDateTime startTime, LocalDateTime endTime) {
        return conflictDetectionService.checkConflicts(roomId, startTime, endTime, 15, 15, null).isEmpty();
    }
    
    private RoomSuggestionDTO calculateOptimizationScore(RoomSuggestionDTO room, OptimizationCriteriaDTO criteria) {
        double score = 0.0;
        
        // Score basé sur la capacité (éviter le surdimensionnement)
        double capacityRatio = (double) criteria.getMinCapacity() / room.getCapacity();
        if (capacityRatio >= 0.7 && capacityRatio <= 1.0) {
            score += 30; // Capacité optimale
        } else if (capacityRatio >= 0.5) {
            score += 20; // Capacité acceptable
        } else {
            score += 10 - (1.0 - capacityRatio) * 10; // Pénalité pour surdimensionnement
        }
        
        // Score basé sur l'équipement
        if (criteria.isRequiresProjector() && room.isHasProjector()) {
            score += 15;
        }
        if (criteria.isRequiresComputer() && room.isHasComputer()) {
            score += 15;
        }
        if (room.isHasAirConditioning()) {
            score += 5; // Bonus confort
        }
        if (room.isAccessible()) {
            score += 5; // Bonus accessibilité
        }
        
        // Score basé sur l'historique d'utilisation (simulé)
        score += calculateUsageHistoryScore(room.getId());
        
        // Score basé sur la proximité (si building/floor spécifié)
        if (criteria.getPreferredBuilding() != null && 
            criteria.getPreferredBuilding().equals(room.getBuildingId())) {
            score += 10;
        }
        
        room.setOptimizationScore(score);
        room.setOptimizationReason(buildOptimizationReason(room, criteria, score));
        
        return room;
    }
    
    private double calculateUsageHistoryScore(Long roomId) {
        // Simuler un score basé sur l'historique d'utilisation
        // Dans une vraie implémentation, on interrogerait les statistiques d'utilisation
        return Math.random() * 10; // Score aléatoire pour la démo
    }
    
    private String buildOptimizationReason(RoomSuggestionDTO room, OptimizationCriteriaDTO criteria, double score) {
        StringBuilder reason = new StringBuilder();
        
        double capacityRatio = (double) criteria.getMinCapacity() / room.getCapacity();
        if (capacityRatio >= 0.7) {
            reason.append("Capacité optimale. ");
        } else if (capacityRatio >= 0.5) {
            reason.append("Capacité acceptable. ");
        } else {
            reason.append("Salle spacieuse. ");
        }
        
        if (criteria.isRequiresProjector() && room.isHasProjector()) {
            reason.append("Projecteur disponible. ");
        }
        if (criteria.isRequiresComputer() && room.isHasComputer()) {
            reason.append("Ordinateur disponible. ");
        }
        if (room.isHasAirConditioning()) {
            reason.append("Climatisée. ");
        }
        
        reason.append(String.format("Score: %.1f/100", score));
        
        return reason.toString();
    }
    
    public List<RoomSuggestionDTO> findAlternativeRooms(Long originalRoomId, OptimizationCriteriaDTO criteria) {
        log.info("Finding alternative rooms for room: {}", originalRoomId);
        
        List<RoomSuggestionDTO> suggestions = suggestOptimalRooms(criteria);
        
        // Exclure la salle originale des suggestions
        return suggestions.stream()
                .filter(room -> !room.getId().equals(originalRoomId))
                .collect(Collectors.toList());
    }
    
    @Cacheable(value = "roomUtilization", key = "#roomId + '_' + #startDate + '_' + #endDate")
    public Double calculateRoomUtilizationRate(Long roomId, LocalDateTime startDate, LocalDateTime endDate) {
        try {
            // Appel au service de réservation pour obtenir les statistiques
            String url = "http://reservation-service/api/reservations/stats/utilization" +
                        "?roomId=" + roomId +
                        "&startDate=" + startDate +
                        "&endDate=" + endDate;
            
            Double utilization = restTemplate.getForObject(url, Double.class);
            return utilization != null ? utilization : 0.0;
            
        } catch (Exception e) {
            log.warn("Could not calculate utilization for room {}: {}", roomId, e.getMessage());
            return 0.0;
        }
    }
    
    // Additional methods needed by the controller
    
    public cm.iusjc.reservation.dto.OptimizationResult findOptimalRoom(cm.iusjc.reservation.dto.ReservationRequest request) {
        log.info("Finding optimal room for reservation request");
        
        // Create optimization criteria from reservation request
        OptimizationCriteriaDTO criteria = new OptimizationCriteriaDTO();
        criteria.setMinCapacity(request.getExpectedAttendees() != null ? request.getExpectedAttendees() : 1);
        criteria.setStartTime(request.getStartTime());
        criteria.setEndTime(request.getEndTime());
        criteria.setRoomType("CLASSROOM"); // Default type
        
        List<RoomSuggestionDTO> suggestions = suggestOptimalRooms(criteria);
        
        cm.iusjc.reservation.dto.OptimizationResult result = new cm.iusjc.reservation.dto.OptimizationResult();
        result.setOptimized(!suggestions.isEmpty());
        result.setMessage(suggestions.isEmpty() ? "No optimal rooms found" : "Found " + suggestions.size() + " optimal rooms");
        
        // Convert room suggestions to optimization suggestions
        List<cm.iusjc.reservation.dto.OptimizationSuggestion> optimizationSuggestions = suggestions.stream()
                .map(room -> {
                    cm.iusjc.reservation.dto.OptimizationSuggestion suggestion = new cm.iusjc.reservation.dto.OptimizationSuggestion();
                    suggestion.setSuggestedResourceId(room.getId());
                    suggestion.setSuggestedStartTime(request.getStartTime());
                    suggestion.setSuggestedEndTime(request.getEndTime());
                    suggestion.setReason(room.getOptimizationReason());
                    suggestion.setImprovementScore(room.getOptimizationScore());
                    suggestion.setType("RESOURCE_CHANGE");
                    return suggestion;
                })
                .collect(Collectors.toList());
        
        result.setSuggestions(optimizationSuggestions);
        result.setEfficiencyScore(suggestions.isEmpty() ? 0.0 : suggestions.get(0).getOptimizationScore());
        
        return result;
    }
    
    public List<cm.iusjc.reservation.dto.OptimizationSuggestion> optimizeRoomUsage(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Optimizing room usage for period: {} to {}", startDate, endDate);
        
        // This is a stub implementation
        // In a real implementation, this would analyze current reservations and suggest optimizations
        return List.of();
    }
    
    public Double calculateRoomEfficiencyScore(Long roomId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Calculating efficiency score for room: {} from {} to {}", roomId, startDate, endDate);
        
        // Calculate utilization rate
        Double utilizationRate = calculateRoomUtilizationRate(roomId, startDate, endDate);
        
        // Simple efficiency calculation based on utilization
        // In a real implementation, this would consider more factors
        return utilizationRate * 100; // Convert to percentage
    }
}