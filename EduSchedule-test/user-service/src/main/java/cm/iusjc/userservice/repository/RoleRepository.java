package cm.iusjc.userservice.repository;

import cm.iusjc.userservice.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    /**
     * Trouve un rôle par nom
     */
    Optional<Role> findByName(String name);
    
    /**
     * Vérifie si un rôle existe par nom
     */
    boolean existsByName(String name);
    
    /**
     * Trouve tous les rôles actifs
     */
    List<Role> findByActiveTrue();
    
    /**
     * Trouve tous les rôles inactifs
     */
    List<Role> findByActiveFalse();
    
    /**
     * Compte les rôles actifs
     */
    long countByActiveTrue();
    
    /**
     * Compte les rôles inactifs
     */
    long countByActiveFalse();
    
    /**
     * Recherche des rôles par nom (contient, insensible à la casse)
     */
    List<Role> findByNameContainingIgnoreCase(String name);
    
    /**
     * Recherche des rôles par description (contient, insensible à la casse)
     */
    List<Role> findByDescriptionContainingIgnoreCase(String description);
    
    /**
     * Trouve les rôles par nom et statut actif
     */
    @Query("SELECT r FROM Role r WHERE r.name LIKE %:name% AND r.active = :active")
    List<Role> findByNameContainingAndActive(@Param("name") String name, @Param("active") boolean active);
    
    /**
     * Trouve les rôles les plus récents
     */
    @Query("SELECT r FROM Role r ORDER BY r.createdAt DESC")
    List<Role> findRecentRoles();
    
    /**
     * Compte les utilisateurs par rôle
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.role.id = :roleId")
    long countUsersByRoleId(@Param("roleId") Long roleId);
}