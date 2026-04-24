package cm.iusjc.school.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Configuration applicative retournée au frontend.
 * Remplace toutes les listes hardcodées dans les composants Angular.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppConfigDTO {

    /** Niveaux académiques : L1, L2, L3, M1, M2 */
    private List<String> academicLevels;

    /** Semestres : S1, S2, ... S10 */
    private List<String> semesters;

    /** Types de cours : CM, TD, TP, Séminaire */
    private List<SessionTypeDTO> sessionTypes;

    /** Types d'événements */
    private List<EventTypeDTO> eventTypes;

    /** Départements / spécialités */
    private List<String> departments;

    /** Durées de cours en minutes */
    private List<Integer> courseDurations;

    /** Crédits ECTS possibles */
    private List<Integer> creditValues;

    /** Créneaux horaires d'examen */
    private List<String> examSlots;

    /** Jours ouvrables */
    private List<WorkDayDTO> workDays;

    /** Heures de travail (08:00 → 18:00) */
    private List<String> workHours;

    /** Types de niveaux académiques */
    private List<String> levelTypes;

    /** Types de cours pour disponibilité équipement */
    private List<String> courseTypes;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SessionTypeDTO {
        private String value;
        private String label;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class EventTypeDTO {
        private String value;
        private String label;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class WorkDayDTO {
        private String key;
        private String label;
    }
}
