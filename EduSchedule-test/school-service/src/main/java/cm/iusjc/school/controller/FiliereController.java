package cm.iusjc.school.controller;

import cm.iusjc.school.dto.FiliereDTO;
import cm.iusjc.school.service.FiliereService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/filieres")
@RequiredArgsConstructor
@Slf4j
public class FiliereController {

    private final FiliereService filiereService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAll(
            @RequestParam(required = false) Long schoolId) {
        try {
            var list = schoolId != null
                    ? filiereService.getBySchool(schoolId)
                    : filiereService.getAll();
            return ResponseEntity.ok(Map.of("success", true, "data", list, "total", list.size()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable Long id) {
        return filiereService.getById(id)
                .map(f -> ResponseEntity.ok(Map.<String, Object>of("success", true, "data", f)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@Valid @RequestBody FiliereDTO dto) {
        try {
            FiliereDTO created = filiereService.create(dto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("success", true, "data", created));
        } catch (Exception e) {
            log.error("Error creating filiere: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable Long id, @Valid @RequestBody FiliereDTO dto) {
        try {
            FiliereDTO updated = filiereService.update(id, dto);
            return ResponseEntity.ok(Map.of("success", true, "data", updated));
        } catch (Exception e) {
            log.error("Error updating filiere {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        try {
            filiereService.delete(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Filiere deleted"));
        } catch (Exception e) {
            log.error("Error deleting filiere {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
