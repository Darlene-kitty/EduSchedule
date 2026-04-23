package cm.iusjc.course.service;

import cm.iusjc.course.dto.CourseDocumentDTO;
import cm.iusjc.course.entity.CourseDocument;
import cm.iusjc.course.entity.DocumentCategory;
import cm.iusjc.course.repository.CourseDocumentRepository;
import cm.iusjc.course.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseDocumentService {

    private final CourseDocumentRepository documentRepository;
    private final CourseRepository courseRepository;

    @Value("${app.documents.upload-dir:./uploads/course-documents}")
    private String uploadDir;

    @Value("${app.documents.base-url:http://localhost:8084/api/v1/courses}")
    private String baseUrl;

    /** Formats autorisés */
    private static final List<String> ALLOWED_TYPES = List.of(
        "application/pdf",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/vnd.ms-powerpoint",
        "application/vnd.openxmlformats-officedocument.presentationml.presentation",
        "application/vnd.ms-excel",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "text/plain",
        "image/jpeg",
        "image/png"
    );

    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50 Mo

    /**
     * Upload un fichier et l'associe à un cours.
     */
    @Transactional
    public CourseDocumentDTO uploadDocument(Long courseId, MultipartFile file,
                                            DocumentCategory category, String description,
                                            Long uploadedBy) throws IOException {
        // Vérifier que le cours existe
        if (!courseRepository.existsById(courseId)) {
            throw new RuntimeException("Course not found with ID: " + courseId);
        }

        // Valider le fichier
        validateFile(file);

        // Créer le répertoire si nécessaire
        Path uploadPath = Paths.get(uploadDir, String.valueOf(courseId));
        Files.createDirectories(uploadPath);

        // Générer un nom de fichier unique
        String ext = getExtension(file.getOriginalFilename());
        String storedFilename = UUID.randomUUID().toString() + ext;
        Path filePath = uploadPath.resolve(storedFilename);

        // Sauvegarder le fichier
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        log.info("File saved: {}", filePath);

        // Persister les métadonnées
        CourseDocument doc = new CourseDocument();
        doc.setCourseId(courseId);
        doc.setOriginalFilename(file.getOriginalFilename());
        doc.setStoredFilename(storedFilename);
        doc.setContentType(file.getContentType());
        doc.setFileSize(file.getSize());
        doc.setCategory(category != null ? category : DocumentCategory.COURS);
        doc.setDescription(description);
        doc.setUploadedBy(uploadedBy);

        CourseDocument saved = documentRepository.save(doc);
        return toDTO(saved);
    }

    /**
     * Liste les documents d'un cours.
     */
    public List<CourseDocumentDTO> getDocumentsByCourse(Long courseId) {
        return documentRepository.findByCourseIdOrderByCreatedAtDesc(courseId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    /**
     * Charge un fichier pour le téléchargement par ID de document.
     */
    public Resource loadFileAsResourceById(Long documentId) {
        CourseDocument doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found: " + documentId));
        return loadFileAsResource(doc.getCourseId(), doc.getStoredFilename());
    }

    /**
     * Charge un fichier pour le téléchargement.
     */
    public Resource loadFileAsResource(Long courseId, String storedFilename) {
        try {
            Path filePath = Paths.get(uploadDir, String.valueOf(courseId), storedFilename);
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
            throw new RuntimeException("File not found: " + storedFilename);
        } catch (MalformedURLException e) {
            throw new RuntimeException("File not found: " + storedFilename, e);
        }
    }

    /**
     * Supprime un document.
     */
    @Transactional
    public void deleteDocument(Long documentId) {
        CourseDocument doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found: " + documentId));

        // Supprimer le fichier physique
        try {
            Path filePath = Paths.get(uploadDir, String.valueOf(doc.getCourseId()), doc.getStoredFilename());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.warn("Could not delete file: {}", e.getMessage());
        }

        documentRepository.delete(doc);
        log.info("Document deleted: {}", documentId);
    }

    // ── private ──────────────────────────────────────────────────────────────

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) throw new RuntimeException("File is empty");
        if (file.getSize() > MAX_FILE_SIZE)
            throw new RuntimeException("File exceeds maximum size of 50 MB");
        if (!ALLOWED_TYPES.contains(file.getContentType()))
            throw new RuntimeException("File type not allowed: " + file.getContentType()
                + ". Allowed: PDF, DOC, DOCX, PPT, PPTX, XLS, XLSX, TXT, JPG, PNG");
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf('.'));
    }

    private CourseDocumentDTO toDTO(CourseDocument doc) {
        CourseDocumentDTO dto = new CourseDocumentDTO();
        dto.setId(doc.getId());
        dto.setCourseId(doc.getCourseId());
        dto.setOriginalFilename(doc.getOriginalFilename());
        dto.setContentType(doc.getContentType());
        dto.setFileSize(doc.getFileSize());
        dto.setCategory(doc.getCategory());
        dto.setDescription(doc.getDescription());
        dto.setUploadedBy(doc.getUploadedBy());
        dto.setCreatedAt(doc.getCreatedAt());
        dto.setDownloadUrl(baseUrl + "/" + doc.getCourseId()
                + "/documents/" + doc.getId() + "/download");
        return dto;
    }
}
