package cm.iusjc.school.repository;

import cm.iusjc.school.entity.Filiere;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FiliereRepository extends JpaRepository<Filiere, Long> {
    boolean existsByCodeAndSchoolId(String code, Long schoolId);
}
