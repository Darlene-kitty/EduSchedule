package cm.iusjc.course.scheduling.service;

import cm.iusjc.course.dto.CourseDTO;
import cm.iusjc.course.scheduling.algorithm.FordFulkersonScheduler;
import cm.iusjc.course.scheduling.dto.ScheduleSlotDTO;
import cm.iusjc.course.scheduling.dto.SchedulingRequestDTO;
import cm.iusjc.course.scheduling.dto.SchedulingResultDTO;
import cm.iusjc.course.scheduling.model.FlowEdge;
import cm.iusjc.course.scheduling.model.FlowNetwork;
import cm.iusjc.course.scheduling.model.FlowNode;
import cm.iusjc.course.service.CourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class TimetableGenerationService {

    private final CourseService courseService;
    private final FordFulkersonScheduler scheduler;

    // Stockage en mémoire des jobs avec timestamp (à remplacer par Redis en prod)
    private final Map<String, SchedulingResultDTO> jobStore = new ConcurrentHashMap<>();
    private final Map<String, Instant> jobTimestamps = new ConcurrentHashMap<>();

    /**
     * Lance la génération de manière asynchrone.
     * Retourne immédiatement un jobId pour polling.
     */
    @Async("schedulingExecutor")
    public void generateAsync(String jobId, SchedulingRequestDTO request) {
        SchedulingResultDTO result = jobStore.get(jobId);
        result.setStatus("RUNNING");
        result.setProgress(10);

        long start = System.currentTimeMillis();
        try {
            List<CourseDTO> courses = courseService.getCoursesBySchool(request.getSchoolId())
                    .stream()
                    .filter(c -> c.isActive()
                            && request.getLevel().equals(c.getLevel())
                            && request.getSemester().equals(c.getDepartment()) // à adapter selon ton modèle
                            && c.getHoursPerWeek() != null && c.getHoursPerWeek() > 0)
                    .toList();

            List<String> slots = request.getAvailableSlots() != null
                    ? request.getAvailableSlots()
                    : defaultSlots();

            // Nombre de salles (simplifié : on utilise roomIds.size() ou 5 par défaut)
            int roomCount = request.getRoomIds() != null ? request.getRoomIds().size() : 5;

            result.setProgress(30);

            // ── Construction du graphe ──────────────────────────────────────
            // Nœuds : 0=source, 1..C=cours, C+1..C+S=créneaux, C+S+1..C+S+R=salles, dernier=sink
            int C = courses.size();
            int S = slots.size();
            int R = roomCount;
            int totalNodes = 1 + C + S + R + 1;
            int source = 0;
            int sink   = totalNodes - 1;

            FlowNetwork graph = new FlowNetwork(totalNodes);

            // Source → Cours (capacité = hoursPerWeek)
            for (int i = 0; i < C; i++) {
                graph.addEdge(source, 1 + i, courses.get(i).getHoursPerWeek());
            }

            // Cours → Créneaux (capacité = 1 pour chaque combinaison possible)
            for (int i = 0; i < C; i++) {
                for (int j = 0; j < S; j++) {
                    graph.addEdge(1 + i, 1 + C + j, 1);
                }
            }

            // Créneaux → Salles (capacité = 1 : une salle par créneau)
            for (int j = 0; j < S; j++) {
                for (int k = 0; k < R; k++) {
                    graph.addEdge(1 + C + j, 1 + C + S + k, 1);
                }
            }

            // Salles → Sink (capacité = nombre de créneaux max par salle)
            for (int k = 0; k < R; k++) {
                graph.addEdge(1 + C + S + k, sink, S);
            }

            result.setProgress(50);

            // ── Exécution Ford-Fulkerson ────────────────────────────────────
            int flowValue = scheduler.maxFlow(graph, source, sink);

            result.setProgress(80);

            // ── Lecture du résultat : arêtes saturées cours→créneau ─────────
            List<ScheduleSlotDTO> assignedSlots = new ArrayList<>();
            List<String> unassigned = new ArrayList<>();
            int totalDemand = courses.stream().mapToInt(c -> c.getHoursPerWeek() != null ? c.getHoursPerWeek() : 0).sum();

            for (int i = 0; i < C; i++) {
                CourseDTO course = courses.get(i);
                int assignedCount = 0;

                List<FlowEdge> courseEdges = graph.getEdges(1 + i);
                for (int j = 0; j < S; j++) {
                    FlowEdge edge = courseEdges.get(j); // arête cours→créneau
                    if (edge.getFlow() > 0) {
                        String slot = slots.get(j);
                        String[] parts = slot.split("_");
                        ScheduleSlotDTO dto = new ScheduleSlotDTO();
                        dto.setCourseId(course.getId());
                        dto.setCourseCode(course.getCode());
                        dto.setCourseName(course.getName());
                        dto.setTeacherId(course.getTeacherId());
                        dto.setLevel(course.getLevel());
                        dto.setSemester(request.getSemester());
                        if (parts.length >= 3) {
                            dto.setDayOfWeek(parts[0]);
                            dto.setStartTime(parts[1]);
                            dto.setEndTime(parts[2]);
                        }
                        // Salle : on cherche l'arête créneau→salle saturée
                        List<FlowEdge> slotEdges = graph.getEdges(1 + C + j);
                        for (int k = 0; k < R; k++) {
                            if (slotEdges.get(k).getFlow() > 0) {
                                Long roomId = (request.getRoomIds() != null && k < request.getRoomIds().size())
                                        ? request.getRoomIds().get(k)
                                        : (long)(k + 1);
                                dto.setRoomId(roomId);
                                dto.setRoomName("Salle " + roomId);
                                break;
                            }
                        }
                        assignedSlots.add(dto);
                        assignedCount++;
                    }
                }

                if (assignedCount < course.getHoursPerWeek()) {
                    unassigned.add(course.getCode() + " (" + assignedCount + "/" + course.getHoursPerWeek() + " h placées)");
                }
            }

            result.setSlots(assignedSlots);
            result.setUnassignedCourses(unassigned);
            result.setMaxFlowValue(flowValue);
            result.setTotalDemand(totalDemand);
            result.setGenerationTimeMs(System.currentTimeMillis() - start);
            result.setStatus(unassigned.isEmpty() ? "COMPLETED" : "PARTIAL");
            result.setProgress(100);
            result.setMessage(unassigned.isEmpty()
                    ? "Emploi du temps généré avec succès"
                    : unassigned.size() + " cours non entièrement placés");

            log.info("Timetable generation [{}] done: flow={}/{} in {}ms",
                    jobId, flowValue, totalDemand, result.getGenerationTimeMs());

        } catch (Exception e) {
            log.error("Timetable generation [{}] failed: {}", jobId, e.getMessage(), e);
            result.setStatus("FAILED");
            result.setMessage("Erreur : " + e.getMessage());
            result.setProgress(0);
        }

        jobStore.put(jobId, result);
    }

    /** Crée un job en état PENDING et retourne son ID */
    public String createJob() {
        String jobId = UUID.randomUUID().toString();
        SchedulingResultDTO result = new SchedulingResultDTO();
        result.setJobId(jobId);
        result.setStatus("PENDING");
        result.setProgress(0);
        jobStore.put(jobId, result);
        jobTimestamps.put(jobId, Instant.now());
        return jobId;
    }

    public Optional<SchedulingResultDTO> getJob(String jobId) {
        return Optional.ofNullable(jobStore.get(jobId));
    }

    /** Purge les jobs terminés de plus d'1 heure */
    @Scheduled(fixedDelay = 1800000) // toutes les 30 min
    public void purgeOldJobs() {
        Instant cutoff = Instant.now().minusSeconds(3600);
        jobTimestamps.entrySet().removeIf(entry -> {
            if (entry.getValue().isBefore(cutoff)) {
                jobStore.remove(entry.getKey());
                return true;
            }
            return false;
        });
        log.debug("Job store size after purge: {}", jobStore.size());
    }

    /** Créneaux par défaut si non fournis : Lun-Ven, 08h-18h par tranches de 2h */
    private List<String> defaultSlots() {
        String[] days  = {"LUNDI", "MARDI", "MERCREDI", "JEUDI", "VENDREDI"};
        String[] times = {"08:00_10:00", "10:00_12:00", "14:00_16:00", "16:00_18:00"};
        List<String> slots = new ArrayList<>();
        for (String day : days)
            for (String time : times)
                slots.add(day + "_" + time);
        return slots;
    }
}
