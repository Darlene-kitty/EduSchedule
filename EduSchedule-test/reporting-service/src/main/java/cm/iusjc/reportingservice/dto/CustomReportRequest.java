package cm.iusjc.reportingservice.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomReportRequest {
    private String title;
    private String type; // OCCUPANCY, PERFORMANCE, EQUIPMENT, CONFLICTS, TRENDS
    private DateRange dateRange;
    private ReportFilters filters;
    private String groupBy; // DAY, WEEK, MONTH, ROOM, TEACHER
    private List<String> visualizations; // TABLE, CHART, GRAPH, HEATMAP
    private List<String> exportFormats; // PDF, EXCEL, CSV, JSON
    private Map<String, Object> customParameters;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DateRange {
        private LocalDateTime start;
        private LocalDateTime end;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportFilters {
        private List<String> schools;
        private List<String> rooms;
        private List<String> teachers;
        private List<String> subjects;
        private List<String> roomTypes;
        private String status;
        private Map<String, Object> customFilters;
    }
}