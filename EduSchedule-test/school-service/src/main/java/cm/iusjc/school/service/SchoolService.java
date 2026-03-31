package cm.iusjc.school.service;

import cm.iusjc.school.dto.SchoolDTO;
import cm.iusjc.school.entity.School;
import cm.iusjc.school.repository.SchoolRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchoolService {
    
    private final SchoolRepository schoolRepository;
    
    /**
     * Crée une nouvelle école
     */
    @Transactional
    @CacheEvict(value = "schools", allEntries = true)
    public SchoolDTO createSchool(SchoolDTO schoolDTO) {
        // Résoudre les alias frontend (nom→name, sigle→code, telephone→phone, enabled→active)
        if (schoolDTO.getName() == null && schoolDTO.getSigle() != null) {
            schoolDTO.setName(schoolDTO.getSigle());
        }
        // Si "nom" n'est pas mappé directement (champ ignoré), on utilise sigle comme fallback
        if (schoolDTO.getName() == null || schoolDTO.getName().isBlank()) {
            throw new RuntimeException("School name is required");
        }
        if (schoolDTO.getCode() == null && schoolDTO.getSigle() != null) {
            schoolDTO.setCode(schoolDTO.getSigle());
        }
        if (schoolDTO.getPhone() == null && schoolDTO.getTelephone() != null) {
            schoolDTO.setPhone(schoolDTO.getTelephone());
        }
        if (schoolDTO.getEnabled() != null) {
            schoolDTO.setActive(schoolDTO.getEnabled());
        }

        log.info("Creating new school: {}", schoolDTO.getName());
        
        if (schoolRepository.existsByName(schoolDTO.getName())) {
            throw new RuntimeException("School with name '" + schoolDTO.getName() + "' already exists");
        }
        
        School school = new School();
        school.setName(schoolDTO.getName());
        school.setCode(schoolDTO.getCode());
        school.setAddress(schoolDTO.getAddress());
        school.setCity(schoolDTO.getCity());
        school.setPostalCode(schoolDTO.getPostalCode());
        school.setCountry(schoolDTO.getCountry());
        school.setPhone(schoolDTO.getPhone());
        school.setEmail(schoolDTO.getEmail());
        school.setWebsite(schoolDTO.getWebsite());
        school.setDescription(schoolDTO.getDescription());
        school.setActive(schoolDTO.isActive());
        school.setCreatedAt(LocalDateTime.now());
        school.setUpdatedAt(LocalDateTime.now());
        
        School savedSchool = schoolRepository.save(school);
        log.info("School created successfully with ID: {}", savedSchool.getId());
        
        return convertToDTO(savedSchool);
    }
    
    /**
     * Récupère toutes les écoles
     */
    @Cacheable(value = "schools")
    public List<SchoolDTO> getAllSchools() {
        log.debug("Fetching all schools");
        return schoolRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les écoles avec pagination
     */
    public Page<SchoolDTO> getAllSchools(Pageable pageable) {
        log.debug("Fetching schools with pagination: {}", pageable);
        return schoolRepository.findAll(pageable)
                .map(this::convertToDTO);
    }
    
    /**
     * Récupère une école par ID
     */
    @Cacheable(value = "schools", key = "#id")
    public Optional<SchoolDTO> getSchoolById(Long id) {
        log.debug("Fetching school by ID: {}", id);
        return schoolRepository.findById(id)
                .map(this::convertToDTO);
    }
    
    /**
     * Récupère une école par nom
     */
    @Cacheable(value = "schools", key = "#name")
    public Optional<SchoolDTO> getSchoolByName(String name) {
        log.debug("Fetching school by name: {}", name);
        return schoolRepository.findByName(name)
                .map(this::convertToDTO);
    }
    
    /**
     * Récupère une école par code
     */
    @Cacheable(value = "schools", key = "#code")
    public Optional<SchoolDTO> getSchoolByCode(String code) {
        log.debug("Fetching school by code: {}", code);
        return schoolRepository.findByCode(code)
                .map(this::convertToDTO);
    }
    
    /**
     * Récupère les écoles actives
     */
    @Cacheable(value = "activeSchools")
    public List<SchoolDTO> getActiveSchools() {
        log.debug("Fetching active schools");
        return schoolRepository.findByActiveTrue().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les écoles par ville
     */
    public List<SchoolDTO> getSchoolsByCity(String city) {
        log.debug("Fetching schools by city: {}", city);
        return schoolRepository.findByCity(city).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les écoles par pays
     */
    public List<SchoolDTO> getSchoolsByCountry(String country) {
        log.debug("Fetching schools by country: {}", country);
        return schoolRepository.findByCountry(country).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Met à jour une école
     */
    @Transactional
    @CacheEvict(value = {"schools", "activeSchools"}, allEntries = true)
    public SchoolDTO updateSchool(Long id, SchoolDTO schoolDTO) {
        log.info("Updating school with ID: {}", id);
        
        // Résoudre les alias frontend
        if (schoolDTO.getName() == null && schoolDTO.getSigle() != null) {
            schoolDTO.setName(schoolDTO.getSigle());
        }
        if (schoolDTO.getCode() == null && schoolDTO.getSigle() != null) {
            schoolDTO.setCode(schoolDTO.getSigle());
        }
        if (schoolDTO.getPhone() == null && schoolDTO.getTelephone() != null) {
            schoolDTO.setPhone(schoolDTO.getTelephone());
        }
        if (schoolDTO.getEnabled() != null) {
            schoolDTO.setActive(schoolDTO.getEnabled());
        }

        School school = schoolRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("School not found with ID: " + id));
        
        if (schoolDTO.getName() != null && !school.getName().equals(schoolDTO.getName()) && 
            schoolRepository.existsByName(schoolDTO.getName())) {
            throw new RuntimeException("School with name '" + schoolDTO.getName() + "' already exists");
        }
        
        if (schoolDTO.getName() != null) school.setName(schoolDTO.getName());
        if (schoolDTO.getCode() != null) school.setCode(schoolDTO.getCode());
        if (schoolDTO.getAddress() != null) school.setAddress(schoolDTO.getAddress());
        if (schoolDTO.getCity() != null) school.setCity(schoolDTO.getCity());
        if (schoolDTO.getPostalCode() != null) school.setPostalCode(schoolDTO.getPostalCode());
        if (schoolDTO.getCountry() != null) school.setCountry(schoolDTO.getCountry());
        if (schoolDTO.getPhone() != null) school.setPhone(schoolDTO.getPhone());
        if (schoolDTO.getEmail() != null) school.setEmail(schoolDTO.getEmail());
        if (schoolDTO.getWebsite() != null) school.setWebsite(schoolDTO.getWebsite());
        if (schoolDTO.getDescription() != null) school.setDescription(schoolDTO.getDescription());
        school.setActive(schoolDTO.isActive());
        school.setUpdatedAt(LocalDateTime.now());
        
        School updatedSchool = schoolRepository.save(school);
        log.info("School updated successfully: {}", updatedSchool.getId());
        
        return convertToDTO(updatedSchool);
    }
    
    /**
     * Active/désactive une école
     */
    @Transactional
    @CacheEvict(value = {"schools", "activeSchools"}, allEntries = true)
    public SchoolDTO toggleSchoolStatus(Long id) {
        log.info("Toggling status for school ID: {}", id);
        
        School school = schoolRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("School not found with ID: " + id));
        
        school.setActive(!school.isActive());
        school.setUpdatedAt(LocalDateTime.now());
        School updatedSchool = schoolRepository.save(school);
        
        log.info("School status toggled: {} - Active: {}", updatedSchool.getName(), updatedSchool.isActive());
        return convertToDTO(updatedSchool);
    }
    
    /**
     * Supprime une école (soft delete)
     */
    @Transactional
    @CacheEvict(value = {"schools", "activeSchools"}, allEntries = true)
    public void deleteSchool(Long id) {
        log.info("Deleting school with ID: {}", id);
        
        School school = schoolRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("School not found with ID: " + id));
        
        // Vérifier si l'école a des utilisateurs ou des ressources associées
        if (hasAssociatedData(id)) {
            throw new RuntimeException("Cannot delete school: it has associated data");
        }
        
        // Soft delete - désactiver l'école
        school.setActive(false);
        school.setUpdatedAt(LocalDateTime.now());
        schoolRepository.save(school);
        
        log.info("School soft deleted: {}", school.getName());
    }
    
    /**
     * Supprime définitivement une école
     */
    @Transactional
    @CacheEvict(value = {"schools", "activeSchools"}, allEntries = true)
    public void hardDeleteSchool(Long id) {
        log.warn("Hard deleting school with ID: {}", id);
        
        if (hasAssociatedData(id)) {
            throw new RuntimeException("Cannot delete school: it has associated data");
        }
        
        schoolRepository.deleteById(id);
        log.warn("School hard deleted with ID: {}", id);
    }
    
    /**
     * Recherche des écoles par nom
     */
    public List<SchoolDTO> searchSchoolsByName(String name) {
        log.debug("Searching schools by name containing: {}", name);
        return schoolRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Recherche des écoles par critères multiples
     */
    public List<SchoolDTO> searchSchools(String searchTerm) {
        log.debug("Searching schools with term: {}", searchTerm);
        return schoolRepository.searchSchools(searchTerm).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Vérifie si une école existe
     */
    public boolean existsById(Long id) {
        return schoolRepository.existsById(id);
    }
    
    /**
     * Vérifie si une école existe par nom
     */
    public boolean existsByName(String name) {
        return schoolRepository.existsByName(name);
    }
    
    /**
     * Vérifie si une école existe par code
     */
    public boolean existsByCode(String code) {
        return schoolRepository.existsByCode(code);
    }
    
    /**
     * Compte le nombre total d'écoles
     */
    public long countSchools() {
        return schoolRepository.count();
    }
    
    /**
     * Compte le nombre d'écoles actives
     */
    public long countActiveSchools() {
        return schoolRepository.countByActiveTrue();
    }
    
    /**
     * Obtient les statistiques des écoles par pays
     */
    public List<Object[]> getSchoolStatisticsByCountry() {
        return schoolRepository.getSchoolCountByCountry();
    }
    
    /**
     * Obtient les statistiques des écoles par ville
     */
    public List<Object[]> getSchoolStatisticsByCity() {
        return schoolRepository.getSchoolCountByCity();
    }
    
    /**
     * Vérifie si l'école a des données associées
     */
    private boolean hasAssociatedData(Long schoolId) {
        // Cette méthode devrait vérifier dans les autres services
        // Pour l'instant, on retourne false
        return false;
    }
    
    /**
     * Convertit une entité School en DTO
     */
    private SchoolDTO convertToDTO(School school) {
        SchoolDTO dto = new SchoolDTO();
        dto.setId(school.getId());
        dto.setName(school.getName());
        dto.setCode(school.getCode());
        dto.setAddress(school.getAddress());
        dto.setCity(school.getCity());
        dto.setPostalCode(school.getPostalCode());
        dto.setCountry(school.getCountry());
        dto.setPhone(school.getPhone());
        dto.setEmail(school.getEmail());
        dto.setWebsite(school.getWebsite());
        dto.setDescription(school.getDescription());
        dto.setActive(school.isActive());
        dto.setCreatedAt(school.getCreatedAt());
        dto.setUpdatedAt(school.getUpdatedAt());
        // Alias pour le frontend Angular
        dto.setSigle(school.getCode() != null ? school.getCode() : school.getName());
        dto.setTelephone(school.getPhone());
        dto.setEnabled(school.isActive());
        return dto;
    }
}