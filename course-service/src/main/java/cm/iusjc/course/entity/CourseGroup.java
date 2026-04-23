package cm.iusjc.course.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "course_groups")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseGroup {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long courseId;
    
    @Column(nullable = false, length = 50)
    private String groupName; // Ex: Groupe A, Groupe B, TD1, TP2
    
    @Column(nullable = false, length = 20)
    private String type; // COURS, TD, TP, EXAMEN
    
    @Column(name = "max_students")
    private Integer maxStudents;
    
    @Column(name = "current_students")
    private Integer currentStudents = 0;
    
    @Column(name = "teacher_id")
    private Long teacherId; // Enseignant pour ce groupe spécifique
    
    @Column(nullable = false)
    private Boolean active = true;
    
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