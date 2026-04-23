package cm.iusjc.scheduling.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomCriteria {
    private Integer minCapacity;
    private Integer maxCapacity;
    private List<String> roomTypes;
    private List<String> requiredEquipment;
    private List<String> preferredBuildings;
    private Boolean accessibilityRequired;
    private Boolean airConditioningRequired;
    private Boolean projectorRequired;
    private Boolean computerRequired;
    private String floorPreference; // "GROUND", "UPPER", "ANY"
    
    public static RoomCriteria forCourseType(String courseType, int attendees) {
        RoomCriteria criteria = new RoomCriteria();
        criteria.setMinCapacity(attendees);
        criteria.setMaxCapacity((int) (attendees * 1.5));
        
        switch (courseType.toUpperCase()) {
            case "COURS":
                criteria.setRoomTypes(List.of("AMPHITHEATRE", "LECTURE_HALL", "CLASSROOM"));
                criteria.setProjectorRequired(true);
                break;
            case "TD":
                criteria.setRoomTypes(List.of("CLASSROOM", "SEMINAR_ROOM"));
                criteria.setProjectorRequired(false);
                break;
            case "TP":
                criteria.setRoomTypes(List.of("LABORATORY", "COMPUTER_LAB", "WORKSHOP"));
                criteria.setComputerRequired(true);
                break;
            case "EXAMEN":
                criteria.setRoomTypes(List.of("EXAM_ROOM", "CLASSROOM", "AMPHITHEATRE"));
                criteria.setMaxCapacity(attendees * 2); // Plus d'espace pour les examens
                break;
            default:
                criteria.setRoomTypes(List.of("CLASSROOM"));
        }
        
        return criteria;
    }
}