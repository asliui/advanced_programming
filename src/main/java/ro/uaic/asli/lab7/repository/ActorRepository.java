package ro.uaic.asli.lab7.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.uaic.asli.lab7.entity.ActorEntity;

public interface ActorRepository extends JpaRepository<ActorEntity, Integer> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Integer id);
}
