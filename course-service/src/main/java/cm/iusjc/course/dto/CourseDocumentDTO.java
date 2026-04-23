package cm.iusjc.course.dto;

import cm.iusjc.course.entity.DocumentCategory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseDocumentDTO {
    private Long id;
    private Long courseId;
    private String originalFilename;
    private String contentType;
    private Long fileSize;
    private DocumentCategory category;
    private String description;
    private Long uploadedBy;
    private LocalDateTime createdAt;
    /** URL de téléchargement exposée au frontend */
    private String downloadUrl;
}
