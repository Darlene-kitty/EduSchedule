package cm.iusjc.school.service;

import cm.iusjc.school.dto.GroupeDTO;
import cm.iusjc.school.entity.Filiere;
import cm.iusjc.school.entity.Groupe;
import cm.iusjc.school.entity.Niveau;
import cm.iusjc.school.entity.School;
import cm.iusjc.school.repository.AffectationRepository;
import cm.iusjc.school.repository.GroupeRepository;
import cm.iusjc.school.repository.NiveauRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupeService {

    private final GroupeRepository groupeRepository;
    private final NiveauRepository niveauRepository;
    private final AffectationRepository affectationRepository;

    @Transactional(readOnly = true)
    public List<GroupeDTO> getAll() {
        return groupeRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<GroupeDTO> getByNiveau(Long niveauId) {
        return groupeRepository.findAll().stream()
                .filter(g -> g.getNiveau().getId().equals(niveauId))
                .map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<GroupeDTO> getById(Long id) {
        return groupeRepository.findById(id).map(this::toDTO);
    }

    @Transactional
    public GroupeDTO create(GroupeDTO dto) {
        Niveau niveau = niveauRepository.findById(dto.getNiveauId())
                .orElseThrow(() -> new RuntimeException("Niveau not found: " + dto.getNiveauId()));
        Groupe g = new Groupe();
        g.setName(dto.getName());
        g.setCode(dto.getCode());
        g.setCapacite(dto.getCapacite());
        g.setNiveau(niveau);
        g.setActive(dto.isActive());
        return toDTO(groupeRepository.save(g));
    }

    @Transactional
    public GroupeDTO update(Long id, GroupeDTO dto) {
        Groupe g = groupeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Groupe not found: " + id));
        Niveau niveau = niveauRepository.findById(dto.getNiveauId())
                .orElseThrow(() -> new RuntimeException("Niveau not found: " + dto.getNiveauId()));
        g.setName(dto.getName());
        g.setCode(dto.getCode());
        g.setCapacite(dto.getCapacite());
        g.setNiveau(niveau);
        g.setActive(dto.isActive());
        return toDTO(groupeRepository.save(g));
    }

    @Transactional
    public void delete(Long id) {
        Groupe g = groupeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Groupe not found: " + id));
        g.setActive(false);
        groupeRepository.save(g);
    }

    private GroupeDTO toDTO(Groupe g) {
        GroupeDTO dto = new GroupeDTO();
        dto.setId(g.getId());
        dto.setName(g.getName());
        dto.setCode(g.getCode());
        dto.setCapacite(g.getCapacite());

        // Niveau
        Niveau niveau = g.getNiveau();
        dto.setNiveauId(niveau.getId());
        dto.setNiveauName(niveau.getName());
        dto.setNiveauCode(niveau.getCode());

        // Filière (parent du niveau)
        Filiere filiere = niveau.getFiliere();
        dto.setFiliereId(filiere.getId());
        dto.setFiliereName(filiere.getName());
        dto.setFiliereCode(filiere.getCode());

        // École (parent de la filière)
        School school = filiere.getSchool();
        dto.setSchoolId(school.getId());
        dto.setSchoolName(school.getName());
        dto.setSchoolCode(school.getCode());
        dto.setSchoolCouleur(school.getCouleur());

        dto.setActive(g.getActive());
        // Calcul de l'effectif réel depuis les affectations actives
        dto.setEffectif((int) affectationRepository.countActiveByGroupeId(g.getId()));
        return dto;
    }
}
