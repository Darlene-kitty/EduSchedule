package cm.iusjc.resource.controller;

import cm.iusjc.resource.entity.EquipementReservation;
import cm.iusjc.resource.entity.SalleMateriel;
import cm.iusjc.resource.repository.EquipementReservationRepository;
import cm.iusjc.resource.service.SalleMaterielService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * API pour la gestion de l'inventaire des équipements par salle
 * et l'allocation automatique selon le type de cours.
 */
@RestController
@RequestMapping("/api/v1/salle-materiels")
@RequiredArgsConstructor
public class SalleMaterielController {

    private final SalleMaterielService service;
    private final EquipementReservationRepository equipReservationRepo;

    // ── Inventaire par salle ──────────────────────────────────────────────────

    @GetMapping("/salle/{salleId}")
    public ResponseEntity<Map<String, Object>> getInventaireSalle(@PathVariable Long salleId) {
        List<SalleMateriel> list = service.getInventaireSalle(salleId);
        return ResponseEntity.ok(Map.of("success", true, "data", list, "total", list.size()));
    }

    @GetMapping("/salle/{salleId}/disponibles")
    public ResponseEntity<Map<String, Object>> getDisponibles(@PathVariable Long salleId) {
        List<SalleMateriel> list = service.getEquipementsDisponibles(salleId);
        return ResponseEntity.ok(Map.of("success", true, "data", list, "total", list.size()));
    }

    @PostMapping("/salle/{salleId}/materiel/{materielId}")
    public ResponseEntity<Map<String, Object>> ajouterMateriel(
            @PathVariable Long salleId,
            @PathVariable Long materielId,
            @RequestBody Map<String, Object> body) {
        try {
            int quantite = body.containsKey("quantite") ? ((Number) body.get("quantite")).intValue() : 1;
            boolean requis = body.containsKey("requis") && (Boolean) body.get("requis");
            String notes = (String) body.getOrDefault("notes", null);
            SalleMateriel sm = service.ajouterMaterielDansSalle(salleId, materielId, quantite, requis, notes);
            return ResponseEntity.status(201).body(Map.of("success", true, "data", sm));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PutMapping("/{id}/quantite")
    public ResponseEntity<Map<String, Object>> mettreAJourQuantite(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        try {
            int quantite = ((Number) body.get("quantite")).intValue();
            SalleMateriel sm = service.mettreAJourQuantite(id, quantite);
            return ResponseEntity.ok(Map.of("success", true, "data", sm));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> retirerMateriel(@PathVariable Long id) {
        try {
            service.retirerMaterielDeSalle(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Matériel retiré de la salle"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // ── Vérification et allocation selon type de cours ────────────────────────

    /**
     * Vérifie la disponibilité des équipements requis pour un type de cours
     * dans une salle sur une plage horaire.
     */
    @GetMapping("/salle/{salleId}/disponibilite")
    public ResponseEntity<Map<String, Object>> verifierDisponibilite(
            @PathVariable Long salleId,
            @RequestParam String typeCours,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime debut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        Map<String, Object> result = service.verifierDisponibiliteEquipements(salleId, typeCours, debut, fin);
        return ResponseEntity.ok(result);
    }

    /**
     * Retourne les types d'équipements requis pour un type de cours.
     */
    @GetMapping("/equipements-requis")
    public ResponseEntity<Map<String, Object>> getEquipementsRequis(@RequestParam String typeCours) {
        List<String> requis = SalleMaterielService.getEquipementsRequisPourTypeCours(typeCours);
        return ResponseEntity.ok(Map.of("success", true, "typeCours", typeCours, "equipementsRequis", requis));
    }

    /**
     * Alloue automatiquement les équipements pour une réservation confirmée.
     * Appelé par le reservation-service lors de la confirmation.
     */
    @PostMapping("/allouer")
    public ResponseEntity<Map<String, Object>> allouerEquipements(@RequestBody Map<String, Object> body) {
        try {
            Long reservationId = ((Number) body.get("reservationId")).longValue();
            Long salleId       = ((Number) body.get("salleId")).longValue();
            String typeCours   = (String) body.getOrDefault("typeCours", "COURS");
            LocalDateTime debut = LocalDateTime.parse((String) body.get("dateDebut"));
            LocalDateTime fin   = LocalDateTime.parse((String) body.get("dateFin"));

            List<EquipementReservation> allocations =
                    service.allouerEquipements(reservationId, salleId, typeCours, debut, fin);
            return ResponseEntity.status(201).body(Map.of(
                    "success", true,
                    "data", allocations,
                    "nombreAlloues", allocations.size()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Libère les équipements alloués pour une réservation (annulation).
     */
    @PostMapping("/liberer/{reservationId}")
    public ResponseEntity<Map<String, Object>> libererEquipements(@PathVariable Long reservationId) {
        try {
            service.libererEquipements(reservationId);
            return ResponseEntity.ok(Map.of("success", true, "message", "Équipements libérés"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Marque les équipements d'une réservation comme terminés.
     */
    @PostMapping("/terminer/{reservationId}")
    public ResponseEntity<Map<String, Object>> terminerEquipements(@PathVariable Long reservationId) {
        try {
            service.terminerEquipements(reservationId);
            return ResponseEntity.ok(Map.of("success", true, "message", "Équipements marqués terminés"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // ── Réservations d'équipements ────────────────────────────────────────────

    @GetMapping("/reservations/{reservationId}")
    public ResponseEntity<Map<String, Object>> getReservationsEquipements(@PathVariable Long reservationId) {
        List<EquipementReservation> list = equipReservationRepo.findByReservationId(reservationId);
        return ResponseEntity.ok(Map.of("success", true, "data", list, "total", list.size()));
    }

    // ── Statistiques ──────────────────────────────────────────────────────────

    @GetMapping("/statistiques")
    public ResponseEntity<Map<String, Object>> getStatistiques() {
        return ResponseEntity.ok(service.getStatistiques());
    }
}
