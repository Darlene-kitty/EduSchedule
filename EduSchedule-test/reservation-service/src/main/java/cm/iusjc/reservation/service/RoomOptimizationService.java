package cm.iusjc.reservation.service;

import cm.iusjc.reservation.dto.RoomSuggestionDTO;
import cm.iusjc.reservation.dto.OptimizationCriteriaDTO;
import cm.iusjc.reservation.dto.ConflictResolutionDTO;
import cm.iusjc.reservation.entity.Reservation;
import cm.iusjc.reservation.entity.Resource;
import cm.iusjc.reservation.repository.ReservationRepository;
import cm.iusjc.reservation.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomOptimizationService {
    
    private final ResourceRepository resourceRepository;
    private final ReservationRepository reservationRepository;
    private final ConflictDetectionService conflictDetectionService;
    
    /**
     * Suggère les salles optimales selon les critères donnés
     */
    public List<RoomSuggestionDTO> suggestOptimalRooms(OptimizationCriteriaDTO criteria) {
        log.info("Searching optimal rooms for criteria: {}", criteria);
        
        List<Resource> availableRooms = findAvailableRooms(
            criteria.getStartTime(), 
            criteria.getEndTime(),
            criteria.getMinCapacity()
        );
        
        return availableRooms.stream()
            .map(room -> calculateRoomScore(room, criteria))
            .sorted((a, b) -> Double.compare(b.getOptimizationScore(), a.getOptimizationScore()))
            .limit(criteria.getMaxSuggestions() != null ? criteria.getMaxSuggestions() : 5)
            .collect(Collectors.toList());
    }
    
    /**
     * Trouve des solutions alternatives en cas de conflit
     */
    public List<ConflictResolutionDTO> findConflictResolutions(Long reservationId) {
        log.info("Finding conflict resolutions for reservation: {}", reservationId);
        
        Optional<Reservation> reservationOpt = reservationRepository.findById(reservationId);
        if (reservationOpt.isEmpty()) {
            return Collections.emptyList();
        }
        
        Reservation reservation = reservationOpt.get();
        List<ConflictResolutionDTO> resolutions = new ArrayList<>();
        
        // Solution 1: Salles alternatives au même créneau
        List<RoomSuggestionDTO> alternativeRooms = suggestOptimalRooms(
            OptimizationCriteriaDTO.builder()
                .startTime(reservation.getStartTime())
                .endTime(reservation.getEndTime())
                .minCapacity(reservation.getExpectedAttendees())
                .courseType(reservation.getType().toString())
                .maxSuggestions(3)
                .build()
        );
        
        for (RoomSuggestionDTO room : alternativeRooms) {
            if (!room.getResourceId().equals(reservation.getResourceId())) {
                resolutions.add(ConflictResolutionDTO.builder()
                    .resolutionType("ALTERNATIVE_ROOM")
                    .description("Utiliser la salle " + room.getRoomName() + " au lieu de la salle actuelle")
                    .newResourceId(room.getResourceId())
                    .newStartTime(reservation.getStartTime())
                    .newEndTime(reservation.getEndTime())
                    .feasibilityScore(room.getOptimizationScore())
                    .impactLevel("LOW")
                    .build());
            }
        }
        
        // Solution 2: Créneaux alternatifs dans la même salle
        List<LocalDateTime[]> alternativeSlots = findAlternativeTimeSlots(
            reservation.getResourceId(),
            reservation.getStartTime(),
            reservation.getEndTime()
        );
        
        for (LocalDateTime[] slot : alternativeSlots) {
            resolutions.add(ConflictResolutionDTO.builder()
                .resolutionType("ALTERNATIVE_TIME")
                .description("Décaler le cours de " + 
                    formatTimeSlot(reservation.getStartTime(), reservation.getEndTime()) + 
                    " à " + formatTimeSlot(slot[0], slot[1]))
                .newResourceId(reservation.getResourceId())
                .newStartTime(slot[0])
                .newEndTime(slot[1])
                .feasibilityScore(calculateTimeSlotScore(slot[0], slot[1]))
                .impactLevel(calculateImpactLevel(reservation.getStartTime(), slot[0]))
                .build());
        }
        
        // Solution 3: Diviser en plusieurs créneaux plus courts
        if (isLongSession(reservation.getStartTime(), reservation.getEndTime())) {
            List<ConflictResolutionDTO> splitSolutions = suggestSessionSplit(reservation);
            resolutions.addAll(splitSolutions);
        }
        
        return resolutions.stream()
            .sorted((a, b) -> Double.compare(b.getFeasibilityScore(), a.getFeasibilityScore()))
            .collect(Collectors.toList());
    }
    
    /**
     * Optimise l'utilisation globale des salles pour une période donnée
     */
    public Map<String, Object> optimizeGlobalRoomUsage(LocalDateTime startPeriod, LocalDateTime endPeriod) {
        log.info("Optimizing global room usage from {} to {}", startPeriod, endPeriod);
        
        List<Reservation> reservations = reservationRepository.findByStartTimeBetween(startPeriod, endPeriod);
        List<Resource> rooms = resourceRepository.findByType("ROOM");
        
        Map<String, Object> optimization = new HashMap<>();
        
        // Calcul du taux d'occupation actuel
        double currentOccupancyRate = calculateOccupancyRate(reservations, rooms, startPeriod, endPeriod);
        
        // Identification des salles sous-utilisées
        List<Map<String, Object>> underutilizedRooms = findUnderutilizedRooms(reservations, rooms, startPeriod, endPeriod);
        
        // Identification des créneaux de pointe
        Map<String, Integer> peakHours = identifyPeakHours(reservations);
        
        // Suggestions d'optimisation
        List<String> optimizationSuggestions = generateOptimizationSuggestions(
            currentOccupancyRate, underutilizedRooms, peakHours
        );
        
        optimization.put("currentOccupancyRate", currentOccupancyRate);
        optimization.put("underutilizedRooms", underutilizedRooms);
        optimization.put("peakHours", peakHours);
        optimization.put("suggestions", optimizationSuggestions);
        optimization.put("potentialSavings", calculatePotentialSavings(underutilizedRooms));
        
        return optimization;
    }
    
    private List<Resource> findAvailableRooms(LocalDateTime startTime, LocalDateTime endTime, Integer minCapacity) {
        List<Resource> allRooms = resourceRepository.findByType("ROOM");
        
        return allRooms.stream()
            .filter(room -> room.getCapacity() >= (minCapacity != null ? minCapacity : 0))
            .filter(room -> !hasConflict(room.getId(), startTime, endTime))
            .collect(Collectors.toList());
    }
    
    private boolean hasConflict(Long resourceId, LocalDateTime startTime, LocalDateTime endTime) {
        List<Reservation> conflicts = reservationRepository.findConflictingReservations(
            resourceId, startTime, endTime
        );
        return !conflicts.isEmpty();
    }
    
    private RoomSuggestionDTO calculateRoomScore(Resource room, OptimizationCriteriaDTO criteria) {
        double score = 0.0;
        
        // Score de capacité (éviter le surdimensionnement)
        if (criteria.getMinCapacity() != null && criteria.getMinCapacity() > 0) {
            double capacityRatio = (double) criteria.getMinCapacity() / room.getCapacity();
            if (capacityRatio >= 0.7 && capacityRatio <= 1.0) {
                score += 30; // Capacité optimale
            } else if (capacityRatio >= 0.5) {
                score += 20; // Capacité acceptable
            } else {
                score += 10; // Surdimensionné
            }
        }
        
        // Score d'équipements
        if (criteria.getRequiredEquipment() != null && !criteria.getRequiredEquipment().isEmpty()) {
            Set<String> roomEquipment = parseEquipment(room.getEquipment());
            Set<String> requiredEquipment = new HashSet<>(criteria.getRequiredEquipment());
            
            long matchingEquipment = requiredEquipment.stream()
                .mapToLong(eq -> roomEquipment.contains(eq) ? 1 : 0)
                .sum();
            
            score += (matchingEquipment * 15.0) / requiredEquipment.size();
        }
        
        // Score de type de cours
        if (criteria.getCourseType() != null) {
            if (isRoomSuitableForCourseType(room, criteria.getCourseType())) {
                score += 25;
            }
        }
        
        // Score de localisation (préférer les salles proches)
        if (criteria.getPreferredBuilding() != null) {
            if (room.getLocation() != null && room.getLocation().contains(criteria.getPreferredBuilding())) {
                score += 10;
            }
        }
        
        // Score d'historique d'utilisation
        score += calculateUsageHistoryScore(room.getId());
        
        return RoomSuggestionDTO.builder()
            .resourceId(room.getId())
            .roomName(room.getName())
            .capacity(room.getCapacity())
            .location(room.getLocation())
            .equipment(room.getEquipment())
            .optimizationScore(Math.min(score, 100.0)) // Cap à 100
            .availabilityStatus("AVAILABLE")
            .recommendationReason(generateRecommendationReason(room, criteria, score))
            .build();
    }
    
    private List<LocalDateTime[]> findAlternativeTimeSlots(Long resourceId, LocalDateTime originalStart, LocalDateTime originalEnd) {
        List<LocalDateTime[]> alternatives = new ArrayList<>();
        
        long durationMinutes = java.time.Duration.between(originalStart, originalEnd).toMinutes();
        LocalDateTime dayStart = originalStart.toLocalDate().atTime(8, 0);
        LocalDateTime dayEnd = originalStart.toLocalDate().atTime(18, 0);
        
        // Chercher des créneaux libres le même jour
        LocalDateTime currentSlot = dayStart;
        while (currentSlot.plusMinutes(durationMinutes).isBefore(dayEnd)) {
            LocalDateTime slotEnd = currentSlot.plusMinutes(durationMinutes);
            
            if (!hasConflict(resourceId, currentSlot, slotEnd) && 
                !currentSlot.equals(originalStart)) {
                alternatives.add(new LocalDateTime[]{currentSlot, slotEnd});
            }
            
            currentSlot = currentSlot.plusMinutes(60); // Créneaux d'1h
        }
        
        return alternatives.stream()
            .limit(3) // Limiter à 3 alternatives
            .collect(Collectors.toList());
    }
    
    private List<ConflictResolutionDTO> suggestSessionSplit(Reservation reservation) {
        List<ConflictResolutionDTO> splitSolutions = new ArrayList<>();
        
        long durationMinutes = java.time.Duration.between(reservation.getStartTime(), reservation.getEndTime()).toMinutes();
        
        if (durationMinutes >= 120) { // Sessions de 2h ou plus
            // Suggérer de diviser en 2 sessions d'1h
            splitSolutions.add(ConflictResolutionDTO.builder()
                .resolutionType("SPLIT_SESSION")
                .description("Diviser en 2 sessions d'1 heure avec pause")
                .newStartTime(reservation.getStartTime())
                .newEndTime(reservation.getStartTime().plusMinutes(60))
                .feasibilityScore(75.0)
                .impactLevel("MEDIUM")
                .additionalInfo("Seconde session: " + 
                    reservation.getStartTime().plusMinutes(75) + " - " + 
                    reservation.getStartTime().plusMinutes(135))
                .build());
        }
        
        return splitSolutions;
    }
    
    private double calculateOccupancyRate(List<Reservation> reservations, List<Resource> rooms, 
                                        LocalDateTime startPeriod, LocalDateTime endPeriod) {
        if (rooms.isEmpty()) return 0.0;
        
        long totalPossibleHours = rooms.size() * 
            java.time.Duration.between(startPeriod, endPeriod).toHours();
        
        long totalUsedHours = reservations.stream()
            .mapToLong(r -> java.time.Duration.between(r.getStartTime(), r.getEndTime()).toHours())
            .sum();
        
        return totalPossibleHours > 0 ? (double) totalUsedHours / totalPossibleHours * 100 : 0.0;
    }
    
    private List<Map<String, Object>> findUnderutilizedRooms(List<Reservation> reservations, 
                                                            List<Resource> rooms,
                                                            LocalDateTime startPeriod, 
                                                            LocalDateTime endPeriod) {
        return rooms.stream()
            .map(room -> {
                long roomUsageHours = reservations.stream()
                    .filter(r -> r.getResourceId().equals(room.getId()))
                    .mapToLong(r -> java.time.Duration.between(r.getStartTime(), r.getEndTime()).toHours())
                    .sum();
                
                long totalAvailableHours = java.time.Duration.between(startPeriod, endPeriod).toHours();
                double utilizationRate = totalAvailableHours > 0 ? 
                    (double) roomUsageHours / totalAvailableHours * 100 : 0.0;
                
                Map<String, Object> roomUsage = new HashMap<>();
                roomUsage.put("roomId", room.getId());
                roomUsage.put("roomName", room.getName());
                roomUsage.put("utilizationRate", utilizationRate);
                roomUsage.put("usedHours", roomUsageHours);
                roomUsage.put("availableHours", totalAvailableHours);
                
                return roomUsage;
            })
            .filter(usage -> (Double) usage.get("utilizationRate") < 50.0) // Moins de 50% d'utilisation
            .sorted((a, b) -> Double.compare((Double) a.get("utilizationRate"), (Double) b.get("utilizationRate")))
            .collect(Collectors.toList());
    }
    
    private Map<String, Integer> identifyPeakHours(List<Reservation> reservations) {
        Map<String, Integer> hourlyUsage = new HashMap<>();
        
        for (Reservation reservation : reservations) {
            LocalDateTime current = reservation.getStartTime();
            while (current.isBefore(reservation.getEndTime())) {
                String hourKey = String.format("%02d:00", current.getHour());
                hourlyUsage.merge(hourKey, 1, Integer::sum);
                current = current.plusHours(1);
            }
        }
        
        return hourlyUsage.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(5)
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e1,
                LinkedHashMap::new
            ));
    }
    
    private List<String> generateOptimizationSuggestions(double occupancyRate, 
                                                        List<Map<String, Object>> underutilizedRooms,
                                                        Map<String, Integer> peakHours) {
        List<String> suggestions = new ArrayList<>();
        
        if (occupancyRate < 60) {
            suggestions.add("Taux d'occupation global faible (" + String.format("%.1f", occupancyRate) + 
                "%). Considérer la consolidation des cours dans moins de salles.");
        }
        
        if (!underutilizedRooms.isEmpty()) {
            suggestions.add("Salles sous-utilisées détectées. Envisager de réaffecter " + 
                underutilizedRooms.size() + " salle(s) à d'autres usages.");
        }
        
        if (!peakHours.isEmpty()) {
            String peakHour = peakHours.keySet().iterator().next();
            suggestions.add("Pic d'utilisation à " + peakHour + 
                ". Considérer l'étalement des cours sur d'autres créneaux.");
        }
        
        suggestions.add("Utiliser des salles plus petites pour les petits groupes pour optimiser l'espace.");
        suggestions.add("Programmer les cours longs (>2h) pendant les heures creuses.");
        
        return suggestions;
    }
    
    private double calculatePotentialSavings(List<Map<String, Object>> underutilizedRooms) {
        return underutilizedRooms.size() * 1000.0; // Estimation: 1000€ par salle sous-utilisée
    }
    
    private double calculateUsageHistoryScore(Long roomId) {
        // Score basé sur l'historique réel des réservations (0-10 points)
        List<Reservation> history = reservationRepository.findByResourceId(roomId);
        if (history.isEmpty()) return 5.0; // score neutre si pas d'historique

        long total      = history.size();
        long confirmed  = history.stream().filter(r -> r.getStatus() == cm.iusjc.reservation.entity.ReservationStatus.CONFIRMED).count();
        long cancelled  = history.stream().filter(r -> r.getStatus() == cm.iusjc.reservation.entity.ReservationStatus.CANCELLED).count();

        // Taux de confirmation (fiabilité)
        double reliabilityRate = (double) confirmed / total;
        // Pénalité pour annulations fréquentes
        double cancellationPenalty = (double) cancelled / total;

        // Score : fiabilité compte pour 7 pts, faible annulation pour 3 pts
        return Math.round((reliabilityRate * 7.0 + (1.0 - cancellationPenalty) * 3.0) * 10.0) / 10.0;
    }
    
    private boolean isRoomSuitableForCourseType(Resource room, String courseType) {
        if (room.getType() == null) return true;
        
        switch (courseType.toUpperCase()) {
            case "TP":
            case "PRACTICAL":
                return room.getName().toLowerCase().contains("lab") || 
                       room.getName().toLowerCase().contains("tp");
            case "COURS":
            case "LECTURE":
                return room.getName().toLowerCase().contains("amphi") || 
                       room.getCapacity() > 50;
            case "TD":
            case "TUTORIAL":
                return room.getCapacity() <= 50 && room.getCapacity() >= 15;
            default:
                return true;
        }
    }
    
    private Set<String> parseEquipment(String equipment) {
        if (equipment == null || equipment.trim().isEmpty()) {
            return new HashSet<>();
        }
        return Arrays.stream(equipment.split(","))
            .map(String::trim)
            .map(String::toLowerCase)
            .collect(Collectors.toSet());
    }
    
    private double calculateTimeSlotScore(LocalDateTime startTime, LocalDateTime endTime) {
        int hour = startTime.getHour();
        
        // Préférer les créneaux standards
        if (hour >= 8 && hour <= 17) {
            return 80.0;
        } else if (hour >= 7 && hour <= 19) {
            return 60.0;
        } else {
            return 30.0;
        }
    }
    
    private String calculateImpactLevel(LocalDateTime originalTime, LocalDateTime newTime) {
        long hoursDiff = Math.abs(java.time.Duration.between(originalTime, newTime).toHours());
        
        if (hoursDiff <= 1) return "LOW";
        else if (hoursDiff <= 3) return "MEDIUM";
        else return "HIGH";
    }
    
    private boolean isLongSession(LocalDateTime start, LocalDateTime end) {
        return java.time.Duration.between(start, end).toMinutes() >= 120;
    }
    
    private String formatTimeSlot(LocalDateTime start, LocalDateTime end) {
        return start.toLocalTime() + "-" + end.toLocalTime();
    }
    
    private String generateRecommendationReason(Resource room, OptimizationCriteriaDTO criteria, double score) {
        List<String> reasons = new ArrayList<>();
        
        if (criteria.getMinCapacity() != null && room.getCapacity() >= criteria.getMinCapacity()) {
            double ratio = (double) criteria.getMinCapacity() / room.getCapacity();
            if (ratio >= 0.7) {
                reasons.add("Capacité optimale");
            } else {
                reasons.add("Capacité suffisante");
            }
        }
        
        if (criteria.getCourseType() != null && isRoomSuitableForCourseType(room, criteria.getCourseType())) {
            reasons.add("Adaptée au type de cours");
        }
        
        if (score >= 80) {
            reasons.add("Excellent match");
        } else if (score >= 60) {
            reasons.add("Bon choix");
        }
        
        return reasons.isEmpty() ? "Salle disponible" : String.join(", ", reasons);
    }
    
    /**
     * Obtient les statistiques d'optimisation
     */
    public Map<String, Object> getOptimizationStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRooms", resourceRepository.count());
        stats.put("totalReservations", reservationRepository.count());
        stats.put("timestamp", LocalDateTime.now());
        return stats;
    }
    
    /**
     * Calcule le score d'efficacité d'une salle
     */
    public Double calculateRoomEfficiencyScore(Long roomId, LocalDateTime start, LocalDateTime end) {
        List<Reservation> reservations = reservationRepository.findByResourceIdAndStartTimeBetween(roomId, start, end);
        if (reservations.isEmpty()) {
            return 0.0;
        }
        long totalMinutes = java.time.Duration.between(start, end).toMinutes();
        long usedMinutes = reservations.stream()
            .mapToLong(r -> java.time.Duration.between(r.getStartTime(), r.getEndTime()).toMinutes())
            .sum();
        return (double) usedMinutes / totalMinutes * 100;
    }
    
    /**
     * Obtient les recommandations d'optimisation
     */
    public List<Map<String, Object>> getOptimizationRecommendations(LocalDateTime start, LocalDateTime end) {
        List<Map<String, Object>> recommendations = new ArrayList<>();
        List<Resource> rooms = resourceRepository.findAll();
        
        for (Resource room : rooms) {
            Double efficiency = calculateRoomEfficiencyScore(room.getId(), start, end);
            if (efficiency < 30) {
                Map<String, Object> rec = new HashMap<>();
                rec.put("roomId", room.getId());
                rec.put("roomName", room.getName());
                rec.put("efficiency", efficiency);
                rec.put("recommendation", "Salle sous-utilisée - considérer pour réaffectation");
                recommendations.add(rec);
            }
        }
        
        return recommendations;
    }
    
    /**
     * Obtient l'analyse comparative
     */
    public Map<String, Object> getComparativeAnalysis(LocalDateTime start, LocalDateTime end) {
        Map<String, Object> analysis = new HashMap<>();
        List<Resource> rooms = resourceRepository.findAll();
        
        Map<String, Double> efficiencyByRoom = new HashMap<>();
        for (Resource room : rooms) {
            efficiencyByRoom.put(room.getName(), calculateRoomEfficiencyScore(room.getId(), start, end));
        }
        
        analysis.put("efficiencyByRoom", efficiencyByRoom);
        analysis.put("period", Map.of("start", start, "end", end));
        analysis.put("averageEfficiency", efficiencyByRoom.values().stream()
            .mapToDouble(Double::doubleValue).average().orElse(0.0));
        
        return analysis;
    }
    
    /**
     * Trouve la salle optimale pour une requête
     */
    public RoomSuggestionDTO findOptimalRoom(cm.iusjc.reservation.dto.ReservationRequestDTO request) {
        OptimizationCriteriaDTO criteria = OptimizationCriteriaDTO.builder()
            .startTime(request.getStartTime())
            .endTime(request.getEndTime())
            .minCapacity(request.getExpectedAttendees())
            .courseType(request.getType())
            .maxSuggestions(1)
            .build();
        
        List<RoomSuggestionDTO> suggestions = suggestOptimalRooms(criteria);
        return suggestions.isEmpty() ? null : suggestions.get(0);
    }
}
