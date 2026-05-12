package ro.uaic.asli.lab10.client.ui;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Turns raw server protocol lines into ASCII-only, human-readable terminal text.
 * Networking payloads are unchanged; only presentation is formatted here.
 */
public final class MessageFormatter {

    public static final boolean ENABLE_COLORS = false;

    private static final String SEP_EQ = "==================================================";
    private static final String SEP_DASH = "--------------------------------------------------";

    /** After {@code SCOREBOARD|...}, following {@code ROW|...} lines belong to one table. */
    private boolean inScoreboardTable;

    public String format(String rawLine) {
        if (rawLine == null) {
            return "";
        }
        String line = rawLine.trim();
        if (line.isEmpty()) {
            return line;
        }

        StringBuilder prefix = new StringBuilder();
        if (inScoreboardTable && !line.startsWith("ROW|")) {
            prefix.append(SEP_DASH).append('\n');
            inScoreboardTable = false;
        }

        if (line.startsWith("ROW|")) {
            if (inScoreboardTable) {
                return prefix.append(formatScoreboardRow(line)).toString();
            }
            return prefix.length() > 0 ? prefix.append(line).toString() : line;
        }

        if (line.startsWith("QUESTION|")) {
            return prefix + formatQuestion(line);
        }
        if (line.startsWith("ROUND_RESULT|")) {
            return prefix + formatRoundResult(line);
        }
        if (line.startsWith("SCOREBOARD|")) {
            inScoreboardTable = true;
            return prefix + formatScoreboardHeader();
        }
        if (line.startsWith("MATCH|")) {
            return prefix + formatMatch(line);
        }
        if (line.startsWith("OK|")) {
            return prefix + formatOk(line);
        }
        if (line.startsWith("EVENT|")) {
            return prefix + formatEvent(line);
        }
        if (line.startsWith("ERROR|")) {
            return prefix + formatError(line);
        }
        if (line.startsWith("WINNER|")) {
            return prefix + formatWinner(line);
        }
        return prefix.length() > 0 ? prefix + line : line;
    }

    private static String formatQuestion(String line) {
        Map<String, String> kv = parsePipeKeyValues(line.substring("QUESTION|".length()));
        String id = kv.getOrDefault("id", "?");
        String text = kv.getOrDefault("text", "");
        String blitzMs = kv.getOrDefault("blitzMs", "");
        long blitz = parseLongSafe(blitzMs, 0);

        StringBuilder sb = new StringBuilder();
        sb.append(SEP_EQ).append('\n');
        sb.append("QUESTION ").append(id).append('\n');
        sb.append(SEP_EQ).append('\n');
        sb.append(text).append("\n\n");
        appendOption(sb, "A", kv.get("A"));
        appendOption(sb, "B", kv.get("B"));
        appendOption(sb, "C", kv.get("C"));
        appendOption(sb, "D", kv.get("D"));
        sb.append('\n');
        if (blitz > 0) {
            sb.append("Time limit: ").append(formatSecondsOneDecimal(blitz)).append(" seconds\n");
        }
        sb.append('\n');
        sb.append("Command format:\n");
        sb.append("  answer <questionId> <optionLetter>\n");
        sb.append("Example:\n");
        sb.append("  answer ").append(id).append(" A/B/C/D\n");
        sb.append(SEP_DASH);
        return maybeColorize(sb.toString());
    }

    private static void appendOption(StringBuilder sb, String letter, String value) {
        if (value != null && !value.isEmpty()) {
            sb.append(letter).append(") ").append(value).append('\n');
        }
    }

    private static String formatRoundResult(String line) {
        String[] parts = line.split("\\|");
        if (parts.length < 2) {
            return line;
        }
        int qId = parseIntAfterEquals(parts[1], -1);

        List<String> blocks = new ArrayList<>();
        blocks.add("ROUND RESULT");
        blocks.add(SEP_DASH);
        blocks.add("Question " + (qId >= 0 ? qId : "?"));

        String correctAnswer = "";
        int i = 2;
        boolean firstPlayerBlock = true;

        while (i < parts.length) {
            String seg = parts[i];

            if (seg.startsWith("correct=")) {
                correctAnswer = seg.substring("correct=".length());
                i++;
                continue;
            }

            int eq = seg.indexOf('=');
            if (eq <= 0) {
                i++;
                continue;
            }
            String player = seg.substring(0, eq);
            String status = seg.substring(eq + 1);

            if (!firstPlayerBlock) {
                blocks.add("");
            }
            firstPlayerBlock = false;

            if ("TIMEOUT".equals(status)) {
                long timeMs = 0;
                if (i + 1 < parts.length && parts[i + 1].startsWith("timeMs=")) {
                    timeMs = parseLongAfterEquals(parts[i + 1], 0);
                    i += 2;
                } else {
                    i++;
                }
                blocks.add(player + " answered: (no answer)");
                blocks.add("Result: TIMEOUT");
                blocks.add("[TIMEOUT] Time expired.");
                if (timeMs > 0) {
                    blocks.add("Response time: " + formatSecondsOneDecimal(timeMs) + " seconds");
                }
                continue;
            }

            String picked = "";
            long timeMs = 0;
            if (i + 1 < parts.length && parts[i + 1].startsWith("picked=")) {
                picked = parts[i + 1].substring("picked=".length());
            }
            if (i + 2 < parts.length && parts[i + 2].startsWith("timeMs=")) {
                timeMs = parseLongAfterEquals(parts[i + 2], 0);
            }
            blocks.add(player + " answered: " + (picked.isEmpty() ? "(unknown)" : picked));
            blocks.add("Result: " + status);
            if (timeMs > 0) {
                blocks.add("Response time: " + formatSecondsOneDecimal(timeMs) + " seconds");
            }
            i += 3;
        }

        if (!correctAnswer.isEmpty()) {
            blocks.add("");
            blocks.add("Correct answer: " + correctAnswer);
        }
        blocks.add(SEP_DASH);
        return String.join("\n", blocks);
    }

    private String formatScoreboardHeader() {
        StringBuilder sb = new StringBuilder();
        sb.append("SCOREBOARD\n");
        sb.append(SEP_DASH).append('\n');
        sb.append(String.format(Locale.US, "%-6s %-14s %-7s %s%n", "Rank", "Player", "Score", "Total Time"));
        return sb.toString();
    }

    private static String formatScoreboardRow(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length < 5 || !"ROW".equals(parts[0])) {
            return line;
        }
        int rank = parseIntAfterEquals(parts[1], -1);
        String name = parts[2];
        int score = parseIntAfterEquals(parts[3], -1);
        long totalMs = parseLongAfterEquals(parts[4], -1);
        String rankS = rank >= 0 ? String.valueOf(rank) : "?";
        String scoreS = score >= 0 ? String.valueOf(score) : "?";
        String timeS = totalMs >= 0 ? formatSecondsOneDecimal(totalMs) + " sec" : "?";
        return String.format(Locale.US, "%-6s %-14s %-7s %s", rankS, truncate(name, 14), scoreS, timeS);
    }

    private static String formatMatch(String line) {
        String payload = line.substring("MATCH|".length());
        String[] tokens = payload.split("\\|");
        String kind = tokens.length > 0 ? tokens[0] : "";

        if ("start".equalsIgnoreCase(kind)) {
            Map<String, String> kv = new LinkedHashMap<>();
            for (int i = 1; i < tokens.length; i++) {
                int eq = tokens[i].indexOf('=');
                if (eq > 0) {
                    kv.put(tokens[i].substring(0, eq), tokens[i].substring(eq + 1));
                }
            }
            int players = parseIntSafe(kv.get("players"), -1);
            int questions = parseIntSafe(kv.get("questions"), -1);
            long blitzMs = parseLongSafe(kv.get("blitzMs"), 0);

            StringBuilder sb = new StringBuilder();
            sb.append("MATCH STARTED\n");
            sb.append(SEP_DASH).append('\n');
            if (players >= 0) {
                sb.append("Players: ").append(players).append('\n');
            }
            if (questions >= 0) {
                sb.append("Questions: ").append(questions).append('\n');
            }
            if (blitzMs > 0) {
                sb.append("Time per question: ").append(formatSecondsOneDecimal(blitzMs)).append(" seconds\n");
            }
            sb.append(SEP_DASH);
            return sb.toString();
        }
        return line;
    }

    private static String formatOk(String line) {
        String rest = line.substring("OK|".length());
        if (rest.startsWith("joined as ")) {
            return "[OK] Joined match as " + rest.substring("joined as ".length());
        }
        if (rest.startsWith("botAdded|")) {
            String[] p = rest.split("\\|");
            String bot = p.length > 1 ? p[1] : "?";
            String kind = "";
            if (p.length > 2 && p[2].startsWith("kind=")) {
                kind = p[2].substring("kind=".length());
            }
            return "[OK] Bot added: " + bot + (kind.isEmpty() ? "" : " (kind: " + kind + ")");
        }
        if ("matchStarting".equals(rest)) {
            return "[OK] Match starting";
        }
        if ("answerRecorded".equals(rest)) {
            return "[OK] Answer recorded";
        }
        if ("bye".equals(rest)) {
            return "[OK] Bye";
        }
        if ("quit".equals(rest)) {
            return "[OK] Quit";
        }
        return "[OK] " + rest;
    }

    private static String formatEvent(String line) {
        String rest = line.substring("EVENT|".length());
        if (rest.startsWith("playerJoined=")) {
            return "[EVENT] Player joined: " + rest.substring("playerJoined=".length());
        }
        if (rest.startsWith("playerLeft=")) {
            return "[EVENT] Player left: " + rest.substring("playerLeft=".length());
        }
        if (rest.startsWith("botJoined=")) {
            String sub = rest.substring("botJoined=".length());
            int bar = sub.indexOf('|');
            if (bar > 0) {
                String name = sub.substring(0, bar);
                String kindPart = sub.substring(bar + 1);
                String kind = kindPart.startsWith("kind=") ? kindPart.substring("kind=".length()) : kindPart;
                return "[EVENT] Bot joined: " + name + " (kind: " + kind + ")";
            }
            return "[EVENT] Bot joined: " + sub;
        }
        if ("matchEnded".equals(rest)) {
            return "[EVENT] Match ended";
        }
        return "[EVENT] " + rest;
    }

    private static String formatError(String line) {
        String msg = line.length() > "ERROR|".length() ? line.substring("ERROR|".length()) : "";
        return "[ERROR] " + msg;
    }

    private static String formatWinner(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length >= 2 && "none".equals(parts[1])) {
            StringBuilder sb = new StringBuilder();
            sb.append("WINNER\n");
            sb.append(SEP_DASH).append('\n');
            sb.append("No single winner (tie or no players).\n");
            sb.append(SEP_DASH);
            return sb.toString();
        }
        if (parts.length >= 4) {
            String name = parts[1];
            int score = parseIntAfterEquals(parts[2], -1);
            long totalMs = parseLongAfterEquals(parts[3], -1);
            StringBuilder sb = new StringBuilder();
            sb.append("WINNER\n");
            sb.append(SEP_DASH).append('\n');
            sb.append("Winner: ").append(name).append('\n');
            if (score >= 0) {
                sb.append("Score: ").append(score).append('\n');
            }
            if (totalMs >= 0) {
                sb.append("Total time: ").append(formatSecondsOneDecimal(totalMs)).append(" seconds\n");
            }
            sb.append(SEP_DASH);
            return sb.toString();
        }
        return line;
    }

    private static Map<String, String> parsePipeKeyValues(String tail) {
        Map<String, String> map = new LinkedHashMap<>();
        int pos = 0;
        while (pos < tail.length()) {
            int nextPipe = tail.indexOf('|', pos);
            String segment = nextPipe < 0 ? tail.substring(pos) : tail.substring(pos, nextPipe);
            int eq = segment.indexOf('=');
            if (eq > 0) {
                String key = segment.substring(0, eq);
                String val = segment.substring(eq + 1);
                map.put(key, val);
            }
            if (nextPipe < 0) {
                break;
            }
            pos = nextPipe + 1;
        }
        return map;
    }

    private static int parseIntAfterEquals(String segment, int def) {
        int eq = segment.indexOf('=');
        if (eq < 0) {
            return def;
        }
        return parseIntSafe(segment.substring(eq + 1), def);
    }

    private static long parseLongAfterEquals(String segment, long def) {
        int eq = segment.indexOf('=');
        if (eq < 0) {
            return def;
        }
        return parseLongSafe(segment.substring(eq + 1), def);
    }

    private static int parseIntSafe(String s, int def) {
        if (s == null || s.isEmpty()) {
            return def;
        }
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private static long parseLongSafe(String s, long def) {
        if (s == null || s.isEmpty()) {
            return def;
        }
        try {
            return Long.parseLong(s.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private static String formatSecondsOneDecimal(long ms) {
        double sec = ms / 1000.0;
        return String.format(Locale.US, "%.1f", sec);
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return "";
        }
        if (s.length() <= max) {
            return s;
        }
        return s.substring(0, Math.max(0, max - 3)) + "...";
    }

    /**
     * ANSI coloring hook; disabled by default ({@link #ENABLE_COLORS}).
     */
    private static String maybeColorize(String text) {
        if (!ENABLE_COLORS) {
            return text;
        }
        return text;
    }
}
