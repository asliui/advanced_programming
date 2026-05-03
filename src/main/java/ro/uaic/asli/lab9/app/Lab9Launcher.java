package ro.uaic.asli.lab9.app;

import ro.uaic.asli.lab8.service.MazeSerializer;
import ro.uaic.asli.lab9.actors.Bunny;
import ro.uaic.asli.lab9.actors.Robot;
import ro.uaic.asli.lab9.concurrent.CommandListener;
import ro.uaic.asli.lab9.concurrent.ConsoleCommandListener;
import ro.uaic.asli.lab9.concurrent.GameController;
import ro.uaic.asli.lab9.concurrent.GameState;
import ro.uaic.asli.lab9.concurrent.ManagerThread;
import ro.uaic.asli.lab9.concurrent.SharedMemory;
import ro.uaic.asli.lab9.model.Maze;
import ro.uaic.asli.lab9.model.Position;
import ro.uaic.asli.lab9.service.Lab9MazeGenerator;
import ro.uaic.asli.lab9.util.Lab8MazeAdapter;
import ro.uaic.asli.lab9.view.MazeGameGUI;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/**
 * Shared bootstrap for Lab 9. Mode-specific defaults are appended after user arguments so the
 * first matching flag wins (user overrides mode defaults).
 */
public final class Lab9Launcher {

    private Lab9Launcher() {
    }

    public static void launch(Lab9Mode mode, String[] userArgs) throws Exception {
        String[] merged = mergeArgs(userArgs, defaultArgsFor(mode));
        runSimulation(merged, mode);
    }

    private static String[] mergeArgs(String[] userArgs, String[] modeDefaults) {
        if (userArgs == null || userArgs.length == 0) {
            return modeDefaults;
        }
        String[] out = Arrays.copyOf(userArgs, userArgs.length + modeDefaults.length);
        System.arraycopy(modeDefaults, 0, out, userArgs.length, modeDefaults.length);
        return out;
    }

    /**
     * Defaults applied only when the same flag is not already present in user args (user args come first).
     */
    private static String[] defaultArgsFor(Lab9Mode mode) {
        return switch (mode) {
            case COMPULSORY -> new String[]{
                    "--rows", "9",
                    "--cols", "11",
                    "--robots", "2",
                    "--bunnies", "1",
                    "--time-ms", "90000",
                    "--print-ms", "2500",
                    "--sense", "1"
            };
            case HOMEWORK -> new String[]{
                    "--rows", "13",
                    "--cols", "19",
                    "--robots", "3",
                    "--bunnies", "1",
                    "--time-ms", "60000",
                    "--print-ms", "1000",
                    "--sense", "2",
                    "--ascii-walls"
            };
            case ADVANCED -> new String[]{
                    "--rows", "17",
                    "--cols", "23",
                    "--robots", "4",
                    "--bunnies", "2",
                    "--time-ms", "120000",
                    "--print-ms", "800",
                    "--sense", "3",
                    "--ascii-walls",
                    "--gui"
            };
        };
    }

    private static void runSimulation(String[] args, Lab9Mode mode) throws Exception {
        int rows = intArg(args, "--rows", 13);
        int cols = intArg(args, "--cols", 19);
        int robotCount = intArg(args, "--robots", 3);
        int bunnyCount = intArg(args, "--bunnies", 1);
        long timeLimitMs = longArg(args, "--time-ms", 60_000L);
        long printMs = longArg(args, "--print-ms", 1000L);
        int senseRange = intArg(args, "--sense", 2);
        boolean gui = flag(args, "--gui");
        boolean detailedAscii = flag(args, "--ascii-walls");
        Long seed = longOptional(args, "--seed");

        Random random = seed == null ? new Random() : new Random(seed);

        Maze maze;
        if (stringArg(args, "--load") != null) {
            File f = new File(stringArg(args, "--load"));
            MazeSerializer serializer = new MazeSerializer();
            ro.uaic.asli.lab8.model.Maze lab8 = serializer.loadMaze(f);
            maze = Lab8MazeAdapter.copyFrom(lab8);
            rows = maze.getRows();
            cols = maze.getCols();
        } else {
            maze = new Maze(rows, cols);
            Lab9MazeGenerator.generatePerfectMaze(maze, random);
        }

        Position exit = new Position(rows - 1, cols - 1);
        int need = robotCount + bunnyCount;
        SharedMemory memory = new SharedMemory();
        GameController controller = new GameController(maze, exit, memory);
        List<Position> picks = new ArrayList<>(controller.pickDistinctRandomCells(random, need, new Position(0, 0), exit));

        double bunnyBfsBias = switch (mode) {
            case COMPULSORY -> 0.0;
            case HOMEWORK -> 0.25;
            case ADVANCED -> 0.35;
        };
        long bunnyDelay = switch (mode) {
            case COMPULSORY -> 450;
            case HOMEWORK -> 350;
            case ADVANCED -> 280;
        };
        long robotDelay = switch (mode) {
            case COMPULSORY -> 500;
            case HOMEWORK -> 400;
            case ADVANCED -> 320;
        };

        List<Bunny> bunnies = new ArrayList<>();
        List<Robot> robots = new ArrayList<>();
        int idx = 0;
        for (int b = 0; b < bunnyCount; b++) {
            Position p = picks.get(idx++);
            controller.initBunny(b, p);
            bunnies.add(new Bunny(b, controller, random, bunnyDelay, bunnyBfsBias));
        }
        for (int r = 0; r < robotCount; r++) {
            Position p = picks.get(idx++);
            controller.initRobot(r, p);
            robots.add(new Robot(r, controller, random, robotDelay, senseRange, mode));
        }

        for (Bunny bunny : bunnies) {
            Thread t = new Thread(bunny, bunny.getActorName());
            controller.registerActorThread(t);
            t.start();
        }
        for (Robot robot : robots) {
            Thread t = new Thread(robot, robot.getActorName());
            controller.registerActorThread(t);
            t.start();
        }

        ManagerThread manager = new ManagerThread(controller, printMs, timeLimitMs, detailedAscii);
        controller.setManagerThread(manager);
        manager.start();

        CommandListener commands = new ConsoleCommandListener(new Scanner(System.in), controller, bunnies, robots);
        Thread cmdThread = new Thread(commands, "Lab9-Commands");
        cmdThread.setDaemon(true);
        controller.setCommandListenerThread(cmdThread);
        cmdThread.start();

        if (gui) {
            MazeGameGUI.launch(controller);
        }

        System.out.println("Lab 9 (" + mode + ") simulation running. Type help for commands.");

        while (controller.isGameRunning()) {
            Thread.sleep(150);
        }

        controller.joinAllActors();

        System.out.println("Finished: " + controller.getState() + " - " + controller.getEndReasonDetail());
        if (controller.getState() == GameState.BUNNY_ESCAPED) {
            System.out.println("Outcome: bunnies reached the exit.");
        } else if (controller.getState() == GameState.BUNNY_CAUGHT) {
            System.out.println("Outcome: all bunnies were caught (or last one lost).");
        } else if (controller.getState() == GameState.TIME_LIMIT) {
            System.out.println("Outcome: time limit.");
        } else if (controller.getState() == GameState.USER_STOP) {
            System.out.println("Outcome: user stop.");
        }
    }

    private static boolean flag(String[] args, String name) {
        for (String a : args) {
            if (name.equals(a)) {
                return true;
            }
        }
        return false;
    }

    private static int intArg(String[] args, String name, int def) {
        Long v = longOptional(args, name);
        return v == null ? def : Math.max(1, (int) (long) v);
    }

    private static long longArg(String[] args, String name, long def) {
        Long v = longOptional(args, name);
        return v == null ? def : v;
    }

    private static Long longOptional(String[] args, String name) {
        for (int i = 0; i < args.length - 1; i++) {
            if (name.equals(args[i])) {
                return Long.parseLong(args[i + 1]);
            }
        }
        return null;
    }

    private static String stringArg(String[] args, String name) {
        for (int i = 0; i < args.length - 1; i++) {
            if (name.equals(args[i])) {
                return args[i + 1];
            }
        }
        return null;
    }
}
