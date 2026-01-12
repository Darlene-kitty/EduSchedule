package cm.iusjc.eventservice.entity;

public enum EventStatus {
    PLANNED("Planifié"),
    CONFIRMED("Confirmé"),
    IN_PROGRESS("En cours"),
    COMPLETED("Terminé"),
    CANCELLED("Annulé"),
    POSTPONED("Reporté");
    
    private final String displayName;
    
    EventStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}