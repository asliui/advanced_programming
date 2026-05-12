package ro.uaic.asli.lab10.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import ro.uaic.asli.lab10.persistence.entity.ResultEntity;

import jakarta.persistence.QueryHint;
import java.util.List;
import java.util.Optional;

public interface ResultRepository extends JpaRepository<ResultEntity, Long>, JpaSpecificationExecutor<ResultEntity> {

    /**
     * JPQL read query (Lab 11): filter by player name prefix and minimum score.
     */
    @Query(
            "select r from ResultEntity r join r.player p "
                    + "where lower(p.name) like lower(concat(:namePrefix, '%')) and r.score >= :minScore"
    )
    List<ResultEntity> findByPlayerNamePrefixAndMinScore(@Param("namePrefix") String namePrefix, @Param("minScore") int minScore);

    @QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
    @Query("select r from ResultEntity r join fetch r.player join fetch r.game where r.id = :id")
    Optional<ResultEntity> findByIdCached(@Param("id") Long id);

    List<ResultEntity> findByPlayer_Name(String name);
}
