package ro.uaic.asli.lab10.app;

import ro.uaic.asli.lab10.game.CompulsoryGameSession;
import ro.uaic.asli.lab10.game.HomeworkGameSession;
import ro.uaic.asli.lab10.game.Lab10SessionFactory;
import ro.uaic.asli.lab10.server.GameServer;

import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Shared bootstrap for Lab 10 server tiers (same pattern as {@link ro.uaic.asli.lab9.app.Lab9Launcher}).
 */
public final class Lab10Launcher {

    private Lab10Launcher() {
    }

    public static void launch(Lab10Mode mode) throws Exception {
        int port = Integer.getInteger("quiz.port", 5555);
        String questionsPath = System.getProperty("quiz.questions");
        Path questions = questionsPath != null ? Path.of(questionsPath) : null;
        String kbPath = System.getProperty("quiz.kb");
        Path kb = kbPath != null ? Path.of(kbPath) : null;

        ExecutorService virtualThreads = null;
        if (mode == Lab10Mode.ADVANCED) {
            if (Runtime.version().feature() >= 21) {
                virtualThreads = Executors.newVirtualThreadPerTaskExecutor();
                System.out.println("Lab 10 ADVANCED: network executor = virtual threads (Java " + Runtime.version().feature() + ").");
            } else {
                System.err.println("Lab 10 ADVANCED needs Java 21+ for virtual threads; using homework thread pool.");
            }
        }

        Lab10SessionFactory factory = switch (mode) {
            case COMPULSORY -> CompulsoryGameSession::new;
            case HOMEWORK, ADVANCED -> HomeworkGameSession::new;
        };

        String tierLabel = switch (mode) {
            case COMPULSORY -> "COMPULSORY (echo + stop)";
            case HOMEWORK -> "HOMEWORK (full quiz)";
            case ADVANCED -> "ADVANCED (full quiz + virtual threads when available)";
        };
        System.out.println("Starting Lab 10 server - " + tierLabel);

        GameServer server = new GameServer(port, questions, kb, virtualThreads, factory);
        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdownGracefully, "lab10-shutdown-hook"));
        server.start();
    }
}
