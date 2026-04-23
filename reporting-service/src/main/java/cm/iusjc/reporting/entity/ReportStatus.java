package cm.iusjc.reporting.entity;

public enum ReportStatus {
    PENDING("En attente"),
    GENERATING("En cours de génération"),
    COMPLETED("Terminé"),
    FAILED("Échec"),
    EXPIRED("Expiré");
    
    private final String displayName;
    
    ReportStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}