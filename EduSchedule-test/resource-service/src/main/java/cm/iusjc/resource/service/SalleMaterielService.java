package cm.iusjc.resource.service;

import cm.iusjc.resource.entity.*;
import cm.iusjc.resource.entity.EquipementReservation.StatutReservationEquipement;
import cm.iusjc.resource.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service gérant la relation formelle Salle ↔ Matériel et l'allocation
 * automatique d'équipements selon le type de cours (TD, TP, CM, EXAM…).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SalleMaterielService {

    private final SalleMaterielRepository salleMaterielRepo;
    private final EquipementReservationRepository equipReservationRepo;
    private final SalleRepository salleRepo;
    private final MaterielRepository materielRepo;

    // ── Mapping type de cours → codes de types de matériels requis ────────────

    /**
     * Retourne les codes de types de matériels recommandés pour un type de cours.
     * Ces codes correspondent aux TypeMateriel.code en base.
     */
    public static List<String> getEquipementsRequisPourTypeCours(String typeCours) {
        if (typeCours == null) return List.of();
        return switch (typeCours.toUpperCase()) {
            case "TP", "PRACTICAL" -> List.of("ORDINATEUR", "PROJECTEUR", "TABLEAU_BLANC");
            case "TD", "SEMINAR"   -> List.of("TABLEAU_BLANC", "PROJECTEUR");
            case "CM", "CONFERENCE" -> List.of("PROJECTEUR", "SONO", "MICRO");
            case "EXAM"            -> List.of("TABLEAU_BLANC");
            default                -> List.of("TABLEAU_BLANC");
        };
    }

    // ── Inventaire salle ──────────────────────────────────────────────────────

    public List<SalleMateriel> getInventaireSalle(Long salleId) {
        return salleMaterielRepo.findBySalleId(salleId);
    }

    public List<SalleMateriel> getEquipementsDisponibles(Long salleId) {
        return salleMaterielRepo.findDisponiblesBySalleId(salleId);
    }

    @Transactional
    public SalleMateriel ajouterMaterielDansSalle(Long salleId, Long materielId,
                                                   int quantite, boolean requis, String notes) {
        Salle salle = salleRepo.findById(salleId)
                .orElseThrow(() -> new RuntimeException("Salle introuvable: " + salleId));
        Materiel materiel = materielRepo.findById(materielId)
                .orElseThrow(() -> new RuntimeException("Matériel introuvable: " + materielId));

        // Vérifier si la relation existe déjà
        Optional<SalleMateriel> existing = salleMaterielRepo.findBySalleIdAndMaterielId(salleId, materielId);
        if (existing.isPresent()) {
            SalleMateriel sm = existing.get();
            sm.setQuantiteTotale(sm.getQuantiteTotale() + quantite);
            sm.setQuantiteDisponible(sm.getQuantiteDisponible() + quantite);
            if (notes != null) sm.setNotes(notes);
            return salleMaterielRepo.save(sm);
        }

        SalleMateriel sm = new SalleMateriel();
        sm.setSalle(salle);
        sm.setMateriel(materiel);
        sm.setQuantiteTotale(quantite);
        sm.setQuantiteDisponible(quantite);
        sm.setQuantiteReservee(0);
        sm.setRequis(requis);
        sm.setNotes(notes);
        sm.setDateInstallation(LocalDateTime.now());
        return salleMaterielRepo.save(sm);
    }

    @Transactional
    public SalleMateriel mettreAJourQuantite(Long salleMaterielId, int nouvelleQuantiteTotale) {
        SalleMateriel sm = salleMaterielRepo.findById(salleMaterielId)
                .orElseThrow(() -> new RuntimeException("SalleMateriel introuvable: " + salleMaterielId));
        int delta = nouvelleQuantiteTotale - sm.getQuantiteTotale();
        sm.setQuantiteTotale(nouvelleQuantiteTotale);
        sm.setQuantiteDisponible(Math.max(0, sm.getQuantiteDisponible() + delta));
        return salleMaterielRepo.save(sm);
    }

    @Transactional
    public void retirerMaterielDeSalle(Long salleMaterielId) {
        salleMaterielRepo.deleteById(salleMaterielId);
    }

    // ── Allocation automatique selon type de cours ────────────────────────────

    /**
     * Vérifie si une salle dispose des équipements requis pour un type de cours
     * sur une plage horaire donnée.
     */
    public Map<String, Object> verifierDisponibiliteEquipements(Long salleId, String typeCours,
                                                                  LocalDateTime debut, LocalDateTime fin) {
        List<String> codesRequis = getEquipementsRequisPourTypeCours(typeCours);
        List<SalleMateriel> inventaire = salleMaterielRepo.findBySalleId(salleId);

        List<Map<String, Object>> details = new ArrayList<>();
        boolean toutDisponible = true;

        for (String code : codesRequis) {
            List<SalleMateriel> matching = inventaire.stream()
                    .filter(sm -> sm.getMateriel().getTypeMateriel() != null
                            && code.equalsIgnoreCase(sm.getMateriel().getTypeMateriel().getCode()))
                    .collect(Collectors.toList());

            boolean disponible = false;
            int quantiteDisponible = 0;

            for (SalleMateriel sm : matching) {
                // Vérifier les conflits sur la plage horaire
                List<EquipementReservation> conflicts = equipReservationRepo.findConflicts(sm.getId(), debut, fin);
                int quantiteReserveeConflict = conflicts.stream().mapToInt(EquipementReservation::getQuantite).sum();
                int dispo = sm.getQuantiteTotale() - quantiteReserveeConflict;
                if (dispo > 0) {
                    disponible = true;
                    quantiteDisponible += dispo;
                }
            }

            if (!disponible) toutDisponible = false;

            Map<String, Object> detail = new LinkedHashMap<>();
            detail.put("typeEquipement", code);
            detail.put("disponible", disponible);
            detail.put("quantiteDisponible", quantiteDisponible);
            detail.put("requis", true);
            details.add(detail);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("salleId", salleId);
        result.put("typeCours", typeCours);
        result.put("toutDisponible", toutDisponible);
        result.put("equipements", details);
        return result;
    }

    /**
     * Alloue automatiquement les équipements requis pour un type de cours.
     * Appelé lors de la confirmation d'une réservation.
     *
     * @return liste des EquipementReservation créées
     */
    @Transactional
    public List<EquipementReservation> allouerEquipements(Long reservationId, Long salleId,
                                                           String typeCours,
                                                           LocalDateTime debut, LocalDateTime fin) {
        List<String> codesRequis = getEquipementsRequisPourTypeCours(typeCours);
        List<SalleMateriel> inventaire = salleMaterielRepo.findBySalleId(salleId);
        List<EquipementReservation> allocations = new ArrayList<>();

        for (String code : codesRequis) {
            List<SalleMateriel> matching = inventaire.stream()
                    .filter(sm -> sm.getMateriel().getTypeMateriel() != null
                            && code.equalsIgnoreCase(sm.getMateriel().getTypeMateriel().getCode())
                            && sm.getMateriel().getEtat() != Materiel.EtatMateriel.EN_PANNE
                            && sm.getMateriel().getEtat() != Materiel.EtatMateriel.EN_MAINTENANCE)
                    .collect(Collectors.toList());

            for (SalleMateriel sm : matching) {
                List<EquipementReservation> conflicts = equipReservationRepo.findConflicts(sm.getId(), debut, fin);
                int quantiteReserveeConflict = conflicts.stream().mapToInt(EquipementReservation::getQuantite).sum();
                int dispo = sm.getQuantiteTotale() - quantiteReserveeConflict;

                if (dispo > 0) {
                    EquipementReservation er = new EquipementReservation();
                    er.setReservationId(reservationId);
                    er.setSalleMateriel(sm);
                    er.setQuantite(1);
                    er.setTypeCours(typeCours);
                    er.setDateDebut(debut);
                    er.setDateFin(fin);
                    er.setStatut(StatutReservationEquipement.ACTIVE);
                    er.setNotes("Allocation automatique pour " + typeCours);
                    allocations.add(equipReservationRepo.save(er));
                    log.info("Équipement alloué: {} (salle {}) pour réservation {} [{}]",
                            sm.getMateriel().getNom(), salleId, reservationId, typeCours);
                    break; // Un seul équipement par type suffit
                }
            }
        }

        log.info("{} équipement(s) alloué(s) pour réservation {} (salle {}, type {})",
                allocations.size(), reservationId, salleId, typeCours);
        return allocations;
    }

    /**
     * Libère tous les équipements alloués pour une réservation (annulation).
     */
    @Transactional
    public void libererEquipements(Long reservationId) {
        List<EquipementReservation> actives = equipReservationRepo.findActiveByReservationId(reservationId);
        actives.forEach(er -> {
            er.setStatut(StatutReservationEquipement.ANNULEE);
            equipReservationRepo.save(er);
        });
        log.info("{} équipement(s) libéré(s) pour réservation {}", actives.size(), reservationId);
    }

    /**
     * Marque les équipements d'une réservation comme terminés.
     */
    @Transactional
    public void terminerEquipements(Long reservationId) {
        List<EquipementReservation> actives = equipReservationRepo.findActiveByReservationId(reservationId);
        actives.forEach(er -> {
            er.setStatut(StatutReservationEquipement.TERMINEE);
            equipReservationRepo.save(er);
        });
    }

    // ── Statistiques ──────────────────────────────────────────────────────────

    public Map<String, Object> getStatistiques() {
        List<Object[]> statsByType = equipReservationRepo.getStatsByTypeCours();
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("parTypeCours", statsByType.stream().map(row -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("typeCours", row[0]);
            m.put("nombreReservations", row[1]);
            m.put("quantiteTotale", row[2]);
            return m;
        }).collect(Collectors.toList()));
        stats.put("totalSalleMateriel", salleMaterielRepo.count());
        return stats;
    }
}
