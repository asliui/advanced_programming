package ro.uaic.asli.lab8.app;

import javax.swing.SwingUtilities;

public final class Lab8Launcher {
    private Lab8Launcher() {
    }

    public static void launch(Lab8Mode mode) {
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame(mode);
            frame.setVisible(true);
        });
    }
}

