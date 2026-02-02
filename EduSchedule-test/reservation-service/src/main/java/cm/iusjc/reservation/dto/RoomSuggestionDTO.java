package cm.iusjc.reservation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomSuggestionDTO {
    private Long id;
    private String code;
    private String name;
    private String type;
    private Integer capacity;
    private Long schoolId;
    private Long buildingId;
    private String floor;
    private boolean hasProjector;
    private boolean hasComputer;
    private boolean hasWhiteboard;
    private boolean hasAirConditioning;
    private boolean accessible;
    private String equipment;
    private double optimizationScore;
    private String optimizationReason;
    private double utilizationRate;
}