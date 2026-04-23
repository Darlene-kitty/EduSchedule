package cm.iusjc.course;

import cm.iusjc.course.entity.Course;
import cm.iusjc.course.entity.CourseGroup;
import cm.iusjc.course.repository.CourseGroupRepository;
import cm.iusjc.course.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final CourseRepository      courseRepository;
    private final CourseGroupRepository courseGroupRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (courseRepository.count() > 0) {
            log.info("[DataInitializer] Courses already seeded — skipping.");
            return;
        }

        log.info("[DataInitializer] Seeding courses and groups...");

        // schoolId=1 → SJI, schoolId=2 → SJM, schoolId=3 → PRÉPAVOGT, schoolId=4 → CPGE
        // teacherId matches user-service seed order: teacher1=2, teacher2=3, ..., teacher9=10

        Course math   = course("Mathématiques",        "MATH",   "Mathématiques",          3, 120, "L1", "S1", 1L, 6L,  2);
        Course phys   = course("Physique",             "PHYS",   "Sciences Physiques",      3, 120, "L1", "S1", 1L, 7L,  2);
        Course info   = course("Informatique",         "INFO",   "Génie Informatique",      4, 120, "L1", "S1", 1L, 1L,  3);
        Course elec   = course("Électronique",         "ELEC",   "Génie Électronique",      3,  90, "L2", "S3", 1L, 3L,  2);
        Course gest   = course("Gestion & Management", "GEST",   "Management",              3, 120, "L1", "S1", 2L, 4L,  2);
        Course compta = course("Comptabilité",         "COMPTA", "Finance",                 3,  90, "L2", "S3", 2L, 5L,  2);
        Course droit  = course("Droit des Affaires",   "DROIT",  "Droit",                   2,  90, "L2", "S3", 2L, 4L,  2);
        Course lang   = course("Langues",              "LANG",   "Langues & Communication", 2,  90, "L1", "S1", 1L, 9L,  2);
        Course chim   = course("Chimie",               "CHIM",   "Sciences",                3,  90, "L1", "S1", 1L, 3L,  2);
        Course mkt    = course("Marketing",            "MKT",    "Management",              3,  90, "L2", "S3", 2L, 8L,  2);

        // Groups for INFO (3 groups)
        group(info.getId(), "Groupe A", "COURS", 35, 1L);
        group(info.getId(), "Groupe B", "COURS", 35, 1L);
        group(info.getId(), "TP1",      "TP",    20, 1L);

        // Groups for MATH
        group(math.getId(), "Groupe A", "COURS", 35, 6L);
        group(math.getId(), "Groupe B", "COURS", 35, 6L);

        // Groups for GEST
        group(gest.getId(), "Groupe A", "COURS", 35, 4L);
        group(gest.getId(), "Groupe B", "COURS", 35, 4L);

        // Groups for ELEC
        group(elec.getId(), "TD1", "TD", 25, 3L);
        group(elec.getId(), "TD2", "TD", 25, 3L);

        // Groups for COMPTA
        group(compta.getId(), "Groupe A", "TD", 30, 5L);

        // Groups for MKT
        group(mkt.getId(), "Groupe A", "COURS", 35, 8L);

        log.info("[DataInitializer] Done — 10 courses, 11 groups seeded.");
    }

    private Course course(String name, String code, String dept, int credits, int duration,
                          String level, String semester, Long schoolId, Long teacherId, int hoursPerWeek) {
        Course c = new Course();
        c.setName(name);
        c.setCode(code);
        c.setDepartment(dept);
        c.setCredits(credits);
        c.setDuration(duration);
        c.setLevel(level);
        c.setSemester(semester);
        c.setSchoolId(schoolId);
        c.setTeacherId(teacherId);
        c.setHoursPerWeek(hoursPerWeek);
        c.setMaxStudents(70);
        c.setActive(true);
        return courseRepository.save(c);
    }

    private void group(Long courseId, String groupName, String type, int maxStudents, Long teacherId) {
        CourseGroup g = new CourseGroup();
        g.setCourseId(courseId);
        g.setGroupName(groupName);
        g.setType(type);
        g.setMaxStudents(maxStudents);
        g.setCurrentStudents(0);
        g.setTeacherId(teacherId);
        g.setActive(true);
        courseGroupRepository.save(g);
    }
}
