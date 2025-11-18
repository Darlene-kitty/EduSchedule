package cm.iusjc.resource.controller;

import cm.iusjc.resource.entity.Salle;
import cm.iusjc.resource.repository.SalleRepository;
import lombok.RequiredArgsConstructor;
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
    @Cacheable(