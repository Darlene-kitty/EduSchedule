package cm.iusjc.course.repository;

import cm.iusjc.course.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    
    // Recherche par code (unique)
    Optional<Course> findByCode(String code);
    
    // Vérifier si le code existe déjà
    boolean existsByCode(String code);
    
    // Recherche par département
    List<Course> findByDepartmentAndActiveTrue(String department);
    
    // Recherche par niveau
    List<Course> findByLevelAndActiveTrue(String level);
    
    // Recherche par semestre
    List<Course> findBySemesterAndActiveTrue(String semester);
    
    // Recherche par enseignant
    List<Course> findByTeacherIdAndActiveTrue(Long teacherId);
    
    // Recherche par département et niveau
    List<Course> findByDepartmentAndLevelAndActiveTrue(String department, String level);
    
    // Recherche par nom (contient)
    @Query("SELECT c FROM Course c WHERE c.name LIKE %:name% AND c.active = true")
    List<Course> findByNameContainingAndActiveTrue(@Param("name") String name);
    
    // Recherche avancée avec pagination
    @Query("SELECT c FROM Course c WHERE " +
           "(:department IS NULL OR c.department = :department) AND " +
           "(:level IS NULL OR c.level = :level) AND " +
           "(:semester IS NULL OR c.semester = :semester) AND " +
           "(:teacherId IS NULL OR c.teacherId = :teacherId) AND " +
           "c.active = true")
    Page<Course> findCoursesWithFilters(
        @Param("department") String department,
        @Param("level") String level,
        @Param("semester") String semester,
        @Param("teacherId") Long teacherId,
        Pageable pageable
    );
    
    // Compter les cours par département
    @Query("SELECT c.department, COUNT(c) FROM Course c WHERE c.active = true GROUP BY c.department")
    List<Object[]> countCoursesByDepartment();
    
    // Cours actifs seulement
    List<Course> findByActiveTrue();
}