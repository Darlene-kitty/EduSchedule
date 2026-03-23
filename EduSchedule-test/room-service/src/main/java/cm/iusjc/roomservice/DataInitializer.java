package cm.iusjc.roomservice;

import cm.iusjc.roomservice.entity.Room;
import cm.iusjc.roomservice.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final RoomRepository roomRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (roomRepository.count() > 0) {
            log.info("[DataInitializer] Rooms already seeded — skipping.");
            return;
        }

        log.info("[DataInitializer] Seeding rooms...");

        // schoolId=1 → SJI, schoolId=2 → SJM, schoolId=3 → PRÉPAVOGT, schoolId=4 → CPGE
        room("Amphi A",       "AMPHI-A",    "AMPHITHEATER", 200, "Bâtiment A", 0, 1L);
        room("Amphi B",       "AMPHI-B",    "AMPHITHEATER", 150, "Bâtiment B", 0, 2L);
        room("Salle 101",     "S-101",      "CLASSROOM",     40, "Bâtiment A", 1, 1L);
        room("Salle 102",     "S-102",      "CLASSROOM",     40, "Bâtiment A", 1, 1L);
        room("Salle 201",     "S-201",      "CLASSROOM",     40, "Bâtiment B", 2, 2L);
        room("Salle 202",     "S-202",      "CLASSROOM",     40, "Bâtiment B", 2, 2L);
        room("Salle Info 1",  "INFO-1",     "COMPUTER_LAB",  30, "Bâtiment A", 1, 1L);
        room("Salle Info 2",  "INFO-2",     "COMPUTER_LAB",  30, "Bâtiment A", 1, 1L);
        room("TP Électrique", "TP-ELEC",    "LABORATORY",    25, "Bâtiment C", 1, 1L);
        room("Labo Chimie",   "LABO-CHIM",  "LABORATORY",    25, "Bâtiment C", 0, 1L);
        room("Labo Physique", "LABO-PHYS",  "LABORATORY",    25, "Bâtiment C", 0, 1L);
        room("Salle CPGE 1",  "CPGE-1",     "CLASSROOM",     35, "Bâtiment D", 1, 4L);
        room("Salle CPGE 2",  "CPGE-2",     "CLASSROOM",     35, "Bâtiment D", 1, 4L);
        room("Salle Conf.",   "CONF",       "CONFERENCE_ROOM",20,"Bâtiment A", 0, 1L);

        log.info("[DataInitializer] Done — 14 rooms seeded.");
    }

    private void room(String name, String code, String type, int capacity,
                      String building, int floor, Long schoolId) {
        Room r = new Room();
        r.setName(name);
        r.setCode(code);
        r.setType(type);
        r.setCapacity(capacity);
        r.setBuilding(building);
        r.setFloor(floor);
        r.setSchoolId(schoolId);
        r.setAvailable(true);
        r.setAccessible(false);
        r.setEquipments(List.of("Tableau blanc", "Projecteur"));
        roomRepository.save(r);
    }
}
