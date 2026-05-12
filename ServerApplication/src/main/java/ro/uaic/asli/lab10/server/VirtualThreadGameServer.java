package ro.uaic.asli.lab10.server;

import ro.uaic.asli.lab10.app.Lab10Launcher;
import ro.uaic.asli.lab10.app.Lab10Mode;

/**
 * Backward-compatible entry: same as {@link ro.uaic.asli.lab10.app.Lab10AdvancedApp}.
 */
public final class VirtualThreadGameServer {

    private VirtualThreadGameServer() {
    }

    public static void main(String[] args) throws Exception {
        Lab10Launcher.launch(Lab10Mode.ADVANCED);
    }
}
