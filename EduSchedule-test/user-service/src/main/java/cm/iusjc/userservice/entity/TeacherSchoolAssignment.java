package cm.iusjc.userservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "teacher_school_assignments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeacherSchoolAssignment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "teacher_id", nullable = false)
    private Long teacherId;
    
    @Column(name = "school_id", nullable = false)
    private Long schoolId;
    
    @Column(name = "school_name")
    private String schoolName;
    
    @Column(name = "school_address")
    private String schoolAddress;
    
    @Column(name = "is_primary_school")
    private Boolean isPrimarySchool = false;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "working_days", nullable = false)
    @Enumerated(EnumType.STRING)
    @ElementCollection(targetClass = DayOfWeek.class)
    @CollectionTable(name = "teacher_working_days", joinColumns = @JoinColumn(name = "assignment_id"))
    private List<DayOfWeek> workingDays;
    
    @Column(name = "start_time")
    private LocalTime startTime; // Heure d'arrivée habituelle
    
    @Column(name = "end_time")
    private LocalTime endTime; // Heure de départ habituelle
    
    @Column(name = "travel_time_minutes")
    private Integer travelTimeMinutes = 30; // Temps de déplacement par défaut
    
    @Column(name = "max_hours_per_day")
    private Integer maxHoursPerDay = 8; // Nombre max d'heures par jour dans cette école
    
    @Column(name = "max_hours_per_week")
    private Integer maxHoursPerWeek = 20; // Nombre max d'heures par semaine dans cette école
    
    @Column(name = "priority")
    private Integer priority = 1; // 1=École principale, 2=École secondaire, etc.
    
    @Column(name = "contract_type")
    @Enumerated(EnumType.STRING)
    private ContractType contractType = ContractType.PERMANENT;
    
    @Column(name = "effective_from", nullable = false)
    private LocalDateTime effectiveFrom;
    
    @Column(name = "effective_to")
    private LocalDateTime effectiveTo; // null = indéterminé
    
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
    public boolean isActiveOn(LocalDateTime dateTime) {
        return active && 
               !dateTime.isBefore(effectiveFrom) && 
               (effectiveTo == null || !dateTime.isAfter(effectiveTo)) &&
               workingDays.contains(dateTime.getDayOfWeek());
    }
    
    public boolean isWithinWorkingHours(LocalTime time) {
        return startTime != null && endTime != null &&
               !time.isBefore(startTime) && !time.isAfter(endTime);
    }
}