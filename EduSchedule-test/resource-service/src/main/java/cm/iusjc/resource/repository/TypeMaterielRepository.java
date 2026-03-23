package cm.iusjc.resource.repository;

import cm.iusjc.resource.entity.TypeMateriel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TypeMaterielRepository extends JpaRepository<TypeMateriel, Long> {
    boolean existsByCode(String code);
}
