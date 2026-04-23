package cm.iusjc.school.repository;

import cm.iusjc.school.entity.CategorieUE;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategorieUERepository extends JpaRepository<CategorieUE, Long> {
    List<CategorieUE> findByActiveTrue();
    boolean existsByCode(String code);
}
