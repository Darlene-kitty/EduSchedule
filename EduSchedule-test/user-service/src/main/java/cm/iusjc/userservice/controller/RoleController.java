package cm.iusjc.userservice.controller;

import cm.iusjc.userservice.dto.RoleDTO;
import cm.iusjc.userservice.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
@Slf4j
public class RoleController {
    
    private final RoleService roleService;
    
    /**
     * Crée un nouveau rôle
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createRole(@Valid @RequestBody RoleDTO roleDTO) {
        try {
            log.info("Creating role: {}", roleDTO.getName());
            RoleDTO createdRole = roleService.createRole(roleDTO);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "message", "Role created successfully",
                "data", createdRole
            ));
        } catch (Exception e) {
            log.error("Error creating role: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Récupère tous les rôles
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAllRoles() {
        try {
            List<RoleDTO> roles = roleService.getAllRoles();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", roles,
                "total", roles.size()
            ));
        } catch (Exception e) {
            log.error("Error fetching roles: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Récupère les rôles avec pagination
     */
    @GetMapping("/paginated")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAllRolesPaginated(Pageable pageable) {
        try {
            Page<RoleDTO> rolesPage = roleService.getAllRoles(pageable);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", rolesPage.getContent(),
                "page", rolesPage.getNumber(),
                "size", rolesPage.getSize(),
                "totalElements", rolesPage.getTotalElements(),
                "totalPages", rolesPage.getTotalPages()
            ));
        } catch (Exception e) {
            log.error("Error fetching paginated roles: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Récupère un rôle par ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getRoleById(@PathVariable Long id) {
        try {
            return roleService.getRoleById(id)
                    .map(role -> ResponseEntity.ok(Map.of(
                        "success", true,
                        "data", role
                    )))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error fetching role by ID {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Récupère un rôle par nom
     */
    @GetMapping("/by-name/{name}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getRoleByName(@PathVariable String name) {
        try {
            return roleService.getRoleByName(name)
                    .map(role -> ResponseEntity.ok(Map.of(
                        "success", true,
                        "data", role
                    )))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error fetching role by name {}: {}", name, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Récupère les rôles actifs
     */
    @GetMapping("/active")
    public ResponseEntity<Map<String, Object>> getActiveRoles() {
        try {
            List<RoleDTO> activeRoles = roleService.getActiveRoles();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", activeRoles,
                "total", activeRoles.size()
            ));
        } catch (Exception e) {
            log.error("Error fetching active roles: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Met à jour un rôle
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateRole(
            @PathVariable Long id, 
            @Valid @RequestBody RoleDTO roleDTO) {
        try {
            RoleDTO updatedRole = roleService.updateRole(id, roleDTO);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Role updated successfully",
                "data", updatedRole
            ));
        } catch (Exception e) {
            log.error("Error updating role {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Active/désactive un rôle
     */
    @PatchMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> toggleRoleStatus(@PathVariable Long id) {
        try {
            RoleDTO updatedRole = roleService.toggleRoleStatus(id);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Role status updated successfully",
                "data", updatedRole
            ));
        } catch (Exception e) {
            log.error("Error toggling role status {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Supprime un rôle (soft delete)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteRole(@PathVariable Long id) {
        try {
            roleService.deleteRole(id);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Role deleted successfully"
            ));
        } catch (Exception e) {
            log.error("Error deleting role {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Supprime définitivement un rôle
     */
    @DeleteMapping("/{id}/hard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> hardDeleteRole(@PathVariable Long id) {
        try {
            roleService.hardDeleteRole(id);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Role permanently deleted"
            ));
        } catch (Exception e) {
            log.error("Error hard deleting role {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Recherche des rôles par nom
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> searchRoles(@RequestParam String name) {
        try {
            List<RoleDTO> roles = roleService.searchRolesByName(name);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", roles,
                "total", roles.size()
            ));
        } catch (Exception e) {
            log.error("Error searching roles: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Obtient les statistiques des rôles
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getRoleStatistics() {
        try {
            long totalRoles = roleService.countRoles();
            long activeRoles = roleService.countActiveRoles();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", Map.of(
                    "totalRoles", totalRoles,
                    "activeRoles", activeRoles,
                    "inactiveRoles", totalRoles - activeRoles
                )
            ));
        } catch (Exception e) {
            log.error("Error fetching role statistics: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
}