package ro.uaic.asli.lab10.repository;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple supervised-learning style knowledge base: exact question text → correct option letter.
 * <p>
 * File format (one mapping per line): {@code QUESTION_TEXT|CORRECT_OPTION}
 */
public final class KnowledgeBaseRepository {

    private final Map<String, Character> questionToAnswer;

    private KnowledgeBaseRepository(Map<String, Character> questionToAnswer) {
        this.questionToAnswer = Map.copyOf(questionToAnswer);
    }

    public Character lookupExactQuestion(String questionText) {
        return questionToAnswer.get(questionText.trim());
    }

    public static KnowledgeBaseRepository load(Path overrideFile) {
        try {
            if (overrideFile != null) {
                return new KnowledgeBaseRepository(parseLines(Files.readAllLines(overrideFile, StandardCharsets.UTF_8)));
            }
            try (InputStream in = KnowledgeBaseRepository.class.getResourceAsStream("/knowledge-base.txt")) {
                if (in == null) {
                    return new KnowledgeBaseRepository(Map.of());
                }
                List<String> lines;
                try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                    lines = br.lines().toList();
                }
                return new KnowledgeBaseRepository(parseLines(lines));
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load knowledge base", e);
        }
    }

    private static Map<String, Character> parseLines(List<String> lines) {
        Map<String, Character> map = new HashMap<>();
        for (String raw : lines) {
            String line = raw.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            int idx = line.lastIndexOf('|');
            if (idx <= 0 || idx == line.length() - 1) {
                continue;
            }
            String q = line.substring(0, idx).trim();
            String ans = line.substring(idx + 1).trim();
            if (ans.isEmpty()) {
                continue;
            }
            map.put(q, Character.toUpperCase(ans.charAt(0)));
        }
        return map;
    }
}
