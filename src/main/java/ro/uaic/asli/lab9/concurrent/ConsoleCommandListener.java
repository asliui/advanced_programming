package ro.uaic.asli.lab9.concurrent;

import ro.uaic.asli.lab9.actors.Bunny;
import ro.uaic.asli.lab9.actors.Robot;
import ro.uaic.asli.lab9.view.MazeConsoleRenderer;

import java.util.List;
import java.util.Locale;
import java.util.Scanner;

/**
 * Reads {@link System#in} on a dedicated thread. Typing {@code help} lists valid commands.
 * Note: {@code Scanner#nextLine()} blocks; after {@code stop}, you may need to press Enter once to unblock input.
 */
public final class ConsoleCommandListener implements CommandListener {

    private final Scanner scanner;
    private final GameController controller;
    private final List<Bunny> bunnies;
    private final List<Robot> robots;

    public ConsoleCommandListener(Scanner scanner, GameController controller, List<Bunny> bunnies, List<Robot> robots) {
        this.scanner = scanner;
        this.controller = controller;
        this.bunnies = bunnies;
        this.robots = robots;
    }

    @Override
    public void run() {
        printHelp();
        try {
            while (!Thread.currentThread().isInterrupted()) {
                if (!controller.isGameRunning() && controller.getState() != GameState.RUNNING) {
                    break;
                }
                if (!scanner.hasNextLine()) {
                    break;
                }
                String line = scanner.nextLine();
                if (line == null) {
                    break;
                }
                handle(line.trim());
            }
        } catch (Exception ignored) {
            // stdin closed or interrupted
        }
    }

    private void handle(String line) {
        if (line.isEmpty()) {
            return;
        }
        String[] parts = line.toLowerCase(Locale.ROOT).split("\\s+");
        String cmd = parts[0];
        switch (cmd) {
            case "help", "?" -> printHelp();
            case "status" -> printStatus();
            case "stop" -> controller.stopGame(GameState.USER_STOP, "User typed stop");
            case "pause" -> handlePause(parts);
            case "resume" -> handleResume(parts);
            case "speed" -> handleSpeed(parts);
            case "slow" -> handleSlow(parts);
            case "fast" -> handleFast(parts);
            default -> System.out.println("Unknown command. Type help.");
        }
    }

    private void handlePause(String[] parts) {
        if (parts.length >= 2 && "all".equals(parts[1])) {
            controller.setGlobalPaused(true);
            return;
        }
        if (parts.length >= 3 && "robot".equals(parts[1])) {
            try {
                int id = Integer.parseInt(parts[2]);
                findRobot(id).ifPresentOrElse(Robot::pause, () -> System.out.println("No robot id " + id));
            } catch (NumberFormatException e) {
                System.out.println("Invalid robot id");
            }
            return;
        }
        if (parts.length >= 2 && "bunny".equals(parts[1])) {
            if (parts.length >= 3) {
                try {
                    int id = Integer.parseInt(parts[2]);
                    findBunny(id).ifPresentOrElse(Bunny::pause, () -> System.out.println("No bunny id " + id));
                } catch (NumberFormatException e) {
                    System.out.println("Invalid bunny id");
                }
                return;
            }
            bunnies.forEach(Bunny::pause);
            return;
        }
        System.out.println("Usage: pause all | pause robot <id> | pause bunny [<id>]");
    }

    private void handleResume(String[] parts) {
        if (parts.length >= 2 && "all".equals(parts[1])) {
            controller.setGlobalPaused(false);
            return;
        }
        if (parts.length >= 3 && "robot".equals(parts[1])) {
            try {
                int id = Integer.parseInt(parts[2]);
                findRobot(id).ifPresentOrElse(Robot::resume, () -> System.out.println("No robot id " + id));
            } catch (NumberFormatException e) {
                System.out.println("Invalid robot id");
            }
            return;
        }
        if (parts.length >= 2 && "bunny".equals(parts[1])) {
            if (parts.length >= 3) {
                try {
                    int id = Integer.parseInt(parts[2]);
                    findBunny(id).ifPresentOrElse(Bunny::resume, () -> System.out.println("No bunny id " + id));
                } catch (NumberFormatException e) {
                    System.out.println("Invalid bunny id");
                }
                return;
            }
            bunnies.forEach(Bunny::resume);
            return;
        }
        System.out.println("Usage: resume all | resume robot <id> | resume bunny [<id>]");
    }

    private void handleSpeed(String[] parts) {
        try {
            if (parts.length >= 3 && "robots".equals(parts[1])) {
                long ms = Long.parseLong(parts[2]);
                robots.forEach(r -> r.setMoveDelayMs(ms));
                return;
            }
            if (parts.length >= 3 && "bunny".equals(parts[1])) {
                if (parts.length >= 4) {
                    int id = Integer.parseInt(parts[2]);
                    long ms = Long.parseLong(parts[3]);
                    findBunny(id).ifPresentOrElse(b -> b.setMoveDelayMs(ms), () -> System.out.println("No bunny id " + id));
                    return;
                }
                long ms = Long.parseLong(parts[2]);
                bunnies.forEach(b -> b.setMoveDelayMs(ms));
                return;
            }
            if (parts.length >= 4 && "robot".equals(parts[1])) {
                int id = Integer.parseInt(parts[2]);
                long ms = Long.parseLong(parts[3]);
                findRobot(id).ifPresentOrElse(r -> r.setMoveDelayMs(ms), () -> System.out.println("No robot id " + id));
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid number");
            return;
        }
        System.out.println("Usage: speed robots <ms> | speed bunny <ms> | speed bunny <id> <ms> | speed robot <id> <ms>");
    }

    private void handleSlow(String[] parts) {
        if (parts.length >= 2 && "robots".equals(parts[1])) {
            robots.forEach(r -> r.setMoveDelayMs((long) (r.getMoveDelayMs() * 1.5)));
            return;
        }
        if (parts.length >= 3 && "robot".equals(parts[1])) {
            try {
                int id = Integer.parseInt(parts[2]);
                findRobot(id).ifPresentOrElse(
                        r -> r.setMoveDelayMs((long) (r.getMoveDelayMs() * 1.5)),
                        () -> System.out.println("No robot id " + id));
            } catch (NumberFormatException e) {
                System.out.println("Invalid robot id");
            }
            return;
        }
        if (parts.length >= 2 && "bunny".equals(parts[1])) {
            if (parts.length >= 3) {
                try {
                    int id = Integer.parseInt(parts[2]);
                    findBunny(id).ifPresentOrElse(
                            b -> b.setMoveDelayMs((long) (b.getMoveDelayMs() * 1.5)),
                            () -> System.out.println("No bunny id " + id));
                } catch (NumberFormatException e) {
                    System.out.println("Invalid bunny id");
                }
                return;
            }
            bunnies.forEach(b -> b.setMoveDelayMs((long) (b.getMoveDelayMs() * 1.5)));
            return;
        }
        System.out.println("Usage: slow robots | slow robot <id> | slow bunny [<id>]");
    }

    private void handleFast(String[] parts) {
        if (parts.length >= 2 && "robots".equals(parts[1])) {
            robots.forEach(r -> r.setMoveDelayMs(Math.max(0, (long) (r.getMoveDelayMs() / 1.5))));
            return;
        }
        if (parts.length >= 3 && "robot".equals(parts[1])) {
            try {
                int id = Integer.parseInt(parts[2]);
                findRobot(id).ifPresentOrElse(
                        r -> r.setMoveDelayMs(Math.max(0, (long) (r.getMoveDelayMs() / 1.5))),
                        () -> System.out.println("No robot id " + id));
            } catch (NumberFormatException e) {
                System.out.println("Invalid robot id");
            }
            return;
        }
        if (parts.length >= 2 && "bunny".equals(parts[1])) {
            if (parts.length >= 3) {
                try {
                    int id = Integer.parseInt(parts[2]);
                    findBunny(id).ifPresentOrElse(
                            b -> b.setMoveDelayMs(Math.max(0, (long) (b.getMoveDelayMs() / 1.5))),
                            () -> System.out.println("No bunny id " + id));
                } catch (NumberFormatException e) {
                    System.out.println("Invalid bunny id");
                }
                return;
            }
            bunnies.forEach(b -> b.setMoveDelayMs(Math.max(0, (long) (b.getMoveDelayMs() / 1.5))));
            return;
        }
        System.out.println("Usage: fast robots | fast robot <id> | fast bunny [<id>]");
    }

    private java.util.Optional<Robot> findRobot(int id) {
        return robots.stream().filter(r -> r.getId() == id).findFirst();
    }

    private java.util.Optional<Bunny> findBunny(int bunnyId) {
        return bunnies.stream().filter(b -> b.getBunnyId() == bunnyId).findFirst();
    }

    private void printStatus() {
        synchronized (System.out) {
            System.out.println(MazeConsoleRenderer.renderWithInteriorWalls(controller));
            System.out.println("state=" + controller.getState() + " detail=" + controller.getEndReasonDetail());
            for (Bunny b : bunnies) {
                System.out.println(b.getActorName() + " delay=" + b.getMoveDelayMs() + "ms paused=" + b.isPaused());
            }
            for (Robot r : robots) {
                System.out.println(r.getActorName() + " delay=" + r.getMoveDelayMs() + "ms paused=" + r.isPaused());
            }
        }
    }

    private void printHelp() {
        System.out.println("""
                Commands:
                  status
                  stop
                  pause all | resume all
                  speed robots <ms> | speed bunny <ms> | speed bunny <id> <ms> | speed robot <id> <ms>
                  slow robots | fast robots | slow robot <id> | fast robot <id>
                  slow bunny | fast bunny | slow bunny <id> | fast bunny <id>
                  pause robot <id> | resume robot <id>
                  pause bunny [<id>] | resume bunny [<id>]
                  help
                """);
    }
}
