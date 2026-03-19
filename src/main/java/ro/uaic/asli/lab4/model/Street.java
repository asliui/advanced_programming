package ro.uaic.asli.lab4.model;

import java.util.Objects;

public final class Street implements Comparable<Street> {
    private final String name;
    private final double length;
    private final Intersection first;
    private final Intersection second;

    public Street(String name, double length, Intersection first, Intersection second) {
        if (first.equals(second)) {
            throw new IllegalArgumentException("Street endpoints must be different.");
        }
        this.name = Objects.requireNonNull(name);
        this.length = length;
        this.first = Objects.requireNonNull(first);
        this.second = Objects.requireNonNull(second);
    }

    public String getName() {
        return name;
    }

    public double getLength() {
        return length;
    }

    public Intersection getFirst() {
        return first;
    }

    public Intersection getSecond() {
        return second;
    }

    public boolean connects(Intersection intersection) {
        return first.equals(intersection) || second.equals(intersection);
    }

    public Intersection otherEndpoint(Intersection intersection) {
        if (first.equals(intersection)) {
            return second;
        }
        if (second.equals(intersection)) {
            return first;
        }
        throw new IllegalArgumentException("Intersection is not on this street.");
    }

    @Override
    public int compareTo(Street other) {
        return Double.compare(length, other.length);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Street street)) return false;
        return Double.compare(street.length, length) == 0
                && Objects.equals(name, street.name)
                && ((Objects.equals(first, street.first) && Objects.equals(second, street.second))
                || (Objects.equals(first, street.second) && Objects.equals(second, street.first)));
    }

    @Override
    public int hashCode() {
        // Undirected edge hash.
        return Objects.hash(name, length) + first.hashCode() + second.hashCode();
    }

    @Override
    public String toString() {
        return name + "(" + first.name() + "-" + second.name() + ", " + String.format("%.2f", length) + ")";
    }
}
