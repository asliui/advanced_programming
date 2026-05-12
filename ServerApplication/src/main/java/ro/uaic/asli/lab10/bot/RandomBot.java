package ro.uaic.asli.lab10.bot;

import ro.uaic.asli.lab10.model.Question;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Chooses a random option A–D.
 */
public final class RandomBot implements BotStrategy {

    private static final List<Character> OPTIONS = List.of('A', 'B', 'C', 'D');

    @Override
    public char chooseOption(Question question) {
        return OPTIONS.get(ThreadLocalRandom.current().nextInt(4));
    }
}
