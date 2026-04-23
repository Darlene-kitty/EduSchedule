package cm.iusjc.course.scheduling.controller;

import cm.iusjc.course.scheduling.dto.ExamSchedulingRequestDTO;
import cm.iusjc.course.scheduling.service.ExamSchedulingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/exams/scheduling")
@RequiredArgsConstructor
@Slf4j
public class ExamSchedulingController {

    private final ExamSchedulingService examSchedulingService;

    /**
     * Génère automatiquement le planning d'examens.
     * POST /api/v1/exams/scheduling/generate
     */
    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateSchedule(
            @RequestBody ExamSchedulingRequestDTO request) {
        log.info("Exam schedule generation requested for school {}", request.getSchoolId());
        Map<String, Object> result = examSchedulingService.generateExamSchedule(request);
        return ResponseEntity.ok(result);
    }
}
