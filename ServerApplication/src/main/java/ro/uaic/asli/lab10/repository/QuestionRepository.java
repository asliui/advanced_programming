package ro.uaic.asli.lab10.repository;

import ro.uaic.asli.lab10.model.Question;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads questions from {@code questions.txt} using the lab format:
 * {@code QUESTION|OPTION_A|OPTION_B|OPTION_C|OPTION_D|CORRECT_OPTION}
 */
public final class QuestionRepository {

    private final List<Question> questions;

    private QuestionRepository(List<Question> questions) {
        this.questions = List.copyOf(questions);
    }

    public List<Question> getAll() {
        return questions;
    }

    public static QuestionRepository load(Path overrideFile) {
        try {
            if (overrideFile != null) {
                return new QuestionRepository(parseLines(Files.readAllLines(overrideFile, StandardCharsets.UTF_8)));
            }
            try (InputStream in = QuestionRepository.class.getResourceAsStream("/questions.txt")) {
                if (in == null) {
                    throw new IllegalStateException("Missing classpath resource /questions.txt");
                }
                List<String> lines = new ArrayList<>();
                try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                    String l;
                    while ((l = br.readLine()) != null) {
                        lines.add(l);
                    }
                }
                return new QuestionRepository(parseLines(lines));
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load questions", e);
        }
    }

    private static List<Question> parseLines(List<String> lines) {
        List<Question> out = new ArrayList<>();
        int id = 1;
        for (String raw : lines) {
            String line = raw.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            String[] parts = line.split("\\|", -1);
            if (parts.length != 6) {
                throw new IllegalArgumentException("Bad question line (expected 6 fields): " + line);
            }
            String q = parts[0].trim();
            List<String> opts = List.of(parts[1].trim(), parts[2].trim(), parts[3].trim(), parts[4].trim());
            char correct = parts[5].trim().charAt(0);
            out.add(new Question(id++, q, opts, correct));
        }
        if (out.isEmpty()) {
            throw new IllegalArgumentException("No questions found");
        }
        return out;
    }
}
