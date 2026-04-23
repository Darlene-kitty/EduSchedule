package cm.iusjc.school.controller;

import cm.iusjc.school.dto.NiveauDTO;
import cm.iusjc.school.service.NiveauService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/niveaux")
@RequiredArgsConstructor
@Slf4j
public class NiveauController {

    private final NiveauService niveauService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAll(
            @RequestParam(name = "filiereId", required = false) Long filiereId) {
        try {
            var list = filiereId != null
                    ? niveauService.getByFiliere(filiereId)
                    : niveauService.getAll();
            return ResponseEntity.ok(Map.of("success", true, "data", list, "total", list.size()));
        } catch (Exception e) {
            log.error("Error fetching niveaux: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("success", false, "message",
                    e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable Long id) {
        return niveauService.getById(id)
                .map(n -> ResponseEntity.ok(Map.<String, Object>of("success", true, "data", n)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@Valid @RequestBody NiveauDTO dto) {
        try {
            NiveauDTO created = niveauService.create(dto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("success", true, "data", created));
        } catch (Exception e) {
            log.error("Error creating niveau: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success", false, "message",
                    e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable Long id, @Valid @RequestBody NiveauDTO dto) {
        try {
            NiveauDTO updated = niveauService.update(id, dto);
            return ResponseEntity.ok(Map.of("success", true, "data", updated));
        } catch (Exception e) {
            log.error("Error updating niveau {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success", false, "message",
                    e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        try {
            niveauService.delete(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Niveau deleted"));
        } catch (Exception e) {
            log.error("Error deleting niveau {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success", false, "message",
                    e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
        }
    }
}
