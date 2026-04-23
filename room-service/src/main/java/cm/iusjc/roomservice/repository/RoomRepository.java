package cm.iusjc.roomservice.repository;

import cm.iusjc.roomservice.entity.Room;
import cm.iusjc.roomservice.entity.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    Optional<Room> findByName(String name);
    Optional<Room> findByCode(String code);
    boolean existsByName(String name);
    boolean existsByCode(String code);
    boolean existsByNameAndBuildingAndFloor(String name, String building, Integer floor);

    List<Room> findByAvailableTrue();
    List<Room> findByAvailableFalse();
    long countByAvailableTrue();
    long countByAvailableFalse();

    List<Room> findBySchoolId(Long schoolId);

    List<Room> findByType(RoomType type);
    long countByType(RoomType type);

    List<Room> findByBuilding(String building);
    List<Room> findByFloor(Integer floor);
    List<Room> findByBuildingAndFloor(String building, Integer floor);
    List<Room> findByCapacityGreaterThanEqual(Integer capacity);
    List<Room> findByCapacityBetween(Integer minCapacity, Integer maxCapacity);
    List<Room> findByAccessibleTrue();
    long countBySchoolId(Long schoolId);
    long countByBuilding(String building);
    List<Room> findByNameContainingIgnoreCase(String name);
    List<Room> findByCodeContainingIgnoreCase(String code);

    @Query("SELECT r FROM Room r WHERE " +
           "LOWER(r.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(r.code) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(CAST(r.type AS string)) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(r.building) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(r.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Room> searchRooms(@Param("searchTerm") String searchTerm);

    @Query("SELECT r FROM Room r WHERE r.schoolId = :schoolId AND r.available = :available")
    List<Room> findBySchoolIdAndAvailable(@Param("schoolId") Long schoolId, @Param("available") boolean available);

    @Query("SELECT r FROM Room r WHERE r.type = :type AND r.available = :available")
    List<Room> findByTypeAndAvailable(@Param("type") RoomType type, @Param("available") boolean available);

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

    @Query("SELECT r FROM Room r ORDER BY r.createdAt DESC")
    List<Room> findRecentRooms();

    @Query("SELECT r.type, COUNT(r) FROM Room r WHERE r.available = true GROUP BY r.type ORDER BY COUNT(r) DESC")
    List<Object[]> getRoomCountByType();

    @Query("SELECT r.building, COUNT(r) FROM Room r WHERE r.available = true GROUP BY r.building ORDER BY COUNT(r) DESC")
    List<Object[]> getRoomCountByBuilding();

    @Query("SELECT r FROM Room r WHERE r.available = true ORDER BY r.capacity DESC")
    List<Room> findRoomsOrderByCapacity();

    @Query("SELECT r FROM Room r WHERE r.available = true ORDER BY r.capacity DESC")
    List<Room> findMostUsedRooms();

    @Query("SELECT r FROM Room r WHERE r.available = true ORDER BY r.capacity ASC")
    List<Room> findLeastUsedRooms();

    @Query("SELECT AVG(r.capacity) FROM Room r WHERE r.available = true")
    Double getAverageRoomCapacity();

    @Query("SELECT 0.0 FROM Room r WHERE r.available = true")
    Double getAverageRoomUtilization();

    @Query("SELECT r FROM Room r WHERE r.available = true AND " +
           "EXISTS (SELECT 1 FROM r.equipments e WHERE e IN :equipments)")
    List<Room> findRoomsWithEquipments(@Param("equipments") List<String> equipments);

    @Query("SELECT r FROM Room r WHERE r.available = true AND " +
           "(r.equipments IS NULL OR SIZE(r.equipments) = 0)")
    List<Room> findRoomsWithoutEquipments();

    @Query("SELECT r FROM Room r WHERE r.type = :type AND " +
           "r.capacity BETWEEN :minCapacity AND :maxCapacity AND r.available = true")
    List<Room> findRoomsByTypeAndCapacityRange(@Param("type") RoomType type,
                                              @Param("minCapacity") Integer minCapacity,
                                              @Param("maxCapacity") Integer maxCapacity);
}
