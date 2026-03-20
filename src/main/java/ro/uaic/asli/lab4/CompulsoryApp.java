package ro.uaic.asli.lab4;

import ro.uaic.asli.lab4.model.Intersection;
import ro.uaic.asli.lab4.model.Street;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

public final class CompulsoryApp {
    public static void main(String[] args) {
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
}

