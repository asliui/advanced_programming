package ro.uaic.asli.lab8.service;

import ro.uaic.asli.lab8.model.Maze;

import java.awt.Point;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MazeSolver {

    public List<Point> findPathBfs(Maze maze, Point start, Point end) {
        if (maze == null || start == null || end == null) {
            return List.of();
        }
        int sr = start.y;
        int sc = start.x;
        int er = end.y;
        int ec = end.x;
        if (!maze.inBounds(sr, sc) || !maze.inBounds(er, ec)) {
            return List.of();
        }

        Deque<Point> q = new ArrayDeque<>();
        boolean[][] vis = new boolean[maze.getRows()][maze.getCols()];
        Map<Point, Point> parent = new HashMap<>();

        Point s = new Point(sc, sr);
        Point t = new Point(ec, er);

        q.add(s);
        vis[sr][sc] = true;

        int[][] dirs = new int[][]{{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        while (!q.isEmpty()) {
            Point cur = q.removeFirst();
            if (cur.equals(t)) {
                return reconstruct(parent, s, t);
            }
            for (int[] d : dirs) {
                int nr = cur.y + d[0];
                int nc = cur.x + d[1];
                if (!maze.inBounds(nr, nc) || vis[nr][nc]) {
                    continue;
                }
                if (!maze.canMove(cur.y, cur.x, nr, nc)) {
                    continue;
                }
                Point nxt = new Point(nc, nr);
                vis[nr][nc] = true;
                parent.put(nxt, cur);
                q.addLast(nxt);
            }
        }
        return List.of();
    }

    private static List<Point> reconstruct(Map<Point, Point> parent, Point start, Point end) {
        List<Point> path = new ArrayList<>();
        Point cur = end;
        while (cur != null && !cur.equals(start)) {
            path.add(cur);
            cur = parent.get(cur);
        }
        if (cur == null) {
            return List.of();
        }
        path.add(start);
        Collections.reverse(path);
        return path;
    }
}

