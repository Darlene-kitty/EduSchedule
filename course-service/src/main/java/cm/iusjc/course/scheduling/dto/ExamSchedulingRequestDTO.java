package cm.iusjc.course.scheduling.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class ExamSchedulingRequestDTO {
    /** École concernée */
    private Long schoolId;
    /** Semestre : S1 ou S2 */
    private String semester;
    /** Niveaux à planifier : L1, L2, L3, M1, M2 */
    private List<String> levels;
    /** Date de début de la session d'examens */
    private LocalDate sessionStart;
    /** Date de fin de la session d'examens */
    private LocalDate sessionEnd;
    /** Durée par défaut d'un examen en minutes (ex: 120) */
    private Integer defaultDurationMinutes;
    /** Créneaux horaires disponibles par jour, ex: ["08:00-10:00","10:30-12:30","14:00-16:00"] */
    private List<String> availableSlots;
    /** IDs des salles disponibles pour les examens */
    private List<Long> roomIds;
    /** Nombre max d'examens par jour pour un même niveau */
    private Integer maxExamsPerDayPerLevel;
    /** Respecter les disponibilités des enseignants */
    private Boolean respectTeacherAvailability;
}
