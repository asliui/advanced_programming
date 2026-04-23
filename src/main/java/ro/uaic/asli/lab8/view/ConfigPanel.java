package ro.uaic.asli.lab8.view;

import ro.uaic.asli.lab8.app.Lab8Mode;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import java.awt.FlowLayout;

public class ConfigPanel extends JPanel {
    private final JSpinner rowsSpinner = new JSpinner(new SpinnerNumberModel(10, 2, 80, 1));
    private final JSpinner colsSpinner = new JSpinner(new SpinnerNumberModel(15, 2, 120, 1));
    private final JSpinner cellSizeSpinner = new JSpinner(new SpinnerNumberModel(32, 10, 80, 1));

    private final JLabel startLabel = new JLabel("Start (r,c):");
    private final JSpinner startRowSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 200, 1));
    private final JSpinner startColSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 200, 1));
    private final JLabel endLabel = new JLabel("End (r,c):");
    private final JSpinner endRowSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 200, 1));
    private final JSpinner endColSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 200, 1));

    private final JButton drawButton = new JButton("Draw / Create Grid");

    public ConfigPanel(Lab8Mode mode) {
        super(new FlowLayout(FlowLayout.LEFT));
        setBorder(BorderFactory.createTitledBorder("Configuration"));

        add(new JLabel("Rows:"));
        add(rowsSpinner);
        add(new JLabel("Cols:"));
        add(colsSpinner);
        add(new JLabel("Cell size:"));
        add(cellSizeSpinner);

        add(startLabel);
        add(startRowSpinner);
        add(startColSpinner);

        add(endLabel);
        add(endRowSpinner);
        add(endColSpinner);

        add(drawButton);

        boolean showPathControls = mode == Lab8Mode.HOMEWORK || mode == Lab8Mode.ADVANCED;
        setPathControlsVisible(showPathControls);
    }

    public int getRows() {
        return (Integer) rowsSpinner.getValue();
    }

    public int getCols() {
        return (Integer) colsSpinner.getValue();
    }

    public int getCellSize() {
        return (Integer) cellSizeSpinner.getValue();
    }

    public int getStartRow() {
        return (Integer) startRowSpinner.getValue();
    }

    public int getStartCol() {
        return (Integer) startColSpinner.getValue();
    }

    public int getEndRow() {
        return (Integer) endRowSpinner.getValue();
    }

    public int getEndCol() {
        return (Integer) endColSpinner.getValue();
    }

    public JButton getDrawButton() {
        return drawButton;
    }

    public void setPathControlsVisible(boolean visible) {
        startLabel.setVisible(visible);
        startRowSpinner.setVisible(visible);
        startColSpinner.setVisible(visible);
        endLabel.setVisible(visible);
        endRowSpinner.setVisible(visible);
        endColSpinner.setVisible(visible);
    }

    public void clampStartEndToMaze(int rows, int cols) {
        ((SpinnerNumberModel) startRowSpinner.getModel()).setMaximum(rows - 1);
        ((SpinnerNumberModel) startColSpinner.getModel()).setMaximum(cols - 1);
        ((SpinnerNumberModel) endRowSpinner.getModel()).setMaximum(rows - 1);
        ((SpinnerNumberModel) endColSpinner.getModel()).setMaximum(cols - 1);
    }
}

