package ro.uaic.asli.lab11.app;

import ro.uaic.asli.lab10.app.Lab10Mode;

/**
 * Lab 11 — advanced tier (full quiz + virtual threads when available + JPA persistence).
 */
public final class Lab11AdvancedApp {

    private Lab11AdvancedApp() {
    }

    public static void main(String[] args) throws Exception {
        Lab11Launcher.launch(Lab10Mode.ADVANCED);
    }
}
