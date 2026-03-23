package cm.iusjc.school.service;

import cm.iusjc.school.dto.NiveauDTO;
import cm.iusjc.school.entity.Filiere;
import cm.iusjc.school.entity.Niveau;
import cm.iusjc.school.repository.FiliereRepository;
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
public class NiveauService {

    private final NiveauRepository niveauRepository;
    private final FiliereRepository filiereRepository;

    public List<NiveauDTO> getAll() {
        return niveauRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<NiveauDTO> getByFiliere(Long filiereId) {
        return niveauRepository.findAll().stream()
                .filter(n -> n.getFiliere().getId().equals(filiereId))
                .map(this::toDTO).collect(Collectors.toList());
    }

    public Optional<NiveauDTO> getById(Long id) {
        return niveauRepository.findById(id).map(this::toDTO);
    }

    @Transactional
    public NiveauDTO create(NiveauDTO dto) {
        Filiere filiere = filiereRepository.findById(dto.getFiliereId())
                .orElseThrow(() -> new RuntimeException("Filiere not found: " + dto.getFiliereId()));
        Niveau n = new Niveau();
        n.setName(dto.getName());
        n.setCode(dto.getCode());
        n.setOrdre(dto.getOrdre());
        n.setFiliere(filiere);
        n.setActive(dto.isActive());
        return toDTO(niveauRepository.save(n));
    }

    @Transactional
    public NiveauDTO update(Long id, NiveauDTO dto) {
        Niveau n = niveauRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Niveau not found: " + id));
        Filiere filiere = filiereRepository.findById(dto.getFiliereId())
                .orElseThrow(() -> new RuntimeException("Filiere not found: " + dto.getFiliereId()));
        n.setName(dto.getName());
        n.setCode(dto.getCode());
        n.setOrdre(dto.getOrdre());
        n.setFiliere(filiere);
        n.setActive(dto.isActive());
        return toDTO(niveauRepository.save(n));
    }

    @Transactional
    public void delete(Long id) {
        Niveau n = niveauRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Niveau not found: " + id));
        n.setActive(false);
        niveauRepository.save(n);
    }

    private NiveauDTO toDTO(Niveau n) {
        NiveauDTO dto = new NiveauDTO();
        dto.setId(n.getId());
        dto.setName(n.getName());
        dto.setCode(n.getCode());
        dto.setOrdre(n.getOrdre());
        dto.setFiliereId(n.getFiliere().getId());
        dto.setFiliereName(n.getFiliere().getName());
        dto.setActive(n.getActive());
        return dto;
    }
}
