package ro.uaic.asli.lab8.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

public class Maze implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final int rows;
    private final int cols;
    private final Cell[][] cells;

    public Maze(int rows, int cols) {
        if (rows <= 0 || cols <= 0) {
            throw new IllegalArgumentException("Rows and cols must be positive.");
        }
        this.rows = rows;
        this.cols = cols;
        this.cells = new Cell[rows][cols];
        initializeAllWalls();
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public Cell getCell(int row, int col) {
        if (!inBounds(row, col)) {
            return null;
        }
        return cells[row][col];
    }

    public boolean inBounds(int row, int col) {
        return row >= 0 && row < rows && col >= 0 && col < cols;
    }

    public void resetAllWalls() {
        initializeAllWalls();
    }

    public void initializeAllWalls() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                cells[r][c] = new Cell(r, c);
            }
        }
        // Outer boundaries are already walls; internal consistency is ensured by toggling helpers.
    }

    public void setWall(int row, int col, Wall wall, boolean present) {
        Objects.requireNonNull(wall, "wall");
        Cell cell = requireCell(row, col);
        applyWall(cell, wall, present);

        int nr = row;
        int nc = col;
        Wall opposite = opposite(wall);
        switch (wall) {
            case TOP -> nr = row - 1;
            case RIGHT -> nc = col + 1;
            case BOTTOM -> nr = row + 1;
            case LEFT -> nc = col - 1;
        }
        if (inBounds(nr, nc)) {
            Cell neighbor = getCell(nr, nc);
            applyWall(neighbor, opposite, present);
        }
    }

    public void toggleWall(int row, int col, Wall wall) {
        Objects.requireNonNull(wall, "wall");
        Cell cell = requireCell(row, col);
        boolean nowPresent = !hasWall(cell, wall);
        setWall(row, col, wall, nowPresent);
    }

    public void removeWallBetween(int r1, int c1, int r2, int c2) {
        Cell a = requireCell(r1, c1);
        Cell b = requireCell(r2, c2);
        int dr = b.getRow() - a.getRow();
        int dc = b.getCol() - a.getCol();
        if (Math.abs(dr) + Math.abs(dc) != 1) {
            throw new IllegalArgumentException("Cells must be orthogonal neighbors.");
        }
        if (dr == -1) {
            setWall(a.getRow(), a.getCol(), Wall.TOP, false);
        } else if (dr == 1) {
            setWall(a.getRow(), a.getCol(), Wall.BOTTOM, false);
        } else if (dc == -1) {
            setWall(a.getRow(), a.getCol(), Wall.LEFT, false);
        } else if (dc == 1) {
            setWall(a.getRow(), a.getCol(), Wall.RIGHT, false);
        }
    }

    public boolean canMove(int row, int col, int nRow, int nCol) {
        if (!inBounds(row, col) || !inBounds(nRow, nCol)) {
            return false;
        }
        int dr = nRow - row;
        int dc = nCol - col;
        if (Math.abs(dr) + Math.abs(dc) != 1) {
            return false;
        }
        Cell cell = getCell(row, col);
        if (dr == -1) {
            return !cell.hasTopWall();
        }
        if (dr == 1) {
            return !cell.hasBottomWall();
        }
        if (dc == -1) {
            return !cell.hasLeftWall();
        }
        return !cell.hasRightWall();
    }

    private Cell requireCell(int row, int col) {
        Cell cell = getCell(row, col);
        if (cell == null) {
            throw new IndexOutOfBoundsException("Cell out of bounds: (" + row + "," + col + ")");
        }
        return cell;
    }

    private static void applyWall(Cell cell, Wall wall, boolean present) {
        switch (wall) {
            case TOP -> cell.setTopWall(present);
            case RIGHT -> cell.setRightWall(present);
            case BOTTOM -> cell.setBottomWall(present);
            case LEFT -> cell.setLeftWall(present);
        }
    }

    private static boolean hasWall(Cell cell, Wall wall) {
        return switch (wall) {
            case TOP -> cell.hasTopWall();
            case RIGHT -> cell.hasRightWall();
            case BOTTOM -> cell.hasBottomWall();
            case LEFT -> cell.hasLeftWall();
        };
    }

    private static Wall opposite(Wall wall) {
        return switch (wall) {
            case TOP -> Wall.BOTTOM;
            case RIGHT -> Wall.LEFT;
            case BOTTOM -> Wall.TOP;
            case LEFT -> Wall.RIGHT;
        };
    }
}

