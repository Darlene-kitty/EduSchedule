package cm.iusjc.resource.repository;

import cm.iusjc.resource.entity.TypeMateriel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TypeMaterielRepository extends JpaRepository<TypeMateriel, Long> {
    boolean existsByCode(String code);
    List<TypeMateriel> findByActiveTrue();
    Optional<TypeMateriel> findByCode(String code);
}
