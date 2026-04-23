package cm.iusjc.resource.repository;

import cm.iusjc.resource.entity.SalleMateriel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SalleMaterielRepository extends JpaRepository<SalleMateriel, Long> {

    List<SalleMateriel> findBySalleId(Long salleId);

    List<SalleMateriel> findByMaterielId(Long materielId);

    Optional<SalleMateriel> findBySalleIdAndMaterielId(Long salleId, Long materielId);

    /** Équipements disponibles (quantiteDisponible > 0) dans une salle */
    @Query("SELECT sm FROM SalleMateriel sm WHERE sm.salle.id = :salleId AND sm.quantiteDisponible > 0")
    List<SalleMateriel> findDisponiblesBySalleId(@Param("salleId") Long salleId);

    /** Équipements requis dans une salle */
    List<SalleMateriel> findBySalleIdAndRequisTrue(Long salleId);

    /** Équipements d'un type de matériel dans une salle */
    @Query("SELECT sm FROM SalleMateriel sm WHERE sm.salle.id = :salleId AND sm.materiel.typeMateriel.code = :typeCode")
    List<SalleMateriel> findBySalleIdAndTypeMaterielCode(@Param("salleId") Long salleId,
                                                          @Param("typeCode") String typeCode);

    /** Toutes les salles qui possèdent un matériel donné */
    @Query("SELECT sm FROM SalleMateriel sm WHERE sm.materiel.id = :materielId AND sm.quantiteDisponible > 0")
    List<SalleMateriel> findSallesAvecMaterielDisponible(@Param("materielId") Long materielId);
}
