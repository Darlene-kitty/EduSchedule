package cm.iusjc.school.service;

import cm.iusjc.school.dto.CategorieUEDTO;
import cm.iusjc.school.entity.CategorieUE;
import cm.iusjc.school.repository.CategorieUERepository;
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
public class CategorieUEService {

    private final CategorieUERepository repository;

    public List<CategorieUEDTO> getAll() {
        return repository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public Optional<CategorieUEDTO> getById(Long id) {
        return repository.findById(id).map(this::toDTO);
    }

    @Transactional
    public CategorieUEDTO create(CategorieUEDTO dto) {
        CategorieUE entity = new CategorieUE();
        mapFromDTO(dto, entity);
        return toDTO(repository.save(entity));
    }

    @Transactional
    public CategorieUEDTO update(Long id, CategorieUEDTO dto) {
        CategorieUE entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("CategorieUE not found: " + id));
        mapFromDTO(dto, entity);
        return toDTO(repository.save(entity));
    }

    @Transactional
    public void delete(Long id) {
        CategorieUE entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("CategorieUE not found: " + id));
        entity.setActive(false);
        repository.save(entity);
    }

    private void mapFromDTO(CategorieUEDTO dto, CategorieUE entity) {
        entity.setCode(dto.getCode());
        entity.setNom(dto.getNom());
        entity.setCredits(dto.getCredits());
        entity.setVolumeHoraire(dto.getVolumeHoraire());
        entity.setCoefficient(dto.getCoefficient());
        entity.setDescription(dto.getDescription());
        entity.setCouleur(dto.getCouleur());
        entity.setActive(dto.isActive());
        if (dto.getType() != null) {
            try {
                entity.setType(CategorieUE.TypeCategorie.valueOf(dto.getType().toUpperCase()));
            } catch (IllegalArgumentException e) {
                entity.setType(CategorieUE.TypeCategorie.FONDAMENTALE);
            }
        }
    }

    private CategorieUEDTO toDTO(CategorieUE e) {
        CategorieUEDTO dto = new CategorieUEDTO();
        dto.setId(e.getId());
        dto.setCode(e.getCode());
        dto.setNom(e.getNom());
        dto.setType(e.getType() != null ? e.getType().name() : null);
        dto.setCredits(e.getCredits());
        dto.setVolumeHoraire(e.getVolumeHoraire());
        dto.setCoefficient(e.getCoefficient());
        dto.setDescription(e.getDescription());
        dto.setCouleur(e.getCouleur());
        dto.setActive(e.getActive());
        return dto;
    }
}
