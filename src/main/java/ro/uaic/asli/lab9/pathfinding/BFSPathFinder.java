package ro.uaic.asli.lab9.pathfinding;

import ro.uaic.asli.lab9.model.Maze;
import ro.uaic.asli.lab9.model.Position;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public final class BFSPathFinder implements PathFinder {

    private static final int[][] DELTAS = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

    @Override
    public List<Position> shortestPath(Position start, Position target, Maze maze) {
        if (start == null || target == null || maze == null) {
            return List.of();
        }
        if (start.equals(target)) {
            return List.of(start);
        }
        if (!maze.inBounds(start.row(), start.col()) || !maze.inBounds(target.row(), target.col())) {
            return List.of();
        }

        Queue<Position> queue = new ArrayDeque<>();
        Map<Position, Position> parent = new HashMap<>();
        queue.add(start);
        parent.put(start, null);

        while (!queue.isEmpty()) {
            Position cur = queue.poll();
            if (cur.equals(target)) {
                return reconstruct(parent, start, target);
            }
            int r = cur.row();
            int c = cur.col();
            for (int[] d : DELTAS) {
                int nr = r + d[0];
                int nc = c + d[1];
                if (!maze.canMove(r, c, nr, nc)) {
                    continue;
                }
                Position next = new Position(nr, nc);
                if (parent.containsKey(next)) {
                    continue;
                }
                parent.put(next, cur);
                queue.add(next);
            }
        }
        return List.of();
    }

    private static List<Position> reconstruct(Map<Position, Position> parent, Position start, Position target) {
        List<Position> path = new ArrayList<>();
        Position at = target;
        while (at != null) {
            path.add(at);
            at = parent.get(at);
        }
        Collections.reverse(path);
        if (!path.isEmpty() && path.get(0).equals(start)) {
            return path;
        }
        return List.of();
    }
}
