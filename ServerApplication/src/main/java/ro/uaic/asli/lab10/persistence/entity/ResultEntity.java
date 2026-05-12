package ro.uaic.asli.lab10.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

@Entity
@Table(
        name = "results",
        uniqueConstraints = @UniqueConstraint(name = "uk_results_game_player", columnNames = {"game_id", "player_id"})
)
public class ResultEntity extends AuditableEntity {

    @Column(name = "score", nullable = false)
    private int score;

    @Column(name = "correct_answers", nullable = false)
    private int correctAnswers;

    @Column(name = "wrong_answers", nullable = false)
    private int wrongAnswers;

    @Column(name = "finished_at", nullable = false)
    private Instant finishedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private PlayerEntity player;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    private GameEntity game;

    protected ResultEntity() {
    }

    public ResultEntity(
            int score,
            int correctAnswers,
            int wrongAnswers,
            Instant finishedAt,
            PlayerEntity player,
            GameEntity game
    ) {
        this.score = score;
        this.correctAnswers = correctAnswers;
        this.wrongAnswers = wrongAnswers;
        this.finishedAt = finishedAt;
        this.player = player;
        this.game = game;
    }

    public int getScore() {
        return score;
    }

    public int getCorrectAnswers() {
        return correctAnswers;
    }

    public int getWrongAnswers() {
        return wrongAnswers;
    }

    public Instant getFinishedAt() {
        return finishedAt;
    }

    public PlayerEntity getPlayer() {
        return player;
    }

    public GameEntity getGame() {
        return game;
    }

    @Override
    public String toString() {
        return "ResultEntity{id=" + getId() + ", score=" + score + ", gameId=" + (game == null ? null : game.getId())
                + ", playerId=" + (player == null ? null : player.getId()) + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ResultEntity that)) {
            return false;
        }
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId() == null ? System.identityHashCode(this) : getId().hashCode();
    }
}
