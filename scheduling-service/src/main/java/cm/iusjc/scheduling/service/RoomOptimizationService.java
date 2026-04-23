package cm.iusjc.scheduling.service;

import cm.iusjc.scheduling.dto.OptimalRoomSuggestion;
import cm.iusjc.scheduling.dto.RoomOptimizationRequest;
import cm.iusjc.scheduling.dto.RoomCriteria;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomOptimizationService {
    
    private final RestTemplate restTemplate;
    
    public List<OptimalRoomSuggestion> findOptimalRooms(RoomOptimizationRequest request) {
        log.info("Finding optimal rooms for {} students, type: {}, duration: {} minutes", 
                request.getExpectedAttendees(), request.getCourseType(), request.getDurationMinutes());
        
        try {
            // 1. Récupérer toutes les salles disponibles
            List<Map<String, Object>> availableRooms = getAvailableRooms(request.getStartTime(), request.getEndTime());
            
            // 2. Filtrer selon les critères
            List<Map<String, Object>> suitableRooms = filterRoomsByCriteria(availableRooms, request);
            
            // 3. Calculer le score d'optimisation pour chaque salle
            List<OptimalRoomSuggestion> suggestions = suitableRooms.stream()
                    .map(room -> calculateRoomScore(room, request))
                    .filter(Objects::nonNull)
                    .sorted((a, b) -> Double.compare(b.getOptimizationScore(), a.getOptimizationScore()))
                    .limit(5) // Top 5 suggestions
                    .collect(Collectors.toList());
            
            log.info("Found {} optimal room suggestions", suggestions.size());
            return suggestions;
            
        } catch (Exception e) {
            log.error("Failed to find optimal rooms: {}", e.getMessage());
            return List.of();
        }
    }
    
    public List<OptimalRoomSuggestion> findAlternativeRooms(Long originalRoomId, RoomOptimizationRequest request) {
        log.info("Finding alternative rooms for room ID: {}", originalRoomId);
        
        // Exclure la salle originale de la recherche
        request.setExcludeRoomIds(List.of(originalRoomId));
        
        return findOptimalRooms(request);
    }
    
    public OptimalRoomSuggestion getBestRoomSuggestion(RoomOptimizationRequest request) {
        List<OptimalRoomSuggestion> suggestions = findOptimalRooms(request);
        return suggestions.isEmpty() ? null : suggestions.get(0);
    }
    
    private List<Map<String, Object>> getAvailableRooms(LocalDateTime startTime, LocalDateTime endTime) {
        try {
            String url = "http://resource-service/api/resources/available" +
                        "?startTime=" + startTime + "&endTime=" + endTime + "&type=ROOM";
            
            Map<String, Object>[] rooms = restTemplate.getForObject(url, Map[].class);
            return rooms != null ? Arrays.asList(rooms) : List.of();
            
        } catch (Exception e) {
            log.warn("Failed to get available rooms: {}", e.getMessage());
            return List.of();
        }
    }
    
    private List<Map<String, Object>> filterRoomsByCriteria(List<Map<String, Object>> rooms, RoomOptimizationRequest request) {
        return rooms.stream()
                .filter(room -> matchesCapacityCriteria(room, request))
                .filter(room -> matchesTypeCriteria(room, request))
                .filter(room -> matchesEquipmentCriteria(room, request))
                .filter(room -> matchesLocationCriteria(room, request))
                .filter(room -> !isExcluded(room, request))
                .collect(Collectors.toList());
    }
    
    private boolean matchesCapacityCriteria(Map<String, Object> room, RoomOptimizationRequest request) {
        Integer capacity = getIntegerValue(room, "capacity");
        if (capacity == null) return false;
        
        int expectedAttendees = request.getExpectedAttendees();
        
        // La salle doit avoir au moins la capacité requise, mais pas plus de 50% de surCapacité
        return capacity >= expectedAttendees && capacity <= (expectedAttendees * 1.5);
    }
    
    private boolean matchesTypeCriteria(Map<String, Object> room, RoomOptimizationRequest request) {
        String roomType = getStringValue(room, "type");
        String courseType = request.getCourseType();
        
        if (roomType == null || courseType == null) return true;
        
        // Mapping des types de cours vers les types de salles
        Map<String, List<String>> courseToRoomMapping = Map.of(
            "COURS", List.of("AMPHITHEATRE", "CLASSROOM", "LECTURE_HALL"),
            "TD", List.of("CLASSROOM", "SEMINAR_ROOM"),
            "TP", List.of("LABORATORY", "COMPUTER_LAB", "WORKSHOP"),
            "EXAMEN", List.of("EXAM_ROOM", "CLASSROOM", "AMPHITHEATRE"),
            "CONFERENCE", List.of("AMPHITHEATRE", "CONFERENCE_ROOM"),
            "SEMINAIRE", List.of("SEMINAR_ROOM", "CONFERENCE_ROOM")
        );
        
        List<String> suitableRoomTypes = courseToRoomMapping.get(courseType.toUpperCase());
        return suitableRoomTypes == null || suitableRoomTypes.contains(roomType.toUpperCase());
    }
    
    private boolean matchesEquipmentCriteria(Map<String, Object> room, RoomOptimizationRequest request) {
        if (request.getRequiredEquipment() == null || request.getRequiredEquipment().isEmpty()) {
            return true;
        }
        
        List<String> roomEquipment = getListValue(room, "equipment");
        if (roomEquipment == null) return false;
        
        // Vérifier que tous les équipements requis sont disponibles
        return roomEquipment.containsAll(request.getRequiredEquipment());
    }
    
    private boolean matchesLocationCriteria(Map<String, Object> room, RoomOptimizationRequest request) {
        if (request.getPreferredBuilding() == null) return true;
        
        String roomBuilding = getStringValue(room, "building");
        return request.getPreferredBuilding().equals(roomBuilding);
    }
    
    private boolean isExcluded(Map<String, Object> room, RoomOptimizationRequest request) {
        if (request.getExcludeRoomIds() == null) return false;
        
        Long roomId = getLongValue(room, "id");
        return roomId != null && request.getExcludeRoomIds().contains(roomId);
    }
    
    private OptimalRoomSuggestion calculateRoomScore(Map<String, Object> room, RoomOptimizationRequest request) {
        try {
            Long roomId = getLongValue(room, "id");
            String roomName = getStringValue(room, "name");
            Integer capacity = getIntegerValue(room, "capacity");
            String building = getStringValue(room, "building");
            String floor = getStringValue(room, "floor");
            
            if (roomId == null || roomName == null || capacity == null) {
                return null;
            }
            
            double score = 0.0;
            List<String> reasons = new ArrayList<>();
            
            // 1. Score de capacité (40% du score total)
            double capacityScore = calculateCapacityScore(capacity, request.getExpectedAttendees());
            score += capacityScore * 0.4;
            reasons.add(String.format("Capacité optimale: %.1f%%", capacityScore * 100));
            
            // 2. Score de type de salle (25% du score total)
            double typeScore = calculateTypeScore(room, request);
            score += typeScore * 0.25;
            reasons.add(String.format("Adéquation type: %.1f%%", typeScore * 100));
            
            // 3. Score d'équipement (20% du score total)
            double equipmentScore = calculateEquipmentScore(room, request);
            score += equipmentScore * 0.2;
            reasons.add(String.format("Équipements: %.1f%%", equipmentScore * 100));
            
            // 4. Score de localisation (10% du score total)
            double locationScore = calculateLocationScore(room, request);
            score += locationScore * 0.1;
            reasons.add(String.format("Localisation: %.1f%%", locationScore * 100));
            
            // 5. Score d'historique d'utilisation (5% du score total)
            double historyScore = calculateHistoryScore(roomId, request);
            score += historyScore * 0.05;
            reasons.add(String.format("Historique: %.1f%%", historyScore * 100));
            
            // Calculer les métriques supplémentaires
            double utilizationRate = calculateUtilizationRate(capacity, request.getExpectedAttendees());
            int travelTimeMinutes = calculateTravelTime(room, request);
            
            OptimalRoomSuggestion suggestion = new OptimalRoomSuggestion();
            suggestion.setRoomId(roomId);
            suggestion.setRoomName(roomName);
            suggestion.setCapacity(capacity);
            suggestion.setBuilding(building);
            suggestion.setFloor(floor);
            suggestion.setOptimizationScore(score);
            suggestion.setUtilizationRate(utilizationRate);
            suggestion.setTravelTimeMinutes(travelTimeMinutes);
            suggestion.setReasons(reasons);
            suggestion.setRoomType(getStringValue(room, "type"));
            suggestion.setAvailableEquipment(getListValue(room, "equipment"));
            
            return suggestion;
            
        } catch (Exception e) {
            log.warn("Failed to calculate room score: {}", e.getMessage());
            return null;
        }
    }
    
    private double calculateCapacityScore(int roomCapacity, int expectedAttendees) {
        if (roomCapacity < expectedAttendees) {
            return 0.0; // Salle trop petite
        }
        
        double ratio = (double) expectedAttendees / roomCapacity;
        
        // Score optimal entre 70% et 90% d'occupation
        if (ratio >= 0.7 && ratio <= 0.9) {
            return 1.0;
        } else if (ratio >= 0.5 && ratio < 0.7) {
            return 0.8;
        } else if (ratio >= 0.9 && ratio <= 1.0) {
            return 0.9;
        } else {
            return Math.max(0.3, 1.0 - (1.0 - ratio)); // Pénalité pour sous-utilisation
        }
    }
    
    private double calculateTypeScore(Map<String, Object> room, RoomOptimizationRequest request) {
        String roomType = getStringValue(room, "type");
        String courseType = request.getCourseType();
        
        if (roomType == null || courseType == null) return 0.5;
        
        // Scores de compatibilité type de cours / type de salle
        Map<String, Map<String, Double>> compatibilityMatrix = Map.of(
            "COURS", Map.of(
                "AMPHITHEATRE", 1.0,
                "LECTURE_HALL", 0.9,
                "CLASSROOM", 0.7
            ),
            "TD", Map.of(
                "CLASSROOM", 1.0,
                "SEMINAR_ROOM", 0.9,
                "AMPHITHEATRE", 0.3
            ),
            "TP", Map.of(
                "LABORATORY", 1.0,
                "COMPUTER_LAB", 0.9,
                "WORKSHOP", 0.8,
                "CLASSROOM", 0.2
            )
        );
        
        Map<String, Double> courseCompatibility = compatibilityMatrix.get(courseType.toUpperCase());
        if (courseCompatibility != null) {
            return courseCompatibility.getOrDefault(roomType.toUpperCase(), 0.3);
        }
        
        return 0.5; // Score neutre si pas de mapping
    }
    
    private double calculateEquipmentScore(Map<String, Object> room, RoomOptimizationRequest request) {
        List<String> requiredEquipment = request.getRequiredEquipment();
        if (requiredEquipment == null || requiredEquipment.isEmpty()) {
            return 1.0; // Pas d'équipement requis
        }
        
        List<String> roomEquipment = getListValue(room, "equipment");
        if (roomEquipment == null) return 0.0;
        
        long matchingEquipment = requiredEquipment.stream()
                .mapToLong(eq -> roomEquipment.contains(eq) ? 1 : 0)
                .sum();
        
        return (double) matchingEquipment / requiredEquipment.size();
    }
    
    private double calculateLocationScore(Map<String, Object> room, RoomOptimizationRequest request) {
        if (request.getPreferredBuilding() == null) return 1.0;
        
        String roomBuilding = getStringValue(room, "building");
        if (request.getPreferredBuilding().equals(roomBuilding)) {
            return 1.0;
        }
        
        // Score réduit si bâtiment différent mais proche
        return 0.6;
    }
    
    private double calculateHistoryScore(Long roomId, RoomOptimizationRequest request) {
        try {
            // Récupérer l'historique d'utilisation de la salle
            String url = "http://reservation-service/api/reservations/room/" + roomId + "/usage-stats";
            Map<String, Object> stats = restTemplate.getForObject(url, Map.class);
            
            if (stats != null) {
                Double satisfactionRate = getDoubleValue(stats, "satisfactionRate");
                Integer conflictCount = getIntegerValue(stats, "conflictCount");
                
                double score = 1.0;
                
                if (satisfactionRate != null) {
                    score *= satisfactionRate;
                }
                
                if (conflictCount != null && conflictCount > 0) {
                    score *= Math.max(0.5, 1.0 - (conflictCount * 0.1));
                }
                
                return score;
            }
            
        } catch (Exception e) {
            log.debug("Failed to get room history for {}: {}", roomId, e.getMessage());
        }
        
        return 0.8; // Score neutre par défaut
    }
    
    private double calculateUtilizationRate(int roomCapacity, int expectedAttendees) {
        return Math.min(1.0, (double) expectedAttendees / roomCapacity);
    }
    
    private int calculateTravelTime(Map<String, Object> room, RoomOptimizationRequest request) {
        if (request.getPreviousRoomId() == null) return 0;
        
        try {
            String url = "http://resource-service/api/resources/travel-time" +
                        "?fromRoomId=" + request.getPreviousRoomId() +
                        "&toRoomId=" + getLongValue(room, "id");
            
            Integer travelTime = restTemplate.getForObject(url, Integer.class);
            return travelTime != null ? travelTime : 5; // 5 minutes par défaut
            
        } catch (Exception e) {
            return 5; // Défaut
        }
    }
    
    // Méthodes utilitaires pour extraire les valeurs des maps
    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }
    
    private Integer getIntegerValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return null;
    }
    
    private Long getLongValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return null;
    }
    
    private Double getDoubleValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return null;
    }
    
    @SuppressWarnings("unchecked")
    private List<String> getListValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof List) {
            return (List<String>) value;
        }
        return null;
    }
}