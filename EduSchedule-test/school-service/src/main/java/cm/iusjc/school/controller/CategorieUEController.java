package cm.iusjc.school.controller;

import cm.iusjc.school.dto.CategorieUEDTO;
import cm.iusjc.school.service.CategorieUEService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/categories-ue")
@RequiredArgsConstructor
@Slf4j
public class CategorieUEController {

    private final CategorieUEService service;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAll() {
        try {
            var list = service.getAll();
            return ResponseEntity.ok(Map.of("success", true, "data", list, "total", list.size()));
        } catch (Exception e) {
            log.error("Error fetching categoriesUE: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("success", false, "message",
                    e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable Long id) {
        return service.getById(id)
                .map(dto -> ResponseEntity.ok(Map.<String, Object>of("success", true, "data", dto)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@Valid @RequestBody CategorieUEDTO dto) {
        try {
            CategorieUEDTO created = service.create(dto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("success", true, "data", created));
        } catch (Exception e) {
            log.error("Error creating categorieUE: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success", false, "message",
                    e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable Long id, @Valid @RequestBody CategorieUEDTO dto) {
        try {
            CategorieUEDTO updated = service.update(id, dto);
            return ResponseEntity.ok(Map.of("success", true, "data", updated));
        } catch (Exception e) {
            log.error("Error updating categorieUE {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success", false, "message",
                    e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        try {
            service.delete(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "CategorieUE deleted"));
        } catch (jakarta.persistence.EntityNotFoundException e) {
            return ResponseEntity.status(404).body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error deleting categorieUE {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success", false, "message",
                    e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
        }
    }
}
