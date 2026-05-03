package ro.uaic.asli.lab9.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * Immutable grid coordinate used by actors and pathfinding.
 */
public final class Position implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final int row;
    private final int col;

    public Position(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int row() {
        return row;
    }

    public int col() {
        return col;
    }

    public int manhattanTo(Position other) {
        if (other == null) {
            return Integer.MAX_VALUE;
        }
        return Math.abs(row - other.row) + Math.abs(col - other.col);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Position position = (Position) o;
        return row == position.row && col == position.col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }

    @Override
    public String toString() {
        return "(" + row + "," + col + ")";
    }
}
