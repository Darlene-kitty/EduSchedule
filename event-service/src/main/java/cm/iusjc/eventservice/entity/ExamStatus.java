package cm.iusjc.eventservice.entity;

public enum ExamStatus {
    SCHEDULED("Programmé"),
    CONFIRMED("Confirmé"),
    IN_PROGRESS("En cours"),
    COMPLETED("Terminé"),
    CANCELLED("Annulé"),
    POSTPONED("Reporté"),
    GRADING("Correction en cours"),
    GRADED("Corrigé");
    
    private final String displayName;
    
    ExamStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}