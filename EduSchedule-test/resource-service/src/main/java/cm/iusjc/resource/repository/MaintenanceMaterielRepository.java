package cm.iusjc.resource.repository;

import cm.iusjc.resource.entity.MaintenanceMateriel;
import cm.iusjc.resource.entity.MaintenanceMateriel.StatutIntervention;
import cm.iusjc.resource.entity.MaintenanceMateriel.TypeIntervention;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MaintenanceMaterielRepository extends JpaRepository<MaintenanceMateriel, Long> {

    List<MaintenanceMateriel> findByMaterielIdOrderByDateDebutDesc(Long materielId);

    List<MaintenanceMateriel> findByStatut(StatutIntervention statut);

    List<MaintenanceMateriel> findByMaterielIdAndStatut(Long materielId, StatutIntervention statut);

    @Query("SELECT COUNT(m) FROM MaintenanceMateriel m WHERE m.materiel.id = :materielId " +
           "AND m.typeIntervention = :type")
    Long countByMaterielIdAndType(@Param("materielId") Long materielId,
                                   @Param("type") TypeIntervention type);

    /** Matériels dont la dernière maintenance remonte à plus de X jours */
    @Query("SELECT DISTINCT m.materiel.id FROM MaintenanceMateriel m " +
           "WHERE m.statut = 'TERMINEE' " +
           "GROUP BY m.materiel.id " +
           "HAVING MAX(m.dateFin) < :seuil")
    List<Long> findMaterielsDueForMaintenance(@Param("seuil") LocalDateTime seuil);

    @Query("SELECT m FROM MaintenanceMateriel m WHERE m.materiel.id = :materielId " +
           "AND m.statut = 'TERMINEE' ORDER BY m.dateFin DESC")
    List<MaintenanceMateriel> findLastMaintenanceByMaterielId(@Param("materielId") Long materielId);
}
