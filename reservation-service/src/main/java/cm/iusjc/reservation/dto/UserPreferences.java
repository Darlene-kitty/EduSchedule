package cm.iusjc.reservation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPreferences {
    private String preferredBuilding;
    private Integer preferredFloor;
    private List<String> preferredEquipments;
    private List<Long> favoriteRooms;
}
