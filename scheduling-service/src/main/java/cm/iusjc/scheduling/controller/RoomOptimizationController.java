package cm.iusjc.scheduling.controller;

import cm.iusjc.scheduling.dto.OptimalRoomSuggestion;
import cm.iusjc.scheduling.dto.RoomOptimizationRequest;
import cm.iusjc.scheduling.service.RoomOptimizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/room-optimization")
@RequiredArgsConstructor
@Slf4j
public class RoomOptimizationController {
    
    private final RoomOptimizationService optimizationService;
    
    @PostMapping("/find-optimal")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<List<OptimalRoomSuggestion>> findOptimalRooms(@RequestBody RoomOptimizationRequest request) {
        log.info("Finding optimal rooms for {} attendees, type: {}", 
                request.getExpectedAttendees(), request.getCourseType());
        
        List<OptimalRoomSuggestion> suggestions = optimizationService.findOptimalRooms(request);
        return ResponseEntity.ok(suggestions);
    }
    
    @GetMapping("/find-optimal")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<List<OptimalRoomSuggestion>> findOptimalRoomsGet(
            @RequestParam LocalDateTime startTime,
            @RequestParam LocalDateTime endTime,
            @RequestParam int expectedAttendees,
            @RequestParam String courseType,
            @RequestParam(required = false) String preferredBuilding,
            @RequestParam(required = false) Long previousRoomId) {
        
        RoomOptimizationRequest request = new RoomOptimizationRequest(startTime, endTime, expectedAttendees, courseType);
        request.setPreferredBuilding(preferredBuilding);
        request.setPreviousRoomId(previousRoomId);
        
        List<OptimalRoomSuggestion> suggestions = optimizationService.findOptimalRooms(request);
        return ResponseEntity.ok(suggestions);
    }
    
    @PostMapping("/find-alternatives")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<List<OptimalRoomSuggestion>> findAlternativeRooms(
            @RequestParam Long originalRoomId,
            @RequestBody RoomOptimizationRequest request) {
        
        log.info("Finding alternative rooms for room ID: {}", originalRoomId);
        List<OptimalRoomSuggestion> alternatives = optimizationService.findAlternativeRooms(originalRoomId, request);
        return ResponseEntity.ok(alternatives);
    }
    
    @PostMapping("/best-suggestion")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<OptimalRoomSuggestion> getBestRoomSuggestion(@RequestBody RoomOptimizationRequest request) {
        log.info("Getting best room suggestion for {} attendees", request.getExpectedAttendees());
        
        OptimalRoomSuggestion bestSuggestion = optimizationService.getBestRoomSuggestion(request);
        
        if (bestSuggestion != null) {
            return ResponseEntity.ok(bestSuggestion);
        } else {
            return ResponseEntity.noContent().build();
        }
    }
    
    @GetMapping("/quick-suggest")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<OptimalRoomSuggestion> quickSuggest(
            @RequestParam LocalDateTime startTime,
            @RequestParam LocalDateTime endTime,
            @RequestParam int expectedAttendees,
            @RequestParam(defaultValue = "COURS") String courseType) {
        
        RoomOptimizationRequest request = new RoomOptimizationRequest(startTime, endTime, expectedAttendees, courseType);
        OptimalRoomSuggestion suggestion = optimizationService.getBestRoomSuggestion(request);
        
        if (suggestion != null) {
            return ResponseEntity.ok(suggestion);
        } else {
            return ResponseEntity.noContent().build();
        }
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        log.error("Error in room optimization controller: {}", e.getMessage());
        return ResponseEntity.badRequest().body("Erreur lors de l'optimisation des salles: " + e.getMessage());
    }
}