package cm.iusjc.course.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Support de cours attaché à un cours (PDF, DOC, PPTX, etc.)
 */
@Entity
@Table(name = "course_documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ID du cours auquel ce document est rattaché */
    @Column(name = "course_id", nullable = false)
    private Long courseId;

    /** Nom original du fichier tel qu'uploadé */
    @Column(name = "original_filename", nullable = false, length = 255)
    private String originalFilename;

    /** Nom de fichier stocké sur le disque (UUID + extension) */
    @Column(name = "stored_filename", nullable = false, length = 255)
    private String storedFilename;

    /** Type MIME : application/pdf, application/msword, etc. */
    @Column(name = "content_type", length = 100)
    private String contentType;

    /** Taille en octets */
    @Column(name = "file_size")
    private Long fileSize;

    /** Catégorie : COURS, TD, TP, EXAMEN, AUTRE */
    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 20)
    private DocumentCategory category = DocumentCategory.COURS;

    /** Description optionnelle */
    @Column(length = 500)
    private String description;

    /** ID de l'utilisateur qui a uploadé le fichier */
    @Column(name = "uploaded_by")
    private Long uploadedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
