package cm.iusjc.reporting.dto;

import cm.iusjc.reporting.entity.ReportFormat;
import cm.iusjc.reporting.entity.ReportType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportRequest {
    
    @NotNull(message = "Report type is required")
    private ReportType type;
    
    @NotNull(message = "Report format is required")
    private ReportFormat format;
    
    private String title;
    private String description;
    
    // Paramètres de filtrage
    private LocalDate startDate;
    private LocalDate endDate;
    private Long userId;
    private Long courseId;
    private Long resourceId;
    private String department;
    private String level;
    
    // Paramètres additionnels personnalisés
    private Map<String, Object> customParameters;
}