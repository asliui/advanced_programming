package ro.uaic.asli.lab8;

import ro.uaic.asli.lab8.app.Lab8AdvancedApp;

public final class Lab8App {
    private Lab8App() {
    }

    public static void main(String[] args) {
        System.out.println("--- LAB 8 (Swing + Java2D) ---");
        System.out.println("Compulsory: ro.uaic.asli.lab8.app.Lab8CompulsoryApp");
        System.out.println("Homework:   ro.uaic.asli.lab8.app.Lab8HomeworkApp");
        System.out.println("Advanced:   ro.uaic.asli.lab8.app.Lab8AdvancedApp (default: ro.uaic.asli.lab8.app.Main)");
        System.out.println();
        Lab8AdvancedApp.main(args);
    }
}

