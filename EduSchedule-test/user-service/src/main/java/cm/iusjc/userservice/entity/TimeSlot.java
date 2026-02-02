package cm.iusjc.userservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity
@Table(name = "time_slots")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeSlot {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek;
    
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;
    
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;
    
    @Column(length = 100)
    private String description;
    
    // Constructeur utilitaire
    public TimeSlot(DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
    }
    
    // Méthodes utilitaires
    public boolean overlaps(TimeSlot other) {
        if (!this.dayOfWeek.equals(other.dayOfWeek)) {
            return false;
        }
        return this.startTime.isBefore(other.endTime) && this.endTime.isAfter(other.startTime);
    }
    
    public boolean contains(DayOfWeek day, LocalTime time) {
        return this.dayOfWeek.equals(day) && 
               !time.isBefore(this.startTime) && 
               time.isBefore(this.endTime);
    }
    
    public int getDurationMinutes() {
        return (int) java.time.Duration.between(startTime, endTime).toMinutes();
    }
}