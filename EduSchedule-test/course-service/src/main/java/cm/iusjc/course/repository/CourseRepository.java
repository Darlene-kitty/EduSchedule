package cm.iusjc.course.repository;

import cm.iusjc.course.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    
    /**
     * Trouve un cours par code
     */
    Optional<Course> findByCode(String code);
    
    /**
     * Vérifie si un cours existe par code
     */
    boolean existsByCode(String code);
    
    /**
     * Vérifie si un cours existe par code et école
     */
    boolean existsByCodeAndSchoolId(String code, Long schoolId);
    
    /**
     * Trouve tous les cours actifs
     */
    List<Course> findByActiveTrue();
    
    /**
     * Trouve tous les cours inactifs
     */
    List<Course> findByActiveFalse();
    
    /**
     * Compte les cours actifs
     */
    long countByActiveTrue();
    
    /**
     * Compte les cours inactifs
     */
    long countByActiveFalse();
    
    /**
     * Trouve les cours par école
     */
    List<Course> findBySchoolId(Long schoolId);
    
    /**
     * Trouve les cours par enseignant
     */
    List<Course> findByTeacherId(Long teacherId);
    
    /**
     * Trouve les cours par département
     */
    List<Course> findByDepartment(String department);
    
    /**
     * Trouve les cours par niveau
     */
    List<Course> findByLevel(String level);
    
    /**
     * Compte les cours par école
     */
    long countBySchoolId(Long schoolId);
    
    /**
     * Compte les cours par enseignant
     */
    long countByTeacherId(Long teacherId);
    
    /**
     * Compte les cours par département
     */
    long countByDepartment(String department);
    
    /**
     * Recherche des cours par nom (contient, insensible à la casse)
     */
    List<Course> findByNameContainingIgnoreCase(String name);
    
    /**
     * Recherche des cours par code (contient, insensible à la casse)
     */
    List<Course> findByCodeContainingIgnoreCase(String code);
    
    /**
     * Recherche globale dans les cours
     */
    @Query("SELECT c FROM Course c WHERE " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.code) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.department) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.level) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Course> searchCourses(@Param("searchTerm") String searchTerm);
    
    /**
     * Trouve les cours par école et statut actif
     */
    @Query("SELECT c FROM Course c WHERE c.schoolId = :schoolId AND c.active = :active")
    List<Course> findBySchoolIdAndActive(@Param("schoolId") Long schoolId, @Param("active") boolean active);
    
    /**
     * Trouve les cours par enseignant et statut actif
     */
    @Query("SELECT c FROM Course c WHERE c.teacherId = :teacherId AND c.active = :active")
    List<Course> findByTeacherIdAndActive(@Param("teacherId") Long teacherId, @Param("active") boolean active);
    
    /**
     * Trouve les cours les plus récents
     */
    @Query("SELECT c FROM Course c ORDER BY c.createdAt DESC")
    List<Course> findRecentCourses();
    
    /**
     * Compte les cours par département
     */
    @Query("SELECT c.department, COUNT(c) FROM Course c WHERE c.active = true GROUP BY c.department ORDER BY COUNT(c) DESC")
    List<Object[]> getCourseCountByDepartment();
    
    /**
     * Compte les cours par niveau
     */
    @Query("SELECT c.level, COUNT(c) FROM Course c WHERE c.active = true GROUP BY c.level ORDER BY COUNT(c) DESC")
    List<Object[]> getCourseCountByLevel();
    
    /**
     * Trouve les cours avec le plus de crédits
     */
    @Query("SELECT c FROM Course c WHERE c.active = true ORDER BY c.credits DESC")
    List<Course> findCoursesOrderByCredits();
    
    /**
     * Trouve les cours avec le plus d'heures par semaine
     */
    @Query("SELECT c FROM Course c WHERE c.active = true ORDER BY c.hoursPerWeek DESC")
    List<Course> findCoursesOrderByHoursPerWeek();
    
    /**
     * Trouve les cours populaires (avec le plus de groupes)
     */
    @Query("SELECT c FROM Course c WHERE c.active = true ORDER BY " +
           "(SELECT COUNT(cg) FROM CourseGroup cg WHERE cg.courseId = c.id) DESC")
    List<Course> findPopularCourses();
    
    /**
     * Trouve les cours sans enseignant assigné
     */
    @Query("SELECT c FROM Course c WHERE c.teacherId IS NULL AND c.active = true")
    List<Course> findCoursesWithoutTeacher();
    
    /**
     * Trouve les cours par plage de crédits
     */
    @Query("SELECT c FROM Course c WHERE c.credits BETWEEN :minCredits AND :maxCredits AND c.active = true")
    List<Course> findCoursesByCreditsRange(@Param("minCredits") Integer minCredits, @Param("maxCredits") Integer maxCredits);
    
    /**
     * Trouve les cours par plage d'heures par semaine
     */
    @Query("SELECT c FROM Course c WHERE c.hoursPerWeek BETWEEN :minHours AND :maxHours AND c.active = true")
    List<Course> findCoursesByHoursRange(@Param("minHours") Integer minHours, @Param("maxHours") Integer maxHours);
    
    /**
     * Calcule la charge totale d'un enseignant (somme des heures par semaine)
     */
    @Query("SELECT SUM(c.hoursPerWeek) FROM Course c WHERE c.teacherId = :teacherId AND c.active = true")
    Integer calculateTeacherWorkload(@Param("teacherId") Long teacherId);
    
    /**
     * Trouve les cours d'un département par niveau
     */
    @Query("SELECT c FROM Course c WHERE c.department = :department AND c.level = :level AND c.active = true")
    List<Course> findByDepartmentAndLevel(@Param("department") String department, @Param("level") String level);
}