package ro.uaic.asli.lab8.view;

import ro.uaic.asli.lab8.app.Lab8Mode;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import java.awt.FlowLayout;

public class ControlPanel extends JPanel {
    private final JButton createButton = new JButton("Create (Random Remove Walls)");
    private final JButton resetButton = new JButton("Reset");
    private final JButton validateButton = new JButton("Validate Path");
    private final JButton saveButton = new JButton("Save");
    private final JButton loadButton = new JButton("Load");
    private final JButton exportPngButton = new JButton("Export PNG");
    private final JButton perfectMazeButton = new JButton("Generate Perfect Maze");
    private final JButton stopAnimationButton = new JButton("Stop");
    private final JButton exitButton = new JButton("Exit");

    private final JSlider speedSlider = new JSlider(0, 300, 60);

    public ControlPanel(Lab8Mode mode) {
        super(new FlowLayout(FlowLayout.LEFT));
        setBorder(BorderFactory.createTitledBorder("Controls"));

        add(createButton);
        add(resetButton);
        add(validateButton);
        add(saveButton);
        add(loadButton);
        add(exportPngButton);

        add(perfectMazeButton);
        add(stopAnimationButton);

        add(new JLabel("Speed:"));
        speedSlider.setToolTipText("Animation delay (ms) - lower is faster");
        speedSlider.setMajorTickSpacing(100);
        speedSlider.setMinorTickSpacing(20);
        speedSlider.setPaintTicks(true);
        add(speedSlider);

        add(exitButton);

        boolean homework = mode == Lab8Mode.HOMEWORK || mode == Lab8Mode.ADVANCED;
        boolean advanced = mode == Lab8Mode.ADVANCED;

        validateButton.setVisible(homework);
        saveButton.setVisible(homework);
        loadButton.setVisible(homework);
        exportPngButton.setVisible(homework);

        perfectMazeButton.setVisible(advanced);
        stopAnimationButton.setVisible(advanced);
        speedSlider.setVisible(advanced);
    }

    public JButton getCreateButton() {
        return createButton;
    }

    public JButton getResetButton() {
        return resetButton;
    }

    public JButton getExitButton() {
        return exitButton;
    }

    public JButton getValidateButton() {
        return validateButton;
    }

    public JButton getSaveButton() {
        return saveButton;
    }

    public JButton getLoadButton() {
        return loadButton;
    }

    public JButton getExportPngButton() {
        return exportPngButton;
    }

    public JButton getPerfectMazeButton() {
        return perfectMazeButton;
    }

    public JButton getStopAnimationButton() {
        return stopAnimationButton;
    }

    public int getAnimationDelayMs() {
        return speedSlider.getValue();
    }
}

