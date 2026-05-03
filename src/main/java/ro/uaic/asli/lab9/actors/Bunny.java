package ro.uaic.asli.lab9.actors;

import ro.uaic.asli.lab9.concurrent.GameController;
import ro.uaic.asli.lab9.concurrent.MoveResult;
import ro.uaic.asli.lab9.model.Maze;
import ro.uaic.asli.lab9.model.Position;
import ro.uaic.asli.lab9.pathfinding.PathFinder;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Bunny thread: mostly random valid steps, sometimes follows one BFS step toward the exit.
 */
public final class Bunny extends PausableActor {

    private final int bunnyId;
    private final Random random;
    private final Position exit;
    private final PathFinder pathFinder;
    private final double bfsTowardExitProbability;

    public Bunny(int bunnyId, GameController controller, Random random, long moveDelayMs,
                 double bfsTowardExitProbability) {
        super(bunnyId, "Bunny-" + bunnyId, controller, moveDelayMs);
        this.bunnyId = bunnyId;
        this.random = random;
        this.exit = controller.getExitPosition();
        this.pathFinder = controller.getPathFinder();
        this.bfsTowardExitProbability = bfsTowardExitProbability;
    }

    @Override
    protected String defaultName(int id) {
        return "Bunny-" + id;
    }

    @Override
    public void run() {
        try {
            while (getController().isGameRunning()) {
                getController().waitIfPaused(this);
                if (!getController().isGameRunning()) {
                    break;
                }
                Position here = getController().getBunnyPosition(bunnyId);
                if (here == null) {
                    break;
                }
                if (here.equals(exit)) {
                    sleepStepDelay();
                    continue;
                }
                Maze maze = getController().getMaze();
                Position next = chooseNext(here, maze);
                if (next == null) {
                    sleepStepDelay();
                    continue;
                }
                MoveResult r = getController().moveBunny(bunnyId, here, next);
                if (r == MoveResult.GAME_OVER) {
                    break;
                }
                sleepStepDelay();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private Position chooseNext(Position here, Maze maze) {
        List<Position> moves = new ArrayList<>();
        addIfOk(maze, here, here.row() - 1, here.col(), moves);
        addIfOk(maze, here, here.row() + 1, here.col(), moves);
        addIfOk(maze, here, here.row(), here.col() - 1, moves);
        addIfOk(maze, here, here.row(), here.col() + 1, moves);
        if (moves.isEmpty()) {
            return null;
        }
        if (random.nextDouble() < bfsTowardExitProbability) {
            List<Position> path = pathFinder.shortestPath(here, exit, maze);
            if (path.size() >= 2) {
                Position step = path.get(1);
                if (moves.contains(step)) {
                    return step;
                }
            }
        }
        return moves.get(random.nextInt(moves.size()));
    }

    private static void addIfOk(Maze maze, Position from, int r, int c, List<Position> out) {
        if (maze.canMove(from.row(), from.col(), r, c)) {
            out.add(new Position(r, c));
        }
    }

    public int getBunnyId() {
        return bunnyId;
    }
}
