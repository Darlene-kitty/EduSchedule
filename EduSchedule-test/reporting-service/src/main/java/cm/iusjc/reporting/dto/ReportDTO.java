package cm.iusjc.reporting.dto;

import cm.iusjc.reporting.entity.ReportFormat;
import cm.iusjc.reporting.entity.ReportStatus;
import cm.iusjc.reporting.entity.ReportType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportDTO {
    
    private Long id;
    private String title;
    private String description;
    private ReportType type;
    private ReportFormat format;
    private ReportStatus status;
    private String filePath;
    private Long fileSize;
    private Long generatedBy;
    private LocalDateTime generatedAt;
    private String parameters;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // URL de téléchargement
    private String downloadUrl;
}