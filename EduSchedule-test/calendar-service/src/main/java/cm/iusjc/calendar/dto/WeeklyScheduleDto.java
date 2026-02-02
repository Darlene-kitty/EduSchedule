package cm.iusjc.calendar.dto;

import lombok.Data;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class WeeklyScheduleDto {
    private String userId;
    private LocalDate weekStart;
    private LocalDate weekEnd;
    private Map<DayOfWeek, List<CalendarEventDto>> dailySchedules = new HashMap<>();
    
    // Statistiques de la semaine
    private int totalEvents;
    private int totalHours;
    private Map<String, Integer> eventsByType = new HashMap<>();
    private Map<String, Integer> eventsByLocation = new HashMap<>();
}