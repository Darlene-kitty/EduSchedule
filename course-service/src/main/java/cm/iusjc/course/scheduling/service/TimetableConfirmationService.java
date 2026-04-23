package cm.iusjc.course.scheduling.service;

import cm.iusjc.course.scheduling.dto.ScheduleSlotDTO;
import cm.iusjc.course.scheduling.dto.SchedulingResultDTO;
import cm.iusjc.course.scheduling.entity.GeneratedSchedule;
import cm.iusjc.course.scheduling.repository.GeneratedScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TimetableConfirmationService {

    private final GeneratedScheduleRepository repository;
    private final TimetableGenerationService generationService;
    private final CalendarServiceClient calendarClient;

    /**
     * Valide un emploi du temps généré :
     * 1. Supprime l'ancien emploi du temps pour ce niveau/semestre/école
     * 2. Persiste les nouveaux créneaux
     * 3. Exporte vers le calendar-service
     *
     * @param jobId     ID du job à confirmer
     * @param schoolId  ID de l'école
     * @param userId    ID de l'utilisateur qui valide (pour la sync calendrier)
     * @param weekStart date de début du semestre (lundi de référence)
     * @return nombre de créneaux sauvegardés
     */
    @Transactional
    public int confirm(String jobId, Long schoolId, String userId, LocalDate weekStart) {
        SchedulingResultDTO job = generationService.getJob(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job introuvable : " + jobId));

        if (!"COMPLETED".equals(job.getStatus()) && !"PARTIAL".equals(job.getStatus())) {
            throw new IllegalStateException("Le job n'est pas terminé (status=" + job.getStatus() + ")");
        }

        List<ScheduleSlotDTO> slots = job.getSlots();
        if (slots == null || slots.isEmpty()) {
            throw new IllegalStateException("Aucun créneau à sauvegarder");
        }

        // Détermine niveau/semestre depuis le premier créneau
        String level    = slots.get(0).getLevel();
        String semester = slots.get(0).getSemester();

        // Supprime l'ancien emploi du temps pour éviter les doublons
        repository.deleteBySchoolIdAndSemesterAndLevel(schoolId, semester, level);
        log.info("Deleted previous timetable for school={} semester={} level={}", schoolId, semester, level);

        // Persiste les nouveaux créneaux
        List<GeneratedSchedule> entities = slots.stream()
                .map(s -> toEntity(s, jobId, schoolId))
                .toList();

        List<GeneratedSchedule> saved = repository.saveAll(entities);
        log.info("Saved {} schedule slots for jobId={}", saved.size(), jobId);

        // Export async vers le calendar-service (non bloquant)
        try {
            calendarClient.exportSlots(saved, userId, weekStart);
            calendarClient.triggerSync(userId);
        } catch (Exception e) {
            // La sync calendrier ne doit pas faire échouer la confirmation
            log.warn("Calendar sync failed after confirmation: {}", e.getMessage());
        }

        return saved.size();
    }

    private GeneratedSchedule toEntity(ScheduleSlotDTO dto, String jobId, Long schoolId) {
        GeneratedSchedule e = new GeneratedSchedule();
        e.setJobId(jobId);
        e.setSchoolId(schoolId);
        e.setCourseId(dto.getCourseId());
        e.setCourseCode(dto.getCourseCode());
        e.setCourseName(dto.getCourseName());
        e.setTeacherId(dto.getTeacherId());
        e.setRoomId(dto.getRoomId());
        e.setRoomName(dto.getRoomName());
        e.setDayOfWeek(dto.getDayOfWeek());
        e.setStartTime(dto.getStartTime());
        e.setEndTime(dto.getEndTime());
        e.setLevel(dto.getLevel());
        e.setSemester(dto.getSemester());
        return e;
    }
}
