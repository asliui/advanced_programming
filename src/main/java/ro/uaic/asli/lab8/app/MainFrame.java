package ro.uaic.asli.lab8.app;

import ro.uaic.asli.lab8.controller.MazeController;
import ro.uaic.asli.lab8.view.ConfigPanel;
import ro.uaic.asli.lab8.view.ControlPanel;
import ro.uaic.asli.lab8.view.MazePanel;

import javax.swing.JFrame;
import java.awt.BorderLayout;

public class MainFrame extends JFrame {

    public MainFrame(Lab8Mode mode) {
        super("Lab 8 - Maze Builder (Swing + Java2D)");

        MazePanel mazePanel = new MazePanel();
        ConfigPanel configPanel = new ConfigPanel(mode);
        ControlPanel controlPanel = new ControlPanel(mode);

        setLayout(new BorderLayout());
        add(configPanel, BorderLayout.NORTH);
        add(mazePanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 800);
        setLocationRelativeTo(null);

        new MazeController(mode, this, configPanel, mazePanel, controlPanel).init();
    }
}

