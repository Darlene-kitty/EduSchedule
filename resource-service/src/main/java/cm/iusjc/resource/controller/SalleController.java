package cm.iusjc.resource.controller;

import cm.iusjc.resource.entity.Salle;
import cm.iusjc.resource.repository.SalleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/salles")
@RequiredArgsConstructor
public class SalleController {

    private final SalleRepository salleRepository;

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Resource Service is UP");
    }

    @GetMapping
    @Cacheable("salles")
    public ResponseEntity<List<Salle>> getAllSalles() {
        return ResponseEntity.ok(salleRepository.findAll());
    }

    @GetMapping("/disponibles")
    @Cacheable("sallesDisponibles")
    public ResponseEntity<List<Salle>> getSallesDisponibles() {
        return ResponseEntity.ok(salleRepository.findByDisponibleTrue());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Salle> getSalleById(@PathVariable Long id) {
        return salleRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @CacheEvict(value = {"salles", "sallesDisponibles"}, allEntries = true)
    public ResponseEntity<Salle> createSalle(@RequestBody Salle salle) {
        return ResponseEntity.ok(salleRepository.save(salle));
    }

    @PutMapping("/{id}")
    @CacheEvict(value = {"salles", "sallesDisponibles"}, allEntries = true)
    public ResponseEntity<Salle> updateSalle(@PathVariable Long id, @RequestBody Salle salle) {
        return salleRepository.findById(id)
                .map(existingSalle -> {
                    salle.setId(id);
                    return ResponseEntity.ok(salleRepository.save(salle));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @CacheEvict(value = {"salles", "sallesDisponibles"}, allEntries = true)
    public ResponseEntity<Void> deleteSalle(@PathVariable Long id) {
        if (salleRepository.existsById(id)) {
            salleRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}