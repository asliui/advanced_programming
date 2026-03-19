package ro.uaic.asli.lab2;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;

public class Lab2Homework {
    public enum RoadType {
        HIGHWAY,
        EXPRESS,
        COUNTRY
    }

    public sealed abstract static class Location permits City, Airport, GasStation {
        private final String name;
        private final double x;
        private final double y;

        protected Location(String name, double x, double y) {
            this.name = name;
            this.x = x;
            this.y = y;
        }

        public String getName() {
            return name;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public double distanceTo(Location other) {
            double dx = this.x - other.x;
            double dy = this.y - other.y;
            return Math.sqrt(dx * dx + dy * dy);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Location other = (Location) obj;
            return Double.compare(other.x, x) == 0
                    && Double.compare(other.y, y) == 0
                    && Objects.equals(name, other.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(getClass(), name, x, y);
        }
    }

    public static final class City extends Location {
        private final int population;

        public City(String name, double x, double y, int population) {
            super(name, x, y);
            this.population = population;
        }

        public int getPopulation() {
            return population;
        }
    }

    public static final class Airport extends Location {
        private final int terminals;

        public Airport(String name, double x, double y, int terminals) {
            super(name, x, y);
            this.terminals = terminals;
        }

        public int getTerminals() {
            return terminals;
        }
    }

    public static final class GasStation extends Location {
        private final double gasPrice;

        public GasStation(String name, double x, double y, double gasPrice) {
            super(name, x, y);
            this.gasPrice = gasPrice;
        }

        public double getGasPrice() {
            return gasPrice;
        }
    }

    public static final class Road {
        private final RoadType type;
        private final Location from;
        private final Location to;
        private final double length;
        private final double speedLimit;

        public Road(RoadType type, Location from, Location to, double length, double speedLimit) {
            this.type = type;
            this.from = from;
            this.to = to;
            this.length = length;
            this.speedLimit = speedLimit;
        }

        public RoadType getType() {
            return type;
        }

        public Location getFrom() {
            return from;
        }

        public Location getTo() {
            return to;
        }

        public double getLength() {
            return length;
        }

        public double getSpeedLimit() {
            return speedLimit;
        }
    }

    public static final class ProblemInstance {
        private final List<Location> locations = new ArrayList<>();
        private final List<Road> roads = new ArrayList<>();

        public boolean addLocation(Location location) {
            if (locations.contains(location)) {
                return false;
            }
            locations.add(location);
            return true;
        }

        public boolean addRoad(Road road) {
            if (roads.contains(road)) {
                return false;
            }
            roads.add(road);
            return true;
        }

        public boolean isValid() {
            Set<Location> locationSet = new HashSet<>(locations);
            if (locationSet.size() != locations.size()) {
                return false;
            }
            Set<Road> roadSet = new HashSet<>(roads);
            if (roadSet.size() != roads.size()) {
                return false;
            }
            for (Road road : roads) {
                if (!locationSet.contains(road.getFrom()) || !locationSet.contains(road.getTo())) {
                    return false;
                }
                if (road.getSpeedLimit() <= 0) {
                    return false;
                }
                double minLength = road.getFrom().distanceTo(road.getTo());
                if (road.getLength() < minLength) {
                    return false;
                }
            }
            return true;
        }

        public boolean canReach(Location start, Location end) {
            if (start == null || end == null) {
                return false;
            }
            if (start.equals(end)) {
                return true;
            }
            Set<Location> visited = new HashSet<>();
            Queue<Location> queue = new ArrayDeque<>();
            visited.add(start);
            queue.add(start);

            while (!queue.isEmpty()) {
                Location current = queue.poll();
                for (Road road : roads) {
                    Location neighbor = null;
                    if (road.getFrom().equals(current)) {
                        neighbor = road.getTo();
                    } else if (road.getTo().equals(current)) {
                        neighbor = road.getFrom();
                    }
                    if (neighbor != null && !visited.contains(neighbor)) {
                        if (neighbor.equals(end)) {
                            return true;
                        }
                        visited.add(neighbor);
                        queue.add(neighbor);
                    }
                }
            }
            return false;
        }
    }
}
