package cm.iusjc.roomservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomDTO {
    
    private Long id;
    
    @NotBlank(message = "Room name is required")
    @Size(min = 1, max = 100, message = "Room name must be between 1 and 100 characters")
    private String name;
    
    @Size(max = 20, message = "Room code cannot exceed 20 characters")
    private String code;
    
    @NotBlank(message = "Room type is required")
    @Size(max = 50, message = "Room type cannot exceed 50 characters")
    private String type; // CLASSROOM, LABORATORY, AMPHITHEATER, OFFICE, etc.
    
    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;
    
    @NotBlank(message = "Building is required")
    @Size(max = 50, message = "Building cannot exceed 50 characters")
    private String building;
    
    @NotNull(message = "Floor is required")
    private Integer floor;
    
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
    
    private List<String> equipments; // Liste des équipements disponibles
    
    private boolean accessible = false; // Accessible aux personnes à mobilité réduite
    
    private boolean available = true; // Disponible pour réservation
    
    @NotNull(message = "School ID is required")
    private Long schoolId;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Champs calculés (non persistés)
    private String schoolName;
    private Integer activeReservations; // Nombre de réservations actives
    private Double utilizationRate; // Taux d'utilisation
    private String status; // AVAILABLE, OCCUPIED, MAINTENANCE, etc.
}