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
public class RoomCandidate {
    private Long id;
    private String name;
    private String code;
    private String type;
    private Integer capacity;
    private String building;
    private String buildingId;
    private Integer floor;
    private Boolean accessible;
    private List<String> availableEquipments;
    private String location;
    private Boolean hasNaturalLight;
    private Boolean isQuiet;
    private Boolean isAvailable;
    private Double utilizationRate;
    private Double satisfactionScore;
}
