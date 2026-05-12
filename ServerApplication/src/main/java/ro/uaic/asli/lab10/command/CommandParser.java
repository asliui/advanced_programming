package ro.uaic.asli.lab10.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Parses one line of text into a verb + arguments (simple university-lab style parser).
 */
public final class CommandParser {

    private CommandParser() {
    }

    public static String firstToken(String line) {
        String trimmed = line.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        int sp = trimmed.indexOf(' ');
        return sp < 0 ? trimmed : trimmed.substring(0, sp);
    }

    public static List<String> tokens(String line) {
        String[] parts = line.trim().split("\\s+");
        List<String> out = new ArrayList<>();
        for (String p : parts) {
            if (!p.isEmpty()) {
                out.add(p);
            }
        }
        return out;
    }

    public static String verb(String line) {
        return firstToken(line).toLowerCase(Locale.ROOT);
    }
}
