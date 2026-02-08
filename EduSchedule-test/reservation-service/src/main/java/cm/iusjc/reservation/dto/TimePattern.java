package cm.iusjc.reservation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimePattern {
    private DayOfWeek dayOfWeek;
    private Integer hour;
    private Boolean isPeakHour;
    private Boolean isWeekend;
}
