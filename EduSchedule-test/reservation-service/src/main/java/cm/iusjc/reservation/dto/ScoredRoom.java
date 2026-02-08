package cm.iusjc.reservation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScoredRoom {
    private RoomCandidate room;
    private Double totalScore;
    private Map<String, Double> detailedScores;
    private String reasoningExplanation;
    private List<String> strengths;
    private List<String> weaknesses;
    private String recommendation;
    
    public ScoredRoom(RoomCandidate room) {
        this.room = room;
        this.totalScore = 0.0;
    }
}
