package cm.iusjc.school.controller;

import cm.iusjc.school.dto.AffectationDTO;
import cm.iusjc.school.dto.AutoAffectationRequestDTO;
import cm.iusjc.school.dto.AutoAffectationResultDTO;
import cm.iusjc.school.service.AffectationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AffectationController {

    private final AffectationService affectationService;

    // ─────────────────────────────────────────────────────────────────────────
    // Lecture
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * GET /api/v1/groupes/{groupeId}/etudiants
     * Liste les étudiants actifs d'un groupe avec l'effectif courant.
     */
    @GetMapping("/api/v1/groupes/{groupeId}/etudiants")
    public ResponseEntity<Map<String, Object>> getEtudiantsByGroupe(@PathVariable Long groupeId) {
        try {
            List<AffectationDTO> list = affectationService.getEtudiantsByGroupe(groupeId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", list,
                    "effectif", list.size()
            ));
        } catch (Exception e) {
            log.error("Error fetching students for groupe {}: {}", groupeId, e.getMessage());
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * GET /api/v1/etudiants/{etudiantId}/groupe
     * Retourne le groupe actif d'un étudiant.
     */
    @GetMapping("/api/v1/etudiants/{etudiantId}/groupe")
    public ResponseEntity<Map<String, Object>> getGroupeByEtudiant(@PathVariable Long etudiantId) {
        return affectationService.getGroupeByEtudiant(etudiantId)
                .map(dto -> ResponseEntity.ok(Map.<String, Object>of("success", true, "data", dto)))
                .orElse(ResponseEntity.ok(Map.of("success", true, "data", Map.of())));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Affectation manuelle
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * POST /api/v1/groupes/{groupeId}/affecter
     * Body: { "etudiantId": 42 }
     * Affecte manuellement un étudiant à un groupe.
     */
    @PostMapping("/api/v1/groupes/{groupeId}/affecter")
    public ResponseEntity<Map<String, Object>> affecter(
            @PathVariable Long groupeId,
            @RequestBody Map<String, Long> body) {
        try {
            Long etudiantId = body.get("etudiantId");
            if (etudiantId == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "etudiantId est requis"));
            }
            AffectationDTO result = affectationService.affecter(groupeId, etudiantId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("success", true, "data", result));
        } catch (Exception e) {
            log.error("Error assigning student to groupe {}: {}", groupeId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * DELETE /api/v1/groupes/{groupeId}/etudiants/{etudiantId}
     * Retire un étudiant d'un groupe.
     */
    @DeleteMapping("/api/v1/groupes/{groupeId}/etudiants/{etudiantId}")
    public ResponseEntity<Map<String, Object>> desaffecter(
            @PathVariable Long groupeId,
            @PathVariable Long etudiantId) {
        try {
            affectationService.desaffecter(groupeId, etudiantId);
            return ResponseEntity.ok(Map.of("success", true, "message", "Étudiant retiré du groupe"));
        } catch (Exception e) {
            log.error("Error removing student {} from groupe {}: {}", etudiantId, groupeId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Auto-affectation round-robin
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * POST /api/v1/niveaux/{niveauId}/auto-affecter
     * Body: { "etudiantIds": [1, 2, 3, ...], "forceReaffectation": false }
     *
     * Distribue automatiquement les étudiants entre les groupes actifs du niveau
     * via un algorithme round-robin pondéré par la capacité disponible.
     */
    @PostMapping("/api/v1/niveaux/{niveauId}/auto-affecter")
    public ResponseEntity<Map<String, Object>> autoAffecter(
            @PathVariable Long niveauId,
            @Valid @RequestBody AutoAffectationRequestDTO request) {
        try {
            AutoAffectationResultDTO result = affectationService.autoAffecter(niveauId, request);
            return ResponseEntity.ok(Map.of("success", true, "data", result));
        } catch (Exception e) {
            log.error("Error auto-assigning students to niveau {}: {}", niveauId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
