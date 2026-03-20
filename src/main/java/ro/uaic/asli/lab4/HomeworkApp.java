package ro.uaic.asli.lab4;

import ro.uaic.asli.lab4.algorithm.CableSolution;
import ro.uaic.asli.lab4.algorithm.MinimumCablePlanner;
import ro.uaic.asli.lab4.generator.RandomCityGenerator;

import java.util.List;

public final class HomeworkApp {
    public static void main(String[] args) {
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
}

