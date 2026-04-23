package cm.iusjc.reporting.dto;

import cm.iusjc.reporting.entity.ReportFormat;
import cm.iusjc.reporting.entity.ReportType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduledReportConfigDTO {
    private String name;
    private String cronExpression;
    private String description;
    private ReportType reportType;
    private ReportFormat reportFormat;
    private boolean enabled;
    private String nextExecution;
}
