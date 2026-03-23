package cm.iusjc.course.scheduling.repository;

import cm.iusjc.course.scheduling.entity.GeneratedSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GeneratedScheduleRepository extends JpaRepository<GeneratedSchedule, Long> {

    List<GeneratedSchedule> findBySchoolIdAndSemesterAndLevel(Long schoolId, String semester, String level);

    List<GeneratedSchedule> findByJobId(String jobId);

    void deleteBySchoolIdAndSemesterAndLevel(Long schoolId, String semester, String level);
}
