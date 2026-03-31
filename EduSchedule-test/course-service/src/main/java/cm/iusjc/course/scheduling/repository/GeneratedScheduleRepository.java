package cm.iusjc.course.scheduling.repository;

import cm.iusjc.course.scheduling.entity.GeneratedSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GeneratedScheduleRepository extends JpaRepository<GeneratedSchedule, Long> {

    List<GeneratedSchedule> findBySchoolIdAndSemesterAndLevel(Long schoolId, String semester, String level);

    List<GeneratedSchedule> findByJobId(String jobId);

    void deleteBySchoolIdAndSemesterAndLevel(Long schoolId, String semester, String level);

    /** Tous les créneaux d'un enseignant dans un planning actif */
    List<GeneratedSchedule> findByTeacherId(Long teacherId);

    /** Créneaux d'un enseignant pour une école donnée */
    List<GeneratedSchedule> findByTeacherIdAndSchoolId(Long teacherId, Long schoolId);

    /** Créneaux actifs d'un enseignant sur un jour+heure donnés */
    @Query("""
        SELECT g FROM GeneratedSchedule g
        WHERE g.teacherId = :teacherId
          AND g.dayOfWeek = :dayOfWeek
          AND g.startTime = :startTime
          AND g.status = 'ACTIVE'
        """)
    List<GeneratedSchedule> findActiveByTeacherAndSlot(
            @Param("teacherId") Long teacherId,
            @Param("dayOfWeek") String dayOfWeek,
            @Param("startTime") String startTime);

    /** Tous les créneaux ACTIVE d'une école/semestre/niveau */
    @Query("""
        SELECT g FROM GeneratedSchedule g
        WHERE g.schoolId = :schoolId
          AND g.semester = :semester
          AND g.level = :level
          AND g.status = 'ACTIVE'
        """)
    List<GeneratedSchedule> findActiveBySchoolSemesterLevel(
            @Param("schoolId") Long schoolId,
            @Param("semester") String semester,
            @Param("level") String level);

    /** Créneaux en conflit (status=CONFLICT) pour un enseignant */
    List<GeneratedSchedule> findByTeacherIdAndStatus(Long teacherId, String status);

    /** Tous les créneaux en conflit toutes écoles */
    List<GeneratedSchedule> findByStatus(String status);

    /** Conflits de salle : même salle, même jour, même créneau, IDs différents */
    @Query("""
        SELECT g FROM GeneratedSchedule g
        WHERE g.roomId = :roomId
          AND g.dayOfWeek = :dayOfWeek
          AND g.startTime = :startTime
          AND g.id <> :excludeId
        """)
    List<GeneratedSchedule> findRoomConflicts(
            @Param("roomId") Long roomId,
            @Param("dayOfWeek") String dayOfWeek,
            @Param("startTime") String startTime,
            @Param("excludeId") Long excludeId);

    /** Conflits enseignant : même enseignant, même jour, même créneau, IDs différents */
    @Query("""
        SELECT g FROM GeneratedSchedule g
        WHERE g.teacherId = :teacherId
          AND g.dayOfWeek = :dayOfWeek
          AND g.startTime = :startTime
          AND g.id <> :excludeId
        """)
    List<GeneratedSchedule> findTeacherConflicts(
            @Param("teacherId") Long teacherId,
            @Param("dayOfWeek") String dayOfWeek,
            @Param("startTime") String startTime,
            @Param("excludeId") Long excludeId);

    /** Tous les créneaux d'un enseignant sur un créneau donné (toutes écoles) */
    @Query("""
        SELECT g FROM GeneratedSchedule g
        WHERE g.teacherId = :teacherId
          AND g.dayOfWeek = :dayOfWeek
          AND g.startTime = :startTime
        """)
    List<GeneratedSchedule> findByTeacherAndSlot(
            @Param("teacherId") Long teacherId,
            @Param("dayOfWeek") String dayOfWeek,
            @Param("startTime") String startTime);
}
