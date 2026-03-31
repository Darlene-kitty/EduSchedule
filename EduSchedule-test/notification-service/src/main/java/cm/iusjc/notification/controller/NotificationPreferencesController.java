package cm.iusjc.notification.controller;

import cm.iusjc.notification.dto.NotificationPreferencesDTO;
import cm.iusjc.notification.service.NotificationPreferencesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications/preferences")
@RequiredArgsConstructor
public class NotificationPreferencesController {

    private final NotificationPreferencesService preferencesService;

    @GetMapping("/{userId}")
    public ResponseEntity<NotificationPreferencesDTO> getPreferences(@PathVariable Long userId) {
        return ResponseEntity.ok(preferencesService.getPreferences(userId));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<NotificationPreferencesDTO> savePreferences(
            @PathVariable Long userId,
            @RequestBody NotificationPreferencesDTO dto) {
        return ResponseEntity.ok(preferencesService.savePreferences(userId, dto));
    }
}
