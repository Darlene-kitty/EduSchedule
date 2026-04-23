package cm.iusjc.reservation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomSuggestionDTO {
    private Long id;
    private Long resourceId; // Alias pour id
    private String code;
    private String name;
    private String roomName; // Alias pour name
    private String type;
    private Integer capacity;
    private Long schoolId;
    private Long buildingId;
    private String floor;
    private String location;
    private boolean hasProjector;
    private boolean hasComputer;
    private boolean hasWhiteboard;
    private boolean hasAirConditioning;
    private boolean accessible;
    private String equipment;
    private double optimizationScore;
    private String optimizationReason;
    private String recommendationReason; // Alias pour optimizationReason
    private double utilizationRate;
    private String availabilityStatus;
}