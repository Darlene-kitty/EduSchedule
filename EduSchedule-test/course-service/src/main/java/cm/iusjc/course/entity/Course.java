package cm.iusjc.course.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "courses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Course {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(nullable = false, unique = true, length = 20)
    private String code; // Ex: INF101, MAT201
    
    @Column(length = 500)
    private String description;
    
    @Column(nullable = false)
    private Integer credits; // Nombre de crédits
    
    @Column(nullable = false)
    private Integer duration; // Durée en minutes
    
    @Column(nullable = false, length = 100)
    private String department; // Département
    
    @Column(nullable = false, length = 50)
    private String level; // L1, L2, L3, M1, M2
    
    @Column(nullable = false, length = 50)
    private String semester; // S1, S2
    
    @Column(name = "teacher_id")
    private Long teacherId; // ID de l'enseignant responsable
    
    @Column(name = "school_id")
    private Long schoolId; // ID de l'école
    
    @Column(name = "hours_per_week")
    private Integer hoursPerWeek; // Nombre d'heures par semaine
    
    @Column(name = "max_students")
    private Integer maxStudents; // Nombre maximum d'étudiants
    
    @Column(nullable = false)
    private boolean active = true;
    
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