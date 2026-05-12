package ro.uaic.asli.lab10.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.uaic.asli.lab10.persistence.entity.PlayerEntity;

import java.util.Optional;

public interface PlayerRepository extends JpaRepository<PlayerEntity, Long> {

    Optional<PlayerEntity> findByName(String name);
}
