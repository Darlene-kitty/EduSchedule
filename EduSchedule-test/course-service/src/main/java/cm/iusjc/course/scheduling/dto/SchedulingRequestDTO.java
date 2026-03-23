package cm.iusjc.course.scheduling.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

@Data
public class SchedulingRequestDTO {

    @NotNull(message = "schoolId est obligatoire")
    private Long schoolId;

    @NotBlank(message = "semester est obligatoire (ex: S1, S2)")
    private String semester;

    @NotBlank(message = "level est obligatoire (ex: L1, L2)")
    private String level;

    /** Créneaux horaires disponibles dans la semaine, ex: ["LUNDI_08:00_10:00", "LUNDI_10:00_12:00"] */
    private List<String> availableSlots;

    /** IDs des salles à utiliser. Si null, toutes les salles actives sont utilisées. */
    private List<Long> roomIds;

    /** Nombre max d'heures de cours par jour pour un même groupe */
    private Integer maxHoursPerDay;
}
