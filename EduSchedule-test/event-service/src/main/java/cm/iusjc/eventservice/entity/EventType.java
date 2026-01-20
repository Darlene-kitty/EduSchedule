package cm.iusjc.eventservice.entity;

public enum EventType {
    SEMINAR("Séminaire"),
    CONFERENCE("Conférence"),
    WORKSHOP("Atelier"),
    MEETING("Réunion"),
    EXAM("Examen"),
    DEFENSE("Soutenance"),
    CEREMONY("Cérémonie"),
    TRAINING("Formation"),
    OTHER("Autre");
    
    private final String displayName;
    
    EventType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}