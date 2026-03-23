package cm.iusjc.school.repository;

import cm.iusjc.school.entity.Niveau;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NiveauRepository extends JpaRepository<Niveau, Long> {
}
