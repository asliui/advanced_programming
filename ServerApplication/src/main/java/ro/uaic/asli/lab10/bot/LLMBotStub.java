package ro.uaic.asli.lab10.bot;

import ro.uaic.asli.lab10.model.Question;

/**
 * Stub for an LLM-backed player: no API key, no HTTP calls. Replace {@link #chooseOption(Question)} with a real client later.
 */
public final class LLMBotStub implements BotStrategy {

    public enum Difficulty {
        EASY,
        MEDIUM,
        HARD
    }

    private final Difficulty difficulty;

    public LLMBotStub(Difficulty difficulty) {
        this.difficulty = difficulty == null ? Difficulty.MEDIUM : difficulty;
    }

    @Override
    public char chooseOption(Question question) {
        int h = Math.max(1, question.getText().hashCode());
        return switch (h % 4) {
            case 0 -> 'A';
            case 1 -> 'B';
            case 2 -> 'C';
            default -> 'D';
        };
    }

    @Override
    public long thinkDelayMs() {
        return switch (difficulty) {
            case EASY -> 80L;
            case MEDIUM -> 250L;
            case HARD -> 700L;
        };
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }
}
