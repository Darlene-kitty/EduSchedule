package cm.iusjc.roomservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "rooms")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Room {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(unique = true, length = 50)
    private String code;
    
    @Column(nullable = false, length = 50)
    private String type; // CLASSROOM, LABORATORY, AMPHITHEATER, OFFICE, etc.
    
    @Column(nullable = false)
    private Integer capacity;
    
    @Column(nullable = false, length = 50)
    private String building;
    
    @Column(nullable = false)
    private Integer floor;
    
    @Column(length = 500)
    private String description;
    
    @ElementCollection
    @CollectionTable(name = "room_equipments", joinColumns = @JoinColumn(name = "room_id"))
    @Column(name = "equipment")
    private List<String> equipments;
    
    @Column(nullable = false)
    private boolean accessible = false; // Accessible aux personnes à mobilité réduite
    
    @Column(nullable = false)
    private boolean available = true; // Disponible pour réservation
    
    @Column(name = "school_id", nullable = false)
    private Long schoolId;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}