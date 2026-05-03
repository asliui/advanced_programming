package ro.uaic.asli.lab9.concurrent;

import ro.uaic.asli.lab9.actors.PausableActor;
import ro.uaic.asli.lab9.model.Maze;
import ro.uaic.asli.lab9.model.Position;
import ro.uaic.asli.lab9.pathfinding.BFSPathFinder;
import ro.uaic.asli.lab9.pathfinding.PathFinder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Central coordinator (game engine): maze rules, occupancy, game outcome, and pause signalling.
 * All position mutations go through synchronized {@link #worldLock} to avoid races.
 */
public final class GameController {

    private final Maze maze;
    private final Position exitPosition;
    private final SharedMemory sharedMemory;
    private final PathFinder pathFinder = new BFSPathFinder();

    private final Object worldLock = new Object();
    private final Object pauseMonitor = new Object();

    private final Map<Integer, Position> robotPositions = new HashMap<>();
    private final Map<Integer, Position> bunnyPositions = new HashMap<>();

    private final AtomicBoolean gameRunning = new AtomicBoolean(true);
    private final AtomicBoolean globalPaused = new AtomicBoolean(false);
    private final AtomicReference<GameState> state = new AtomicReference<>(GameState.RUNNING);

    private final CopyOnWriteArrayList<Thread> actorThreads = new CopyOnWriteArrayList<>();
    private volatile Thread commandListenerThread;
    private volatile ManagerThread managerThread;

    private volatile String endReasonDetail = "";

    public GameController(Maze maze, Position exitPosition, SharedMemory sharedMemory) {
        this.maze = maze;
        this.exitPosition = exitPosition;
        this.sharedMemory = sharedMemory;
    }

    public Maze getMaze() {
        return maze;
    }

    public Position getExitPosition() {
        return exitPosition;
    }

    public SharedMemory getSharedMemory() {
        return sharedMemory;
    }

    public PathFinder getPathFinder() {
        return pathFinder;
    }

    public void registerActorThread(Thread t) {
        if (t != null) {
            actorThreads.add(t);
        }
    }

    public void setCommandListenerThread(Thread commandListenerThread) {
        this.commandListenerThread = commandListenerThread;
    }

    public void setManagerThread(ManagerThread managerThread) {
        this.managerThread = managerThread;
    }

    public boolean isGameRunning() {
        return gameRunning.get() && state.get() == GameState.RUNNING;
    }

    public GameState getState() {
        return state.get();
    }

    public String getEndReasonDetail() {
        return endReasonDetail;
    }

    public void stopGame(GameState reason, String detail) {
        gameRunning.set(false);
        state.set(reason);
        endReasonDetail = detail == null ? "" : detail;
        wakeAllPausedActors();
        interruptCommandListenerIfNeeded();
    }

    private void interruptCommandListenerIfNeeded() {
        Thread t = commandListenerThread;
        if (t != null && t.isAlive()) {
            t.interrupt();
        }
    }

    public void setGlobalPaused(boolean paused) {
        globalPaused.set(paused);
        wakeAllPausedActors();
    }

    public boolean isGlobalPaused() {
        return globalPaused.get();
    }

    public void wakeAllPausedActors() {
        synchronized (pauseMonitor) {
            pauseMonitor.notifyAll();
        }
    }

    /**
     * Blocks the caller while the game is running and either global or actor-specific pause is active.
     */
    public void waitIfPaused(PausableActor actor) throws InterruptedException {
        synchronized (pauseMonitor) {
            while (gameRunning.get()
                    && state.get() == GameState.RUNNING
                    && (globalPaused.get() || actor.isPaused())) {
                pauseMonitor.wait(300);
            }
        }
    }

    public void initRobot(int robotId, Position start) {
        synchronized (worldLock) {
            robotPositions.put(robotId, start);
        }
    }

    public void initBunny(int bunnyId, Position start) {
        synchronized (worldLock) {
            bunnyPositions.put(bunnyId, start);
        }
    }

    public Position getRobotPosition(int robotId) {
        synchronized (worldLock) {
            return robotPositions.get(robotId);
        }
    }

    public Position getBunnyPosition(int bunnyId) {
        synchronized (worldLock) {
            return bunnyPositions.get(bunnyId);
        }
    }

    /**
     * Snapshot of all live bunny positions (for sensing / display).
     */
    public Map<Integer, Position> snapshotBunnyPositions() {
        synchronized (worldLock) {
            return Map.copyOf(bunnyPositions);
        }
    }

    public Map<Integer, Position> snapshotRobotPositions() {
        synchronized (worldLock) {
            return Map.copyOf(robotPositions);
        }
    }

    public MoveResult moveRobot(int robotId, Position from, Position to) {
        synchronized (worldLock) {
            if (state.get() != GameState.RUNNING) {
                return MoveResult.GAME_OVER;
            }
            Position actual = robotPositions.get(robotId);
            if (actual == null || !actual.equals(from)) {
                return MoveResult.BLOCKED;
            }
            if (!maze.canMove(from.row(), from.col(), to.row(), to.col())) {
                return MoveResult.BLOCKED;
            }
            for (Map.Entry<Integer, Position> e : robotPositions.entrySet()) {
                if (e.getKey() != robotId && to.equals(e.getValue())) {
                    return MoveResult.BLOCKED;
                }
            }
            for (Map.Entry<Integer, Position> e : bunnyPositions.entrySet()) {
                // Bunnies standing on the exit are safe (escape achieved for that cell).
                if (exitPosition.equals(e.getValue())) {
                    continue;
                }
                if (to.equals(e.getValue())) {
                    robotPositions.put(robotId, to);
                    bunnyPositions.remove(e.getKey());
                    if (bunnyPositions.isEmpty()) {
                        state.set(GameState.BUNNY_CAUGHT);
                        gameRunning.set(false);
                        endReasonDetail = "Robot " + robotId + " caught last bunny " + e.getKey();
                        wakeAllPausedActors();
                        interruptCommandListenerIfNeeded();
                        return MoveResult.GAME_OVER;
                    }
                    endReasonDetail = "Robot " + robotId + " caught bunny " + e.getKey();
                    return MoveResult.OK;
                }
            }
            robotPositions.put(robotId, to);
            return MoveResult.OK;
        }
    }

    public MoveResult moveBunny(int bunnyId, Position from, Position to) {
        synchronized (worldLock) {
            if (state.get() != GameState.RUNNING) {
                return MoveResult.GAME_OVER;
            }
            Position actual = bunnyPositions.get(bunnyId);
            if (actual == null || !actual.equals(from)) {
                return MoveResult.BLOCKED;
            }
            if (!maze.canMove(from.row(), from.col(), to.row(), to.col())) {
                return MoveResult.BLOCKED;
            }
            for (Map.Entry<Integer, Position> rp : robotPositions.entrySet()) {
                if (to.equals(rp.getValue())) {
                    bunnyPositions.remove(bunnyId);
                    if (bunnyPositions.isEmpty()) {
                        state.set(GameState.BUNNY_CAUGHT);
                        gameRunning.set(false);
                        endReasonDetail = "Bunny " + bunnyId + " stepped onto robot " + rp.getKey();
                        wakeAllPausedActors();
                        interruptCommandListenerIfNeeded();
                        return MoveResult.GAME_OVER;
                    }
                    endReasonDetail = "Bunny " + bunnyId + " stepped onto robot " + rp.getKey();
                    return MoveResult.OK;
                }
            }
            bunnyPositions.put(bunnyId, to);
            if (to.equals(exitPosition)) {
                if (allBunniesAtExit()) {
                    state.set(GameState.BUNNY_ESCAPED);
                    gameRunning.set(false);
                    endReasonDetail = "All bunnies reached the exit";
                    wakeAllPausedActors();
                    interruptCommandListenerIfNeeded();
                }
                return state.get() == GameState.RUNNING ? MoveResult.OK : MoveResult.GAME_OVER;
            }
            return MoveResult.OK;
        }
    }

    private boolean allBunniesAtExit() {
        for (Position p : bunnyPositions.values()) {
            if (!exitPosition.equals(p)) {
                return false;
            }
        }
        return !bunnyPositions.isEmpty();
    }

    /**
     * For multi-bunny mode: win only when every bunny is on exit.
     */
    public boolean allBunniesEscaped() {
        synchronized (worldLock) {
            return allBunniesAtExit();
        }
    }

    /**
     * Graph distance capped at {@code maxInclusive}; empty if unreachable beyond cap.
     */
    public Optional<Integer> graphDistanceLimited(Position from, Position to, int maxInclusive) {
        if (from == null || to == null) {
            return Optional.empty();
        }
        synchronized (worldLock) {
            // read-only on maze; lock keeps consistency with rare maze swaps if ever added
            if (state.get() != GameState.RUNNING && !from.equals(to)) {
                return Optional.empty();
            }
        }
        return bfsDistance(from, to, maxInclusive);
    }

    private Optional<Integer> bfsDistance(Position from, Position to, int maxInclusive) {
        if (from.equals(to)) {
            return Optional.of(0);
        }
        List<Position> frontier = new ArrayList<>();
        frontier.add(from);
        Set<Position> seen = new HashSet<>();
        seen.add(from);
        int dist = 0;
        while (!frontier.isEmpty() && dist < maxInclusive) {
            dist++;
            List<Position> next = new ArrayList<>();
            for (Position p : frontier) {
                for (Position n : neighborsPassable(p)) {
                    if (seen.add(n)) {
                        if (n.equals(to)) {
                            return Optional.of(dist);
                        }
                        next.add(n);
                    }
                }
            }
            frontier = next;
        }
        return Optional.empty();
    }

    private List<Position> neighborsPassable(Position p) {
        int r = p.row();
        int c = p.col();
        List<Position> out = new ArrayList<>(4);
        if (maze.canMove(r, c, r - 1, c)) {
            out.add(new Position(r - 1, c));
        }
        if (maze.canMove(r, c, r + 1, c)) {
            out.add(new Position(r + 1, c));
        }
        if (maze.canMove(r, c, r, c - 1)) {
            out.add(new Position(r, c - 1));
        }
        if (maze.canMove(r, c, r, c + 1)) {
            out.add(new Position(r, c + 1));
        }
        return out;
    }

    /**
     * Picks {@code count} distinct random free cells (not exit), all in the same connected component as {@code anchor}.
     */
    public List<Position> pickDistinctRandomCells(java.util.Random rnd, int count, Position anchor, Position exit) {
        List<Position> component = floodFillReachable(anchor);
        component.removeIf(p -> p.equals(exit));
        Collections.shuffle(component, rnd);
        if (component.size() < count) {
            throw new IllegalStateException("Not enough free cells for actors");
        }
        return new ArrayList<>(component.subList(0, count));
    }

    private List<Position> floodFillReachable(Position start) {
        List<Position> all = new ArrayList<>();
        Set<Position> seen = new HashSet<>();
        ArrayList<Position> q = new ArrayList<>();
        q.add(start);
        seen.add(start);
        while (!q.isEmpty()) {
            Position p = q.remove(q.size() - 1);
            all.add(p);
            for (Position n : neighborsPassable(p)) {
                if (seen.add(n)) {
                    q.add(n);
                }
            }
        }
        return all;
    }

    public void joinAllActors() throws InterruptedException {
        for (Thread t : actorThreads) {
            if (t != null) {
                t.join();
            }
        }
        ManagerThread mt = managerThread;
        if (mt != null) {
            mt.join(3000);
        }
    }

}
