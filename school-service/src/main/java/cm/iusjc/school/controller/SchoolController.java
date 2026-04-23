package cm.iusjc.school.controller;

import cm.iusjc.school.dto.SchoolDTO;
import cm.iusjc.school.service.SchoolService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/schools")
@RequiredArgsConstructor
@Slf4j
public class SchoolController {
    
    private final SchoolService schoolService;
    
    /**
     * Crée une nouvelle école
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createSchool(@Valid @RequestBody SchoolDTO schoolDTO) {
        try {
            log.info("Creating school: {}", schoolDTO.getName());
            SchoolDTO createdSchool = schoolService.createSchool(schoolDTO);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "message", "School created successfully",
                "data", createdSchool
            ));
        } catch (Exception e) {
            log.error("Error creating school: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Récupère toutes les écoles
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllSchools() {
        try {
            List<SchoolDTO> schools = schoolService.getAllSchools();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", schools,
                "total", schools.size()
            ));
        } catch (Exception e) {
            log.error("Error fetching schools: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Récupère les écoles avec pagination
     */
    @GetMapping("/paginated")
    public ResponseEntity<Map<String, Object>> getAllSchoolsPaginated(Pageable pageable) {
        try {
            Page<SchoolDTO> schoolsPage = schoolService.getAllSchools(pageable);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", schoolsPage.getContent(),
                "page", schoolsPage.getNumber(),
                "size", schoolsPage.getSize(),
                "totalElements", schoolsPage.getTotalElements(),
                "totalPages", schoolsPage.getTotalPages()
            ));
        } catch (Exception e) {
            log.error("Error fetching paginated schools: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Récupère une école par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getSchoolById(@PathVariable Long id) {
        try {
            return schoolService.getSchoolById(id)
                    .map(school -> ResponseEntity.ok(Map.of(
                        "success", true,
                        "data", school
                    )))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error fetching school by ID {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Récupère une école par nom
     */
    @GetMapping("/by-name/{name}")
    public ResponseEntity<Map<String, Object>> getSchoolByName(@PathVariable String name) {
        try {
            return schoolService.getSchoolByName(name)
                    .map(school -> ResponseEntity.ok(Map.of(
                        "success", true,
                        "data", school
                    )))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error fetching school by name {}: {}", name, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Récupère une école par code
     */
    @GetMapping("/by-code/{code}")
    public ResponseEntity<Map<String, Object>> getSchoolByCode(@PathVariable String code) {
        try {
            return schoolService.getSchoolByCode(code)
                    .map(school -> ResponseEntity.ok(Map.of(
                        "success", true,
                        "data", school
                    )))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error fetching school by code {}: {}", code, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Récupère les écoles actives
     */
    @GetMapping("/active")
    public ResponseEntity<Map<String, Object>> getActiveSchools() {
        try {
            List<SchoolDTO> activeSchools = schoolService.getActiveSchools();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", activeSchools,
                "total", activeSchools.size()
            ));
        } catch (Exception e) {
            log.error("Error fetching active schools: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Récupère les écoles par ville
     */
    @GetMapping("/city/{city}")
    public ResponseEntity<Map<String, Object>> getSchoolsByCity(@PathVariable String city) {
        try {
            List<SchoolDTO> schools = schoolService.getSchoolsByCity(city);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", schools,
                "total", schools.size()
            ));
        } catch (Exception e) {
            log.error("Error fetching schools by city {}: {}", city, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Récupère les écoles par pays
     */
    @GetMapping("/country/{country}")
    public ResponseEntity<Map<String, Object>> getSchoolsByCountry(@PathVariable String country) {
        try {
            List<SchoolDTO> schools = schoolService.getSchoolsByCountry(country);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", schools,
                "total", schools.size()
            ));
        } catch (Exception e) {
            log.error("Error fetching schools by country {}: {}", country, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Met à jour une école
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateSchool(
            @PathVariable Long id, 
            @Valid @RequestBody SchoolDTO schoolDTO) {
        try {
            SchoolDTO updatedSchool = schoolService.updateSchool(id, schoolDTO);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "School updated successfully",
                "data", updatedSchool
            ));
        } catch (Exception e) {
            log.error("Error updating school {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Active/désactive une école
     */
    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<Map<String, Object>> toggleSchoolStatus(@PathVariable Long id) {
        try {
            SchoolDTO updatedSchool = schoolService.toggleSchoolStatus(id);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "School status updated successfully",
                "data", updatedSchool
            ));
        } catch (Exception e) {
            log.error("Error toggling school status {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Supprime une école (soft delete)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteSchool(@PathVariable Long id) {
        try {
            schoolService.deleteSchool(id);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "School deleted successfully"
            ));
        } catch (Exception e) {
            log.error("Error deleting school {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Supprime définitivement une école
     */
    @DeleteMapping("/{id}/hard")
    public ResponseEntity<Map<String, Object>> hardDeleteSchool(@PathVariable Long id) {
        try {
            schoolService.hardDeleteSchool(id);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "School permanently deleted"
            ));
        } catch (Exception e) {
            log.error("Error hard deleting school {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Recherche des écoles par nom
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchSchools(@RequestParam String name) {
        try {
            List<SchoolDTO> schools = schoolService.searchSchoolsByName(name);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", schools,
                "total", schools.size()
            ));
        } catch (Exception e) {
            log.error("Error searching schools: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Recherche avancée d'écoles
     */
    @GetMapping("/advanced-search")
    public ResponseEntity<Map<String, Object>> advancedSearchSchools(@RequestParam String searchTerm) {
        try {
            List<SchoolDTO> schools = schoolService.searchSchools(searchTerm);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", schools,
                "total", schools.size()
            ));
        } catch (Exception e) {
            log.error("Error performing advanced search: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Obtient les statistiques des écoles
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getSchoolStatistics() {
        try {
            long totalSchools = schoolService.countSchools();
            long activeSchools = schoolService.countActiveSchools();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", Map.of(
                    "totalSchools", totalSchools,
                    "activeSchools", activeSchools,
                    "inactiveSchools", totalSchools - activeSchools,
                    "countryStats", schoolService.getSchoolStatisticsByCountry(),
                    "cityStats", schoolService.getSchoolStatisticsByCity()
                )
            ));
        } catch (Exception e) {
            log.error("Error fetching school statistics: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Vérifie si une école existe par nom
     */
    @GetMapping("/exists/name/{name}")
    public ResponseEntity<Map<String, Object>> checkSchoolExistsByName(@PathVariable String name) {
        try {
            boolean exists = schoolService.existsByName(name);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", Map.of("exists", exists)
            ));
        } catch (Exception e) {
            log.error("Error checking school existence by name {}: {}", name, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Vérifie si une école existe par code
     */
    @GetMapping("/exists/code/{code}")
    public ResponseEntity<Map<String, Object>> checkSchoolExistsByCode(@PathVariable String code) {
        try {
            boolean exists = schoolService.existsByCode(code);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", Map.of("exists", exists)
            ));
        } catch (Exception e) {
            log.error("Error checking school existence by code {}: {}", code, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
}
