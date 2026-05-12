package ro.uaic.asli.lab10.game;

import ro.uaic.asli.lab10.model.Question;
import ro.uaic.asli.lab10.repository.QuestionRepository;

import java.util.List;

/**
 * Small OOP wrapper around the ordered list of questions (progression through the quiz).
 */
public final class QuizGame {

    private final List<Question> questions;
    private int cursor = -1;

    public QuizGame(QuestionRepository repository) {
        this.questions = repository.getAll();
    }

    public boolean hasNext() {
        return cursor + 1 < questions.size();
    }

    public Question nextQuestion() {
        if (!hasNext()) {
            throw new IllegalStateException("No more questions");
        }
        cursor++;
        return questions.get(cursor);
    }

    public int getCurrentIndex() {
        return cursor;
    }

    public int getQuestionCount() {
        return questions.size();
    }
}
