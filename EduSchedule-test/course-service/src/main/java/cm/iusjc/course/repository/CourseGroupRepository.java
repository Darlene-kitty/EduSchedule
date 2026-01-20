package cm.iusjc.course.repository;

import cm.iusjc.course.entity.CourseGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseGroupRepository extends JpaRepository<CourseGroup, Long> {
    
    // Groupes par cours
    List<CourseGroup> findByCourseIdAndActiveTrue(Long courseId);
    
    // Groupes par enseignant
    List<CourseGroup> findByTeacherIdAndActiveTrue(Long teacherId);
    
    // Groupes par type
    List<CourseGroup> findByTypeAndActiveTrue(String type);
    
    // Groupes par cours et type
    List<CourseGroup> findByCourseIdAndTypeAndActiveTrue(Long courseId, String type);
    
    // Vérifier si un nom de groupe existe déjà pour un cours
    boolean existsByCourseIdAndGroupNameAndActiveTrue(Long courseId, String groupName);
    
    // Compter les groupes par cours
    @Query("SELECT COUNT(cg) FROM CourseGroup cg WHERE cg.courseId = :courseId AND cg.active = true")
    Long countByCourseId(@Param("courseId") Long courseId);
    
    // Groupes avec places disponibles
    @Query("SELECT cg FROM CourseGroup cg WHERE cg.currentStudents < cg.maxStudents AND cg.active = true")
    List<CourseGroup> findGroupsWithAvailableSpots();
    
    // Groupes par cours avec places disponibles
    @Query("SELECT cg FROM CourseGroup cg WHERE cg.courseId = :courseId AND cg.currentStudents < cg.maxStudents AND cg.active = true")
    List<CourseGroup> findAvailableGroupsByCourse(@Param("courseId") Long courseId);
}