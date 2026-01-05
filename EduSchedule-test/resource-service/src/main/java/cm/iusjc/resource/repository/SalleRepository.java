package cm.iusjc.resource.repository;

import cm.iusjc.resource.entity.Salle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SalleRepository extends JpaRepository<Salle, Long> {
    List<Salle> findByDisponibleTrue();
    List<Salle> findByDisponibleTrueAndActiveTrue();
    List<Salle> findByType(Salle.TypeSalle type);
}
