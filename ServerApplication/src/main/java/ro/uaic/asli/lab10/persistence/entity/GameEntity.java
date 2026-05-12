package ro.uaic.asli.lab10.persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(
        name = "games",
        uniqueConstraints = @UniqueConstraint(name = "uk_games_code", columnNames = "game_code")
)
public class GameEntity extends AuditableEntity {

    @Column(name = "game_code", nullable = false, length = 64)
    private String gameCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private GameStatus status = GameStatus.WAITING;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "ended_at")
    private Instant endedAt;

    @ManyToMany
    @JoinTable(
            name = "game_questions",
            joinColumns = @JoinColumn(name = "game_id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "question_id", nullable = false)
    )
    private Set<QuestionEntity> questions = new LinkedHashSet<>();

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ResultEntity> results = new LinkedHashSet<>();

    protected GameEntity() {
    }

    public GameEntity(String gameCode, GameStatus status) {
        this.gameCode = gameCode;
        this.status = status;
    }

    public String getGameCode() {
        return gameCode;
    }

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(Instant endedAt) {
        this.endedAt = endedAt;
    }

    public Set<QuestionEntity> getQuestions() {
        return questions;
    }

    public Set<ResultEntity> getResults() {
        return results;
    }

    @Override
    public String toString() {
        return "GameEntity{id=" + getId() + ", gameCode='" + gameCode + "', status=" + status + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GameEntity that)) {
            return false;
        }
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId() == null ? System.identityHashCode(this) : getId().hashCode();
    }
}
