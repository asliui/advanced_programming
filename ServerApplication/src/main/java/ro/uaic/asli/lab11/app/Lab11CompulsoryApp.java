package ro.uaic.asli.lab11.app;

import ro.uaic.asli.lab10.app.Lab10Mode;

/**
 * Lab 11 — compulsory tier (echo + {@code stop}). Same behaviour as {@link ro.uaic.asli.lab10.app.Lab10CompulsoryApp};
 * no JPA (Spring context is not started).
 */
public final class Lab11CompulsoryApp {

    private Lab11CompulsoryApp() {
    }

    public static void main(String[] args) throws Exception {
        Lab11Launcher.launch(Lab10Mode.COMPULSORY);
    }
}
