package ro.uaic.asli.lab10.persistence.search;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import ro.uaic.asli.lab10.persistence.entity.GameEntity;
import ro.uaic.asli.lab10.persistence.entity.PlayerEntity;
import ro.uaic.asli.lab10.persistence.entity.ResultEntity;

import java.util.ArrayList;
import java.util.List;

public final class ResultSpecification {

    private ResultSpecification() {
    }

    public static Specification<ResultEntity> from(ResultSearchCriteria c) {
        return (root, query, cb) -> {
            if (query != null) {
                query.distinct(true);
            }

            List<Predicate> predicates = new ArrayList<>();
            Join<ResultEntity, PlayerEntity> player = root.join("player", JoinType.INNER);
            Join<ResultEntity, GameEntity> game = root.join("game", JoinType.INNER);

            if (c.getPlayerNameStartsWith() != null && !c.getPlayerNameStartsWith().isBlank()) {
                String pattern = c.getPlayerNameStartsWith().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(player.get("name")), pattern));
            }
            if (c.getMinScore() != null) {
                predicates.add(cb.ge(root.get("score"), c.getMinScore()));
            }
            if (c.getMaxScore() != null) {
                predicates.add(cb.le(root.get("score"), c.getMaxScore()));
            }
            if (c.getGameStatus() != null) {
                predicates.add(cb.equal(game.get("status"), c.getGameStatus()));
            }
            if (c.getGameCode() != null && !c.getGameCode().isBlank()) {
                predicates.add(cb.equal(game.get("gameCode"), c.getGameCode()));
            }
            if (c.getStartedAfter() != null) {
                predicates.add(cb.greaterThanOrEqualTo(game.get("startedAt"), c.getStartedAfter()));
            }
            if (c.getStartedBefore() != null) {
                predicates.add(cb.lessThanOrEqualTo(game.get("startedAt"), c.getStartedBefore()));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
