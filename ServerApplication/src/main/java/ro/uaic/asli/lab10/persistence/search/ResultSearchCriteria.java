package ro.uaic.asli.lab10.persistence.search;

import ro.uaic.asli.lab10.persistence.entity.GameStatus;

import java.time.Instant;

/**
 * Optional filters for dynamic {@link ro.uaic.asli.lab10.persistence.entity.ResultEntity} search.
 */
public class ResultSearchCriteria {

    private String playerNameStartsWith;
    private Integer minScore;
    private Integer maxScore;
    private GameStatus gameStatus;
    private String gameCode;
    private Instant startedAfter;
    private Instant startedBefore;

    public String getPlayerNameStartsWith() {
        return playerNameStartsWith;
    }

    public void setPlayerNameStartsWith(String playerNameStartsWith) {
        this.playerNameStartsWith = playerNameStartsWith;
    }

    public Integer getMinScore() {
        return minScore;
    }

    public void setMinScore(Integer minScore) {
        this.minScore = minScore;
    }

    public Integer getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(Integer maxScore) {
        this.maxScore = maxScore;
    }

    public GameStatus getGameStatus() {
        return gameStatus;
    }

    public void setGameStatus(GameStatus gameStatus) {
        this.gameStatus = gameStatus;
    }

    public String getGameCode() {
        return gameCode;
    }

    public void setGameCode(String gameCode) {
        this.gameCode = gameCode;
    }

    public Instant getStartedAfter() {
        return startedAfter;
    }

    public void setStartedAfter(Instant startedAfter) {
        this.startedAfter = startedAfter;
    }

    public Instant getStartedBefore() {
        return startedBefore;
    }

    public void setStartedBefore(Instant startedBefore) {
        this.startedBefore = startedBefore;
    }
}
