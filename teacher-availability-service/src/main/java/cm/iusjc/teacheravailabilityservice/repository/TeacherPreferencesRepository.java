package cm.iusjc.teacheravailabilityservice.repository;

import cm.iusjc.teacheravailabilityservice.entity.TeacherPreferences;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TeacherPreferencesRepository extends JpaRepository<TeacherPreferences, Long> {
    
    // Recherche par enseignant
    Optional<TeacherPreferences> findByTeacherId(Long teacherId);
    
    // Recherche par nom d'enseignant
    List<TeacherPreferences> findByTeacherNameContainingIgnoreCase(String teacherName);
    
    // Recherche par préférences horaires
    @Query("SELECT tp FROM TeacherPreferences tp WHERE tp.preferredStartTime <= :time AND tp.preferredEndTime >= :time")
    List<TeacherPreferences> findByPreferredTimeRange(@Param("time") LocalTime time);
    
    // Recherche par jour préféré
    @Query("SELECT tp FROM TeacherPreferences tp JOIN tp.preferredDays pd WHERE pd = :day")
    List<TeacherPreferences> findByPreferredDay(@Param("day") DayOfWeek day);
    
    // Recherche des enseignants acceptant le multi-écoles
    List<TeacherPreferences> findByAcceptsMultiSchoolTrue();
    
    // Recherche par nombre maximum d'écoles par jour
    List<TeacherPreferences> findByMaxSchoolsPerDayGreaterThanEqual(Integer minSchools);
    
    // Recherche par préférences de cours du matin
    List<TeacherPreferences> findByPrefersMorningCoursesTrue();
    
    // Recherche par préférences de cours de l'après-midi
    List<TeacherPreferences> findByPrefersAfternoonCoursesTrue();
    
    // Recherche par préférences de cours du soir
    List<TeacherPreferences> findByPrefersEveningCoursesTrue();
    
    // Recherche par exigence de pause déjeuner
    List<TeacherPreferences> findByLunchBreakRequiredTrue();
    
    // Recherche par heures consécutives maximales
    List<TeacherPreferences> findByMaxConsecutiveHoursGreaterThanEqual(Integer minHours);
    
    // Recherche par notifications activées
    List<TeacherPreferences> findByNotifyScheduleChangesTrue();
    
    List<TeacherPreferences> findByNotifyConflictsTrue();
    
    // Recherche par temps de déplacement minimum
    List<TeacherPreferences> findByMinTravelTimeMinutesLessThanEqual(Integer maxTravelTime);
    
    // Vérification d'existence
    boolean existsByTeacherId(Long teacherId);
    
    // Suppression par enseignant
    void deleteByTeacherId(Long teacherId);
    
    // Statistiques
    @Query("SELECT COUNT(tp) FROM TeacherPreferences tp WHERE tp.acceptsMultiSchool = true")
    long countTeachersAcceptingMultiSchool();
    
    @Query("SELECT COUNT(tp) FROM TeacherPreferences tp WHERE tp.prefersMorningCourses = true")
    long countTeachersPreferringMorning();
    
    @Query("SELECT COUNT(tp) FROM TeacherPreferences tp WHERE tp.prefersAfternoonCourses = true")
    long countTeachersPreferringAfternoon();
    
    @Query("SELECT COUNT(tp) FROM TeacherPreferences tp WHERE tp.prefersEveningCourses = true")
    long countTeachersPreferringEvening();
    
    @Query("SELECT AVG(tp.maxConsecutiveHours) FROM TeacherPreferences tp")
    Double getAverageMaxConsecutiveHours();
    
    @Query("SELECT AVG(tp.minTravelTimeMinutes) FROM TeacherPreferences tp WHERE tp.acceptsMultiSchool = true")
    Double getAverageTravelTimeForMultiSchoolTeachers();
}