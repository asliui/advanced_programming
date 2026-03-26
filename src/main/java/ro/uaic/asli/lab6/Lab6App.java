package ro.uaic.asli.lab6;

public final class Lab6App {
    public static void main(String[] args) {
        System.out.println("--- LAB 6 ---");
        CompulsoryApp.main(args);
        System.out.println("\n--- LAB 6 HOMEWORK ---");
        HomeworkApp.main(args);
        System.out.println("\n--- LAB 6 ADVANCED ---");
        ro.uaic.asli.lab6.service.AdvancedApp.main(args);
    }
}

