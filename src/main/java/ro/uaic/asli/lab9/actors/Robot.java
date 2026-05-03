package ro.uaic.asli.lab9.actors;

import ro.uaic.asli.lab9.app.Lab9Mode;
import ro.uaic.asli.lab9.concurrent.GameController;
import ro.uaic.asli.lab9.concurrent.MoveResult;
import ro.uaic.asli.lab9.concurrent.SharedMemory;
import ro.uaic.asli.lab9.model.Maze;
import ro.uaic.asli.lab9.model.Position;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

/**
 * Robot thread: behavior depends on {@link Lab9Mode}:
 * <ul>
 *     <li>Compulsory: random valid moves (shared visit marks still demonstrate synchronization).</li>
 *     <li>Homework: systematic exploration using only local visited state; optional chase when this robot senses a bunny (no team intel).</li>
 *     <li>Advanced: collaborative exploration via shared memory, proximity sensing with broadcast intel,
 *     shortest-path chase toward shared targets, multi-bunny aware.</li>
 * </ul>
 */
public final class Robot extends PausableActor {

    private final int robotId;
    private final Random random;
    private final int sensingRange;
    private final SharedMemory memory;
    private final Lab9Mode mode;

    private final Set<Position> localVisited = new HashSet<>();
    private volatile boolean chaseMode;
    private volatile Position chaseTarget;
    private volatile Integer chaseBunnyId;

    public Robot(int robotId, GameController controller, Random random, long moveDelayMs, int sensingRange,
                 Lab9Mode mode) {
        super(robotId, "Robot-" + robotId, controller, moveDelayMs);
        this.robotId = robotId;
        this.random = random;
        this.sensingRange = Math.max(1, sensingRange);
        this.memory = controller.getSharedMemory();
        this.mode = mode == null ? Lab9Mode.COMPULSORY : mode;
    }

    @Override
    protected String defaultName(int id) {
        return "Robot-" + id;
    }

    @Override
    public void run() {
        try {
            while (getController().isGameRunning()) {
                getController().waitIfPaused(this);
                if (!getController().isGameRunning()) {
                    break;
                }
                Position here = getController().getRobotPosition(robotId);
                if (here == null) {
                    break;
                }
                localVisited.add(here);
                memory.markVisited(here, robotId);

                updateChaseState(here);

                Maze maze = getController().getMaze();
                Position next = chooseMove(here, maze);
                if (next == null) {
                    sleepStepDelay();
                    continue;
                }
                MoveResult r = getController().moveRobot(robotId, here, next);
                if (r == MoveResult.GAME_OVER) {
                    break;
                }
                sleepStepDelay();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void updateChaseState(Position here) {
        switch (mode) {
            case COMPULSORY -> {
                chaseMode = false;
                chaseTarget = null;
                chaseBunnyId = null;
            }
            case HOMEWORK -> {
                Optional<SensedBunny> sensed = senseNearestBunny(here);
                if (sensed.isPresent()) {
                    chaseMode = true;
                    chaseBunnyId = sensed.get().bunnyId();
                    chaseTarget = sensed.get().position();
                } else if (chaseMode && chaseTarget != null) {
                    if (here.equals(chaseTarget)) {
                        Position actual = chaseBunnyId != null
                                ? getController().getBunnyPosition(chaseBunnyId)
                                : null;
                        if (actual == null || !actual.equals(here)) {
                            chaseMode = false;
                            chaseTarget = null;
                            chaseBunnyId = null;
                        }
                    }
                }
            }
            case ADVANCED -> {
                pruneStaleBunnyIntel(here);
                Optional<SensedBunny> sensed = senseNearestBunny(here);
                if (sensed.isPresent()) {
                    chaseMode = true;
                    chaseBunnyId = sensed.get().bunnyId();
                    chaseTarget = sensed.get().position();
                    memory.updateBunnyLocation(chaseBunnyId, chaseTarget);
                } else if (memory.hasBunnyIntel()) {
                    Optional<ChasePick> pick = pickBestIntelTarget(here, getController().getMaze());
                    if (pick.isPresent()) {
                        chaseMode = true;
                        chaseBunnyId = pick.get().bunnyId();
                        chaseTarget = pick.get().position();
                    } else {
                        chaseMode = false;
                        chaseTarget = null;
                        chaseBunnyId = null;
                    }
                } else {
                    chaseMode = false;
                    chaseTarget = null;
                    chaseBunnyId = null;
                }
            }
        }
    }

    /**
     * If we stand on a stale reported cell, drop that bunny's intel.
     */
    private void pruneStaleBunnyIntel(Position here) {
        Map<Integer, Position> intel = memory.snapshotLastKnownBunnies();
        for (Map.Entry<Integer, Position> e : intel.entrySet()) {
            if (here.equals(e.getValue())) {
                Position actual = getController().getBunnyPosition(e.getKey());
                if (actual == null || !actual.equals(here)) {
                    memory.clearBunnyLocation(e.getKey());
                }
            }
        }
    }

    private Optional<ChasePick> pickBestIntelTarget(Position here, Maze maze) {
        Map<Integer, Position> intel = memory.snapshotLastKnownBunnies();
        ChasePick best = null;
        int bestPathLen = Integer.MAX_VALUE;
        for (Map.Entry<Integer, Position> e : intel.entrySet()) {
            List<Position> path = getController().getPathFinder().shortestPath(here, e.getValue(), maze);
            if (path.size() >= 2 && path.size() < bestPathLen) {
                bestPathLen = path.size();
                best = new ChasePick(e.getKey(), e.getValue());
            }
        }
        return Optional.ofNullable(best);
    }

    private Optional<SensedBunny> senseNearestBunny(Position here) {
        Map<Integer, Position> bunnies = getController().snapshotBunnyPositions();
        SensedBunny best = null;
        for (Map.Entry<Integer, Position> e : bunnies.entrySet()) {
            Optional<Integer> d = getController().graphDistanceLimited(here, e.getValue(), sensingRange);
            if (d.isPresent()) {
                if (best == null || d.get() < best.distance()) {
                    best = new SensedBunny(e.getKey(), e.getValue(), d.get());
                }
            }
        }
        return Optional.ofNullable(best);
    }

    private Position chooseMove(Position here, Maze maze) {
        if (mode != Lab9Mode.COMPULSORY && chaseMode && chaseTarget != null) {
            List<Position> path = getController().getPathFinder().shortestPath(here, chaseTarget, maze);
            if (path.size() >= 2) {
                return path.get(1);
            }
        }
        if (mode == Lab9Mode.COMPULSORY) {
            return pickRandomNeighbor(here, maze);
        }
        if (mode == Lab9Mode.HOMEWORK) {
            return pickExplorationStepHomework(here, maze);
        }
        return pickExplorationStepAdvanced(here, maze);
    }

    private Position pickRandomNeighbor(Position here, Maze maze) {
        List<Position> nbrs = new ArrayList<>();
        addIfOk(maze, here, here.row() - 1, here.col(), nbrs);
        addIfOk(maze, here, here.row() + 1, here.col(), nbrs);
        addIfOk(maze, here, here.row(), here.col() - 1, nbrs);
        addIfOk(maze, here, here.row(), here.col() + 1, nbrs);
        if (nbrs.isEmpty()) {
            return null;
        }
        return nbrs.get(random.nextInt(nbrs.size()));
    }

    private Position pickExplorationStepHomework(Position here, Maze maze) {
        List<Position> nbrs = new ArrayList<>();
        addIfOk(maze, here, here.row() - 1, here.col(), nbrs);
        addIfOk(maze, here, here.row() + 1, here.col(), nbrs);
        addIfOk(maze, here, here.row(), here.col() - 1, nbrs);
        addIfOk(maze, here, here.row(), here.col() + 1, nbrs);
        if (nbrs.isEmpty()) {
            return null;
        }
        Comparator<Position> cmp = Comparator
                .comparing((Position p) -> localVisited.contains(p))
                .thenComparing(p -> p.row())
                .thenComparing(p -> p.col());
        nbrs.sort(cmp);
        List<Position> best = new ArrayList<>();
        Position first = nbrs.get(0);
        for (Position p : nbrs) {
            if (cmp.compare(p, first) == 0) {
                best.add(p);
            }
        }
        return best.get(random.nextInt(best.size()));
    }

    private Position pickExplorationStepAdvanced(Position here, Maze maze) {
        List<Position> nbrs = new ArrayList<>();
        addIfOk(maze, here, here.row() - 1, here.col(), nbrs);
        addIfOk(maze, here, here.row() + 1, here.col(), nbrs);
        addIfOk(maze, here, here.row(), here.col() - 1, nbrs);
        addIfOk(maze, here, here.row(), here.col() + 1, nbrs);
        if (nbrs.isEmpty()) {
            return null;
        }
        Comparator<Position> cmp = Comparator
                .comparing((Position p) -> localVisited.contains(p))
                .thenComparingInt(p -> memory.otherRobotVisitWeight(p, robotId))
                .thenComparingInt(p -> memory.isVisited(p) ? 1 : 0)
                .thenComparing(p -> p.row())
                .thenComparing(p -> p.col());
        nbrs.sort(cmp);
        List<Position> best = new ArrayList<>();
        Position first = nbrs.get(0);
        for (Position p : nbrs) {
            if (cmp.compare(p, first) == 0) {
                best.add(p);
            }
        }
        return best.get(random.nextInt(best.size()));
    }

    private static void addIfOk(Maze maze, Position from, int r, int c, List<Position> out) {
        if (maze.canMove(from.row(), from.col(), r, c)) {
            out.add(new Position(r, c));
        }
    }

    private record SensedBunny(int bunnyId, Position position, int distance) {
    }

    private record ChasePick(int bunnyId, Position position) {
    }
}
