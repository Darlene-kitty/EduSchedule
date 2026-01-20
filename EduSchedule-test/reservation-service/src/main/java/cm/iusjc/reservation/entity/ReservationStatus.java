package cm.iusjc.reservation.entity;

public enum ReservationStatus {
    PENDING,     // En attente d'approbation
    CONFIRMED,   // Confirmée
    CANCELLED,   // Annulée
    REJECTED,    // Rejetée
    COMPLETED    // Terminée
}