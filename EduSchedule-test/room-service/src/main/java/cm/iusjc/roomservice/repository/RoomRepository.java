package cm.iusjc.roomservice.repository;

import cm.iusjc.roomservice.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    
    /**
     * Trouve une salle par nom
     */
    Optional<Room> findByName(String name);
    
    /**
     * Trouve une salle par code
     */
    Optional<Room> findByCode(String code);
    
    /**
     * Vérifie si une salle existe par nom
     */
    boolean existsByName(String name);
    
    /**
     * Vérifie si une salle existe par code
     */
    boolean existsByCode(String code);
    
    /**
     * Vérifie si une salle existe par nom, bâtiment et étage
     */
    boolean existsByNameAndBuildingAndFloor(String name, String building, Integer floor);
    
    /**
     * Trouve toutes les salles disponibles
     */
    List<Room> findByAvailableTrue();
    
    /**
     * Trouve toutes les salles indisponibles
     */
    List<Room> findByAvailableFalse();
    
    /**
     * Compte les salles disponibles
     */
    long countByAvailableTrue();
    
    /**
     * Compte les salles indisponibles
     */
    long countByAvailableFalse();
    
    /**
     * Trouve les salles par école
     */
    List<Room> findBySchoolId(Long schoolId);
    
    /**
     * Trouve les salles par type
     */
    List<Room> findByType(String type);
    
    /**
     * Trouve les salles par bâtiment
     */
    List<Room> findByBuilding(String building);
    
    /**
     * Trouve les salles par étage
     */
    List<Room> findByFloor(Integer floor);
    
    /**
     * Trouve les salles par bâtiment et étage
     */
    List<Room> findByBuildingAndFloor(String building, Integer floor);
    
    /**
     * Trouve les salles avec capacité minimale
     */
    List<Room> findByCapacityGreaterThanEqual(Integer capacity);
    
    /**
     * Trouve les salles avec capacité dans une plage
     */
    List<Room> findByCapacityBetween(Integer minCapacity, Integer maxCapacity);
    
    /**
     * Trouve les salles accessibles
     */
    List<Room> findByAccessibleTrue();
    
    /**
     * Compte les salles par école
     */
    long countBySchoolId(Long schoolId);
    
    /**
     * Compte les salles par type
     */
    long countByType(String type);
    
    /**
     * Compte les salles par bâtiment
     */
    long countByBuilding(String building);
    
    /**
     * Recherche des salles par nom (contient, insensible à la casse)
     */
    List<Room> findByNameContainingIgnoreCase(String name);
    
    /**
     * Recherche des salles par code (contient, insensible à la casse)
     */
    List<Room> findByCodeContainingIgnoreCase(String code);
    
    /**
     * Recherche globale dans les salles
     */
    @Query("SELECT r FROM Room r WHERE " +
           "LOWER(r.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(r.code) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(r.type) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(r.building) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(r.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Room> searchRooms(@Param("searchTerm") String searchTerm);
    
    /**
     * Trouve les salles par école et disponibilité
     */
    @Query("SELECT r FROM Room r WHERE r.schoolId = :schoolId AND r.available = :available")
    List<Room> findBySchoolIdAndAvailable(@Param("schoolId") Long schoolId, @Param("available") boolean available);
    
    /**
     * Trouve les salles par type et disponibilité
     */
    @Query("SELECT r FROM Room r WHERE r.type = :type AND r.available = :available")
    List<Room> findByTypeAndAvailable(@Param("type") String type, @Param("available") boolean available);
    
    /**
     * Recherche avancée avec filtres multiples
     */
    /**
     * Recherche avancée avec filtres multiples
     */
    @Query(value = "SELECT * FROM rooms r WHERE " +
           "(:type IS NULL OR r.type = :type) AND " +
           "(:minCapacity IS NULL OR r.capacity >= :minCapacity) AND " +
           "(:building IS NULL OR r.building = :building) AND " +
           "(:floor IS NULL OR r.floor = :floor) AND " +
           "(:accessible IS NULL OR r.is_accessible = :accessible) AND " +
           "r.available = true",
           nativeQuery = true)
    List<Room> findRoomsWithFilters(@Param("type") String type,
                                   @Param("minCapacity") Integer minCapacity,
                                   @Param("building") String building,
                                   @Param("floor") Integer floor,
                                   @Param("accessible") Boolean accessible);
    
    /**
     * Trouve les salles les plus récentes
     */
    @Query("SELECT r FROM Room r ORDER BY r.createdAt DESC")
    List<Room> findRecentRooms();
    
    /**
     * Compte les salles par type
     */
    @Query("SELECT r.type, COUNT(r) FROM Room r WHERE r.available = true GROUP BY r.type ORDER BY COUNT(r) DESC")
    List<Object[]> getRoomCountByType();
    
    /**
     * Compte les salles par bâtiment
     */
    @Query("SELECT r.building, COUNT(r) FROM Room r WHERE r.available = true GROUP BY r.building ORDER BY COUNT(r) DESC")
    List<Object[]> getRoomCountByBuilding();
    
    /**
     * Trouve les salles par capacité (ordre décroissant)
     */
    @Query("SELECT r FROM Room r WHERE r.available = true ORDER BY r.capacity DESC")
    List<Room> findRoomsOrderByCapacity();
    
    /**
     * Trouve les salles les plus utilisées (ordre par capacité décroissante, fallback sans cross-service join)
     */
    @Query("SELECT r FROM Room r WHERE r.available = true ORDER BY r.capacity DESC")
    List<Room> findMostUsedRooms();
    
    /**
     * Trouve les salles les moins utilisées (ordre par capacité croissante, fallback sans cross-service join)
     */
    @Query("SELECT r FROM Room r WHERE r.available = true ORDER BY r.capacity ASC")
    List<Room> findLeastUsedRooms();
    
    /**
     * Calcule la capacité moyenne des salles
     */
    @Query("SELECT AVG(r.capacity) FROM Room r WHERE r.available = true")
    Double getAverageRoomCapacity();
    
    /**
     * Taux d'utilisation moyen non disponible localement (retourne 0.0)
     */
    @Query("SELECT 0.0 FROM Room r WHERE r.available = true")
    Double getAverageRoomUtilization();
    
    /**
     * Trouve les salles avec équipements spécifiques
     */
    @Query("SELECT r FROM Room r WHERE r.available = true AND " +
           "EXISTS (SELECT 1 FROM r.equipments e WHERE e IN :equipments)")
    List<Room> findRoomsWithEquipments(@Param("equipments") List<String> equipments);
    
    /**
     * Trouve les salles sans équipements
     */
    @Query("SELECT r FROM Room r WHERE r.available = true AND " +
           "(r.equipments IS NULL OR SIZE(r.equipments) = 0)")
    List<Room> findRoomsWithoutEquipments();
    
    /**
     * Trouve les salles par plage de capacité et type
     */
    @Query("SELECT r FROM Room r WHERE r.type = :type AND " +
           "r.capacity BETWEEN :minCapacity AND :maxCapacity AND r.available = true")
    List<Room> findRoomsByTypeAndCapacityRange(@Param("type") String type, 
                                              @Param("minCapacity") Integer minCapacity, 
                                              @Param("maxCapacity") Integer maxCapacity);
}