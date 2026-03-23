package cm.iusjc.course.scheduling.dto;

import lombok.Data;
import java.util.List;

@Data
public class SchedulingResultDTO {

    private String jobId;
    private String status;          // PENDING | RUNNING | COMPLETED | FAILED | PARTIAL
    private int progress;           // 0-100

    /** Créneaux affectés par l'algorithme */
    private List<ScheduleSlotDTO> slots;

    /** Cours qui n'ont pas pu être placés (flot non saturé) */
    private List<String> unassignedCourses;

    /** Valeur du flot max calculé */
    private int maxFlowValue;

    /** Flot total demandé (somme des hoursPerWeek) */
    private int totalDemand;

    private String message;
    private long generationTimeMs;
}
