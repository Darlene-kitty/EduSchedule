package cm.iusjc.resource.controller;

import cm.iusjc.resource.entity.TypeMateriel;
import cm.iusjc.resource.repository.TypeMaterielRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/equipment-types")
@RequiredArgsConstructor
public class TypeMaterielController {

    private final TypeMaterielRepository repository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAll() {
        List<TypeMateriel> list = repository.findByActiveTrue();
        return ResponseEntity.ok(Map.of("success", true, "data", list, "total", list.size()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable Long id) {
        return repository.findById(id)
                .map(t -> ResponseEntity.ok(Map.<String, Object>of("success", true, "data", t)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody TypeMateriel type) {
        try {
            TypeMateriel saved = repository.save(type);
            return ResponseEntity.status(201).body(Map.of("success", true, "data", saved));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable Long id, @RequestBody TypeMateriel type) {
        return repository.findById(id).map(existing -> {
            type.setId(id);
            return ResponseEntity.ok(Map.<String, Object>of("success", true, "data", repository.save(type)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        return repository.findById(id).map(existing -> {
            existing.setActive(false);
            repository.save(existing);
            return ResponseEntity.ok(Map.<String, Object>of("success", true, "message", "Deleted"));
        }).orElse(ResponseEntity.notFound().build());
    }
}
