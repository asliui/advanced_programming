package ro.uaic.asli.lab9.view;

import ro.uaic.asli.lab9.concurrent.GameController;
import ro.uaic.asli.lab9.model.Maze;
import ro.uaic.asli.lab9.model.Position;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.Map;

/**
 * Optional Swing view: does not alter Lab 8; safe to skip and use console-only mode.
 */
public final class MazeGameGUI {

    private MazeGameGUI() {
    }

    public static void launch(GameController controller) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Lab 9 concurrent maze");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            MazePanel panel = new MazePanel(controller);
            frame.setContentPane(panel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            Timer timer = new Timer(400, e -> panel.repaint());
            timer.start();
        });
    }

    private static final class MazePanel extends JPanel {
        private final GameController controller;

        MazePanel(GameController controller) {
            this.controller = controller;
            Maze m = controller.getMaze();
            int cell = 14;
            setPreferredSize(new Dimension(m.getCols() * cell + 2, m.getRows() * cell + 2));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            Maze maze = controller.getMaze();
            int cell = 14;
            Map<Integer, Position> robots = controller.snapshotRobotPositions();
            Map<Integer, Position> bunnies = controller.snapshotBunnyPositions();
            Position exit = controller.getExitPosition();

            for (int r = 0; r < maze.getRows(); r++) {
                for (int c = 0; c < maze.getCols(); c++) {
                    int x = c * cell;
                    int y = r * cell;
                    g2.setColor(Color.WHITE);
                    g2.fillRect(x, y, cell, cell);
                    g2.setColor(Color.LIGHT_GRAY);
                    g2.drawRect(x, y, cell, cell);
                    var mc = maze.getCell(r, c);
                    g2.setColor(Color.DARK_GRAY);
                    if (mc.hasTopWall()) {
                        g2.fillRect(x, y, cell, 2);
                    }
                    if (mc.hasLeftWall()) {
                        g2.fillRect(x, y, 2, cell);
                    }
                    if (mc.hasRightWall()) {
                        g2.fillRect(x + cell - 2, y, 2, cell);
                    }
                    if (mc.hasBottomWall()) {
                        g2.fillRect(x, y + cell - 2, cell, 2);
                    }
                }
            }

            Map<Position, Character> overlay = new HashMap<>();
            if (maze.inBounds(exit.row(), exit.col())) {
                overlay.put(exit, 'E');
            }
            for (Position p : robots.values()) {
                overlay.put(p, 'R');
            }
            for (Position p : bunnies.values()) {
                overlay.put(p, 'B');
            }

            g2.setFont(g2.getFont().deriveFont(11f));
            for (Map.Entry<Position, Character> e : overlay.entrySet()) {
                Position p = e.getKey();
                int x = p.col() * cell + 4;
                int y = p.row() * cell + 12;
                if ('B' == e.getValue()) {
                    g2.setColor(Color.BLUE);
                } else if ('E' == e.getValue()) {
                    g2.setColor(new Color(0, 140, 0));
                } else {
                    g2.setColor(Color.RED);
                }
                g2.drawString(String.valueOf(e.getValue()), x, y);
            }
        }
    }
}
