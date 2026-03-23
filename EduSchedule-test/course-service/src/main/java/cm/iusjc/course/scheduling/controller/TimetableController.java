package cm.iusjc.course.scheduling.controller;

import cm.iusjc.course.scheduling.dto.SchedulingRequestDTO;
import cm.iusjc.course.scheduling.dto.SchedulingResultDTO;
import cm.iusjc.course.scheduling.service.TimetableConfirmationService;
import cm.iusjc.course.scheduling.service.TimetableGenerationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/timetable")
@RequiredArgsConstructor
@Slf4j
public class TimetableController {

    private final TimetableGenerationService generationService;
    private final TimetableConfirmationService confirmationService;

    /**
     * Lance la génération de l'emploi du temps.
     * Retourne immédiatement un jobId pour polling.
     */
    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generate(
            @Valid @RequestBody SchedulingRequestDTO request) {
        try {
            String jobId = generationService.createJob();
            generationService.generateAsync(jobId, request);
            log.info("Timetable generation started, jobId={}", jobId);
            return ResponseEntity.accepted().body(Map.of(
                    "success", true,
                    "jobId", jobId,
                    "message", "Génération démarrée"
            ));
        } catch (Exception e) {
            log.error("Failed to start generation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Polling : retourne l'état courant du job.
     */
    @GetMapping("/status/{jobId}")
    public ResponseEntity<Map<String, Object>> getStatus(@PathVariable String jobId) {
        return generationService.getJob(jobId)
                .map(result -> ResponseEntity.ok(Map.of(
                        "success", true,
                        "data", result
                )))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                        "success", false,
                        "message", "Job introuvable : " + jobId
                )));
    }

    /**
     * Validation : vérifie les conflits dans un résultat généré.
     */
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validate(
            @RequestBody SchedulingResultDTO result) {
        long conflicts = 0;
        if (result.getSlots() != null) {
            conflicts = result.getSlots().stream()
                    .collect(java.util.stream.Collectors.groupingBy(
                            s -> s.getRoomId() + "_" + s.getDayOfWeek() + "_" + s.getStartTime(),
                            java.util.stream.Collectors.counting()))
                    .values().stream()
                    .filter(count -> count > 1)
                    .count();
        }
        return ResponseEntity.ok(Map.of(
                "success", true,
                "conflicts", conflicts,
                "valid", conflicts == 0,
                "message", conflicts == 0 ? "Aucun conflit détecté" : conflicts + " conflit(s) détecté(s)"
        ));
    }

    /**
     * Confirme et sauvegarde un emploi du temps généré, puis déclenche la sync calendrier.
     *
     * @param jobId     ID du job à confirmer
     * @param schoolId  ID de l'école
     * @param userId    ID de l'utilisateur qui valide
     * @param weekStart lundi de référence pour les dates calendrier (ISO: yyyy-MM-dd)
     */
    @PostMapping("/{jobId}/confirm")
    public ResponseEntity<Map<String, Object>> confirm(
            @PathVariable String jobId,
            @RequestParam Long schoolId,
            @RequestParam String userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        try {
            int saved = confirmationService.confirm(jobId, schoolId, userId, weekStart);
            log.info("Timetable confirmed: jobId={} saved={} slots", jobId, saved);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "savedSlots", saved,
                    "message", "Emploi du temps sauvegardé et synchronisation calendrier déclenchée"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false, "message", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false, "message", e.getMessage()));
        } catch (Exception e) {
            log.error("Confirm failed for jobId={}: {}", jobId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false, "message", e.getMessage()));
        }
    }
}
