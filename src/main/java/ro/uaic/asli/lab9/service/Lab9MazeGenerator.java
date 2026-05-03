package ro.uaic.asli.lab9.service;

import ro.uaic.asli.lab9.model.Maze;

import java.awt.Point;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Random;

/**
 * Headless perfect maze generation (same algorithm idea as Lab 8 {@code MazeGenerator}, without Swing).
 */
public final class Lab9MazeGenerator {

    private Lab9MazeGenerator() {
    }

    public static void generatePerfectMaze(Maze maze, Random random) {
        if (maze == null) {
            throw new IllegalArgumentException("Maze is null");
        }
        if (random == null) {
            random = new Random();
        }
        int rows = maze.getRows();
        int cols = maze.getCols();
        maze.initializeAllWalls();

        boolean[][] visited = new boolean[rows][cols];
        int sr = random.nextInt(rows);
        int sc = random.nextInt(cols);

        Deque<Point> stack = new ArrayDeque<>();
        stack.push(new Point(sc, sr));
        visited[sr][sc] = true;

        while (!stack.isEmpty()) {
            Point cur = stack.peek();
            int r = cur.y;
            int c = cur.x;

            List<Point> unvisitedNeighbors = new ArrayList<>(4);
            addIfUnvisited(maze, visited, unvisitedNeighbors, r - 1, c);
            addIfUnvisited(maze, visited, unvisitedNeighbors, r + 1, c);
            addIfUnvisited(maze, visited, unvisitedNeighbors, r, c - 1);
            addIfUnvisited(maze, visited, unvisitedNeighbors, r, c + 1);

            if (unvisitedNeighbors.isEmpty()) {
                stack.pop();
                continue;
            }

            Point next = unvisitedNeighbors.get(random.nextInt(unvisitedNeighbors.size()));
            maze.removeWallBetween(r, c, next.y, next.x);
            visited[next.y][next.x] = true;
            stack.push(next);
        }
    }

    private static void addIfUnvisited(Maze maze, boolean[][] visited, List<Point> out, int r, int c) {
        if (!maze.inBounds(r, c)) {
            return;
        }
        if (!visited[r][c]) {
            out.add(new Point(c, r));
        }
    }
}
