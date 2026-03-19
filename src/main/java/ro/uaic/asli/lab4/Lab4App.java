package ro.uaic.asli.lab4;

import ro.uaic.asli.lab4.algorithm.CableSolution;
import ro.uaic.asli.lab4.algorithm.MaintenanceRoute;
import ro.uaic.asli.lab4.algorithm.MaintenanceRoutePlanner;
import ro.uaic.asli.lab4.algorithm.MinimumCablePlanner;
import ro.uaic.asli.lab4.generator.RandomCityGenerator;
import ro.uaic.asli.lab4.model.Intersection;
import ro.uaic.asli.lab4.model.Street;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Lab4App {
    public static void main(String[] args) {
        System.out.println("--- COMPULSORY (1p) ---");
        runCompulsory();

        System.out.println("\n--- HOMEWORK (2p) ---");
        runHomework();

        System.out.println("\n--- ADVANCED (2p) ---");
        runAdvanced();
    }

    /**
     * Goal: Basic OOP model, Java Streams for creation,
     * LinkedList sorting with method reference, and HashSet duplicate check.
     */
    private static void runCompulsory() {
        List<Intersection> intersections = IntStream.range(0, 10)
                .mapToObj(i -> new Intersection("V" + i, i * 5.0, i * 2.0))
                .toList();
        System.out.println("Created " + intersections.size() + " intersections using Stream API.");

        LinkedList<Street> streets = new LinkedList<>();
        streets.add(new Street("Main St", 15.5, intersections.get(0), intersections.get(1)));
        streets.add(new Street("Oak Ave", 10.2, intersections.get(1), intersections.get(2)));
        streets.add(new Street("Short Lane", 5.0, intersections.get(2), intersections.get(3)));

        streets.sort(Street::compareTo);
        System.out.println("Streets sorted by length (Compulsory check):");
        streets.forEach(System.out::println);

        Set<Intersection> intersectionSet = new HashSet<>(intersections);
        intersectionSet.add(intersections.get(0));
        System.out.println("HashSet size (Should be 10): " + intersectionSet.size());
    }

    /**
     * Goal: City class, JGraphT for MST, DataFaker for names,
     * and Stream queries for specific data filtering.
     */
    private static void runHomework() {
        RandomCityGenerator generator = new RandomCityGenerator();
        City city = generator.generate("IasiCity", 15, 0.3);
        System.out.println("City Generated: " + city.getName() + " with Faker names.");

        MinimumCablePlanner planner = new MinimumCablePlanner();
        CableSolution mstSolution = planner.minimumCostSolution(city);
        System.out.println("Optimal Cable Solution Cost: " + String.format("%.2f", mstSolution.totalCost()));

        var filteredStreets = city.streetsLongerThanAndJoiningAtLeast(50.0, 3);
        System.out.println("Filtered Streets (Long & Busy): " + filteredStreets.size());

        List<CableSolution> topSolutions = planner.bestSolutions(city, 3);
        System.out.println("Top 3 alternative solutions generated and sorted by cost.");
        for (int i = 0; i < topSolutions.size(); i++) {
            var solution = topSolutions.get(i);
            System.out.println((i + 1) + ") cost=" + String.format("%.2f", solution.totalCost())
                    + ", edges=" + solution.streets().size());
        }
    }

    /**
     * Goal: TSP 2-Approximation (maintenance route),
     * Euclidean random problem generation (triangle inequality).
     */
    private static void runAdvanced() {
        RandomCityGenerator generator = new RandomCityGenerator();
        City advancedCity = generator.generate("AdvancedIasi", 20, 0.5);
        System.out.println("Random problem generated. Distances satisfy triangle inequality.");

        MaintenanceRoutePlanner routePlanner = new MaintenanceRoutePlanner();
        MaintenanceRoute route = routePlanner.twoApproximateRoute(advancedCity.getIntersections());

        System.out.println("Maintenance Route Found (TSP 2-Approx):");
        System.out.println("Total Length: " + String.format("%.2f", route.totalLength()));
        System.out.println("Route Path: " + route.tour().stream()
                .map(Intersection::name)
                .collect(Collectors.joining(" -> ")));
    }
}
