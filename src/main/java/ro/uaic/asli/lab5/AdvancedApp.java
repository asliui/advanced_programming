package ro.uaic.asli.lab5;

import ro.uaic.asli.lab5.algorithm.RandomInstanceGenerator;
import ro.uaic.asli.lab5.algorithm.SetCoverSolver;

public final class AdvancedApp {
    public static void main(String[] args) {
        var generator = new RandomInstanceGenerator();
        var universe = generator.conceptUniverse(200);
        var resources = generator.resources(5000, universe, 3, 10);

        var solver = new SetCoverSolver();
        long start = System.nanoTime();
        var cover = solver.greedyCover(universe, resources);
        long end = System.nanoTime();

        var covered = cover.stream()
                .flatMap(r -> r.getConcepts().stream())
                .collect(java.util.stream.Collectors.toSet());

        System.out.println("Concept universe size: " + universe.size());
        System.out.println("Resources count: " + resources.size());
        System.out.println("Cover size (greedy): " + cover.size());
        System.out.println("Covered concepts: " + covered.size());
        System.out.println("Execution time ms: " + ((end - start) / 1_000_000.0));
    }
}
