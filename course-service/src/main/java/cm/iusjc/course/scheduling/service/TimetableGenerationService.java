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
    private final TeacherAvailabilityClient availabilityClient;

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
            List<CourseDTO> rawCourses = courseService.getCoursesBySchool(request.getSchoolId())
                    .stream()
                    .filter(c -> c.isActive()
                            && request.getLevel().equals(c.getLevel())
                            && request.getSemester().equals(c.getSemester())
                            && c.getHoursPerWeek() != null && c.getHoursPerWeek() > 0)
                    .toList();

            List<String> slots = request.getAvailableSlots() != null
                    ? request.getAvailableSlots()
                    : defaultSlots();

            // Nombre de salles (simplifié : on utilise roomIds.size() ou 5 par défaut)
            int roomCount = request.getRoomIds() != null ? request.getRoomIds().size() : 5;

            result.setProgress(30);

            // ── Chargement des disponibilités des enseignants ───────────────
            // Un appel par teacherId unique ; fallback = tout autorisé si aucune dispo enregistrée
            Set<Long> teacherIds = new HashSet<>();
            for (CourseDTO c : rawCourses) {
                if (c.getTeacherId() != null) teacherIds.add(c.getTeacherId());
            }
            Map<Long, Set<String>> teacherAllowedKeys = new HashMap<>();
            for (Long tid : teacherIds) {
                teacherAllowedKeys.put(tid, availabilityClient.getAllowedSlotKeys(tid));
            }
            log.info("Loaded availability constraints for {} teachers", teacherIds.size());

            // ── Tri par priorité : enseignants les plus contraints en premier ──
            // Compte le nombre de créneaux autorisés par cours → tri croissant
            // Un enseignant avec peu de créneaux disponibles est prioritaire
            List<CourseDTO> courses = new ArrayList<>(rawCourses);
            courses.sort(Comparator.comparingInt(c -> {
                Long tid = c.getTeacherId();
                Set<String> allowed = tid != null
                        ? teacherAllowedKeys.getOrDefault(tid, Collections.emptySet())
                        : Collections.emptySet();
                if (allowed.isEmpty()) return slots.size(); // fallback = tout autorisé = moins prioritaire
                // Compte combien de slots sont couverts par les dispos de cet enseignant
                long count = slots.stream().filter(slot -> {
                    String[] p = slot.split("_");
                    return p.length >= 3 && availabilityClient.isSlotAllowed(allowed, p[0], p[1], p[2]);
                }).count();
                return (int) count;
            }));
            log.info("Courses sorted by teacher availability constraint (most constrained first): {}",
                    courses.stream().map(CourseDTO::getCode).toList());

            // ── Construction du graphe ──────────────────────────────────────
            // Structure à 4 niveaux :
            //   Source → Cours → (Cours×Créneau) → (Créneau×Salle) → Sink
            //
            // Le nœud (Cours×Créneau) avec capacité 1 garantit qu'un cours
            // n'occupe qu'UNE SEULE salle par créneau.
            // Le nœud (Créneau×Salle) avec capacité 1 garantit qu'une salle
            // n'accueille qu'UN SEUL cours par créneau.
            int C  = courses.size();
            int S  = slots.size();
            int R  = roomCount;
            int CS = S * R; // combinaisons (créneau, salle)

            // Index des nœuds :
            //   0          = source
            //   1..C       = cours
            //   C+1..C+C*S = nœuds (cours×créneau)
            //   C+C*S+1..C+C*S+CS = nœuds (créneau×salle)
            //   dernier    = sink
            int totalNodes = 1 + C + C * S + CS + 1;
            int source = 0;
            int sink   = totalNodes - 1;

            FlowNetwork graph = new FlowNetwork(totalNodes);

            // Source → Cours (capacité = hoursPerWeek)
            for (int i = 0; i < C; i++) {
                graph.addEdge(source, 1 + i, courses.get(i).getHoursPerWeek());
            }

            // Cours → (Cours×Créneau) : capacité 1, filtrée par disponibilité enseignant
            // Garantit qu'un cours n'est assigné qu'une fois par créneau
            for (int i = 0; i < C; i++) {
                CourseDTO course = courses.get(i);
                Long tid = course.getTeacherId();
                Set<String> allowed = tid != null
                        ? teacherAllowedKeys.getOrDefault(tid, Collections.emptySet())
                        : Collections.emptySet(); // vide = tout autorisé

                for (int j = 0; j < S; j++) {
                    String slot = slots.get(j);
                    String[] parts = slot.split("_");
                    boolean slotOk = parts.length < 3
                            || availabilityClient.isSlotAllowed(allowed, parts[0], parts[1], parts[2]);

                    if (slotOk) {
                        // nœud (cours i, créneau j) = 1 + C + i*S + j
                        graph.addEdge(1 + i, 1 + C + i * S + j, 1);
                    }
                }
            }

            // (Cours×Créneau) → (Créneau×Salle) : capacité 1 par salle
            // Un cours sur un créneau peut aller dans n'importe quelle salle disponible
            for (int i = 0; i < C; i++) {
                for (int j = 0; j < S; j++) {
                    int csNode = 1 + C + i * S + j;
                    for (int k = 0; k < R; k++) {
                        // nœud (créneau j, salle k) = 1 + C + C*S + j*R + k
                        graph.addEdge(csNode, 1 + C + C * S + j * R + k, 1);
                    }
                }
            }

            // (Créneau×Salle) → Sink : capacité 1 — une salle ne peut accueillir qu'un cours par créneau
            for (int cs = 0; cs < CS; cs++) {
                graph.addEdge(1 + C + C * S + cs, sink, 1);
            }

            result.setProgress(50);

            // ── Exécution de l'algorithme choisi ───────────────────────────
            boolean useDfs = "ford-fulkerson".equalsIgnoreCase(request.getAlgorithm());
            int flowValue = useDfs
                    ? scheduler.maxFlowDfs(graph, source, sink)
                    : scheduler.maxFlow(graph, source, sink);

            result.setProgress(80);

            // ── Lecture du résultat ─────────────────────────────────────────
            // On cherche les arêtes saturées : (Cours×Créneau) → (Créneau×Salle) avec flow > 0
            List<ScheduleSlotDTO> assignedSlots = new ArrayList<>();
            List<String> unassigned = new ArrayList<>();
            int totalDemand = courses.stream().mapToInt(c -> c.getHoursPerWeek() != null ? c.getHoursPerWeek() : 0).sum();

            // Plage des nœuds (cours×créneau) : [1+C .. 1+C+C*S-1]
            int csCourseStart = 1 + C;
            int csCourseEnd   = 1 + C + C * S - 1;
            // Plage des nœuds (créneau×salle) : [1+C+C*S .. 1+C+C*S+CS-1]
            int csRoomStart   = 1 + C + C * S;

            for (int i = 0; i < C; i++) {
                CourseDTO course = courses.get(i);
                int assignedCount = 0;

                // Arêtes (Cours×Créneau) pour ce cours : nœuds [1+C+i*S .. 1+C+i*S+S-1]
                for (int j = 0; j < S; j++) {
                    int ccNode = 1 + C + i * S + j;
                    // Arêtes de ce nœud vers (Créneau×Salle)
                    List<FlowEdge> ccEdges = graph.getEdges(ccNode).stream()
                            .filter(e -> e.getCapacity() > 0
                                    && e.getTo() >= csRoomStart
                                    && e.getTo() < csRoomStart + CS)
                            .toList();

                    for (FlowEdge edge : ccEdges) {
                        if (edge.getFlow() > 0) {
                            int csIdx   = edge.getTo() - csRoomStart; // [0..CS-1]
                            int slotIdx = csIdx / R;
                            int roomIdx = csIdx % R;

                            String slot = slots.get(slotIdx);
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
                            Long roomId = (request.getRoomIds() != null && roomIdx < request.getRoomIds().size())
                                    ? request.getRoomIds().get(roomIdx)
                                    : (long)(roomIdx + 1);
                            dto.setRoomId(roomId);
                            dto.setRoomName("Salle " + roomId);

                            assignedSlots.add(dto);
                            assignedCount++;
                        }
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
