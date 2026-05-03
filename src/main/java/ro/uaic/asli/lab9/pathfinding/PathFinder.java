package ro.uaic.asli.lab9.pathfinding;

import ro.uaic.asli.lab9.model.Maze;
import ro.uaic.asli.lab9.model.Position;

import java.util.List;

public interface PathFinder {
    /**
     * Shortest path in the maze graph from start to target (inclusive of both ends if reachable).
     * Returns empty list if unreachable.
     */
    List<Position> shortestPath(Position start, Position target, Maze maze);
}
