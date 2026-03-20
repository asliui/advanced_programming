package ro.uaic.asli.lab4;

import ro.uaic.asli.lab4.algorithm.MaintenanceRoute;
import ro.uaic.asli.lab4.algorithm.MaintenanceRoutePlanner;
import ro.uaic.asli.lab4.generator.RandomCityGenerator;
import ro.uaic.asli.lab4.model.Intersection;

import java.util.stream.Collectors;

public final class AdvancedApp {
    public static void main(String[] args) {
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

