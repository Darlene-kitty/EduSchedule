package cm.iusjc.scheduling.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OptimalRoomSuggestion {
    private Long roomId;
    private String roomName;
    private String roomType;
    private Integer capacity;
    private String building;
    private String floor;
    private double optimizationScore; // Score de 0 à 1
    private double utilizationRate; // Taux d'utilisation de la capacité
    private int travelTimeMinutes; // Temps de déplacement depuis la salle précédente
    private List<String> reasons; // Raisons du score
    private List<String> availableEquipment;
    private String recommendation; // Recommandation textuelle
    
    public String getScorePercentage() {
        return String.format("%.1f%%", optimizationScore * 100);
    }
    
    public String getUtilizationPercentage() {
        return String.format("%.1f%%", utilizationRate * 100);
    }
    
    public String getOptimizationLevel() {
        if (optimizationScore >= 0.9) return "EXCELLENT";
        if (optimizationScore >= 0.8) return "TRÈS BON";
        if (optimizationScore >= 0.7) return "BON";
        if (optimizationScore >= 0.6) return "ACCEPTABLE";
        return "FAIBLE";
    }
}