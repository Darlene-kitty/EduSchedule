package cm.iusjc.school.repository;

import cm.iusjc.school.entity.Affectation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AffectationRepository extends JpaRepository<Affectation, Long> {

    /** Toutes les affectations actives d'un groupe */
    List<Affectation> findByGroupeIdAndActiveTrue(Long groupeId);

    /** Affectation active d'un étudiant (un étudiant ne peut être que dans un seul groupe actif) */
    Optional<Affectation> findByEtudiantIdAndActiveTrue(Long etudiantId);

    /** Vérifie si un étudiant est déjà dans un groupe actif */
    boolean existsByEtudiantIdAndActiveTrue(Long etudiantId);

    /** Nombre d'étudiants actifs dans un groupe */
    @Query("SELECT COUNT(a) FROM Affectation a WHERE a.groupe.id = :groupeId AND a.active = true")
    long countActiveByGroupeId(@Param("groupeId") Long groupeId);

    /** Tous les groupes d'un niveau avec leur effectif actuel */
    @Query("SELECT a.groupe.id, COUNT(a) FROM Affectation a WHERE a.groupe.niveau.id = :niveauId AND a.active = true GROUP BY a.groupe.id")
    List<Object[]> countActiveByNiveauId(@Param("niveauId") Long niveauId);
}
