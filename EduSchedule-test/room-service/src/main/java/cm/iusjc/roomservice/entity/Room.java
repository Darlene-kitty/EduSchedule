package cm.iusjc.roomservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "rooms")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Room {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 50)
    private String code; // Ex: A101, B203, Lab1
    
    @Column(nullable = false, length = 100)
    private String nom; // Changed from 'name' to 'nom' for frontend compatibility
    
    @Column(length = 500)
    private String description;
    
    @Column(nullable = false)
    private String type; // Changed from enum to String for simplicity
    
    @Column(nullable = false)
    private Integer capacite; // Changed from 'capacity' to 'capacite'
    
    @Column(name = "school_id", nullable = false)
    private Long schoolId;
    
    @Column(length = 50)
    private String batiment; // Changed from 'buildingId' to 'batiment'
    
    @Column
    private Integer etage; // Changed from 'floor' to 'etage'
    
    @Column(nullable = false)
    private Boolean disponible = true; // Changed from 'active' to 'disponible'
    
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