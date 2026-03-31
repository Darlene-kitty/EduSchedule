package cm.iusjc.resource.controller;

import cm.iusjc.resource.entity.MaintenanceMateriel;
import cm.iusjc.resource.entity.MaintenanceMateriel.TypeIntervention;
import cm.iusjc.resource.entity.UsageMateriel;
import cm.iusjc.resource.repository.MaintenanceMaterielRepository;
import cm.iusjc.resource.repository.UsageMaterielRepository;
import cm.iusjc.resource.service.EquipmentUsageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/equipment-usage")
@RequiredArgsConstructor
public class EquipmentUsageController {

    private final EquipmentUsageService usageService;
    private final UsageMaterielRepository usageRepo;
    private final MaintenanceMaterielRepository maintenanceRepo;

    // ── Dashboard global ──────────────────────────────────────────────────────

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        return ResponseEntity.ok(usageService.getDashboard());
    }

    @GetMapping("/alertes")
    public ResponseEntity<List<Map<String, Object>>> getAlertes() {
        return ResponseEntity.ok(usageService.getAlertes());
    }

    // ── Stats par matériel ────────────────────────────────────────────────────

    @GetMapping("/stats/{materielId}")
    public ResponseEntity<Map<String, Object>> getStats(@PathVariable Long materielId) {
        return ResponseEntity.ok(usageService.getStatsMateriel(materielId));
    }

    @GetMapping("/{materielId}/usages")
    public ResponseEntity<List<UsageMateriel>> getUsages(@PathVariable Long materielId) {
        return ResponseEntity.ok(usageRepo.findByMaterielIdOrderByDateDebutDesc(materielId));
    }

    // ── Enregistrement d'utilisation ──────────────────────────────────────────

    @PostMapping("/enregistrer")
    public ResponseEntity<Map<String, Object>> enregistrerUsage(@RequestBody Map<String, Object> body) {
        try {
            Long materielId   = ((Number) body.get("materielId")).longValue();
            Long reservationId = body.get("reservationId") != null ? ((Number) body.get("reservationId")).longValue() : null;
            Long coursId       = body.get("coursId") != null ? ((Number) body.get("coursId")).longValue() : null;
            String typeCours   = (String) body.get("typeCours");
            LocalDateTime debut = LocalDateTime.parse((String) body.get("dateDebut"));
            LocalDateTime fin   = LocalDateTime.parse((String) body.get("dateFin"));

            UsageMateriel saved = usageService.enregistrerUsage(materielId, reservationId, coursId, typeCours, debut, fin);
            return ResponseEntity.status(201).body(Map.of("success", true, "data", saved));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/{usageId}/signaler-probleme")
    public ResponseEntity<Map<String, Object>> signalerProbleme(@PathVariable Long usageId,
                                                                  @RequestBody Map<String, String> body) {
        try {
            usageService.signalerProbleme(usageId, body.get("description"));
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // ── Maintenance ───────────────────────────────────────────────────────────

    @GetMapping("/{materielId}/maintenances")
    public ResponseEntity<List<MaintenanceMateriel>> getMaintenances(@PathVariable Long materielId) {
        return ResponseEntity.ok(maintenanceRepo.findByMaterielIdOrderByDateDebutDesc(materielId));
    }

    @PostMapping("/{materielId}/maintenances")
    public ResponseEntity<Map<String, Object>> creerMaintenance(@PathVariable Long materielId,
                                                                  @RequestBody Map<String, Object> body) {
        try {
            TypeIntervention type = TypeIntervention.valueOf((String) body.get("typeIntervention"));
            LocalDateTime dateDebut = LocalDateTime.parse((String) body.get("dateDebut"));
            String description = (String) body.get("description");
            String technicien  = (String) body.getOrDefault("technicien", "");

            MaintenanceMateriel saved = usageService.creerMaintenance(materielId, type, dateDebut, description, technicien);
            return ResponseEntity.status(201).body(Map.of("success", true, "data", saved));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PutMapping("/maintenances/{maintenanceId}/terminer")
    public ResponseEntity<Map<String, Object>> terminerMaintenance(@PathVariable Long maintenanceId,
                                                                     @RequestBody Map<String, String> body) {
        try {
            MaintenanceMateriel updated = usageService.terminerMaintenance(
                    maintenanceId, body.get("notes"), body.get("cout"));
            return ResponseEntity.ok(Map.of("success", true, "data", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
