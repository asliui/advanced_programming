package ro.uaic.asli.lab9.concurrent;

import ro.uaic.asli.lab9.view.MazeConsoleRenderer;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Daemon thread: prints status periodically, tracks elapsed time, enforces optional time limit.
 */
public final class ManagerThread extends Thread {

    private final GameController controller;
    private final long printIntervalMs;
    private final long timeLimitMs;
    private final long startedNs;
    private final AtomicLong lastPrintNs = new AtomicLong(System.nanoTime());
    private final boolean useDetailedAscii;

    public ManagerThread(GameController controller, long printIntervalMs, long timeLimitMs, boolean useDetailedAscii) {
        super("Lab9-Manager");
        this.controller = controller;
        this.printIntervalMs = Math.max(200, printIntervalMs);
        this.timeLimitMs = timeLimitMs;
        this.useDetailedAscii = useDetailedAscii;
        this.startedNs = System.nanoTime();
        setDaemon(true);
    }

    @Override
    public void run() {
        try {
            while (controller.isGameRunning()) {
                Thread.sleep(50);
                long now = System.nanoTime();
                if ((now - lastPrintNs.get()) / 1_000_000L < printIntervalMs) {
                    continue;
                }
                lastPrintNs.set(now);
                long elapsedMs = (now - startedNs) / 1_000_000L;
                printSnapshot(elapsedMs);
                if (timeLimitMs > 0L && elapsedMs >= timeLimitMs) {
                    controller.stopGame(GameState.TIME_LIMIT, "Time limit " + timeLimitMs + " ms exceeded");
                    break;
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void printSnapshot(long elapsedMs) {
        synchronized (System.out) {
            System.out.println("----- Lab 9 snapshot (" + elapsedMs + " ms) state=" + controller.getState() + " -----");
            String ascii = useDetailedAscii
                    ? MazeConsoleRenderer.renderWithInteriorWalls(controller)
                    : MazeConsoleRenderer.render(controller, true);
            System.out.print(ascii);
            System.out.println("------------------------------------------------------------------");
        }
    }
}
