package cm.iusjc.userservice.service;

import cm.iusjc.userservice.dto.RoleDTO;
import cm.iusjc.userservice.entity.Role;
import cm.iusjc.userservice.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleService {
    
    private final RoleRepository roleRepository;
    
    /**
     * Crée un nouveau rôle
     */
    @Transactional
    @CacheEvict(value = "roles", allEntries = true)
    public RoleDTO createRole(RoleDTO roleDTO) {
        log.info("Creating new role: {}", roleDTO.getName());
        
        // Vérifier si le rôle existe déjà
        if (roleRepository.existsByName(roleDTO.getName())) {
            throw new RuntimeException("Role with name '" + roleDTO.getName() + "' already exists");
        }
        
        Role role = new Role();
        role.setName(roleDTO.getName());
        role.setDescription(roleDTO.getDescription());
        role.setActive(roleDTO.isActive());
        
        Role savedRole = roleRepository.save(role);
        log.info("Role created successfully with ID: {}", savedRole.getId());
        
        return convertToDTO(savedRole);
    }
    
    /**
     * Récupère tous les rôles
     */
    @Cacheable(value = "roles")
    public List<RoleDTO> getAllRoles() {
        log.debug("Fetching all roles");
        return roleRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les rôles avec pagination
     */
    public Page<RoleDTO> getAllRoles(Pageable pageable) {
        log.debug("Fetching roles with pagination: {}", pageable);
        return roleRepository.findAll(pageable)
                .map(this::convertToDTO);
    }
    
    /**
     * Récupère un rôle par ID
     */
    @Cacheable(value = "roles", key = "#id")
    public Optional<RoleDTO> getRoleById(Long id) {
        log.debug("Fetching role by ID: {}", id);
        return roleRepository.findById(id)
                .map(this::convertToDTO);
    }
    
    /**
     * Récupère un rôle par nom
     */
    @Cacheable(value = "roles", key = "#name")
    public Optional<RoleDTO> getRoleByName(String name) {
        log.debug("Fetching role by name: {}", name);
        return roleRepository.findByName(name)
                .map(this::convertToDTO);
    }
    
    /**
     * Récupère les rôles actifs
     */
    @Cacheable(value = "activeRoles")
    public List<RoleDTO> getActiveRoles() {
        log.debug("Fetching active roles");
        return roleRepository.findByActiveTrue().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Met à jour un rôle
     */
    @Transactional
    @CacheEvict(value = {"roles", "activeRoles"}, allEntries = true)
    public RoleDTO updateRole(Long id, RoleDTO roleDTO) {
        log.info("Updating role with ID: {}", id);
        
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found with ID: " + id));
        
        // Vérifier si le nouveau nom existe déjà (sauf pour ce rôle)
        if (!role.getName().equals(roleDTO.getName()) && 
            roleRepository.existsByName(roleDTO.getName())) {
            throw new RuntimeException("Role with name '" + roleDTO.getName() + "' already exists");
        }
        
        role.setName(roleDTO.getName());
        role.setDescription(roleDTO.getDescription());
        role.setActive(roleDTO.isActive());
        
        Role updatedRole = roleRepository.save(role);
        log.info("Role updated successfully: {}", updatedRole.getId());
        
        return convertToDTO(updatedRole);
    }
    
    /**
     * Active/désactive un rôle
     */
    @Transactional
    @CacheEvict(value = {"roles", "activeRoles"}, allEntries = true)
    public RoleDTO toggleRoleStatus(Long id) {
        log.info("Toggling status for role ID: {}", id);
        
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found with ID: " + id));
        
        role.setActive(!role.isActive());
        Role updatedRole = roleRepository.save(role);
        
        log.info("Role status toggled: {} - Active: {}", updatedRole.getName(), updatedRole.isActive());
        return convertToDTO(updatedRole);
    }
    
    /**
     * Supprime un rôle (soft delete)
     */
    @Transactional
    @CacheEvict(value = {"roles", "activeRoles"}, allEntries = true)
    public void deleteRole(Long id) {
        log.info("Deleting role with ID: {}", id);
        
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found with ID: " + id));
        
        // Vérifier si le rôle est utilisé par des utilisateurs
        if (hasAssociatedUsers(id)) {
            throw new RuntimeException("Cannot delete role: it is assigned to users");
        }
        
        // Soft delete - désactiver le rôle
        role.setActive(false);
        roleRepository.save(role);
        
        log.info("Role soft deleted: {}", role.getName());
    }
    
    /**
     * Supprime définitivement un rôle
     */
    @Transactional
    @CacheEvict(value = {"roles", "activeRoles"}, allEntries = true)
    public void hardDeleteRole(Long id) {
        log.warn("Hard deleting role with ID: {}", id);
        
        if (hasAssociatedUsers(id)) {
            throw new RuntimeException("Cannot delete role: it is assigned to users");
        }
        
        roleRepository.deleteById(id);
        log.warn("Role hard deleted with ID: {}", id);
    }
    
    /**
     * Vérifie si un rôle existe
     */
    public boolean existsById(Long id) {
        return roleRepository.existsById(id);
    }
    
    /**
     * Vérifie si un rôle existe par nom
     */
    public boolean existsByName(String name) {
        return roleRepository.existsByName(name);
    }
    
    /**
     * Compte le nombre total de rôles
     */
    public long countRoles() {
        return roleRepository.count();
    }
    
    /**
     * Compte le nombre de rôles actifs
     */
    public long countActiveRoles() {
        return roleRepository.countByActiveTrue();
    }
    
    /**
     * Recherche des rôles par nom (contient)
     */
    public List<RoleDTO> searchRolesByName(String name) {
        log.debug("Searching roles by name containing: {}", name);
        return roleRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Vérifie si le rôle a des utilisateurs associés
     */
    private boolean hasAssociatedUsers(Long roleId) {
        // Cette méthode devrait vérifier dans la table des utilisateurs
        // Pour l'instant, on retourne false
        return false;
    }
    
    /**
     * Convertit une entité Role en DTO
     */
    private RoleDTO convertToDTO(Role role) {
        RoleDTO dto = new RoleDTO();
        dto.setId(role.getId());
        dto.setName(role.getName());
        dto.setDescription(role.getDescription());
        dto.setActive(role.isActive());
        dto.setCreatedAt(role.getCreatedAt());
        dto.setUpdatedAt(role.getUpdatedAt());
        return dto;
    }
}