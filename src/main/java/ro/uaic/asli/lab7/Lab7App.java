package ro.uaic.asli.lab7;

/**
 * Lists Lab 7 entry points (mirrors {@code Lab6App} style) and starts the full Advanced stack.
 */
public final class Lab7App {

    private Lab7App() {
    }

    public static void main(String[] args) {
        System.out.println("--- LAB 7 (Spring Boot / REST) ---");
        System.out.println("Compulsory (GET movies):  ro.uaic.asli.lab7.Lab7CompulsoryApp");
        System.out.println("Homework (CRUD + Swagger): ro.uaic.asli.lab7.Lab7HomeworkApp");
        System.out.println("Advanced (Choco + JWT):    ro.uaic.asli.lab7.Lab7AdvancedApp  (default: Lab7Application)");
        System.out.println();
        Lab7AdvancedApp.main(args);
    }
}
