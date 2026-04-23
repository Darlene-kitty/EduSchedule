package cm.iusjc.resource.controller;

import cm.iusjc.resource.entity.Materiel;
import cm.iusjc.resource.entity.TypeMateriel;
import cm.iusjc.resource.repository.MaterielRepository;
import cm.iusjc.resource.repository.TypeMaterielRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/equipment")
@RequiredArgsConstructor
public class MaterielController {

    private final MaterielRepository materielRepository;
    private final TypeMaterielRepository typeRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAll(
            @RequestParam(required = false) Long typeId,
            @RequestParam(required = false) String ecole) {
        List<Materiel> list;
        if (typeId != null) {
            list = materielRepository.findByTypeMaterielId(typeId);
        } else if (ecole != null) {
            list = materielRepository.findByEcole(ecole);
        } else {
            list = materielRepository.findByActiveTrue();
        }
        return ResponseEntity.ok(Map.of("success", true, "data", list, "total", list.size()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable Long id) {
        return materielRepository.findById(id)
                .map(m -> ResponseEntity.ok(Map.<String, Object>of("success", true, "data", m)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
        try {
            Materiel m = buildFromBody(body, new Materiel());
            Materiel saved = materielRepository.save(m);
            return ResponseEntity.status(201).body(Map.of("success", true, "data", saved));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return materielRepository.findById(id).map(existing -> {
            buildFromBody(body, existing);
            return ResponseEntity.ok(Map.<String, Object>of("success", true, "data", materielRepository.save(existing)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        return materielRepository.findById(id).map(existing -> {
            existing.setActive(false);
            materielRepository.save(existing);
            return ResponseEntity.ok(Map.<String, Object>of("success", true, "message", "Deleted"));
        }).orElse(ResponseEntity.notFound().build());
    }

    private Materiel buildFromBody(Map<String, Object> body, Materiel m) {
        if (body.containsKey("code"))           m.setCode((String) body.get("code"));
        if (body.containsKey("nom"))            m.setNom((String) body.get("nom"));
        if (body.containsKey("marque"))         m.setMarque((String) body.get("marque"));
        if (body.containsKey("modele"))         m.setModele((String) body.get("modele"));
        if (body.containsKey("numeroSerie"))    m.setNumeroSerie((String) body.get("numeroSerie"));
        if (body.containsKey("ecole"))          m.setEcole((String) body.get("ecole"));
        if (body.containsKey("salle"))          m.setSalle((String) body.get("salle"));
        if (body.containsKey("dateAcquisition"))m.setDateAcquisition((String) body.get("dateAcquisition"));
        if (body.containsKey("description"))    m.setDescription((String) body.get("description"));
        if (body.containsKey("valeur"))         m.setValeur(((Number) body.get("valeur")).longValue());
        if (body.containsKey("active"))         m.setActive((Boolean) body.get("active"));
        if (body.containsKey("etat")) {
            try { m.setEtat(Materiel.EtatMateriel.valueOf(((String) body.get("etat")).toUpperCase().replace(" ", "_"))); }
            catch (Exception ignored) {}
        }
        if (body.containsKey("typeMaterielId")) {
            Long typeId = ((Number) body.get("typeMaterielId")).longValue();
            typeRepository.findById(typeId).ifPresent(m::setTypeMateriel);
        }
        return m;
    }
}
