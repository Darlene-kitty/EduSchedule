package cm.iusjc.aiservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Alert {
    private String type;
    private String severity;
    private String title;
    private String message;
    private boolean actionRequired;
    private List<String> suggestedActions;
    private LocalDateTime timestamp;
    private String category;
}
