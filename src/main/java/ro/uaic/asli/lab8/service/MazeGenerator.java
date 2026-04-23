package ro.uaic.asli.lab8.service;

import ro.uaic.asli.lab8.model.Maze;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import java.awt.Point;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class MazeGenerator {

    public void randomRemoveWalls(Maze maze, double removeProbability, Random random) {
        if (maze == null) {
            return;
        }
        if (random == null) {
            random = new Random();
        }
        removeProbability = Math.max(0.0, Math.min(1.0, removeProbability));

        for (int r = 0; r < maze.getRows(); r++) {
            for (int c = 0; c < maze.getCols(); c++) {
                // only remove right and bottom walls to avoid double-processing
                if (c + 1 < maze.getCols() && random.nextDouble() < removeProbability) {
                    maze.removeWallBetween(r, c, r, c + 1);
                }
                if (r + 1 < maze.getRows() && random.nextDouble() < removeProbability) {
                    maze.removeWallBetween(r, c, r + 1, c);
                }
            }
        }
    }

    public interface StepListener {
        void onStep(Point currentCell);
    }

    public SwingWorker<Void, Point> generatePerfectMazeAnimated(
            Maze maze,
            AtomicInteger delayMs,
            AtomicBoolean cancelFlag,
            StepListener stepListener
    ) {
        if (maze == null) {
            throw new IllegalArgumentException("Maze is null");
        }
        if (delayMs == null) {
            delayMs = new AtomicInteger(60);
        }
        if (cancelFlag == null) {
            cancelFlag = new AtomicBoolean(false);
        }

        AtomicInteger finalDelayMs = delayMs;
        AtomicBoolean finalCancelFlag = cancelFlag;

        SwingWorker<Void, Point> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                int rows = maze.getRows();
                int cols = maze.getCols();

                boolean[][] visited = new boolean[rows][cols];
                Random rnd = new Random();

                int sr = rnd.nextInt(rows);
                int sc = rnd.nextInt(cols);

                Deque<Point> stack = new ArrayDeque<>();
                stack.push(new Point(sc, sr));
                visited[sr][sc] = true;

                while (!stack.isEmpty() && !finalCancelFlag.get()) {
                    Point cur = stack.peek();

                    List<Point> unvisitedNeighbors = new ArrayList<>(4);
                    int r = cur.y;
                    int c = cur.x;

                    addIfUnvisited(maze, visited, unvisitedNeighbors, r - 1, c);
                    addIfUnvisited(maze, visited, unvisitedNeighbors, r + 1, c);
                    addIfUnvisited(maze, visited, unvisitedNeighbors, r, c - 1);
                    addIfUnvisited(maze, visited, unvisitedNeighbors, r, c + 1);

                    if (unvisitedNeighbors.isEmpty()) {
                        stack.pop();
                        continue;
                    }

                    Point next = unvisitedNeighbors.get(rnd.nextInt(unvisitedNeighbors.size()));
                    maze.removeWallBetween(r, c, next.y, next.x);
                    visited[next.y][next.x] = true;
                    stack.push(next);

                    publish(next);
                    int d = Math.max(0, finalDelayMs.get());
                    if (d > 0) {
                        Thread.sleep(d);
                    }
                }
                return null;
            }

            @Override
            protected void process(List<Point> chunks) {
                if (stepListener == null || chunks.isEmpty()) {
                    return;
                }
                Point last = chunks.get(chunks.size() - 1);
                stepListener.onStep(last);
            }
        };

        worker.execute();
        return worker;
    }

    private static void addIfUnvisited(Maze maze, boolean[][] visited, List<Point> out, int r, int c) {
        if (!maze.inBounds(r, c)) {
            return;
        }
        if (!visited[r][c]) {
            out.add(new Point(c, r));
        }
    }

    public static void onEdt(Runnable r) {
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            SwingUtilities.invokeLater(r);
        }
    }
}

