package cm.iusjc.userservice.entity;

public enum AvailabilityType {
    AVAILABLE,      // Disponible
    UNAVAILABLE,    // Indisponible
    PREFERRED,      // Créneau préféré
    BUSY,          // Occupé (cours existant)
    BLOCKED        // Bloqué (congé, réunion, etc.)
}