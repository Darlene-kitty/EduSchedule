package cm.iusjc.resource.service;

import cm.iusjc.resource.entity.MaintenanceMateriel;
import cm.iusjc.resource.entity.MaintenanceMateriel.StatutIntervention;
import cm.iusjc.resource.entity.MaintenanceMateriel.TypeIntervention;
import cm.iusjc.resource.entity.Materiel;
import cm.iusjc.resource.entity.UsageMateriel;
import cm.iusjc.resource.repository.MaintenanceMaterielRepository;
import cm.iusjc.resource.repository.MaterielRepository;
import cm.iusjc.resource.repository.UsageMaterielRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class EquipmentUsageService {

    private final UsageMaterielRepository usageRepo;
    private final MaintenanceMaterielRepository maintenanceRepo;
    private final MaterielRepository materielRepo;

    /** Seuil d'heures d'utilisation avant alerte de maintenance préventive */
    private static final long SEUIL_HEURES_MAINTENANCE = 200L;
    /** Seuil de jours sans maintenance avant alerte */
    private static final int SEUIL_JOURS_SANS_MAINTENANCE = 90;

    // ── Enregistrement d'utilisation ──────────────────────────────────────────

    @Transactional
    public UsageMateriel enregistrerUsage(Long materielId, Long reservationId, Long coursId,
                                           String typeCours, LocalDateTime debut, LocalDateTime fin) {
        Materiel materiel = materielRepo.findById(materielId)
                .orElseThrow(() -> new RuntimeException("Matériel introuvable: " + materielId));

        UsageMateriel usage = new UsageMateriel();
        usage.setMateriel(materiel);
        usage.setReservationId(reservationId);
        usage.setCoursId(coursId);
        usage.setTypeCours(typeCours);
        usage.setDateDebut(debut);
        usage.setDateFin(fin);

        UsageMateriel saved = usageRepo.save(usage);
        log.info("Usage enregistré pour matériel {} : {} min", materielId,
                java.time.Duration.between(debut, fin).toMinutes());

        verifierAlertesMaintenance(materiel);
        return saved;
    }

    @Transactional
    public void signalerProbleme(Long usageId, String description) {
        UsageMateriel usage = usageRepo.findById(usageId)
                .orElseThrow(() -> new RuntimeException("Usage introuvable: " + usageId));
        usage.setProblemeSignale(true);
        usage.setDescriptionProbleme(description);
        usageRepo.save(usage);

        // Mettre le matériel en panne automatiquement
        Materiel m = usage.getMateriel();
        m.setEtat(Materiel.EtatMateriel.EN_PANNE);
        materielRepo.save(m);
        log.warn("Problème signalé sur matériel {} : {}", m.getId(), description);
    }

    // ── Historique de maintenance ─────────────────────────────────────────────

    @Transactional
    public MaintenanceMateriel creerMaintenance(Long materielId, TypeIntervention type,
                                                 LocalDateTime dateDebut, String description,
                                                 String technicien) {
        Materiel materiel = materielRepo.findById(materielId)
                .orElseThrow(() -> new RuntimeException("Matériel introuvable: " + materielId));

        MaintenanceMateriel maintenance = new MaintenanceMateriel();
        maintenance.setMateriel(materiel);
        maintenance.setTypeIntervention(type);
        maintenance.setDateDebut(dateDebut);
        maintenance.setDescription(description);
        maintenance.setTechnicien(technicien);
        maintenance.setStatut(StatutIntervention.PLANIFIEE);
        maintenance.setHeuresUtilisationAuMoment(
                usageRepo.sumDureeMinutesByMaterielId(materielId) / 60);

        // Mettre à jour l'état du matériel
        if (type == TypeIntervention.PANNE) {
            materiel.setEtat(Materiel.EtatMateriel.EN_PANNE);
        } else {
            materiel.setEtat(Materiel.EtatMateriel.EN_MAINTENANCE);
        }
        materielRepo.save(materiel);

        return maintenanceRepo.save(maintenance);
    }

    @Transactional
    public MaintenanceMateriel terminerMaintenance(Long maintenanceId, String notes, String cout) {
        MaintenanceMateriel m = maintenanceRepo.findById(maintenanceId)
                .orElseThrow(() -> new RuntimeException("Maintenance introuvable: " + maintenanceId));
        m.setStatut(StatutIntervention.TERMINEE);
        m.setDateFin(LocalDateTime.now());
        m.setNotes(notes);
        m.setCoutReparation(cout);

        // Remettre le matériel en bon état
        Materiel mat = m.getMateriel();
        mat.setEtat(Materiel.EtatMateriel.BON_ETAT);
        materielRepo.save(mat);

        return maintenanceRepo.save(m);
    }

    // ── Statistiques d'utilisation ────────────────────────────────────────────

    public Map<String, Object> getStatsMateriel(Long materielId) {
        long totalMinutes = usageRepo.sumDureeMinutesByMaterielId(materielId);
        long totalHeures = totalMinutes / 60;
        long totalUsages = usageRepo.countUsagesByMaterielId(materielId);
        long totalProblemes = usageRepo.countProblemesByMaterielId(materielId);
        long totalPannes = maintenanceRepo.countByMaterielIdAndType(materielId, TypeIntervention.PANNE);

        double tauxFiabilite = totalUsages > 0
                ? Math.round((1.0 - (double) totalProblemes / totalUsages) * 1000.0) / 10.0
                : 100.0;

        List<MaintenanceMateriel> historique = maintenanceRepo.findByMaterielIdOrderByDateDebutDesc(materielId);
        List<UsageMateriel> derniersUsages = usageRepo.findByMaterielIdOrderByDateDebutDesc(materielId)
                .stream().limit(10).toList();

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("materielId", materielId);
        stats.put("totalHeuresUtilisation", totalHeures);
        stats.put("totalUsages", totalUsages);
        stats.put("totalProblemes", totalProblemes);
        stats.put("totalPannes", totalPannes);
        stats.put("tauxFiabilite", tauxFiabilite);
        stats.put("historiqueMaintenances", historique);
        stats.put("derniersUsages", derniersUsages);
        stats.put("alerteMaintenanceRequise", totalHeures >= SEUIL_HEURES_MAINTENANCE);
        return stats;
    }

    /** Tableau de bord global des équipements */
    public Map<String, Object> getDashboard() {
        List<Materiel> tous = materielRepo.findByActiveTrue();

        long bonEtat = tous.stream().filter(m -> m.getEtat() == Materiel.EtatMateriel.BON_ETAT).count();
        long usage = tous.stream().filter(m -> m.getEtat() == Materiel.EtatMateriel.USAGE).count();
        long enPanne = tous.stream().filter(m -> m.getEtat() == Materiel.EtatMateriel.EN_PANNE).count();
        long enMaintenance = tous.stream().filter(m -> m.getEtat() == Materiel.EtatMateriel.EN_MAINTENANCE).count();

        List<Long> dueForMaintenance = maintenanceRepo.findMaterielsDueForMaintenance(
                LocalDateTime.now().minusDays(SEUIL_JOURS_SANS_MAINTENANCE));

        List<Object[]> topUsed = usageRepo.findTopUsedMateriels();
        List<Map<String, Object>> topUsedList = topUsed.stream().limit(5).map(row -> {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("materielId", row[0]);
            entry.put("totalMinutes", row[1]);
            materielRepo.findById(((Number) row[0]).longValue())
                    .ifPresent(m -> entry.put("nom", m.getNom()));
            return entry;
        }).toList();

        Map<String, Object> dashboard = new LinkedHashMap<>();
        dashboard.put("totalMateriels", tous.size());
        dashboard.put("bonEtat", bonEtat);
        dashboard.put("usage", usage);
        dashboard.put("enPanne", enPanne);
        dashboard.put("enMaintenance", enMaintenance);
        dashboard.put("tauxDisponibilite", tous.isEmpty() ? 100.0
                : Math.round((double) bonEtat / tous.size() * 1000.0) / 10.0);
        dashboard.put("materielsDueForMaintenance", dueForMaintenance.size());
        dashboard.put("topMaterielsUtilises", topUsedList);
        dashboard.put("maintenancesEnCours",
                maintenanceRepo.findByStatut(StatutIntervention.EN_COURS).size());
        dashboard.put("maintenancesPlanifiees",
                maintenanceRepo.findByStatut(StatutIntervention.PLANIFIEE).size());
        return dashboard;
    }

    /** Alertes de maintenance préventive */
    public List<Map<String, Object>> getAlertes() {
        List<Map<String, Object>> alertes = new ArrayList<>();

        // Matériels dépassant le seuil d'heures
        materielRepo.findByActiveTrue().forEach(m -> {
            long heures = usageRepo.sumDureeMinutesByMaterielId(m.getId()) / 60;
            if (heures >= SEUIL_HEURES_MAINTENANCE) {
                Map<String, Object> alerte = new LinkedHashMap<>();
                alerte.put("type", "SEUIL_HEURES");
                alerte.put("materielId", m.getId());
                alerte.put("nom", m.getNom());
                alerte.put("heuresUtilisation", heures);
                alerte.put("seuil", SEUIL_HEURES_MAINTENANCE);
                alerte.put("message", m.getNom() + " a dépassé " + SEUIL_HEURES_MAINTENANCE + "h d'utilisation");
                alertes.add(alerte);
            }
        });

        // Matériels sans maintenance depuis trop longtemps
        List<Long> overdue = maintenanceRepo.findMaterielsDueForMaintenance(
                LocalDateTime.now().minusDays(SEUIL_JOURS_SANS_MAINTENANCE));
        overdue.forEach(id -> materielRepo.findById(id).ifPresent(m -> {
            Map<String, Object> alerte = new LinkedHashMap<>();
            alerte.put("type", "MAINTENANCE_OVERDUE");
            alerte.put("materielId", m.getId());
            alerte.put("nom", m.getNom());
            alerte.put("message", m.getNom() + " n'a pas eu de maintenance depuis plus de "
                    + SEUIL_JOURS_SANS_MAINTENANCE + " jours");
            alertes.add(alerte);
        }));

        // Matériels en panne sans intervention planifiée
        materielRepo.findByActiveTrue().stream()
                .filter(m -> m.getEtat() == Materiel.EtatMateriel.EN_PANNE)
                .forEach(m -> {
                    boolean hasPending = !maintenanceRepo
                            .findByMaterielIdAndStatut(m.getId(), StatutIntervention.PLANIFIEE).isEmpty();
                    if (!hasPending) {
                        Map<String, Object> alerte = new LinkedHashMap<>();
                        alerte.put("type", "PANNE_SANS_INTERVENTION");
                        alerte.put("materielId", m.getId());
                        alerte.put("nom", m.getNom());
                        alerte.put("message", m.getNom() + " est en panne sans intervention planifiée");
                        alertes.add(alerte);
                    }
                });

        return alertes;
    }

    // ── Privé ─────────────────────────────────────────────────────────────────

    private void verifierAlertesMaintenance(Materiel materiel) {
        long heures = usageRepo.sumDureeMinutesByMaterielId(materiel.getId()) / 60;
        if (heures >= SEUIL_HEURES_MAINTENANCE && materiel.getEtat() == Materiel.EtatMateriel.BON_ETAT) {
            log.warn("ALERTE MAINTENANCE: {} a atteint {}h d'utilisation", materiel.getNom(), heures);
            // Passer en état "Usagé" automatiquement
            materiel.setEtat(Materiel.EtatMateriel.USAGE);
            materielRepo.save(materiel);
        }
    }
}
