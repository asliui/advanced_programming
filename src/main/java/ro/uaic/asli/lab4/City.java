package ro.uaic.asli.lab4;

import ro.uaic.asli.lab4.model.Intersection;
import ro.uaic.asli.lab4.model.Street;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class City {
    private final String name;
    private final Set<Intersection> intersections;
    private final LinkedList<Street> streets;

    public City(String name, Collection<Intersection> intersections, Collection<Street> streets) {
        this.name = name;
        this.intersections = new LinkedHashSet<>(intersections);
        this.streets = new LinkedList<>(streets);
    }

    public String getName() {
        return name;
    }

    public Set<Intersection> getIntersections() {
        return Set.copyOf(intersections);
    }

    public LinkedList<Street> getStreets() {
        return new LinkedList<>(streets);
    }

    public Map<Intersection, Long> degreeMap() {
        return intersections.stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        i -> streets.stream().filter(s -> s.connects(i)).count()
                ));
    }

    public List<Street> streetsLongerThanAndJoiningAtLeast(double minLength, long minDegree) {
        Map<Intersection, Long> degrees = degreeMap();
        return streets.stream()
                .filter(street -> street.getLength() > minLength)
                .filter(street -> degrees.getOrDefault(street.getFirst(), 0L) >= minDegree)
                .filter(street -> degrees.getOrDefault(street.getSecond(), 0L) >= minDegree)
                .sorted()
                .toList();
    }

    public boolean hasDuplicateIntersections() {
        Set<Intersection> unique = new HashSet<>(intersections);
        return unique.size() != intersections.size();
    }
}
