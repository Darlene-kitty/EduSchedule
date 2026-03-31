package cm.iusjc.notification.service;

import cm.iusjc.notification.dto.NotificationPreferencesDTO;
import cm.iusjc.notification.entity.NotificationPreferences;
import cm.iusjc.notification.repository.NotificationPreferencesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationPreferencesService {

    private final NotificationPreferencesRepository preferencesRepository;

    public NotificationPreferencesDTO getPreferences(Long userId) {
        return preferencesRepository.findByUserId(userId)
                .map(this::toDTO)
                .orElseGet(() -> defaultPreferences(userId));
    }

    @Transactional
    public NotificationPreferencesDTO savePreferences(Long userId, NotificationPreferencesDTO dto) {
        NotificationPreferences prefs = preferencesRepository.findByUserId(userId)
                .orElseGet(NotificationPreferences::new);

        prefs.setUserId(userId);
        prefs.setEmailEnabled(Boolean.TRUE.equals(dto.getEmailEnabled()));
        prefs.setPushEnabled(Boolean.TRUE.equals(dto.getPushEnabled()));
        prefs.setScheduleChanges(Boolean.TRUE.equals(dto.getScheduleChanges()));
        prefs.setConflictAlerts(Boolean.TRUE.equals(dto.getConflictAlerts()));
        prefs.setReservationUpdates(Boolean.TRUE.equals(dto.getReservationUpdates()));
        prefs.setReminderNotifications(Boolean.TRUE.equals(dto.getReminderNotifications()));
        if (dto.getReminderMinutesBefore() != null) {
            prefs.setReminderMinutesBefore(dto.getReminderMinutesBefore());
        }

        log.info("Saved notification preferences for user {}", userId);
        return toDTO(preferencesRepository.save(prefs));
    }

    private NotificationPreferencesDTO toDTO(NotificationPreferences p) {
        NotificationPreferencesDTO dto = new NotificationPreferencesDTO();
        dto.setUserId(p.getUserId());
        dto.setEmailEnabled(p.isEmailEnabled());
        dto.setPushEnabled(p.isPushEnabled());
        dto.setScheduleChanges(p.isScheduleChanges());
        dto.setConflictAlerts(p.isConflictAlerts());
        dto.setReservationUpdates(p.isReservationUpdates());
        dto.setReminderNotifications(p.isReminderNotifications());
        dto.setReminderMinutesBefore(p.getReminderMinutesBefore());
        return dto;
    }

    private NotificationPreferencesDTO defaultPreferences(Long userId) {
        NotificationPreferencesDTO dto = new NotificationPreferencesDTO();
        dto.setUserId(userId);
        dto.setEmailEnabled(true);
        dto.setPushEnabled(true);
        dto.setScheduleChanges(true);
        dto.setConflictAlerts(true);
        dto.setReservationUpdates(true);
        dto.setReminderNotifications(true);
        dto.setReminderMinutesBefore(30);
        return dto;
    }
}
