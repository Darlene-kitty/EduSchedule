package cm.iusjc.teacheravailability.entity;

public enum ContractType {
    FULL_TIME("Temps plein"),
    PART_TIME("Temps partiel"),
    HOURLY("Horaire"),
    SUBSTITUTE("Remplaçant"),
    VISITING("Vacataire");
    
    private final String displayName;
    
    ContractType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}