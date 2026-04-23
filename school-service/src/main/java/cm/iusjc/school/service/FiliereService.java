package cm.iusjc.school.service;

import cm.iusjc.school.dto.FiliereDTO;
import cm.iusjc.school.entity.Filiere;
import cm.iusjc.school.entity.School;
import cm.iusjc.school.repository.FiliereRepository;
import cm.iusjc.school.repository.SchoolRepository;
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
public class FiliereService {

    private final FiliereRepository filiereRepository;
    private final SchoolRepository schoolRepository;

    @Transactional(readOnly = true)
    public List<FiliereDTO> getAll() {
        return filiereRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FiliereDTO> getBySchool(Long schoolId) {
        return filiereRepository.findAll().stream()
                .filter(f -> f.getSchool().getId().equals(schoolId))
                .map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<FiliereDTO> getById(Long id) {
        return filiereRepository.findById(id).map(this::toDTO);
    }

    @Transactional
    public FiliereDTO create(FiliereDTO dto) {
        School school = schoolRepository.findById(dto.getSchoolId())
                .orElseThrow(() -> new RuntimeException("School not found: " + dto.getSchoolId()));
        Filiere f = new Filiere();
        f.setName(dto.getName());
        f.setCode(dto.getCode());
        f.setDescription(dto.getDescription());
        f.setSchool(school);
        f.setActive(dto.isActive());
        return toDTO(filiereRepository.save(f));
    }

    @Transactional
    public FiliereDTO update(Long id, FiliereDTO dto) {
        Filiere f = filiereRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Filiere not found: " + id));
        School school = schoolRepository.findById(dto.getSchoolId())
                .orElseThrow(() -> new RuntimeException("School not found: " + dto.getSchoolId()));
        f.setName(dto.getName());
        f.setCode(dto.getCode());
        f.setDescription(dto.getDescription());
        f.setSchool(school);
        f.setActive(dto.isActive());
        return toDTO(filiereRepository.save(f));
    }

    @Transactional
    public void delete(Long id) {
        Filiere f = filiereRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Filiere not found: " + id));
        f.setActive(false);
        filiereRepository.save(f);
    }

    private FiliereDTO toDTO(Filiere f) {
        FiliereDTO dto = new FiliereDTO();
        dto.setId(f.getId());
        dto.setName(f.getName());
        dto.setCode(f.getCode());
        dto.setDescription(f.getDescription());
        dto.setSchoolId(f.getSchool().getId());
        dto.setSchoolName(f.getSchool().getName());
        dto.setActive(f.getActive());
        return dto;
    }
}
