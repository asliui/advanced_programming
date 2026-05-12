package ro.uaic.asli.lab11.app;

import ro.uaic.asli.lab10.app.Lab10Launcher;
import ro.uaic.asli.lab10.app.Lab10Mode;

/**
 * Lab 11 entry: same TCP server as Lab 10, with JPA persistence for homework/advanced tiers.
 * Delegates to {@link Lab10Launcher} (persistence is started automatically for {@link Lab10Mode#HOMEWORK}
 * and {@link Lab10Mode#ADVANCED}).
 */
public final class Lab11Launcher {

    private Lab11Launcher() {
    }

    public static void launch(Lab10Mode mode) throws Exception {
        Lab10Launcher.launch(mode);
    }
}
