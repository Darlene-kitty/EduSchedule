package cm.iusjc.roomservice.controller;

import cm.iusjc.roomservice.dto.RoomDTO;
import cm.iusjc.roomservice.service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class RoomController {
    
    private final RoomService roomService;
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Room Service is healthy");
    }
    
    /**
     * Crée une nouvelle salle
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createRoom(@Valid @RequestBody RoomDTO roomDTO) {
        try {
            log.info("Creating room: {}", roomDTO.getName());
            RoomDTO createdRoom = roomService.createRoom(roomDTO);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "message", "Room created successfully",
                "data", createdRoom
            ));
        } catch (Exception e) {
            log.error("Error creating room: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Récupère toutes les salles
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllRooms() {
        try {
            List<RoomDTO> rooms = roomService.getAllRooms();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", rooms,
                "total", rooms.size()
            ));
        } catch (Exception e) {
            log.error("Error fetching rooms: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Récupère les salles avec pagination
     */
    @GetMapping("/paginated")
    public ResponseEntity<Map<String, Object>> getAllRoomsPaginated(Pageable pageable) {
        try {
            Page<RoomDTO> roomsPage = roomService.getAllRooms(pageable);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", roomsPage.getContent(),
                "page", roomsPage.getNumber(),
                "size", roomsPage.getSize(),
                "totalElements", roomsPage.getTotalElements(),
                "totalPages", roomsPage.getTotalPages()
            ));
        } catch (Exception e) {
            log.error("Error fetching paginated rooms: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Récupère une salle par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getRoomById(@PathVariable Long id) {
        try {
            return roomService.getRoomById(id)
                    .map(room -> ResponseEntity.ok(Map.of(
                        "success", true,
                        "data", room
                    )))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error fetching room by ID {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Récupère les salles disponibles
     */
    @GetMapping("/available")
    public ResponseEntity<Map<String, Object>> getAvailableRooms() {
        try {
            List<RoomDTO> availableRooms = roomService.getAvailableRooms();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", availableRooms,
                "total", availableRooms.size()
            ));
        } catch (Exception e) {
            log.error("Error fetching available rooms: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Récupère les salles par école
     */
    @GetMapping("/school/{schoolId}")
    public ResponseEntity<Map<String, Object>> getRoomsBySchool(@PathVariable Long schoolId) {
        try {
            List<RoomDTO> rooms = roomService.getRoomsBySchool(schoolId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", rooms,
                "total", rooms.size()
            ));
        } catch (Exception e) {
            log.error("Error fetching rooms by school {}: {}", schoolId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Récupère les salles par type
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<Map<String, Object>> getRoomsByType(@PathVariable String type) {
        try {
            List<RoomDTO> rooms = roomService.getRoomsByType(type);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", rooms,
                "total", rooms.size()
            ));
        } catch (Exception e) {
            log.error("Error fetching rooms by type {}: {}", type, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Recherche avancée de salles
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchRooms(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer minCapacity,
            @RequestParam(required = false) String building,
            @RequestParam(required = false) Integer floor,
            @RequestParam(required = false) List<String> equipments,
            @RequestParam(required = false) Boolean accessible) {
        try {
            List<RoomDTO> rooms;
            
            if (name != null && !name.trim().isEmpty()) {
                rooms = roomService.searchRoomsByName(name);
            } else {
                rooms = roomService.searchRoomsWithFilters(type, minCapacity, building, floor, equipments, accessible);
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", rooms,
                "total", rooms.size()
            ));
        } catch (Exception e) {
            log.error("Error searching rooms: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Met à jour une salle
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateRoom(
            @PathVariable Long id, 
            @Valid @RequestBody RoomDTO roomDTO) {
        try {
            RoomDTO updatedRoom = roomService.updateRoom(id, roomDTO);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Room updated successfully",
                "data", updatedRoom
            ));
        } catch (Exception e) {
            log.error("Error updating room {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Active/désactive une salle
     */
    @PatchMapping("/{id}/toggle-availability")
    public ResponseEntity<Map<String, Object>> toggleRoomAvailability(@PathVariable Long id) {
        try {
            RoomDTO updatedRoom = roomService.toggleRoomAvailability(id);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Room availability updated successfully",
                "data", updatedRoom
            ));
        } catch (Exception e) {
            log.error("Error toggling room availability {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Met à jour les équipements d'une salle
     */
    @PatchMapping("/{id}/equipments")
    public ResponseEntity<Map<String, Object>> updateRoomEquipments(
            @PathVariable Long id, 
            @RequestBody List<String> equipments) {
        try {
            RoomDTO updatedRoom = roomService.updateRoomEquipments(id, equipments);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Room equipments updated successfully",
                "data", updatedRoom
            ));
        } catch (Exception e) {
            log.error("Error updating room equipments {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Supprime une salle (soft delete)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteRoom(@PathVariable Long id) {
        try {
            roomService.deleteRoom(id);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Room deleted successfully"
            ));
        } catch (Exception e) {
            log.error("Error deleting room {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Obtient les statistiques des salles
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getRoomStatistics() {
        try {
            long totalRooms = roomService.countRooms();
            long availableRooms = roomService.countAvailableRooms();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", Map.of(
                    "totalRooms", totalRooms,
                    "availableRooms", availableRooms,
                    "unavailableRooms", totalRooms - availableRooms,
                    "typeStats", roomService.getRoomStatisticsByType(),
                    "buildingStats", roomService.getRoomStatisticsByBuilding(),
                    "averageUtilization", roomService.getAverageRoomUtilization()
                )
            ));
        } catch (Exception e) {
            log.error("Error fetching room statistics: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
}