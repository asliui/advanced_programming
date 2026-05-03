package ro.uaic.asli.lab9.view;

import ro.uaic.asli.lab9.concurrent.GameController;
import ro.uaic.asli.lab9.model.Maze;
import ro.uaic.asli.lab9.model.Position;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Periodic ASCII snapshot. One character per cell: walls as # at boundaries only in detailed mode;
 * default mode shows actors on . / E with a note that connectivity follows the underlying maze graph.
 */
public final class MazeConsoleRenderer {

    private MazeConsoleRenderer() {
    }

    public static String render(GameController controller) {
        return render(controller, false);
    }

    public static String render(GameController controller, boolean labelRobots) {
        var maze = controller.getMaze();
        int rows = maze.getRows();
        int cols = maze.getCols();
        Position exit = controller.getExitPosition();

        Map<Integer, Position> robots = controller.snapshotRobotPositions();
        Map<Integer, Position> bunnies = controller.snapshotBunnyPositions();

        List<Map.Entry<Integer, Position>> robotList = new ArrayList<>(robots.entrySet());
        robotList.sort(Comparator.comparingInt(Map.Entry::getKey));

        char[][] g = new char[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                g[r][c] = '.';
            }
        }
        if (maze.inBounds(exit.row(), exit.col())) {
            g[exit.row()][exit.col()] = 'E';
        }
        for (Map.Entry<Integer, Position> e : robotList) {
            Position p = e.getValue();
            if (!maze.inBounds(p.row(), p.col())) {
                continue;
            }
            char sym = labelRobots && e.getKey() < 10 ? (char) ('0' + e.getKey()) : 'R';
            if (g[p.row()][p.col()] == '.' || g[p.row()][p.col()] == 'E') {
                g[p.row()][p.col()] = sym;
            }
        }
        for (Position p : bunnies.values()) {
            if (!maze.inBounds(p.row(), p.col())) {
                continue;
            }
            g[p.row()][p.col()] = 'B';
        }

        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                sb.append(g[r][c]);
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    /**
     * Richer view: horizontal/vertical walls as # between cells (interior); actors override wall chars in their cell.
     */
    public static String renderWithInteriorWalls(GameController controller) {
        var maze = controller.getMaze();
        int rows = maze.getRows();
        int cols = maze.getCols();
        int h = rows * 2 + 1;
        int w = cols * 2 + 1;
        char[][] g = new char[h][w];
        for (int r = 0; r < h; r++) {
            for (int c = 0; c < w; c++) {
                g[r][c] = ' ';
            }
        }
        for (int r = 0; r < h; r++) {
            for (int c = 0; c < w; c++) {
                if (r % 2 == 0 || c % 2 == 0) {
                    g[r][c] = '#';
                } else {
                    g[r][c] = '.';
                }
            }
        }
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int gr = r * 2 + 1;
                int gc = c * 2 + 1;
                var cell = maze.getCell(r, c);
                if (!cell.hasTopWall()) {
                    g[gr - 1][gc] = '.';
                }
                if (!cell.hasBottomWall()) {
                    g[gr + 1][gc] = '.';
                }
                if (!cell.hasLeftWall()) {
                    g[gr][gc - 1] = '.';
                }
                if (!cell.hasRightWall()) {
                    g[gr][gc + 1] = '.';
                }
            }
        }
        Position exit = controller.getExitPosition();
        if (maze.inBounds(exit.row(), exit.col())) {
            g[exit.row() * 2 + 1][exit.col() * 2 + 1] = 'E';
        }
        Map<Integer, Position> robots = controller.snapshotRobotPositions();
        List<Integer> ids = new ArrayList<>(robots.keySet());
        Collections.sort(ids);
        for (int id : ids) {
            Position p = robots.get(id);
            int gr = p.row() * 2 + 1;
            int gc = p.col() * 2 + 1;
            char sym = id < 10 ? (char) ('0' + id) : 'R';
            if (g[gr][gc] == '.' || g[gr][gc] == 'E') {
                g[gr][gc] = sym;
            }
        }
        for (Position p : controller.snapshotBunnyPositions().values()) {
            g[p.row() * 2 + 1][p.col() * 2 + 1] = 'B';
        }
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < h; r++) {
            for (int c = 0; c < w; c++) {
                sb.append(g[r][c]);
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}
