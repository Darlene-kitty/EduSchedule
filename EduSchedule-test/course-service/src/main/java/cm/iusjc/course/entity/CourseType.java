package cm.iusjc.course.entity;

/**
 * Type de cours — détermine le type de salle à assigner automatiquement.
 *
 * Mapping salle :
 *   CM / CONFERENCE → AMPHITHEATER (capacité > 50)
 *   TD / SEMINAR    → CLASSROOM    (capacité 15-50)
 *   TP / PRACTICAL  → LABORATORY / COMPUTER_LAB / WORKSHOP
 *   EXAM            → EXAM_ROOM ou AMPHITHEATER
 */
public enum CourseType {
    CM,          // Cours Magistral → amphithéâtre
    TD,          // Travaux Dirigés → salle de classe
    TP,          // Travaux Pratiques → laboratoire
    CONFERENCE,  // Conférence → amphithéâtre / auditorium
    SEMINAR,     // Séminaire → salle de réunion / classe
    EXAM,        // Examen → salle d'examen / amphithéâtre
    PRACTICAL,   // Pratique (alias TP anglais) → laboratoire
    OTHER        // Autre → pas de contrainte de type
}
