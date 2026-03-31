package cm.iusjc.school.controller;

import cm.iusjc.school.dto.GroupeDTO;
import cm.iusjc.school.service.GroupeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/groupes")
@RequiredArgsConstructor
@Slf4j
public class GroupeController {

    private final GroupeService groupeService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAll(
            @RequestParam(name = "niveauId", required = false) Long niveauId) {
        try {
            var list = niveauId != null
                    ? groupeService.getByNiveau(niveauId)
                    : groupeService.getAll();
            return ResponseEntity.ok(Map.of("success", true, "data", list, "total", list.size()));
        } catch (Exception e) {
            log.error("Error fetching groupes: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("success", false, "message",
                    e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable Long id) {
        return groupeService.getById(id)
                .map(g -> ResponseEntity.ok(Map.<String, Object>of("success", true, "data", g)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@Valid @RequestBody GroupeDTO dto) {
        try {
            GroupeDTO created = groupeService.create(dto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("success", true, "data", created));
        } catch (Exception e) {
            log.error("Error creating groupe: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success", false, "message",
                    e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable Long id, @Valid @RequestBody GroupeDTO dto) {
        try {
            GroupeDTO updated = groupeService.update(id, dto);
            return ResponseEntity.ok(Map.of("success", true, "data", updated));
        } catch (Exception e) {
            log.error("Error updating groupe {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success", false, "message",
                    e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        try {
            groupeService.delete(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Groupe deleted"));
        } catch (Exception e) {
            log.error("Error deleting groupe {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success", false, "message",
                    e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
        }
    }
}
