package cm.iusjc.teacheravailability.entity;

public enum AvailabilityType {
    AVAILABLE("Disponible"),
    PREFERRED("Préféré"),
    UNAVAILABLE("Indisponible"),
    BUSY("Occupé"),
    TENTATIVE("Provisoire");
    
    private final String displayName;
    
    AvailabilityType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public boolean isAvailable() {
        return this == AVAILABLE || this == PREFERRED || this == TENTATIVE;
    }
    
    public boolean isPreferred() {
        return this == PREFERRED;
    }
}