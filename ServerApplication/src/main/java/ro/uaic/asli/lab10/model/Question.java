package ro.uaic.asli.lab10.model;

import java.util.List;

/**
 * One multiple-choice question. Correct answer is stored as A/B/C/D (as in {@code questions.txt}).
 */
public final class Question {

    private final int id;
    private final String text;
    private final List<String> options; // size 4: A..D
    private final char correctOption;

    public Question(int id, String text, List<String> options, char correctOption) {
        this.id = id;
        this.text = text;
        this.options = List.copyOf(options);
        this.correctOption = Character.toUpperCase(correctOption);
    }

    public int getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public List<String> getOptions() {
        return options;
    }

    public char getCorrectOption() {
        return correctOption;
    }
}
