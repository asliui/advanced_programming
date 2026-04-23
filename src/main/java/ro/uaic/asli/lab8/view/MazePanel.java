package ro.uaic.asli.lab8.view;

import ro.uaic.asli.lab8.model.Cell;
import ro.uaic.asli.lab8.model.Maze;
import ro.uaic.asli.lab8.model.Wall;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MazePanel extends JPanel {
    private Maze maze;
    private int cellSize = 32;
    private int padding = 20;

    private Point start = new Point(0, 0);
    private Point end = new Point(0, 0);

    private Set<Point> pathCells = new HashSet<>();

    public interface WallToggleListener {
        void onWallToggleRequest(int row, int col, Wall wall);
    }

    private WallToggleListener wallToggleListener;

    public MazePanel() {
        setBackground(Color.WHITE);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (maze == null || wallToggleListener == null) {
                    return;
                }
                WallClick hit = hitTestWall(e.getX(), e.getY());
                if (hit != null) {
                    wallToggleListener.onWallToggleRequest(hit.row, hit.col, hit.wall);
                }
            }
        });
    }

    public void setWallToggleListener(WallToggleListener wallToggleListener) {
        this.wallToggleListener = wallToggleListener;
    }

    public void setMaze(Maze maze, int cellSize) {
        this.maze = maze;
        this.cellSize = cellSize;
        clearPath();
        revalidate();
        repaint();
    }

    public Maze getMaze() {
        return maze;
    }

    public void setStartEnd(int sr, int sc, int er, int ec) {
        start = new Point(sc, sr);
        end = new Point(ec, er);
        repaint();
    }

    public Point getStart() {
        return new Point(start);
    }

    public Point getEnd() {
        return new Point(end);
    }

    public void showPath(List<Point> path) {
        pathCells.clear();
        if (path != null) {
            pathCells.addAll(path);
        }
        repaint();
    }

    public void clearPath() {
        pathCells.clear();
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        if (maze == null) {
            return new Dimension(800, 600);
        }
        int w = padding * 2 + maze.getCols() * cellSize;
        int h = padding * 2 + maze.getRows() * cellSize;
        return new Dimension(w, h);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (maze == null) {
            drawHint((Graphics2D) g);
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(2f));

            drawCellsAndPath(g2);
            drawWalls(g2);
        } finally {
            g2.dispose();
        }
    }

    private void drawHint(Graphics2D g2) {
        g2.setColor(new Color(40, 40, 40));
        g2.drawString("Use Configuration -> Draw / Create Grid to start.", 30, 40);
    }

    private void drawCellsAndPath(Graphics2D g2) {
        for (int r = 0; r < maze.getRows(); r++) {
            for (int c = 0; c < maze.getCols(); c++) {
                int x = padding + c * cellSize;
                int y = padding + r * cellSize;

                Point p = new Point(c, r);
                if (pathCells.contains(p)) {
                    g2.setColor(new Color(180, 220, 255));
                    g2.fillRect(x + 1, y + 1, cellSize - 1, cellSize - 1);
                }

                if (p.equals(start)) {
                    g2.setColor(new Color(110, 220, 110));
                    g2.fillRect(x + 1, y + 1, cellSize - 1, cellSize - 1);
                }
                if (p.equals(end)) {
                    g2.setColor(new Color(255, 160, 160));
                    g2.fillRect(x + 1, y + 1, cellSize - 1, cellSize - 1);
                }
            }
        }
        g2.setColor(Color.BLACK);
    }

    private void drawWalls(Graphics2D g2) {
        for (int r = 0; r < maze.getRows(); r++) {
            for (int c = 0; c < maze.getCols(); c++) {
                Cell cell = maze.getCell(r, c);
                int x = padding + c * cellSize;
                int y = padding + r * cellSize;

                if (cell.hasTopWall()) {
                    g2.drawLine(x, y, x + cellSize, y);
                }
                if (cell.hasRightWall()) {
                    g2.drawLine(x + cellSize, y, x + cellSize, y + cellSize);
                }
                if (cell.hasBottomWall()) {
                    g2.drawLine(x, y + cellSize, x + cellSize, y + cellSize);
                }
                if (cell.hasLeftWall()) {
                    g2.drawLine(x, y, x, y + cellSize);
                }
            }
        }
    }

    private record WallClick(int row, int col, Wall wall) {
    }

    private WallClick hitTestWall(int mx, int my) {
        int ox = padding;
        int oy = padding;
        int xRel = mx - ox;
        int yRel = my - oy;
        if (xRel < 0 || yRel < 0) {
            return null;
        }
        int col = xRel / cellSize;
        int row = yRel / cellSize;
        if (maze == null || !maze.inBounds(row, col)) {
            return null;
        }

        int xInCell = xRel % cellSize;
        int yInCell = yRel % cellSize;
        int thresholdPx = Math.max(6, Math.min(12, cellSize / 4));

        int distTop = yInCell;
        int distBottom = cellSize - yInCell;
        int distLeft = xInCell;
        int distRight = cellSize - xInCell;

        int min = Math.min(Math.min(distTop, distBottom), Math.min(distLeft, distRight));
        if (min > thresholdPx) {
            return null;
        }
        if (min == distTop) {
            return new WallClick(row, col, Wall.TOP);
        }
        if (min == distBottom) {
            return new WallClick(row, col, Wall.BOTTOM);
        }
        if (min == distLeft) {
            return new WallClick(row, col, Wall.LEFT);
        }
        return new WallClick(row, col, Wall.RIGHT);
    }
}

