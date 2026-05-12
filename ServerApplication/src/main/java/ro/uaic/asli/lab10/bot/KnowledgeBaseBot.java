package ro.uaic.asli.lab10.bot;

import ro.uaic.asli.lab10.model.Question;
import ro.uaic.asli.lab10.repository.KnowledgeBaseRepository;

/**
 * Exact-match lookup in {@code knowledge-base.txt}; falls back to {@link RandomBot} when unknown.
 */
public final class KnowledgeBaseBot implements BotStrategy {

    private final KnowledgeBaseRepository kb;
    private final RandomBot random = new RandomBot();

    public KnowledgeBaseBot(KnowledgeBaseRepository kb) {
        this.kb = kb;
    }

    @Override
    public char chooseOption(Question question) {
        Character known = kb.lookupExactQuestion(question.getText());
        if (known != null && isValid(known)) {
            return known;
        }
        return random.chooseOption(question);
    }

    private static boolean isValid(char c) {
        return c == 'A' || c == 'B' || c == 'C' || c == 'D';
    }
}
