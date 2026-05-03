package ro.uaic.asli.lab9.util;

import ro.uaic.asli.lab8.model.Cell;

/**
 * Read-only bridge from Lab 8 maze files/instances into Lab 9 maze model (Lab 8 code is not modified).
 */
public final class Lab8MazeAdapter {

    private Lab8MazeAdapter() {
    }

    public static ro.uaic.asli.lab9.model.Maze copyFrom(ro.uaic.asli.lab8.model.Maze src) {
        ro.uaic.asli.lab9.model.Maze dst = new ro.uaic.asli.lab9.model.Maze(src.getRows(), src.getCols());
        dst.initializeAllWalls();
        for (int r = 0; r < src.getRows(); r++) {
            for (int c = 0; c < src.getCols(); c++) {
                Cell a = src.getCell(r, c);
                dst.setWall(r, c, ro.uaic.asli.lab9.model.Wall.TOP, a.hasTopWall());
                dst.setWall(r, c, ro.uaic.asli.lab9.model.Wall.RIGHT, a.hasRightWall());
                dst.setWall(r, c, ro.uaic.asli.lab9.model.Wall.BOTTOM, a.hasBottomWall());
                dst.setWall(r, c, ro.uaic.asli.lab9.model.Wall.LEFT, a.hasLeftWall());
            }
        }
        return dst;
    }
}
