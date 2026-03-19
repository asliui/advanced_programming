package ro.uaic.asli.lab4.generator;

import net.datafaker.Faker;
import ro.uaic.asli.lab4.City;
import ro.uaic.asli.lab4.model.Intersection;
import ro.uaic.asli.lab4.model.Street;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.IntStream;

public final class RandomCityGenerator {
    private final Faker faker = new Faker();
    private final Random random = new Random(42);

    public City generate(String cityName, int intersectionCount, double extraStreetProbability) {
        Set<Intersection> intersections = IntStream.range(0, intersectionCount)
                .mapToObj(i -> new Intersection(
                        faker.address().streetName() + "-" + i,
                        random.nextDouble(0, 1000),
                        random.nextDouble(0, 1000)))
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));

        List<Intersection> list = new ArrayList<>(intersections);
        List<Street> streets = new ArrayList<>();

        // Ensure connectivity with a chain.
        for (int i = 0; i < list.size() - 1; i++) {
            Intersection a = list.get(i);
            Intersection b = list.get(i + 1);
            streets.add(new Street(
                    faker.address().streetName(),
                    a.distanceTo(b),
                    a,
                    b
            ));
        }

        // Add extra random streets; Euclidean lengths satisfy triangle inequality.
        for (int i = 0; i < list.size(); i++) {
            for (int j = i + 2; j < list.size(); j++) {
                if (random.nextDouble() < extraStreetProbability) {
                    Intersection a = list.get(i);
                    Intersection b = list.get(j);
                    streets.add(new Street(
                            faker.address().streetName(),
                            a.distanceTo(b),
                            a,
                            b
                    ));
                }
            }
        }

        return new City(cityName, intersections, streets);
    }
}
