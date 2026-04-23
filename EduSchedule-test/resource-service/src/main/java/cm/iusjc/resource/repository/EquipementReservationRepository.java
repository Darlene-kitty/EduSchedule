package cm.iusjc.resource.repository;

import cm.iusjc.resource.entity.EquipementReservation;
import cm.iusjc.resource.entity.EquipementReservation.StatutReservationEquipement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EquipementReservationRepository extends JpaRepository<EquipementReservation, Long> {

    List<EquipementReservation> findByReservationId(Long reservationId);

    List<EquipementReservation> findBySalleMaterielId(Long salleMaterielId);

    List<EquipementReservation> findByStatut(StatutReservationEquipement statut);

    /** Réservations actives d'un équipement-salle qui chevauchent une plage horaire */
    @Query("""
        SELECT er FROM EquipementReservation er
        WHERE er.salleMateriel.id = :salleMaterielId
          AND er.statut = 'ACTIVE'
          AND er.dateDebut < :fin
          AND er.dateFin > :debut
    """)
    List<EquipementReservation> findConflicts(@Param("salleMaterielId") Long salleMaterielId,
                                               @Param("debut") LocalDateTime debut,
                                               @Param("fin") LocalDateTime fin);

    /** Toutes les réservations d'équipements pour une réservation donnée */
    @Query("SELECT er FROM EquipementReservation er WHERE er.reservationId = :reservationId AND er.statut = 'ACTIVE'")
    List<EquipementReservation> findActiveByReservationId(@Param("reservationId") Long reservationId);

    /** Stats d'utilisation par type de cours */
    @Query("""
        SELECT er.typeCours, COUNT(er), SUM(er.quantite)
        FROM EquipementReservation er
        WHERE er.statut != 'ANNULEE'
        GROUP BY er.typeCours
    """)
    List<Object[]> getStatsByTypeCours();
}
