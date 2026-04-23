package cm.iusjc.resource.repository;

import cm.iusjc.resource.entity.Materiel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MaterielRepository extends JpaRepository<Materiel, Long> {
    List<Materiel> findByActiveTrue();
    List<Materiel> findByTypeMaterielId(Long typeId);
    List<Materiel> findByEcole(String ecole);
    Optional<Materiel> findByCode(String code);
    List<Materiel> findBySalle(String salle);
}
