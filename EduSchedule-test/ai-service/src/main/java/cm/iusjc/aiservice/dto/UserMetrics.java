package cm.iusjc.aiservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserMetrics {
    private int activeUsers;
    private double satisfactionScore;
    private double engagementRate;
    private int newUsers;
    private double retentionRate;
}
