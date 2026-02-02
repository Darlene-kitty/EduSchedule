package cm.iusjc.roomservice.service;

import cm.iusjc.roomservice.entity.Room;
import cm.iusjc.roomservice.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RoomService {

    @Autowired
    private RoomRepository roomRepository;

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public List<Room> getAvailableRooms() {
        return roomRepository.findByDisponibleTrue();
    }

    public Room getRoomById(Long id) {
        Optional<Room> room = roomRepository.findById(id);
        return room.orElse(null);
    }

    public Room createRoom(Room room) {
        return roomRepository.save(room);
    }

    public Room updateRoom(Long id, Room roomDetails) {
        Optional<Room> optionalRoom = roomRepository.findById(id);
        if (optionalRoom.isPresent()) {
            Room room = optionalRoom.get();
            room.setNom(roomDetails.getNom());
            room.setCode(roomDetails.getCode());
            room.setType(roomDetails.getType());
            room.setCapacite(roomDetails.getCapacite());
            room.setBatiment(roomDetails.getBatiment());
            room.setEtage(roomDetails.getEtage());
            room.setDisponible(roomDetails.getDisponible());
            return roomRepository.save(room);
        }
        return null;
    }

    public boolean deleteRoom(Long id) {
        if (roomRepository.existsById(id)) {
            roomRepository.deleteById(id);
            return true;
        }
        return false;
    }
}