package ro.uaic.asli.lab9.actors;

import ro.uaic.asli.lab9.concurrent.GameController;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Base for {@link Runnable} actors: configurable delay, pause/resume via wait/notify, and stop flag.
 */
public abstract class PausableActor implements Runnable {

    private final int id;
    private final String name;
    private final GameController controller;

    private final AtomicLong moveDelayMs = new AtomicLong(400);
    private final AtomicBoolean paused = new AtomicBoolean(false);
    private final AtomicBoolean stopped = new AtomicBoolean(false);

    protected PausableActor(int id, String name, GameController controller, long initialDelayMs) {
        this.id = id;
        this.name = name == null ? defaultName(id) : name;
        this.controller = controller;
        this.moveDelayMs.set(Math.max(0, initialDelayMs));
    }

    protected abstract String defaultName(int id);

    public int getId() {
        return id;
    }

    public String getActorName() {
        return name;
    }

    public GameController getController() {
        return controller;
    }

    public long getMoveDelayMs() {
        return moveDelayMs.get();
    }

    public void setMoveDelayMs(long ms) {
        moveDelayMs.set(Math.max(0, ms));
    }

    public boolean isPaused() {
        return paused.get();
    }

    public void pause() {
        paused.set(true);
    }

    public void resume() {
        paused.set(false);
        controller.wakeAllPausedActors();
    }

    public void stopActor() {
        stopped.set(true);
        resume();
    }

    public boolean isStopped() {
        return stopped.get();
    }

    protected void sleepStepDelay() throws InterruptedException {
        long d = moveDelayMs.get();
        if (d > 0) {
            Thread.sleep(d);
        }
    }
}
