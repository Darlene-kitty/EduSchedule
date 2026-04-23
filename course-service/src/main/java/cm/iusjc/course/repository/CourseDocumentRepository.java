package cm.iusjc.course.repository;

import cm.iusjc.course.entity.CourseDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CourseDocumentRepository extends JpaRepository<CourseDocument, Long> {
    List<CourseDocument> findByCourseIdOrderByCreatedAtDesc(Long courseId);
    void deleteByCourseId(Long courseId);
    long countByCourseId(Long courseId);
}
