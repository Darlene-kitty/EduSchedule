package cm.iusjc.eventservice.entity;

public enum SupervisorStatus {
    ASSIGNED("Assigné"),
    CONFIRMED("Confirmé"),
    PRESENT("Présent"),
    ABSENT("Absent"),
    REPLACED("Remplacé");
    
    private final String displayName;
    
    SupervisorStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}