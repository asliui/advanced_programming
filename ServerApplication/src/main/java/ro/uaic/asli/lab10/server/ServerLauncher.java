package ro.uaic.asli.lab10.server;

import ro.uaic.asli.lab10.app.Lab10Launcher;
import ro.uaic.asli.lab10.app.Lab10Mode;

/**
 * Backward-compatible entry: same as {@link ro.uaic.asli.lab10.app.Lab10HomeworkApp}.
 */
public final class ServerLauncher {

    private ServerLauncher() {
    }

    public static void main(String[] args) throws Exception {
        Lab10Launcher.launch(Lab10Mode.HOMEWORK);
    }
}
