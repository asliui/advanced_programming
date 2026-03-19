package ro.uaic.asli.lab4;

import ro.uaic.asli.lab4.algorithm.CableSolution;
import ro.uaic.asli.lab4.algorithm.MaintenanceRoute;
import ro.uaic.asli.lab4.algorithm.MaintenanceRoutePlanner;
import ro.uaic.asli.lab4.algorithm.MinimumCablePlanner;
import ro.uaic.asli.lab4.generator.RandomCityGenerator;
import ro.uaic.asli.lab4.model.Intersection;
import ro.uaic.asli.lab4.model.Street;

import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Lab4App {
    public static void main(String[] args) {
        // Compulsory: create 10 intersections with streams.
        List<Intersection> intersections = IntStream.range(0, 10)
                .mapToObj(i -> new Intersection("I" + i, i * 10.0, (i % 3) * 12.0))
                .toList();

        // Compulsory: create LinkedList streets and sort with comparator lambda/method ref.
        LinkedList<Street> streets = new LinkedList<>();
        for (int i = 0; i < intersections.size() - 1; i++) {
            Intersection a = intersections.get(i);
            Intersection b = intersections.get(i + 1);
            streets.add(new Street("S" + i, a.distanceTo(b), a, b));
        }
        streets.add(new Street("S-extra-1", intersections.get(0).distanceTo(intersections.get(5)), intersections.get(0), intersections.get(5)));
        streets.add(new Street("S-extra-2", intersections.get(2).distanceTo(intersections.get(7)), intersections.get(2), intersections.get(7)));
        streets.sort(Comparator.comparingDouble(Street::getLength));

        // Compulsory: HashSet duplicate check.
        Set<Intersection> set = new HashSet<>(intersections);
        set.add(intersections.get(0));
        System.out.println("Intersections in HashSet (duplicate ignored): " + set.size());

        City city = new City("SampleCity", set, streets);

        // Homework: stream query.
        var longAndBusy = city.streetsLongerThanAndJoiningAtLeast(15.0, 3);
        System.out.println("Long streets with both endpoints degree >= 3:");
        longAndBusy.forEach(System.out::println);

        // Homework: list of possible minimum cable solutions.
        MinimumCablePlanner planner = new MinimumCablePlanner();
        List<CableSolution> solutions = planner.bestSolutions(city, 3);
        System.out.println("Candidate cable plans ordered by cost:");
        for (int i = 0; i < solutions.size(); i++) {
            CableSolution solution = solutions.get(i);
            System.out.println((i + 1) + ") cost=" + solution.totalCost() + ", edges=" + solution.streets().size());
        }

        // Advanced: 2-approx maintenance route.
        MaintenanceRoutePlanner routePlanner = new MaintenanceRoutePlanner();
        MaintenanceRoute route = routePlanner.twoApproximateRoute(city.getIntersections());
        System.out.println("Maintenance route length: " + route.totalLength());
        System.out.println("Route: " + route.tour().stream().map(Intersection::name).collect(Collectors.joining(" -> ")));

        // Homework/Advanced: random generator using third-party fake names.
        RandomCityGenerator generator = new RandomCityGenerator();
        City generated = generator.generate("GeneratedCity", 12, 0.25);
        System.out.println("Generated city: " + generated.getName()
                + ", intersections=" + generated.getIntersections().size()
                + ", streets=" + generated.getStreets().size());
    }
}
