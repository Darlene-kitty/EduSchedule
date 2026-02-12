package cm.iusjc.roomservice.service;

import cm.iusjc.roomservice.dto.RoomDTO;
import cm.iusjc.roomservice.entity.Room;
import cm.iusjc.roomservice.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomService {
    
    private final RoomRepository roomRepository;
    
    /**
     * Crée une nouvelle salle
     */
    @Transactional
    @CacheEvict(value = "rooms", allEntries = true)
    public RoomDTO createRoom(RoomDTO roomDTO) {
        log.info("Creating new room: {}", roomDTO.getName());
        
        // Vérifier si la salle existe déjà
        if (roomRepository.existsByNameAndBuildingAndFloor(
                roomDTO.getName(), roomDTO.getBuilding(), roomDTO.getFloor())) {
            throw new RuntimeException("Room with name '" + roomDTO.getName() + 
                "' already exists in building " + roomDTO.getBuilding() + " floor " + roomDTO.getFloor());
        }
        
        Room room = new Room();
        room.setName(roomDTO.getName());
        room.setCode(roomDTO.getCode());
        room.setType(roomDTO.getType());
        room.setCapacity(roomDTO.getCapacity());
        room.setBuilding(roomDTO.getBuilding());
        room.setFloor(roomDTO.getFloor());
        room.setDescription(roomDTO.getDescription());
        room.setEquipments(roomDTO.getEquipments());
        room.setAccessible(roomDTO.isAccessible());
        room.setAvailable(roomDTO.isAvailable());
        room.setSchoolId(roomDTO.getSchoolId());
        room.setCreatedAt(LocalDateTime.now());
        room.setUpdatedAt(LocalDateTime.now());
        
        Room savedRoom = roomRepository.save(room);
        log.info("Room created successfully with ID: {}", savedRoom.getId());
        
        return convertToDTO(savedRoom);
    }
    
    /**
     * Récupère toutes les salles
     */
    @Cacheable(value = "rooms")
    public List<RoomDTO> getAllRooms() {
        log.debug("Fetching all rooms");
        return roomRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les salles avec pagination
     */
    public Page<RoomDTO> getAllRooms(Pageable pageable) {
        log.debug("Fetching rooms with pagination: {}", pageable);
        return roomRepository.findAll(pageable)
                .map(this::convertToDTO);
    }
    
    /**
     * Récupère une salle par ID
     */
    @Cacheable(value = "rooms", key = "#id")
    public Optional<RoomDTO> getRoomById(Long id) {
        log.debug("Fetching room by ID: {}", id);
        return roomRepository.findById(id)
                .map(this::convertToDTO);
    }
    
    /**
     * Récupère une salle par nom
     */
    @Cacheable(value = "rooms", key = "#name")
    public Optional<RoomDTO> getRoomByName(String name) {
        log.debug("Fetching room by name: {}", name);
        return roomRepository.findByName(name)
                .map(this::convertToDTO);
    }
    
    /**
     * Récupère une salle par code
     */
    @Cacheable(value = "rooms", key = "#code")
    public Optional<RoomDTO> getRoomByCode(String code) {
        log.debug("Fetching room by code: {}", code);
        return roomRepository.findByCode(code)
                .map(this::convertToDTO);
    }
    
    /**
     * Récupère les salles disponibles
     */
    @Cacheable(value = "availableRooms")
    public List<RoomDTO> getAvailableRooms() {
        log.debug("Fetching available rooms");
        return roomRepository.findByAvailableTrue().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les salles par école
     */
    public List<RoomDTO> getRoomsBySchool(Long schoolId) {
        log.debug("Fetching rooms by school ID: {}", schoolId);
        return roomRepository.findBySchoolId(schoolId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les salles par type
     */
    public List<RoomDTO> getRoomsByType(String type) {
        log.debug("Fetching rooms by type: {}", type);
        return roomRepository.findByType(type).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les salles par bâtiment
     */
    public List<RoomDTO> getRoomsByBuilding(String building) {
        log.debug("Fetching rooms by building: {}", building);
        return roomRepository.findByBuilding(building).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les salles par étage
     */
    public List<RoomDTO> getRoomsByFloor(Integer floor) {
        log.debug("Fetching rooms by floor: {}", floor);
        return roomRepository.findByFloor(floor).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les salles par capacité minimale
     */
    public List<RoomDTO> getRoomsByMinCapacity(Integer minCapacity) {
        log.debug("Fetching rooms with min capacity: {}", minCapacity);
        return roomRepository.findByCapacityGreaterThanEqual(minCapacity).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les salles accessibles
     */
    public List<RoomDTO> getAccessibleRooms() {
        log.debug("Fetching accessible rooms");
        return roomRepository.findByAccessibleTrue().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Met à jour une salle
     */
    @Transactional
    @CacheEvict(value = {"rooms", "availableRooms"}, allEntries = true)
    public RoomDTO updateRoom(Long id, RoomDTO roomDTO) {
        log.info("Updating room with ID: {}", id);
        
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found with ID: " + id));
        
        // Vérifier si le nouveau nom existe déjà (sauf pour cette salle)
        if (!room.getName().equals(roomDTO.getName()) && 
            roomRepository.existsByNameAndBuildingAndFloor(
                roomDTO.getName(), roomDTO.getBuilding(), roomDTO.getFloor())) {
            throw new RuntimeException("Room with name '" + roomDTO.getName() + 
                "' already exists in building " + roomDTO.getBuilding() + " floor " + roomDTO.getFloor());
        }
        
        room.setName(roomDTO.getName());
        room.setCode(roomDTO.getCode());
        room.setType(roomDTO.getType());
        room.setCapacity(roomDTO.getCapacity());
        room.setBuilding(roomDTO.getBuilding());
        room.setFloor(roomDTO.getFloor());
        room.setDescription(roomDTO.getDescription());
        room.setEquipments(roomDTO.getEquipments());
        room.setAccessible(roomDTO.isAccessible());
        room.setAvailable(roomDTO.isAvailable());
        room.setSchoolId(roomDTO.getSchoolId());
        room.setUpdatedAt(LocalDateTime.now());
        
        Room updatedRoom = roomRepository.save(room);
        log.info("Room updated successfully: {}", updatedRoom.getId());
        
        return convertToDTO(updatedRoom);
    }
    
    /**
     * Active/désactive une salle
     */
    @Transactional
    @CacheEvict(value = {"rooms", "availableRooms"}, allEntries = true)
    public RoomDTO toggleRoomAvailability(Long id) {
        log.info("Toggling availability for room ID: {}", id);
        
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found with ID: " + id));
        
        room.setAvailable(!room.isAvailable());
        room.setUpdatedAt(LocalDateTime.now());
        Room updatedRoom = roomRepository.save(room);
        
        log.info("Room availability toggled: {} - Available: {}", updatedRoom.getName(), updatedRoom.isAvailable());
        return convertToDTO(updatedRoom);
    }
    
    /**
     * Met à jour les équipements d'une salle
     */
    @Transactional
    @CacheEvict(value = {"rooms", "availableRooms"}, allEntries = true)
    public RoomDTO updateRoomEquipments(Long id, List<String> equipments) {
        log.info("Updating equipments for room ID: {}", id);
        
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found with ID: " + id));
        
        room.setEquipments(equipments);
        room.setUpdatedAt(LocalDateTime.now());
        Room updatedRoom = roomRepository.save(room);
        
        log.info("Room equipments updated: {}", updatedRoom.getName());
        return convertToDTO(updatedRoom);
    }
    
    /**
     * Supprime une salle (soft delete)
     */
    @Transactional
    @CacheEvict(value = {"rooms", "availableRooms"}, allEntries = true)
    public void deleteRoom(Long id) {
        log.info("Deleting room with ID: {}", id);
        
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found with ID: " + id));
        
        // Vérifier si la salle a des réservations actives
        if (hasActiveReservations(id)) {
            throw new RuntimeException("Cannot delete room: it has active reservations");
        }
        
        // Soft delete - rendre la salle indisponible
        room.setAvailable(false);
        room.setUpdatedAt(LocalDateTime.now());
        roomRepository.save(room);
        
        log.info("Room soft deleted: {}", room.getName());
    }
    
    /**
     * Supprime définitivement une salle
     */
    @Transactional
    @CacheEvict(value = {"rooms", "availableRooms"}, allEntries = true)
    public void hardDeleteRoom(Long id) {
        log.warn("Hard deleting room with ID: {}", id);
        
        if (hasActiveReservations(id)) {
            throw new RuntimeException("Cannot delete room: it has active reservations");
        }
        
        roomRepository.deleteById(id);
        log.warn("Room hard deleted with ID: {}", id);
    }
    
    /**
     * Recherche des salles par nom
     */
    public List<RoomDTO> searchRoomsByName(String name) {
        log.debug("Searching rooms by name containing: {}", name);
        return roomRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Recherche des salles par critères multiples
     */
    public List<RoomDTO> searchRooms(String searchTerm) {
        log.debug("Searching rooms with term: {}", searchTerm);
        return roomRepository.searchRooms(searchTerm).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Recherche des salles avec filtres avancés
     */
    public List<RoomDTO> searchRoomsWithFilters(String type, Integer minCapacity, 
            String building, Integer floor, List<String> equipments, Boolean accessible) {
        log.debug("Searching rooms with advanced filters");
        return roomRepository.findRoomsWithFilters(type, minCapacity, building, floor, equipments, accessible).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtient les statistiques des salles par type
     */
    public List<Object[]> getRoomStatisticsByType() {
        return roomRepository.getRoomCountByType();
    }
    
    /**
     * Obtient les statistiques des salles par bâtiment
     */
    public List<Object[]> getRoomStatisticsByBuilding() {
        return roomRepository.getRoomCountByBuilding();
    }
    
    /**
     * Obtient l'utilisation moyenne des salles
     */
    public Double getAverageRoomUtilization() {
        return roomRepository.getAverageRoomUtilization();
    }
    
    /**
     * Vérifie si une salle existe
     */
    public boolean existsById(Long id) {
        return roomRepository.existsById(id);
    }
    
    /**
     * Vérifie si une salle existe par nom
     */
    public boolean existsByName(String name) {
        return roomRepository.existsByName(name);
    }
    
    /**
     * Compte le nombre total de salles
     */
    public long countRooms() {
        return roomRepository.count();
    }
    
    /**
     * Compte le nombre de salles disponibles
     */
    public long countAvailableRooms() {
        return roomRepository.countByAvailableTrue();
    }
    
    /**
     * Compte les salles par école
     */
    public long countRoomsBySchool(Long schoolId) {
        return roomRepository.countBySchoolId(schoolId);
    }
    
    /**
     * Compte les salles par type
     */
    public long countRoomsByType(String type) {
        return roomRepository.countByType(type);
    }
    
    /**
     * Vérifie si la salle a des réservations actives
     */
    private boolean hasActiveReservations(Long roomId) {
        // Cette méthode devrait vérifier dans le service de réservation
        // Pour l'instant, on retourne false
        return false;
    }
    
    /**
     * Convertit une entité Room en DTO
     */
    private RoomDTO convertToDTO(Room room) {
        RoomDTO dto = new RoomDTO();
        dto.setId(room.getId());
        dto.setName(room.getName());
        dto.setCode(room.getCode());
        dto.setType(room.getType());
        dto.setCapacity(room.getCapacity());
        dto.setBuilding(room.getBuilding());
        dto.setFloor(room.getFloor());
        dto.setDescription(room.getDescription());
        dto.setEquipments(room.getEquipments());
        dto.setAccessible(room.isAccessible());
        dto.setAvailable(room.isAvailable());
        dto.setSchoolId(room.getSchoolId());
        dto.setCreatedAt(room.getCreatedAt());
        dto.setUpdatedAt(room.getUpdatedAt());
        return dto;
    }
}