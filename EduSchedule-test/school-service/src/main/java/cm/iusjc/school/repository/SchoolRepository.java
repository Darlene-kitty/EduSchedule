package cm.iusjc.school.repository;

import cm.iusjc.school.entity.School;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SchoolRepository extends JpaRepository<School, Long> {
    
    /**
     * Trouve une école par nom
     */
    Optional<School> findByName(String name);
    
    /**
     * Trouve une école par code
     */
    Optional<School> findByCode(String code);
    
    /**
     * Vérifie si une école existe par nom
     */
    boolean existsByName(String name);
    
    /**
     * Vérifie si une école existe par code
     */
    boolean existsByCode(String code);
    
    /**
     * Trouve toutes les écoles actives
     */
    List<School> findByActiveTrue();
    
    /**
     * Trouve toutes les écoles inactives
     */
    List<School> findByActiveFalse();
    
    /**
     * Compte les écoles actives
     */
    long countByActiveTrue();
    
    /**
     * Compte les écoles inactives
     */
    long countByActiveFalse();
    
    /**
     * Trouve les écoles par ville
     */
    List<School> findByCity(String city);
    
    /**
     * Trouve les écoles par pays
     */
    List<School> findByCountry(String country);
    
    /**
     * Recherche des écoles par nom (contient, insensible à la casse)
     */
    List<School> findByNameContainingIgnoreCase(String name);
    
    /**
     * Recherche des écoles par ville (contient, insensible à la casse)
     */
    List<School> findByCityContainingIgnoreCase(String city);
    
    /**
     * Recherche des écoles par pays (contient, insensible à la casse)
     */
    List<School> findByCountryContainingIgnoreCase(String country);
    
    /**
     * Recherche globale dans les écoles
     */
    @Query("SELECT s FROM School s WHERE " +
           "LOWER(s.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(s.code) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(s.city) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(s.country) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<School> searchSchools(@Param("searchTerm") String searchTerm);
    
    /**
     * Trouve les écoles par nom et statut actif
     */
    @Query("SELECT s FROM School s WHERE s.name LIKE %:name% AND s.active = :active")
    List<School> findByNameContainingAndActive(@Param("name") String name, @Param("active") boolean active);
    
    /**
     * Trouve les écoles les plus récentes
     */
    @Query("SELECT s FROM School s ORDER BY s.createdAt DESC")
    List<School> findRecentSchools();
    
    /**
     * Compte les écoles par pays
     */
    @Query("SELECT s.country, COUNT(s) FROM School s WHERE s.active = true GROUP BY s.country ORDER BY COUNT(s) DESC")
    List<Object[]> getSchoolCountByCountry();
    
    /**
     * Compte les écoles par ville
     */
    @Query("SELECT s.city, COUNT(s) FROM School s WHERE s.active = true GROUP BY s.city ORDER BY COUNT(s) DESC")
    List<Object[]> getSchoolCountByCity();
    
    /**
     * Trouve les écoles actives (triées par nom)
     */
    @Query("SELECT s FROM School s WHERE s.active = true ORDER BY s.name ASC")
    List<School> findSchoolsOrderByUserCount();
}