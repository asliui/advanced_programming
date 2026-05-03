package ro.uaic.asli.lab9;

import ro.uaic.asli.lab9.app.Lab9AdvancedApp;
import ro.uaic.asli.lab9.app.Lab9Launcher;
import ro.uaic.asli.lab9.app.Lab9Mode;

/**
 * Umbrella entry (same pattern as {@code ro.uaic.asli.lab8.Lab8App}): prints tier class names, then runs Advanced.
 */
public final class Lab9Main {

    private Lab9Main() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length > 0 && ("--help".equals(args[0]) || "-h".equals(args[0]))) {
            printBanner();
            return;
        }
        if (args.length > 0 && "--compulsory".equals(args[0])) {
            Lab9Launcher.launch(Lab9Mode.COMPULSORY, tail(args));
            return;
        }
        if (args.length > 0 && "--homework".equals(args[0])) {
            Lab9Launcher.launch(Lab9Mode.HOMEWORK, tail(args));
            return;
        }
        printBanner();
        Lab9AdvancedApp.main(args);
    }

    private static void printBanner() {
        System.out.println("--- LAB 9 (concurrent maze) ---");
        System.out.println("Compulsory: ro.uaic.asli.lab9.app.Lab9CompulsoryApp");
        System.out.println("Homework:   ro.uaic.asli.lab9.app.Lab9HomeworkApp");
        System.out.println("Advanced:   ro.uaic.asli.lab9.app.Lab9AdvancedApp (default via ro.uaic.asli.lab9.Lab9Main)");
        System.out.println("Optional: first arg --compulsory or --homework to run that tier with remaining args.");
        System.out.println("Flags: --rows, --cols, --robots, --bunnies, --time-ms, --print-ms, --sense, --ascii-walls, --gui, --load <file>, --seed <n>");
        System.out.println();
    }

    private static String[] tail(String[] args) {
        if (args.length <= 1) {
            return new String[0];
        }
        String[] rest = new String[args.length - 1];
        System.arraycopy(args, 1, rest, 0, rest.length);
        return rest;
    }
}
