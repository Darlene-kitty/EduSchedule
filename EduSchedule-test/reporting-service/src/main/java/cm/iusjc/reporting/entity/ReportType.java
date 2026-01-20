package cm.iusjc.reporting.entity;

public enum ReportType {
    USER_STATISTICS("Statistiques des utilisateurs"),
    COURSE_UTILIZATION("Utilisation des cours"),
    ROOM_OCCUPANCY("Occupation des salles"),
    RESERVATION_SUMMARY("Résumé des réservations"),
    SCHEDULE_OVERVIEW("Vue d'ensemble des emplois du temps"),
    ATTENDANCE_REPORT("Rapport de présence"),
    RESOURCE_USAGE("Utilisation des ressources"),
    MONTHLY_SUMMARY("Résumé mensuel"),
    YEARLY_SUMMARY("Résumé annuel"),
    CUSTOM_REPORT("Rapport personnalisé");
    
    private final String displayName;
    
    ReportType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}