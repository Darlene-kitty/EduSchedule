package cm.iusjc.reservation.repository;

import cm.iusjc.reservation.entity.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {
    
    Optional<Resource> findByCode(String code);
    
    List<Resource> findByType(String type);
    
    List<Resource> findByBuilding(String building);
    
    List<Resource> findByIsAvailable(Boolean isAvailable);
    
    @Query("SELECT r FROM Resource r WHERE r.type = :type AND r.capacity >= :minCapacity AND r.isAvailable = true")
    List<Resource> findAvailableByTypeAndMinCapacity(@Param("type") String type, @Param("minCapacity") Integer minCapacity);
    
    @Query("SELECT r FROM Resource r WHERE r.building = :building AND r.floor = :floor")
    List<Resource> findByBuildingAndFloor(@Param("building") String building, @Param("floor") Integer floor);
    
    @Query("SELECT r FROM Resource r WHERE r.type = :type AND r.isAvailable = :active")
    List<Resource> findByTypeAndActive(@Param("type") String type, @Param("active") boolean active);
}
