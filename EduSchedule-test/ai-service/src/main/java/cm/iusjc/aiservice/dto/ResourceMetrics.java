package cm.iusjc.aiservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceMetrics {
    private double utilizationRate;
    private double capacityOptimization;
    private double maintenanceEfficiency;
    private double redundancyLevel;
    private int totalResources;
    private int availableResources;
}
