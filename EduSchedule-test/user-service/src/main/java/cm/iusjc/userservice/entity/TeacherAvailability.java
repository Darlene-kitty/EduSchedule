package cm.iusjc.userservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Table(name = "teacher_availability")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeacherAvailability {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "teacher_id", nullable = false)
    private Long teacherId;
    
    @Column(name = "school_id")
    private Long schoolId; // Disponibilité spécifique à une école
    
    @Column(name = "day_of_week", nullable = false)
    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek;
    
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;
    
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;
    
    @Column(name = "availability_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private AvailabilityType availabilityType = AvailabilityType.AVAILABLE;
    
    @Column(name = "recurring", nullable = false)
    private Boolean recurring = true; // Récurrent chaque semaine
    
    @Column(name = "specific_date")
    private LocalDateTime specificDate; // Pour les disponibilités ponctuelles
    
    @Column(name = "priority")
    private Integer priority = 1; // 1=Préféré, 2=Acceptable, 3=Si nécessaire
    
    @Column(length = 500)
    private String notes;
    
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
    
    // Méthodes utilitaires
    public boolean isAvailableAt(LocalTime time) {
        return availabilityType == AvailabilityType.AVAILABLE && 
               !time.isBefore(startTime) && 
               !time.isAfter(endTime);
    }
    
    public boolean overlaps(LocalTime start, LocalTime end) {
        return startTime.isBefore(end) && endTime.isAfter(start);
    }
}