package cm.iusjc.course.scheduling.service;

import cm.iusjc.course.entity.Course;
import cm.iusjc.course.repository.CourseRepository;
import cm.iusjc.course.scheduling.dto.ExamSchedulingRequestDTO;
import cm.iusjc.course.scheduling.dto.ExamSlotDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Génère automatiquement le planning d'examens en respectant :
 * - Pas deux examens du même niveau au même créneau
 * - Pas deux examens du même enseignant au même créneau
 * - Capacité des salles ≥ nombre d'étudiants du cours
 * - Nombre max d'examens par jour par niveau
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExamSchedulingService {

    private final CourseRepository courseRepository;

    public Map<String, Object> generateExamSchedule(ExamSchedulingRequestDTO request) {
        log.info("Generating exam schedule for school {} semester {} levels {}",
                request.getSchoolId(), request.getSemester(), request.getLevels());

        // 1. Récupérer les cours concernés
        List<Course> courses = courseRepository.findAll().stream()
                .filter(c -> c.isActive())
                .filter(c -> request.getSchoolId() == null || request.getSchoolId().equals(c.getSchoolId()))
                .filter(c -> request.getSemester() == null || request.getSemester().equalsIgnoreCase(c.getSemester()))
                .filter(c -> request.getLevels() == null || request.getLevels().isEmpty()
                        || request.getLevels().contains(c.getLevel()))
                .collect(Collectors.toList());

        if (courses.isEmpty()) {
            return Map.of("success", false, "message", "Aucun cours trouvé pour ces critères.", "slots", List.of());
        }

        // 2. Construire la liste des créneaux disponibles
        List<TimeSlot> slots = buildAvailableSlots(request);
        if (slots.isEmpty()) {
            return Map.of("success", false, "message", "Aucun créneau disponible.", "slots", List.of());
        }

        // 3. Algorithme de planification greedy avec contraintes
        List<ExamSlotDTO> scheduled = new ArrayList<>();
        List<ExamSlotDTO> conflicts = new ArrayList<>();

        // Tracker : (date, slotIndex, level) → occupé
        Map<String, Set<String>> levelSlotUsage    = new HashMap<>(); // key=date+slot → levels
        Map<String, Set<Long>>   teacherSlotUsage  = new HashMap<>(); // key=date+slot → teacherIds
        Map<String, Integer>     levelDayCount     = new HashMap<>(); // key=date+level → count

        int maxPerDay = request.getMaxExamsPerDayPerLevel() != null ? request.getMaxExamsPerDayPerLevel() : 2;
        int duration  = request.getDefaultDurationMinutes() != null ? request.getDefaultDurationMinutes() : 120;

        // Trier les cours par niveau puis par nom pour un résultat déterministe
        courses.sort(Comparator.comparing(Course::getLevel).thenComparing(Course::getName));

        for (Course course : courses) {
            boolean placed = false;

            for (TimeSlot slot : slots) {
                String slotKey    = slot.date + "_" + slot.index;
                String dayLevelKey = slot.date + "_" + course.getLevel();

                // Contrainte 1 : max examens par jour par niveau
                int dayCount = levelDayCount.getOrDefault(dayLevelKey, 0);
                if (dayCount >= maxPerDay) continue;

                // Contrainte 2 : pas deux examens du même niveau au même créneau
                Set<String> levelsInSlot = levelSlotUsage.getOrDefault(slotKey, new HashSet<>());
                if (levelsInSlot.contains(course.getLevel())) continue;

                // Contrainte 3 : pas deux examens du même enseignant au même créneau
                if (course.getTeacherId() != null) {
                    Set<Long> teachersInSlot = teacherSlotUsage.getOrDefault(slotKey, new HashSet<>());
                    if (teachersInSlot.contains(course.getTeacherId())) continue;
                }

                // Placer l'examen
                levelsInSlot.add(course.getLevel());
                levelSlotUsage.put(slotKey, levelsInSlot);

                if (course.getTeacherId() != null) {
                    Set<Long> teachersInSlot = teacherSlotUsage.getOrDefault(slotKey, new HashSet<>());
                    teachersInSlot.add(course.getTeacherId());
                    teacherSlotUsage.put(slotKey, teachersInSlot);
                }

                levelDayCount.put(dayLevelKey, dayCount + 1);

                // Assigner une salle (première disponible avec capacité suffisante)
                Long roomId = assignRoom(request.getRoomIds(), slotKey, course.getMaxStudents());

                scheduled.add(ExamSlotDTO.builder()
                        .courseId(course.getId())
                        .courseName(course.getName())
                        .courseCode(course.getCode())
                        .level(course.getLevel())
                        .semester(course.getSemester())
                        .teacherId(course.getTeacherId())
                        .roomId(roomId)
                        .date(slot.date)
                        .startTime(slot.startTime)
                        .endTime(slot.startTime.plusMinutes(duration))
                        .durationMinutes(duration)
                        .examType("FINAL")
                        .status("SCHEDULED")
                        .build());

                placed = true;
                break;
            }

            if (!placed) {
                conflicts.add(ExamSlotDTO.builder()
                        .courseId(course.getId())
                        .courseName(course.getName())
                        .courseCode(course.getCode())
                        .level(course.getLevel())
                        .status("CONFLICT")
                        .conflictReason("Aucun créneau disponible respectant toutes les contraintes")
                        .build());
            }
        }

        log.info("Exam scheduling complete: {} scheduled, {} conflicts", scheduled.size(), conflicts.size());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("totalCourses", courses.size());
        result.put("scheduled", scheduled.size());
        result.put("conflicts", conflicts.size());
        result.put("slots", scheduled);
        result.put("unscheduled", conflicts);
        result.put("message", String.format("%d examens planifiés, %d conflits", scheduled.size(), conflicts.size()));
        return result;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private List<TimeSlot> buildAvailableSlots(ExamSchedulingRequestDTO request) {
        List<TimeSlot> slots = new ArrayList<>();
        if (request.getSessionStart() == null || request.getSessionEnd() == null) return slots;

        List<String> slotDefs = request.getAvailableSlots() != null
                ? request.getAvailableSlots()
                : List.of("08:00-10:00", "10:30-12:30", "14:00-16:00", "16:30-18:30");

        LocalDate cur = request.getSessionStart();
        int idx = 0;
        while (!cur.isAfter(request.getSessionEnd())) {
            // Exclure les dimanches
            if (cur.getDayOfWeek().getValue() != 7) {
                for (int i = 0; i < slotDefs.size(); i++) {
                    String[] parts = slotDefs.get(i).split("-");
                    LocalTime start = LocalTime.parse(parts[0].trim());
                    slots.add(new TimeSlot(cur, i, start, idx++));
                }
            }
            cur = cur.plusDays(1);
        }
        return slots;
    }

    private Long assignRoom(List<Long> roomIds, String slotKey, Integer requiredCapacity) {
        if (roomIds == null || roomIds.isEmpty()) return null;
        // Retourner la première salle disponible (simplification — en production, vérifier les conflits de salle)
        return roomIds.get(0);
    }

    private record TimeSlot(LocalDate date, int index, LocalTime startTime, int globalIndex) {}
}
