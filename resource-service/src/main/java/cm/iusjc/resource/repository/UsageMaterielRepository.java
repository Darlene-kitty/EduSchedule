package cm.iusjc.resource.repository;

import cm.iusjc.resource.entity.UsageMateriel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UsageMaterielRepository extends JpaRepository<UsageMateriel, Long> {

    List<UsageMateriel> findByMaterielIdOrderByDateDebutDesc(Long materielId);

    List<UsageMateriel> findByReservationId(Long reservationId);

    @Query("SELECT COALESCE(SUM(u.dureeMinutes), 0) FROM UsageMateriel u WHERE u.materiel.id = :materielId")
    Long sumDureeMinutesByMaterielId(@Param("materielId") Long materielId);

    @Query("SELECT COALESCE(SUM(u.dureeMinutes), 0) FROM UsageMateriel u WHERE u.materiel.id = :materielId " +
           "AND u.dateDebut >= :since")
    Long sumDureeMinutesSince(@Param("materielId") Long materielId, @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(u) FROM UsageMateriel u WHERE u.materiel.id = :materielId AND u.problemeSignale = true")
    Long countProblemesByMaterielId(@Param("materielId") Long materielId);

    @Query("SELECT COUNT(u) FROM UsageMateriel u WHERE u.materiel.id = :materielId")
    Long countUsagesByMaterielId(@Param("materielId") Long materielId);

    @Query("SELECT u FROM UsageMateriel u WHERE u.materiel.id = :materielId " +
           "AND u.dateDebut BETWEEN :from AND :to ORDER BY u.dateDebut DESC")
    List<UsageMateriel> findByMaterielIdAndPeriod(@Param("materielId") Long materielId,
                                                   @Param("from") LocalDateTime from,
                                                   @Param("to") LocalDateTime to);

    /** Top matériels les plus utilisés (par durée totale) */
    @Query("SELECT u.materiel.id, SUM(u.dureeMinutes) as total FROM UsageMateriel u " +
           "GROUP BY u.materiel.id ORDER BY total DESC")
    List<Object[]> findTopUsedMateriels();
}
