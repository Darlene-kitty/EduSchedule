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
public class AssignmentContext {
    private TimePattern timePattern;
    private UsageHistory usageHistory;
    private UserPreferences userPreferences;
    private Double systemLoad;
    private List<String> specialEvents;
}
