package ro.uaic.asli.lab10.game;

import ro.uaic.asli.lab10.bot.BotStrategy;
import ro.uaic.asli.lab10.bot.KnowledgeBaseBot;
import ro.uaic.asli.lab10.bot.LLMBotStub;
import ro.uaic.asli.lab10.bot.RandomBot;
import ro.uaic.asli.lab10.persistence.audit.Lab11AuditContextHolder;
import ro.uaic.asli.lab10.persistence.service.QuizGamePersistenceService;
import ro.uaic.asli.lab10.command.CommandParser;
import ro.uaic.asli.lab10.model.Player;
import ro.uaic.asli.lab10.model.Question;
import ro.uaic.asli.lab10.repository.KnowledgeBaseRepository;
import ro.uaic.asli.lab10.repository.QuestionRepository;
import ro.uaic.asli.lab10.server.ClientConnection;
import ro.uaic.asli.lab10.server.GameServer;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Lab 10 homework tier: multiplayer quiz, blitz timing, thread-pool-driven bots, scoreboard.
 * <p>
 * Mutating methods are {@code synchronized} on this instance so socket workers and bot tasks stay consistent.
 */
public final class HomeworkGameSession implements Lab10Session {

    public static final int BLITZ_MS = 15_000;

    private final GameServer server;
    private final QuestionRepository questionRepository;
    private final KnowledgeBaseRepository knowledgeBase;
    private final QuizGamePersistenceService persistence;

    private final List<Player> players = new ArrayList<>();
    private final Set<String> takenNames = new HashSet<>();

    private volatile boolean matchRunning = false;
    private volatile boolean serverShutdown = false;

    private QuizGame quizGame;
    private Question currentQuestion;
    private long roundStartMs;
    private long roundDeadlineMs;

    private final Map<String, Character> chosenOption = new HashMap<>();
    private final Map<String, Long> answerAtMs = new HashMap<>();

    /**
     * Loads question and knowledge-base files from the given paths (or classpath defaults when {@code null}).
     */
    public HomeworkGameSession(GameServer server, Path questionsOverride, Path knowledgeBaseOverride) {
        this(server, questionsOverride, knowledgeBaseOverride, null);
    }

    public HomeworkGameSession(
            GameServer server,
            Path questionsOverride,
            Path knowledgeBaseOverride,
            QuizGamePersistenceService persistence
    ) {
        this.server = server;
        this.questionRepository = QuestionRepository.load(questionsOverride);
        this.knowledgeBase = KnowledgeBaseRepository.load(knowledgeBaseOverride);
        this.persistence = persistence;
    }

    @Override
    public synchronized void onServerShutdown() {
        serverShutdown = true;
        notifyAll();
    }

    @Override
    public synchronized void onClientDisconnected(ClientConnection connection) {
        String name = connection.getPlayerName();
        if (name == null) {
            return;
        }
        boolean removed = players.removeIf(p -> p.getName().equals(name) && !p.isBot());
        takenNames.remove(name);
        if (removed) {
            broadcastLine("EVENT|playerLeft=" + name);
        }
        notifyAll();
    }

    @Override
    public void broadcastLine(String message) {
        for (String line : message.split("\n")) {
            if (line.isEmpty()) {
                continue;
            }
            for (ClientConnection c : server.getClientsSnapshot()) {
                c.sendLine(line);
            }
        }
    }

    @Override
    public synchronized void handleIncomingLine(ClientConnection connection, String rawLine) {
        if (serverShutdown) {
            return;
        }

        String line = rawLine == null ? "" : rawLine.trim();
        if (line.isEmpty()) {
            return;
        }

        String verb = CommandParser.verb(line);

        if ("stop".equals(verb)) {
            connection.sendLine("Server stopped");
            server.initiateShutdownFromCommand();
            return;
        }

        try {
            switch (verb) {
                case "join" -> handleJoin(connection, line);
                case "joinbot" -> handleJoinBot(connection, line);
                case "start" -> handleStart(connection);
                case "answer" -> handleAnswer(connection, line);
                case "score" -> handleScore(connection);
                case "quit" -> handleQuit(connection);
                default -> connection.sendLine("Server received the request: " + line);
            }
        } catch (IllegalArgumentException ex) {
            connection.sendLine("ERROR|" + ex.getMessage());
        } catch (IllegalStateException ex) {
            connection.sendLine("ERROR|" + ex.getMessage());
        }
    }

    private void handleJoin(ClientConnection connection, String line) {
        if (matchRunning) {
            connection.sendLine("ERROR|Cannot join while a match is running.");
            return;
        }
        String name = line.substring("join".length()).trim();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Usage: join <playerName>");
        }
        synchronized (this) {
            if (takenNames.contains(name)) {
                connection.sendLine("ERROR|Name already taken: " + name);
                return;
            }
            players.add(Player.human(name));
            takenNames.add(name);
            connection.setPlayerName(name);
        }
        if (persistence != null) {
            try {
                Lab11AuditContextHolder.setPrincipal(name);
                persistence.registerParticipant(name);
            } finally {
                Lab11AuditContextHolder.clear();
            }
        }
        connection.sendLine("OK|joined as " + name);
        broadcastLine("EVENT|playerJoined=" + name);
    }

    private void handleJoinBot(ClientConnection connection, String line) {
        if (matchRunning) {
            connection.sendLine("ERROR|Cannot add bots while a match is running.");
            return;
        }
        List<String> tok = CommandParser.tokens(line);
        if (tok.size() < 3) {
            throw new IllegalArgumentException("Usage: joinbot random|kb|llm <botName> [easy|medium|hard]");
        }
        String kind = tok.get(1).toLowerCase(Locale.ROOT);
        String botName = tok.get(2);
        BotStrategy strategy = switch (kind) {
            case "random" -> new RandomBot();
            case "kb" -> new KnowledgeBaseBot(knowledgeBase);
            case "llm" -> {
                LLMBotStub.Difficulty d = LLMBotStub.Difficulty.MEDIUM;
                if (tok.size() >= 4) {
                    d = LLMBotStub.Difficulty.valueOf(tok.get(3).toUpperCase(Locale.ROOT));
                }
                yield new LLMBotStub(d);
            }
            default -> throw new IllegalArgumentException("Unknown bot kind: " + kind);
        };

        synchronized (this) {
            if (takenNames.contains(botName)) {
                throw new IllegalArgumentException("Name already taken: " + botName);
            }
            players.add(Player.bot(botName, strategy));
            takenNames.add(botName);
        }
        if (persistence != null) {
            try {
                Lab11AuditContextHolder.setPrincipal(botName);
                persistence.registerParticipant(botName);
            } finally {
                Lab11AuditContextHolder.clear();
            }
        }
        connection.sendLine("OK|botAdded|" + botName + "|kind=" + kind);
        broadcastLine("EVENT|botJoined=" + botName + "|kind=" + kind);
    }

    private void handleStart(ClientConnection connection) {
        synchronized (this) {
            if (matchRunning) {
                connection.sendLine("ERROR|Match already running.");
                return;
            }
            if (players.isEmpty()) {
                connection.sendLine("ERROR|No players joined yet.");
                return;
            }
            matchRunning = true;
        }

        server.getGameDirector().execute(() -> {
            try {
                runFullMatch();
            } catch (Exception e) {
                broadcastLine("ERROR|matchFailed|" + e.getMessage());
            } finally {
                synchronized (HomeworkGameSession.this) {
                    matchRunning = false;
                    quizGame = null;
                    currentQuestion = null;
                    chosenOption.clear();
                    answerAtMs.clear();
                }
                broadcastLine("EVENT|matchEnded");
            }
        });

        connection.sendLine("OK|matchStarting");
    }

    private void runFullMatch() {
        synchronized (this) {
            for (Player p : players) {
                p.resetMatchStats();
            }
        }

        QuizGame game = new QuizGame(questionRepository);
        synchronized (this) {
            this.quizGame = game;
        }

        QuizGamePersistenceService.MatchHandle matchHandle = null;
        if (persistence != null) {
            try {
                matchHandle = persistence.beginMatch(game.getQuestionsSnapshot());
            } catch (RuntimeException ex) {
                broadcastLine("ERROR|persistenceBeginFailed|" + ex.getMessage());
                return;
            }
        }

        int playerCount;
        synchronized (this) {
            playerCount = players.size();
        }

        broadcastLine("MATCH|start|players=" + playerCount + "|questions=" + game.getQuestionCount()
                + "|blitzMs=" + BLITZ_MS);

        while (true) {
            final Question q;
            synchronized (this) {
                if (!game.hasNext()) {
                    break;
                }
                q = game.nextQuestion();
                beginRoundLocked(q);
            }

            broadcastQuestionLine(q, roundStartMs, roundDeadlineMs);
            scheduleBotsForRound(q);

            synchronized (this) {
                try {
                    waitForRoundEndLocked();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    broadcastLine("ERROR|matchInterrupted");
                    return;
                }
                String summary = finalizeRoundLocked(q);
                broadcastLine(summary);
                broadcastLine(ScoreBoard.renderTable(players));
            }
        }

        Optional<Player> w = ScoreBoard.winner(players);
        w.ifPresent(player -> broadcastLine("WINNER|" + player.scoreLine()));
        if (w.isEmpty()) {
            broadcastLine("WINNER|none");
        }

        if (persistence != null && matchHandle != null) {
            List<Player> snapshot;
            int totalRounds = game.getQuestionCount();
            synchronized (this) {
                snapshot = List.copyOf(players);
            }
            try {
                persistence.completeMatch(matchHandle, snapshot, totalRounds);
            } catch (RuntimeException ex) {
                broadcastLine("ERROR|persistenceSaveFailed|" + ex.getMessage());
            }
        }
    }

    private synchronized void beginRoundLocked(Question q) {
        currentQuestion = q;
        chosenOption.clear();
        answerAtMs.clear();
        roundStartMs = System.currentTimeMillis();
        roundDeadlineMs = roundStartMs + BLITZ_MS;
    }

    private void broadcastQuestionLine(Question q, long startMs, long deadlineMs) {
        String a = q.getOptions().get(0);
        String b = q.getOptions().get(1);
        String c = q.getOptions().get(2);
        String d = q.getOptions().get(3);
        broadcastLine("QUESTION|id=" + q.getId()
                + "|text=" + sanitize(q.getText())
                + "|A=" + sanitize(a)
                + "|B=" + sanitize(b)
                + "|C=" + sanitize(c)
                + "|D=" + sanitize(d)
                + "|startMs=" + startMs
                + "|deadlineMs=" + deadlineMs
                + "|blitzMs=" + BLITZ_MS);
    }

    private static String sanitize(String s) {
        return s.replace("|", "/").replace("\n", " ");
    }

    private void scheduleBotsForRound(Question q) {
        List<Player> snapshot;
        synchronized (this) {
            snapshot = List.copyOf(players);
        }

        for (Player p : snapshot) {
            if (!p.isBot()) {
                continue;
            }
            server.getNetworkExecutor().execute(() -> {
                try {
                    long delayMs = ThreadLocalRandom.current().nextInt(150, 1200);
                    delayMs += p.getBotStrategy().orElseThrow().thinkDelayMs();
                    Thread.sleep(delayMs);

                    char choice = p.getBotStrategy().orElseThrow().chooseOption(q);
                    synchronized (HomeworkGameSession.this) {
                        if (serverShutdown || currentQuestion == null || currentQuestion.getId() != q.getId()) {
                            return;
                        }
                        applyAnswerLocked(p.getName(), choice, System.currentTimeMillis(), q.getId());
                        HomeworkGameSession.this.notifyAll();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
    }

    private synchronized void waitForRoundEndLocked() throws InterruptedException {
        while (System.currentTimeMillis() < roundDeadlineMs && !allPlayersAnsweredLocked()) {
            long wait = Math.min(200, roundDeadlineMs - System.currentTimeMillis());
            if (wait > 0) {
                wait(wait);
            }
        }
    }

    private boolean allPlayersAnsweredLocked() {
        for (Player p : players) {
            if (!chosenOption.containsKey(p.getName())) {
                return false;
            }
        }
        return true;
    }

    private String finalizeRoundLocked(Question q) {
        StringBuilder sb = new StringBuilder();
        sb.append("ROUND_RESULT|qId=").append(q.getId());

        for (Player p : players) {
            Character opt = chosenOption.get(p.getName());
            Long when = answerAtMs.get(p.getName());
            if (opt == null || when == null) {
                long timeMs = BLITZ_MS;
                p.addResult(false, timeMs);
                sb.append("|").append(p.getName()).append("=TIMEOUT|timeMs=").append(timeMs);
                continue;
            }

            boolean late = when > roundDeadlineMs;
            long elapsed = Math.min(BLITZ_MS, Math.max(0, when - roundStartMs));
            boolean correct = !late && Character.toUpperCase(opt) == q.getCorrectOption();
            long countedTime = late ? BLITZ_MS : elapsed;
            p.addResult(correct, countedTime);

            String status = late ? "LATE" : (correct ? "CORRECT" : "WRONG");
            sb.append("|").append(p.getName()).append("=").append(status)
                    .append("|picked=").append(Character.toUpperCase(opt))
                    .append("|timeMs=").append(countedTime);
        }
        sb.append("|correct=").append(q.getCorrectOption());
        currentQuestion = null;
        return sb.toString();
    }

    private void handleScore(ClientConnection connection) {
        for (String line : ScoreBoard.renderTable(players).split("\n")) {
            if (!line.isEmpty()) {
                connection.sendLine(line);
            }
        }
    }

    private void handleQuit(ClientConnection connection) {
        String name = connection.getPlayerName();
        if (name == null) {
            connection.sendLine("OK|bye");
            connection.closeQuietly();
            return;
        }
        synchronized (this) {
            players.removeIf(pl -> pl.getName().equals(name) && !pl.isBot());
            takenNames.remove(name);
        }
        connection.setPlayerName(null);
        connection.sendLine("OK|quit");
        connection.closeQuietly();
    }

    private void handleAnswer(ClientConnection connection, String line) {
        String playerName = connection.getPlayerName();
        if (playerName == null) {
            connection.sendLine("ERROR|Join first: join <name>");
            return;
        }

        List<String> tok = CommandParser.tokens(line);
        if (tok.size() != 3) {
            throw new IllegalArgumentException("Usage: answer <questionId> <A|B|C|D>");
        }
        int qid = Integer.parseInt(tok.get(1));
        String letter = tok.get(2).trim();
        if (letter.isEmpty()) {
            throw new IllegalArgumentException("Missing option letter.");
        }
        char opt = Character.toUpperCase(letter.charAt(0));

        long now = System.currentTimeMillis();
        boolean ok;
        synchronized (this) {
            ok = applyAnswerLocked(playerName, opt, now, qid);
            notifyAll();
        }
        connection.sendLine(ok ? "OK|answerRecorded" : "ERROR|answerRejected");
    }

    private boolean applyAnswerLocked(String playerName, char option, long nowMs, int questionId) {
        if (currentQuestion == null || !matchRunning) {
            return false;
        }
        if (currentQuestion.getId() != questionId) {
            return false;
        }
        if (!isAbcd(option)) {
            return false;
        }
        if (chosenOption.containsKey(playerName)) {
            return false;
        }
        chosenOption.put(playerName, option);
        answerAtMs.put(playerName, nowMs);
        return true;
    }

    private static boolean isAbcd(char c) {
        return c == 'A' || c == 'B' || c == 'C' || c == 'D';
    }
}
