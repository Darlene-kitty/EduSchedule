package cm.iusjc.eventservice.entity;

public enum ExamType {
    WRITTEN("Écrit"),
    ORAL("Oral"),
    PRACTICAL("Pratique"),
    PROJECT_DEFENSE("Soutenance de projet"),
    THESIS_DEFENSE("Soutenance de thèse"),
    CONTINUOUS_ASSESSMENT("Contrôle continu"),
    FINAL_EXAM("Examen final"),
    MAKEUP_EXAM("Rattrapage");
    
    private final String displayName;
    
    ExamType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}