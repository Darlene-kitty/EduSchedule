package cm.iusjc.teacheravailabilityservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.util.Set;
import java.util.HashSet;

@Entity
@Table(name = "teacher_preferences")
public class TeacherPreferences {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    @Column(name = "teacher_id", unique = true)
    private Long teacherId;
    
    @Column(name = "teacher_name")
    private String teacherName;
    
    // Préférences horaires
    @Column(name = "preferred_start_time")
    private LocalTime preferredStartTime = LocalTime.of(8, 0);
    
    @Column(name = "preferred_end_time")
    private LocalTime preferredEndTime = LocalTime.of(18, 0);
    
    @ElementCollection(targetClass = DayOfWeek.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "teacher_preferred_days", joinColumns = @JoinColumn(name = "preferences_id"))
    @Column(name = "day_of_week")
    private Set<DayOfWeek> preferredDays = new HashSet<>();
    
    // Contraintes
    @Column(name = "max_consecutive_hours")
    private Integer maxConsecutiveHours = 4;
    
    @Column(name = "min_break_minutes")
    private Integer minBreakMinutes = 15;
    
    @Column(name = "lunch_break_required")
    private Boolean lunchBreakRequired = true;
    
    @Column(name = "lunch_break_start")
    private LocalTime lunchBreakStart = LocalTime.of(12, 0);
    
    @Column(name = "lunch_break_duration")
    private Integer lunchBreakDuration = 60; // minutes
    
    // Préférences de type de cours
    @Column(name = "prefers_morning_courses")
    private Boolean prefersMorningCourses = true;
    
    @Column(name = "prefers_afternoon_courses")
    private Boolean prefersAfternoonCourses = true;
    
    @Column(name = "prefers_evening_courses")
    private Boolean prefersEveningCourses = false;
    
    // Préférences multi-écoles
    @Column(name = "accepts_multi_school")
    private Boolean acceptsMultiSchool = false;
    
    @Column(name = "max_schools_per_day")
    private Integer maxSchoolsPerDay = 1;
    
    @Column(name = "min_travel_time_minutes")
    private Integer minTravelTimeMinutes = 30;
    
    // Notifications
    @Column(name = "notify_schedule_changes")
    private Boolean notifyScheduleChanges = true;
    
    @Column(name = "notify_conflicts")
    private Boolean notifyConflicts = true;
    
    @Column(name = "notification_advance_hours")
    private Integer notificationAdvanceHours = 24;
    
    // Métadonnées
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "notes")
    private String notes;
    
    // Constructors
    public TeacherPreferences() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        initializeDefaultPreferredDays();
    }
    
    public TeacherPreferences(Long teacherId) {
        this();
        this.teacherId = teacherId;
    }
    
    private void initializeDefaultPreferredDays() {
        preferredDays.add(DayOfWeek.MONDAY);
        preferredDays.add(DayOfWeek.TUESDAY);
        preferredDays.add(DayOfWeek.WEDNESDAY);
        preferredDays.add(DayOfWeek.THURSDAY);
        preferredDays.add(DayOfWeek.FRIDAY);
    }
    
    // Business methods
    public boolean isPreferredTime(LocalTime time) {
        return !time.isBefore(preferredStartTime) && !time.isAfter(preferredEndTime);
    }
    
    public boolean isPreferredDay(DayOfWeek day) {
        return preferredDays.contains(day);
    }
    
    public boolean isInLunchBreak(LocalTime time) {
        if (!lunchBreakRequired) {
            return false;
        }
        
        LocalTime lunchEnd = lunchBreakStart.plusMinutes(lunchBreakDuration);
        return !time.isBefore(lunchBreakStart) && !time.isAfter(lunchEnd);
    }
    
    public boolean acceptsTimeSlot(TimeSlot slot) {
        return isPreferredDay(slot.getDayOfWeek()) && 
               isPreferredTime(slot.getStartTime()) && 
               isPreferredTime(slot.getEndTime()) &&
               !conflictsWithLunchBreak(slot);
    }
    
    private boolean conflictsWithLunchBreak(TimeSlot slot) {
        if (!lunchBreakRequired) {
            return false;
        }
        
        LocalTime lunchEnd = lunchBreakStart.plusMinutes(lunchBreakDuration);
        return slot.getStartTime().isBefore(lunchEnd) && slot.getEndTime().isAfter(lunchBreakStart);
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    
    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }
    
    public LocalTime getPreferredStartTime() { return preferredStartTime; }
    public void setPreferredStartTime(LocalTime preferredStartTime) { this.preferredStartTime = preferredStartTime; }
    
    public LocalTime getPreferredEndTime() { return preferredEndTime; }
    public void setPreferredEndTime(LocalTime preferredEndTime) { this.preferredEndTime = preferredEndTime; }
    
    public Set<DayOfWeek> getPreferredDays() { return preferredDays; }
    public void setPreferredDays(Set<DayOfWeek> preferredDays) { this.preferredDays = preferredDays; }
    
    public Integer getMaxConsecutiveHours() { return maxConsecutiveHours; }
    public void setMaxConsecutiveHours(Integer maxConsecutiveHours) { this.maxConsecutiveHours = maxConsecutiveHours; }
    
    public Integer getMinBreakMinutes() { return minBreakMinutes; }
    public void setMinBreakMinutes(Integer minBreakMinutes) { this.minBreakMinutes = minBreakMinutes; }
    
    public Boolean getLunchBreakRequired() { return lunchBreakRequired; }
    public void setLunchBreakRequired(Boolean lunchBreakRequired) { this.lunchBreakRequired = lunchBreakRequired; }
    
    public LocalTime getLunchBreakStart() { return lunchBreakStart; }
    public void setLunchBreakStart(LocalTime lunchBreakStart) { this.lunchBreakStart = lunchBreakStart; }
    
    public Integer getLunchBreakDuration() { return lunchBreakDuration; }
    public void setLunchBreakDuration(Integer lunchBreakDuration) { this.lunchBreakDuration = lunchBreakDuration; }
    
    public Boolean getPrefersMorningCourses() { return prefersMorningCourses; }
    public void setPrefersMorningCourses(Boolean prefersMorningCourses) { this.prefersMorningCourses = prefersMorningCourses; }
    
    public Boolean getPrefersAfternoonCourses() { return prefersAfternoonCourses; }
    public void setPrefersAfternoonCourses(Boolean prefersAfternoonCourses) { this.prefersAfternoonCourses = prefersAfternoonCourses; }
    
    public Boolean getPrefersEveningCourses() { return prefersEveningCourses; }
    public void setPrefersEveningCourses(Boolean prefersEveningCourses) { this.prefersEveningCourses = prefersEveningCourses; }
    
    public Boolean getAcceptsMultiSchool() { return acceptsMultiSchool; }
    public void setAcceptsMultiSchool(Boolean acceptsMultiSchool) { this.acceptsMultiSchool = acceptsMultiSchool; }
    
    public Integer getMaxSchoolsPerDay() { return maxSchoolsPerDay; }
    public void setMaxSchoolsPerDay(Integer maxSchoolsPerDay) { this.maxSchoolsPerDay = maxSchoolsPerDay; }
    
    public Integer getMinTravelTimeMinutes() { return minTravelTimeMinutes; }
    public void setMinTravelTimeMinutes(Integer minTravelTimeMinutes) { this.minTravelTimeMinutes = minTravelTimeMinutes; }
    
    public Boolean getNotifyScheduleChanges() { return notifyScheduleChanges; }
    public void setNotifyScheduleChanges(Boolean notifyScheduleChanges) { this.notifyScheduleChanges = notifyScheduleChanges; }
    
    public Boolean getNotifyConflicts() { return notifyConflicts; }
    public void setNotifyConflicts(Boolean notifyConflicts) { this.notifyConflicts = notifyConflicts; }
    
    public Integer getNotificationAdvanceHours() { return notificationAdvanceHours; }
    public void setNotificationAdvanceHours(Integer notificationAdvanceHours) { this.notificationAdvanceHours = notificationAdvanceHours; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    @Override
    public String toString() {
        return String.format("TeacherPreferences{id=%d, teacherId=%d, preferredTime=%s-%s, preferredDays=%d}", 
                           id, teacherId, preferredStartTime, preferredEndTime, preferredDays.size());
    }
}