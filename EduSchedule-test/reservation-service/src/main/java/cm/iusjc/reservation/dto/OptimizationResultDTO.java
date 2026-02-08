package cm.iusjc.reservation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OptimizationResultDTO {
    
    private boolean success;
    
    private String message;
    
    private RoomRecommendation recommendedRoom;
    
    private List<RoomRecommendation> alternativeRooms;
    
    private Double optimizationScore;
    
    private String optimizationReason;
    
    private List<String> warnings;
    
    private List<String> suggestions;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RoomRecommendation {
        private Long resourceId;
        private String name;
        private String type;
        private Integer capacity;
        private String location;
        private List<String> equipments;
        private Double matchScore;
        private String matchReason;
        private Boolean available;
        private String availabilityNote;
    }
}