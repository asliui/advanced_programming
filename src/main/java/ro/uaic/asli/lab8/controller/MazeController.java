package ro.uaic.asli.lab8.controller;

import ro.uaic.asli.lab8.app.Lab8Mode;
import ro.uaic.asli.lab8.app.MainFrame;
import ro.uaic.asli.lab8.model.Maze;
import ro.uaic.asli.lab8.model.Wall;
import ro.uaic.asli.lab8.service.MazeGenerator;
import ro.uaic.asli.lab8.service.MazeImageExporter;
import ro.uaic.asli.lab8.service.MazeSerializer;
import ro.uaic.asli.lab8.service.MazeSolver;
import ro.uaic.asli.lab8.view.ConfigPanel;
import ro.uaic.asli.lab8.view.ControlPanel;
import ro.uaic.asli.lab8.view.MazePanel;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Point;
import java.io.File;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class MazeController {
    private final Lab8Mode mode;
    private final MainFrame frame;
    private final ConfigPanel configPanel;
    private final MazePanel mazePanel;
    private final ControlPanel controlPanel;

    private final MazeGenerator generator = new MazeGenerator();
    private final MazeSolver solver = new MazeSolver();
    private final MazeSerializer serializer = new MazeSerializer();
    private final MazeImageExporter exporter = new MazeImageExporter();

    private Maze maze;

    private final AtomicInteger animationDelayMs = new AtomicInteger(60);
    private final AtomicBoolean cancelAnimation = new AtomicBoolean(false);
    private SwingWorker<Void, Point> activeWorker;

    public MazeController(Lab8Mode mode, MainFrame frame, ConfigPanel configPanel, MazePanel mazePanel, ControlPanel controlPanel) {
        this.mode = mode;
        this.frame = frame;
        this.configPanel = configPanel;
        this.mazePanel = mazePanel;
        this.controlPanel = controlPanel;
    }

    public void init() {
        if (mode == Lab8Mode.HOMEWORK || mode == Lab8Mode.ADVANCED) {
            mazePanel.setWallToggleListener(this::onWallToggleRequest);
        }

        configPanel.getDrawButton().addActionListener(e -> createNewMaze());

        controlPanel.getCreateButton().addActionListener(e -> {
            if (maze == null) return;
            stopAnimation();
            generator.randomRemoveWalls(maze, 0.25, new Random());
            mazePanel.clearPath();
            mazePanel.repaint();
        });

        controlPanel.getResetButton().addActionListener(e -> {
            if (maze == null) return;
            stopAnimation();
            maze.resetAllWalls();
            mazePanel.clearPath();
            mazePanel.repaint();
        });

        if (mode == Lab8Mode.HOMEWORK || mode == Lab8Mode.ADVANCED) {
            controlPanel.getValidateButton().addActionListener(e -> validatePath());
            controlPanel.getSaveButton().addActionListener(e -> saveMaze());
            controlPanel.getLoadButton().addActionListener(e -> loadMaze());
            controlPanel.getExportPngButton().addActionListener(e -> exportPng());
        }

        if (mode == Lab8Mode.ADVANCED) {
            controlPanel.getPerfectMazeButton().addActionListener(e -> generatePerfectMazeAnimated());
            controlPanel.getStopAnimationButton().addActionListener(e -> stopAnimation());
        }

        controlPanel.getExitButton().addActionListener(e -> frame.dispose());
    }

    private void createNewMaze() {
        stopAnimation();
        int rows = configPanel.getRows();
        int cols = configPanel.getCols();
        int cellSize = configPanel.getCellSize();

        maze = new Maze(rows, cols);
        configPanel.clampStartEndToMaze(rows, cols);

        mazePanel.setMaze(maze, cellSize);
        if (mode == Lab8Mode.HOMEWORK || mode == Lab8Mode.ADVANCED) {
            updateStartEndFromConfig();
        }
        frame.pack();
    }

    private void updateStartEndFromConfig() {
        if (maze == null) return;
        int sr = configPanel.getStartRow();
        int sc = configPanel.getStartCol();
        int er = configPanel.getEndRow();
        int ec = configPanel.getEndCol();
        mazePanel.setStartEnd(sr, sc, er, ec);
    }

    private void onWallToggleRequest(int row, int col, Wall wall) {
        if (maze == null) return;
        stopAnimation();
        maze.toggleWall(row, col, wall);
        mazePanel.clearPath();
        mazePanel.repaint();
    }

    private void validatePath() {
        if (maze == null) {
            JOptionPane.showMessageDialog(frame, "Create a maze first.");
            return;
        }
        stopAnimation();
        updateStartEndFromConfig();

        Point start = mazePanel.getStart();
        Point end = mazePanel.getEnd();
        List<Point> path = solver.findPathBfs(maze, start, end);

        if (path.isEmpty()) {
            mazePanel.showPath(List.of());
            JOptionPane.showMessageDialog(frame, "No path exists between start and end.");
        } else {
            mazePanel.showPath(path);
            JOptionPane.showMessageDialog(frame, "Path exists! Length: " + path.size());
        }
    }

    private void saveMaze() {
        if (maze == null) {
            JOptionPane.showMessageDialog(frame, "Create a maze first.");
            return;
        }
        stopAnimation();
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Maze");
        chooser.setFileFilter(new FileNameExtensionFilter("Maze file (*.maze)", "maze"));
        if (chooser.showSaveDialog(frame) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".maze")) {
            file = new File(file.getParentFile(), file.getName() + ".maze");
        }
        try {
            serializer.saveMaze(maze, file);
            JOptionPane.showMessageDialog(frame, "Saved: " + file.getAbsolutePath());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Save failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadMaze() {
        stopAnimation();
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Load Maze");
        chooser.setFileFilter(new FileNameExtensionFilter("Maze file (*.maze)", "maze"));
        if (chooser.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();
        try {
            Maze loaded = serializer.loadMaze(file);
            this.maze = loaded;

            configPanel.clampStartEndToMaze(loaded.getRows(), loaded.getCols());
            mazePanel.setMaze(loaded, configPanel.getCellSize());
            updateStartEndFromConfig();
            frame.pack();

            JOptionPane.showMessageDialog(frame, "Loaded: " + file.getAbsolutePath());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Load failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportPng() {
        if (maze == null) {
            JOptionPane.showMessageDialog(frame, "Create a maze first.");
            return;
        }
        stopAnimation();
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export Maze as PNG");
        chooser.setFileFilter(new FileNameExtensionFilter("PNG image (*.png)", "png"));
        if (chooser.showSaveDialog(frame) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".png")) {
            file = new File(file.getParentFile(), file.getName() + ".png");
        }
        try {
            exporter.exportComponentToPng(mazePanel, file);
            JOptionPane.showMessageDialog(frame, "Exported: " + file.getAbsolutePath());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void generatePerfectMazeAnimated() {
        if (maze == null) {
            JOptionPane.showMessageDialog(frame, "Create a maze first.");
            return;
        }

        stopAnimation();
        maze.resetAllWalls();
        mazePanel.clearPath();
        mazePanel.repaint();

        animationDelayMs.set(controlPanel.getAnimationDelayMs());
        cancelAnimation.set(false);

        activeWorker = generator.generatePerfectMazeAnimated(
                maze,
                animationDelayMs,
                cancelAnimation,
                p -> {
                    // repaint after each carve step
                    mazePanel.repaint();
                }
        );
    }

    private void stopAnimation() {
        cancelAnimation.set(true);
        if (activeWorker != null) {
            activeWorker.cancel(true);
            activeWorker = null;
        }
    }
}

