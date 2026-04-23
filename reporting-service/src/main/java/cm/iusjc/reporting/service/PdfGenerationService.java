package cm.iusjc.reporting.service;

import cm.iusjc.reporting.dto.StatisticsDTO;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
@Slf4j
public class PdfGenerationService {
    
    /**
     * Génère un rapport PDF des statistiques
     */
    public byte[] generateStatisticsPdf(StatisticsDTO statistics, String title) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        try (PdfWriter writer = new PdfWriter(baos);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {
            
            // Titre du rapport
            document.add(new Paragraph(title != null ? title : "Rapport de Statistiques")
                    .setFontSize(20)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER));
            
            // Date de génération
            document.add(new Paragraph("Généré le : " + 
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.RIGHT));
            
            document.add(new Paragraph("\n"));
            
            // Statistiques générales
            addGeneralStatistics(document, statistics);
            
            // Répartition par rôle
            addUsersByRoleTable(document, statistics.getUsersByRole());
            
            // Réservations par statut
            addReservationsByStatusTable(document, statistics.getReservationsByStatus());
            
            // Cours par département
            addCoursesByDepartmentTable(document, statistics.getCoursesByDepartment());
            
            // Ressources par type
            addResourcesByTypeTable(document, statistics.getResourcesByType());
            
            // Taux d'occupation
            addOccupancyRates(document, statistics);
            
        } catch (Exception e) {
            log.error("Error generating PDF: {}", e.getMessage(), e);
            throw new IOException("Failed to generate PDF report", e);
        }
        
        return baos.toByteArray();
    }
    
    private void addGeneralStatistics(Document document, StatisticsDTO statistics) {
        document.add(new Paragraph("Statistiques Générales")
                .setFontSize(16)
                .setBold());
        
        Table table = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                .setWidth(UnitValue.createPercentValue(100));
        
        table.addHeaderCell(new Cell().add(new Paragraph("Métrique").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Valeur").setBold()));
        
        table.addCell("Nombre total d'utilisateurs");
        table.addCell(String.valueOf(statistics.getTotalUsers()));
        
        table.addCell("Nombre total de cours");
        table.addCell(String.valueOf(statistics.getTotalCourses()));
        
        table.addCell("Nombre total de réservations");
        table.addCell(String.valueOf(statistics.getTotalReservations()));
        
        table.addCell("Nombre total de ressources");
        table.addCell(String.valueOf(statistics.getTotalResources()));
        
        document.add(table);
        document.add(new Paragraph("\n"));
    }
    
    private void addUsersByRoleTable(Document document, Map<String, Long> usersByRole) {
        if (usersByRole == null || usersByRole.isEmpty()) {
            return;
        }
        
        document.add(new Paragraph("Répartition des Utilisateurs par Rôle")
                .setFontSize(16)
                .setBold());
        
        Table table = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                .setWidth(UnitValue.createPercentValue(100));
        
        table.addHeaderCell(new Cell().add(new Paragraph("Rôle").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Nombre").setBold()));
        
        usersByRole.forEach((role, count) -> {
            table.addCell(role);
            table.addCell(String.valueOf(count));
        });
        
        document.add(table);
        document.add(new Paragraph("\n"));
    }
    
    private void addReservationsByStatusTable(Document document, Map<String, Long> reservationsByStatus) {
        if (reservationsByStatus == null || reservationsByStatus.isEmpty()) {
            return;
        }
        
        document.add(new Paragraph("Réservations par Statut")
                .setFontSize(16)
                .setBold());
        
        Table table = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                .setWidth(UnitValue.createPercentValue(100));
        
        table.addHeaderCell(new Cell().add(new Paragraph("Statut").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Nombre").setBold()));
        
        reservationsByStatus.forEach((status, count) -> {
            table.addCell(status);
            table.addCell(String.valueOf(count));
        });
        
        document.add(table);
        document.add(new Paragraph("\n"));
    }
    
    private void addCoursesByDepartmentTable(Document document, Map<String, Long> coursesByDepartment) {
        if (coursesByDepartment == null || coursesByDepartment.isEmpty()) {
            return;
        }
        
        document.add(new Paragraph("Cours par Département")
                .setFontSize(16)
                .setBold());
        
        Table table = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                .setWidth(UnitValue.createPercentValue(100));
        
        table.addHeaderCell(new Cell().add(new Paragraph("Département").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Nombre de Cours").setBold()));
        
        coursesByDepartment.forEach((department, count) -> {
            table.addCell(department);
            table.addCell(String.valueOf(count));
        });
        
        document.add(table);
        document.add(new Paragraph("\n"));
    }
    
    private void addResourcesByTypeTable(Document document, Map<String, Long> resourcesByType) {
        if (resourcesByType == null || resourcesByType.isEmpty()) {
            return;
        }
        
        document.add(new Paragraph("Ressources par Type")
                .setFontSize(16)
                .setBold());
        
        Table table = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                .setWidth(UnitValue.createPercentValue(100));
        
        table.addHeaderCell(new Cell().add(new Paragraph("Type de Ressource").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Nombre").setBold()));
        
        resourcesByType.forEach((type, count) -> {
            table.addCell(type);
            table.addCell(String.valueOf(count));
        });
        
        document.add(table);
        document.add(new Paragraph("\n"));
    }
    
    private void addOccupancyRates(Document document, StatisticsDTO statistics) {
        document.add(new Paragraph("Taux d'Occupation")
                .setFontSize(16)
                .setBold());
        
        Table table = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                .setWidth(UnitValue.createPercentValue(100));
        
        table.addHeaderCell(new Cell().add(new Paragraph("Métrique").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Taux (%)").setBold()));
        
        table.addCell("Occupation moyenne des salles");
        table.addCell(String.format("%.2f%%", statistics.getAverageRoomOccupancy()));
        
        table.addCell("Utilisation moyenne des cours");
        table.addCell(String.format("%.2f%%", statistics.getAverageCourseUtilization()));
        
        document.add(table);
        document.add(new Paragraph("\n"));
    }
}