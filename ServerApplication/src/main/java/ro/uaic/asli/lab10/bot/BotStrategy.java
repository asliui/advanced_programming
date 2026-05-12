package ro.uaic.asli.lab10.bot;

import ro.uaic.asli.lab10.model.Question;

/**
 * Bot decision contract. {@link RandomBot}, {@link KnowledgeBaseBot}, and {@link LLMBotStub} are the implementations.
 */
public sealed interface BotStrategy permits RandomBot, KnowledgeBaseBot, LLMBotStub {

    char chooseOption(Question question);

    /** Extra simulated “thinking” time for demos (LLM stub uses non-zero values). */
    default long thinkDelayMs() {
        return 0L;
    }
}
