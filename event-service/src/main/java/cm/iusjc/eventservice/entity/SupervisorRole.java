package cm.iusjc.eventservice.entity;

public enum SupervisorRole {
    MAIN_SUPERVISOR("Surveillant principal"),
    ASSISTANT_SUPERVISOR("Surveillant assistant"),
    TECHNICAL_SUPERVISOR("Surveillant technique"),
    BACKUP_SUPERVISOR("Surveillant de réserve");
    
    private final String displayName;
    
    SupervisorRole(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}