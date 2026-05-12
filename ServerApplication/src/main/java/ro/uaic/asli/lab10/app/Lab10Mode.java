package ro.uaic.asli.lab10.app;

public enum Lab10Mode {
    /** Echo server + {@code stop} only (minimal lab spec). */
    COMPULSORY,
    /** Full multiplayer quiz (thread pool, blitz, scoreboard, bots). */
    HOMEWORK,
    /** Same quiz as homework, but each client connection runs on a virtual thread (Java 21+). */
    ADVANCED
}
