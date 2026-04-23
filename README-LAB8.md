## Lab 8 – Maze Builder (Swing + Java2D)

Desktop Java application that lets you create, edit, validate, export, and save/load a maze represented as a rectangular grid of cells.

### Implemented features
- **Grid creation** (rows/cols/cell size) with **all walls present**
- **Random wall removal** (Compulsory “Create” button)
- **Manual wall editing**: click near a wall to toggle it (updates the adjacent cell’s opposite wall)
- **Traversability validation**: BFS from start cell to end cell + optional path highlight
- **PNG export**: exports exactly what is drawn in the maze panel
- **Serialization**: save/load maze to/from a `.maze` file
- **Bonus**: **Perfect maze generation** (recursive backtracking) with animation + speed slider + stop button

### How to run
From the project root:

```bash
mvn -q -DskipTests package
```

Then run one of the GUI entry points from your IDE:
- **Compulsory**: `ro.uaic.asli.lab8.app.Lab8CompulsoryApp`
- **Homework**: `ro.uaic.asli.lab8.app.Lab8HomeworkApp`
- **Advanced (default)**: `ro.uaic.asli.lab8.app.Lab8AdvancedApp` (also `ro.uaic.asli.lab8.app.Main`)

If you want to run from terminal using Maven exec, you can add `exec-maven-plugin`, but IDE run is simplest for Swing.

### How manual wall toggling works
- `MazePanel` converts mouse coordinates to a cell `(row,col)` and checks which wall is closest (top/right/bottom/left).
- It calls the controller with `(row,col,wall)`.
- `Maze.toggleWall(...)` flips that wall and also flips the **neighbor’s opposite wall** (if the neighbor exists).

### How path validation works
- The start/end cells are set via the spinners in `ConfigPanel`.
- `Validate Path` runs BFS in `MazeSolver.findPathBfs`.
- A move between two orthogonal neighbors is allowed only if there is **no wall** between them (`Maze.canMove`).
- If a path is found, it is highlighted in light blue; start is green; end is red.

### How serialization works
- `MazeSerializer.saveMaze` uses `ObjectOutputStream` to write the `Maze` object.
- `MazeSerializer.loadMaze` reads it back using `ObjectInputStream`.
- `Maze`, `Cell`, and `Wall` are `Serializable`.

### How PNG export works
- `MazeImageExporter.exportComponentToPng` renders the `MazePanel` into a `BufferedImage` using `component.printAll(g2)`.
- Then it writes the image using `ImageIO.write(..., "png", file)`.

### Perfect maze generation proof (Recursive Backtracking)
The recursive backtracking algorithm is a DFS traversal of the grid graph:
- It starts from a random cell, marks it visited, and repeatedly chooses an unvisited neighbor.
- When moving to a neighbor, it **removes the wall** between the current cell and that neighbor.
- This creates a set of removed walls that corresponds to a **DFS spanning tree** of the grid graph.

Why this produces a *perfect maze*:
- A spanning tree is **connected** → every cell is reachable (one connected component).
- A tree has **no cycles** → there is **exactly one simple path** between any two cells.

### Notes
- The speed slider controls the delay (ms) between generation steps (lower is faster).
- The **Stop** button cancels the current animation worker.

