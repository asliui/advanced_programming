package ro.uaic.asli.lab4.model;

public record Intersection(String name, double x, double y) {
    public double distanceTo(Intersection other) {
        double dx = x - other.x();
        double dy = y - other.y();
        return Math.sqrt(dx * dx + dy * dy);
    }
}
