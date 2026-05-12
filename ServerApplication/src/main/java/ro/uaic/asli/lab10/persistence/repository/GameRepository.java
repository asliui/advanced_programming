package ro.uaic.asli.lab10.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import ro.uaic.asli.lab10.persistence.entity.GameEntity;
import ro.uaic.asli.lab10.persistence.entity.GameStatus;

import java.time.Instant;
import java.util.Optional;

public interface GameRepository extends JpaRepository<GameEntity, Long> {

    Optional<GameEntity> findByGameCode(String gameCode);

    @Modifying
    @Transactional
    @Query("update GameEntity g set g.status = :status, g.endedAt = :endedAt where g.id = :id")
    int updateStatusAndEndedAtById(@Param("id") Long id, @Param("status") GameStatus status, @Param("endedAt") Instant endedAt);
}
