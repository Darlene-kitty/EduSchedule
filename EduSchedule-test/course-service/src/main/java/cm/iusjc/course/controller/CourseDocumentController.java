package cm.iusjc.course.controller;

import cm.iusjc.course.dto.CourseDocumentDTO;
import cm.iusjc.course.entity.DocumentCategory;
import cm.iusjc.course.service.CourseDocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Gestion des supports de cours (PDF, DOC, PPTX, etc.)
 * Base URL : /api/v1/courses/{courseId}/documents
 */
@RestController
@RequestMapping("/api/v1/courses/{courseId}/documents")
@RequiredArgsConstructor
@Slf4j
public class CourseDocumentController {

    private final CourseDocumentService documentService;

    /**
     * Upload un support de cours.
     * Multipart form : file + category (optionnel) + description (optionnel) + uploadedBy (optionnel)
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadDocument(
            @PathVariable Long courseId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "category", defaultValue = "COURS") String category,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "uploadedBy", required = false) Long uploadedBy) {
        try {
            DocumentCategory cat = DocumentCategory.valueOf(category.toUpperCase());
            CourseDocumentDTO doc = documentService.uploadDocument(courseId, file, cat, description, uploadedBy);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "message", "Document uploaded successfully",
                "data", doc
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false,
                "message", "Invalid category. Use: COURS, TD, TP, EXAMEN, AUTRE"));
        } catch (IOException e) {
            log.error("IO error uploading document: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false, "message", "Storage error: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Error uploading document: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Liste tous les documents d'un cours.
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getDocuments(@PathVariable Long courseId) {
        try {
            List<CourseDocumentDTO> docs = documentService.getDocumentsByCourse(courseId);
            return ResponseEntity.ok(Map.of("success", true, "data", docs, "total", docs.size()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Télécharge un document.
     */
    @GetMapping("/{documentId}/download")
    public ResponseEntity<Resource> downloadDocument(
            @PathVariable Long courseId,
            @PathVariable Long documentId) {
        try {
            // Récupérer les métadonnées pour le nom de fichier
            List<CourseDocumentDTO> docs = documentService.getDocumentsByCourse(courseId);
            CourseDocumentDTO meta = docs.stream()
                    .filter(d -> d.getId().equals(documentId))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Document not found: " + documentId));

            // Charger le fichier depuis le disque
            // Le storedFilename n'est pas dans le DTO — on passe par le service
            Resource resource = documentService.loadFileAsResourceById(documentId);

            String contentType = meta.getContentType() != null
                    ? meta.getContentType() : "application/octet-stream";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + meta.getOriginalFilename() + "\"")
                    .body(resource);
        } catch (Exception e) {
            log.error("Error downloading document {}: {}", documentId, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Supprime un document.
     */
    @DeleteMapping("/{documentId}")
    public ResponseEntity<Map<String, Object>> deleteDocument(
            @PathVariable Long courseId,
            @PathVariable Long documentId) {
        try {
            documentService.deleteDocument(documentId);
            return ResponseEntity.ok(Map.of("success", true, "message", "Document deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
