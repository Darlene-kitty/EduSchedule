package cm.iusjc.school.service;

import cm.iusjc.school.dto.AffectationDTO;
import cm.iusjc.school.dto.AutoAffectationRequestDTO;
import cm.iusjc.school.dto.AutoAffectationResultDTO;
import cm.iusjc.school.entity.Affectation;
import cm.iusjc.school.entity.Filiere;
import cm.iusjc.school.entity.Groupe;
import cm.iusjc.school.entity.Niveau;
import cm.iusjc.school.entity.School;
import cm.iusjc.school.repository.AffectationRepository;
import cm.iusjc.school.repository.GroupeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AffectationService {

    private final AffectationRepository affectationRepository;
    private final GroupeRepository groupeRepository;

    // ─────────────────────────────────────────────────────────────────────────
    // Lecture
    // ─────────────────────────────────────────────────────────────────────────

    /** Retourne tous les étudiants actifs d'un groupe avec leur effectif */
    @Transactional(readOnly = true)
    public List<AffectationDTO> getEtudiantsByGroupe(Long groupeId) {
        groupeRepository.findById(groupeId)
                .orElseThrow(() -> new RuntimeException("Groupe not found: " + groupeId));
        return affectationRepository.findByGroupeIdAndActiveTrue(groupeId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    /** Retourne l'affectation active d'un étudiant (null si non affecté) */
    @Transactional(readOnly = true)
    public Optional<AffectationDTO> getGroupeByEtudiant(Long etudiantId) {
        return affectationRepository.findByEtudiantIdAndActiveTrue(etudiantId)
                .map(this::toDTO);
    }

    /** Retourne l'effectif actuel (nombre d'étudiants actifs) d'un groupe */
    @Transactional(readOnly = true)
    public long getEffectif(Long groupeId) {
        return affectationRepository.countActiveByGroupeId(groupeId);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Affectation manuelle
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Affecte manuellement un étudiant à un groupe.
     * Si l'étudiant est déjà dans un autre groupe du même niveau, l'ancienne
     * affectation est clôturée avant la nouvelle.
     */
    @Transactional
    public AffectationDTO affecter(Long groupeId, Long etudiantId) {
        Groupe groupe = groupeRepository.findById(groupeId)
                .orElseThrow(() -> new RuntimeException("Groupe not found: " + groupeId));

        if (!Boolean.TRUE.equals(groupe.getActive())) {
            throw new RuntimeException("Le groupe " + groupe.getName() + " est inactif");
        }

        long effectifActuel = affectationRepository.countActiveByGroupeId(groupeId);
        if (groupe.getCapacite() != null && effectifActuel >= groupe.getCapacite()) {
            throw new RuntimeException("Le groupe " + groupe.getName() + " est complet ("
                    + groupe.getCapacite() + "/" + groupe.getCapacite() + ")");
        }

        // Clôturer l'ancienne affectation si elle existe
        affectationRepository.findByEtudiantIdAndActiveTrue(etudiantId).ifPresent(old -> {
            old.setActive(false);
            old.setDateFin(LocalDate.now());
            affectationRepository.save(old);
            log.info("Closed previous affectation {} for student {}", old.getId(), etudiantId);
        });

        Affectation a = new Affectation();
        a.setEtudiantId(etudiantId);
        a.setGroupe(groupe);
        a.setDateDebut(LocalDate.now());
        a.setActive(true);
        return toDTO(affectationRepository.save(a));
    }

    /**
     * Retire un étudiant d'un groupe (clôture l'affectation active).
     */
    @Transactional
    public void desaffecter(Long groupeId, Long etudiantId) {
        Affectation a = affectationRepository.findByEtudiantIdAndActiveTrue(etudiantId)
                .orElseThrow(() -> new RuntimeException(
                        "Aucune affectation active trouvée pour l'étudiant " + etudiantId));
        if (!a.getGroupe().getId().equals(groupeId)) {
            throw new RuntimeException("L'étudiant " + etudiantId
                    + " n'est pas dans le groupe " + groupeId);
        }
        a.setActive(false);
        a.setDateFin(LocalDate.now());
        affectationRepository.save(a);
        log.info("Student {} removed from groupe {}", etudiantId, groupeId);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Auto-affectation round-robin
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Distribue automatiquement une liste d'étudiants entre les groupes actifs
     * d'un niveau donné, en utilisant un algorithme round-robin pondéré par
     * la place disponible dans chaque groupe.
     *
     * Algorithme :
     *  1. Récupérer tous les groupes actifs du niveau, triés par place disponible DESC
     *  2. Pour chaque étudiant (dans l'ordre de la liste) :
     *     a. Si déjà affecté et forceReaffectation=false → ignorer
     *     b. Choisir le groupe avec le plus de place disponible (round-robin équitable)
     *     c. Si aucun groupe n'a de place → rejeter
     *     d. Sinon → affecter et décrémenter la place disponible en mémoire
     */
    @Transactional
    public AutoAffectationResultDTO autoAffecter(Long niveauId, AutoAffectationRequestDTO request) {
        // Charger les groupes actifs du niveau
        List<Groupe> groupes = groupeRepository.findAll().stream()
                .filter(g -> Boolean.TRUE.equals(g.getActive())
                        && g.getNiveau().getId().equals(niveauId))
                .sorted(Comparator.comparing(g -> g.getCapacite() == null ? 0 : g.getCapacite()))
                .collect(Collectors.toList());

        if (groupes.isEmpty()) {
            throw new RuntimeException("Aucun groupe actif trouvé pour le niveau " + niveauId);
        }

        // Calculer les places disponibles actuelles pour chaque groupe
        Map<Long, Integer> placesDisponibles = new HashMap<>();
        for (Groupe g : groupes) {
            long effectif = affectationRepository.countActiveByGroupeId(g.getId());
            int capacite = g.getCapacite() != null ? g.getCapacite() : Integer.MAX_VALUE;
            placesDisponibles.put(g.getId(), (int) Math.max(0, capacite - effectif));
        }

        List<AffectationDTO> affectations = new ArrayList<>();
        List<Long> etudiantsRejetes = new ArrayList<>();
        int ignores = 0;

        // Index round-robin : on tourne sur les groupes dans l'ordre
        int groupeIndex = 0;

        for (Long etudiantId : request.getEtudiantIds()) {
            // Vérifier si déjà affecté à un groupe de ce niveau
            Optional<Affectation> existante = affectationRepository.findByEtudiantIdAndActiveTrue(etudiantId);
            boolean dejaAffecteAuNiveau = existante.isPresent()
                    && existante.get().getGroupe().getNiveau().getId().equals(niveauId);

            if (dejaAffecteAuNiveau && !request.isForceReaffectation()) {
                log.debug("Student {} already assigned to niveau {} — skipping", etudiantId, niveauId);
                ignores++;
                continue;
            }

            // Trouver le prochain groupe avec de la place (round-robin)
            Groupe groupeChoisi = null;
            for (int tentative = 0; tentative < groupes.size(); tentative++) {
                Groupe candidat = groupes.get(groupeIndex % groupes.size());
                groupeIndex++;
                if (placesDisponibles.getOrDefault(candidat.getId(), 0) > 0) {
                    groupeChoisi = candidat;
                    break;
                }
            }

            if (groupeChoisi == null) {
                log.warn("No available group for student {} in niveau {}", etudiantId, niveauId);
                etudiantsRejetes.add(etudiantId);
                continue;
            }

            // Clôturer l'ancienne affectation si forceReaffectation
            if (existante.isPresent()) {
                Affectation old = existante.get();
                old.setActive(false);
                old.setDateFin(LocalDate.now());
                affectationRepository.save(old);
            }

            // Créer la nouvelle affectation
            Affectation a = new Affectation();
            a.setEtudiantId(etudiantId);
            a.setGroupe(groupeChoisi);
            a.setDateDebut(LocalDate.now());
            a.setActive(true);
            affectations.add(toDTO(affectationRepository.save(a)));

            // Décrémenter la place disponible en mémoire
            placesDisponibles.merge(groupeChoisi.getId(), -1, Integer::sum);

            log.info("Auto-assigned student {} to groupe {} (niveau {})",
                    etudiantId, groupeChoisi.getName(), niveauId);
        }

        String message = String.format(
                "%d étudiant(s) affecté(s), %d ignoré(s) (déjà affectés), %d rejeté(s) (groupes pleins)",
                affectations.size(), ignores, etudiantsRejetes.size());

        return new AutoAffectationResultDTO(
                request.getEtudiantIds().size(),
                affectations.size(),
                ignores,
                etudiantsRejetes.size(),
                affectations,
                etudiantsRejetes,
                message
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Mapping
    // ─────────────────────────────────────────────────────────────────────────

    private AffectationDTO toDTO(Affectation a) {
        AffectationDTO dto = new AffectationDTO();
        dto.setId(a.getId());
        dto.setEtudiantId(a.getEtudiantId());
        dto.setGroupeId(a.getGroupe().getId());
        dto.setGroupeName(a.getGroupe().getName());
        dto.setGroupeCode(a.getGroupe().getCode());

        Niveau niveau = a.getGroupe().getNiveau();
        dto.setNiveauId(niveau.getId());
        dto.setNiveauName(niveau.getName());

        Filiere filiere = niveau.getFiliere();
        dto.setFiliereId(filiere.getId());
        dto.setFiliereName(filiere.getName());

        School school = filiere.getSchool();
        dto.setSchoolId(school.getId());
        dto.setSchoolName(school.getName());

        dto.setDateDebut(a.getDateDebut());
        dto.setDateFin(a.getDateFin());
        dto.setActive(Boolean.TRUE.equals(a.getActive()));
        return dto;
    }
}
