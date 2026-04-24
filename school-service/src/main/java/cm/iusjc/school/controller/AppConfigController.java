package cm.iusjc.school.controller;

import cm.iusjc.school.dto.AppConfigDTO;
import cm.iusjc.school.dto.AppConfigDTO.EventTypeDTO;
import cm.iusjc.school.dto.AppConfigDTO.SessionTypeDTO;
import cm.iusjc.school.dto.AppConfigDTO.WorkDayDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Endpoint de configuration applicative.
 * Fournit toutes les listes de référence utilisées par le frontend
 * (niveaux, semestres, types de cours, jours, heures, etc.)
 * afin d'éliminer les données hardcodées dans les composants Angular.
 *
 * Route : GET /api/v1/config/app
 */
@RestController
@RequestMapping("/api/v1/config")
@Slf4j
public class AppConfigController {

    @GetMapping("/app")
    public ResponseEntity<Map<String, Object>> getAppConfig() {
        log.debug("Fetching application configuration");

        AppConfigDTO config = AppConfigDTO.builder()
            .academicLevels(List.of("L1", "L2", "L3", "M1", "M2"))
            .semesters(List.of("S1", "S2", "S3", "S4", "S5", "S6", "S7", "S8", "S9", "S10"))
            .sessionTypes(List.of(
                new SessionTypeDTO("CM",        "Cours magistral"),
                new SessionTypeDTO("TD",        "TD"),
                new SessionTypeDTO("TP",        "TP"),
                new SessionTypeDTO("Séminaire", "Séminaire")
            ))
            .eventTypes(List.of(
                new EventTypeDTO("CONFERENCE",  "Conférence"),
                new EventTypeDTO("SEMINAR",     "Séminaire"),
                new EventTypeDTO("WORKSHOP",    "Atelier"),
                new EventTypeDTO("MEETING",     "Réunion"),
                new EventTypeDTO("EXAM",        "Examen"),
                new EventTypeDTO("DEFENSE",     "Soutenance"),
                new EventTypeDTO("CEREMONY",    "Cérémonie"),
                new EventTypeDTO("TRAINING",    "Formation"),
                new EventTypeDTO("COMPETITION", "Compétition"),
                new EventTypeDTO("OTHER",       "Autre")
            ))
            .departments(List.of(
                "Informatique", "Mathématiques", "Physique", "Chimie",
                "Biologie", "Économie", "Droit", "Lettres",
                "Sciences Humaines", "Génie Civil", "Génie Électrique"
            ))
            .courseDurations(List.of(30, 45, 60, 90, 120, 150, 180, 240))
            .creditValues(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10))
            .examSlots(List.of(
                "08:00-10:00", "10:30-12:30",
                "14:00-16:00", "16:30-18:30"
            ))
            .workDays(List.of(
                new WorkDayDTO("MONDAY",    "Lundi"),
                new WorkDayDTO("TUESDAY",   "Mardi"),
                new WorkDayDTO("WEDNESDAY", "Mercredi"),
                new WorkDayDTO("THURSDAY",  "Jeudi"),
                new WorkDayDTO("FRIDAY",    "Vendredi")
            ))
            .workHours(List.of(
                "07:00", "08:00", "09:00", "10:00", "11:00",
                "12:00", "13:00", "14:00", "15:00", "16:00",
                "17:00", "18:00", "19:00"
            ))
            .levelTypes(List.of("Licence", "Master", "Doctorat", "Préparatoire", "CPGE"))
            .courseTypes(List.of("CM", "TD", "TP", "EXAM", "CONFERENCE", "SEMINAR"))
            .build();

        return ResponseEntity.ok(Map.of(
            "success", true,
            "data", config
        ));
    }
}
