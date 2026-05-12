package ro.uaic.asli.lab10.model;

import ro.uaic.asli.lab10.bot.BotStrategy;

import java.util.Objects;
import java.util.Optional;

/**
 * A participant in the quiz (human connected via TCP, or a bot managed by the server).
 */
public final class Player {

    private final String name;
    private final boolean bot;
    private final BotStrategy botStrategy;

    private int score;
    private long totalResponseTimeMs;

    public Player(String name, boolean bot, BotStrategy botStrategy) {
        this.name = Objects.requireNonNull(name, "name");
        this.bot = bot;
        this.botStrategy = botStrategy;
        if (bot && botStrategy == null) {
            throw new IllegalArgumentException("Bots require a strategy");
        }
        if (!bot && botStrategy != null) {
            throw new IllegalArgumentException("Humans must not have a bot strategy");
        }
    }

    public static Player human(String name) {
        return new Player(name, false, null);
    }

    public static Player bot(String name, BotStrategy strategy) {
        return new Player(name, true, strategy);
    }

    public String getName() {
        return name;
    }

    public boolean isBot() {
        return bot;
    }

    public Optional<BotStrategy> getBotStrategy() {
        return Optional.ofNullable(botStrategy);
    }

    public int getScore() {
        return score;
    }

    public long getTotalResponseTimeMs() {
        return totalResponseTimeMs;
    }

    public synchronized void addResult(boolean correct, long responseTimeMs) {
        if (correct) {
            score++;
        }
        totalResponseTimeMs += Math.max(0, responseTimeMs);
    }

    public synchronized String scoreLine() {
        return name + "|score=" + score + "|totalTimeMs=" + totalResponseTimeMs;
    }

    /** Clears cumulative stats before a new match (same players, fresh scoreboard). */
    public synchronized void resetMatchStats() {
        score = 0;
        totalResponseTimeMs = 0;
    }
}
