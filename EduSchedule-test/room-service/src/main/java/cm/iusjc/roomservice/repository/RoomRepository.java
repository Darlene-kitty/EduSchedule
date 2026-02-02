package cm.iusjc.roomservice.repository;

import cm.iusjc.roomservice.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByDisponibleTrue();
    List<Room> findByType(String type);
    List<Room> findByBatiment(String batiment);
    List<Room> findByCapaciteGreaterThanEqual(Integer capacite);
}