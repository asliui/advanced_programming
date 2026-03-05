import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;

/**
 * Entry class for the lab2 homework implementation.
 */
public class lab2_hw {
    /**
     * Road types used in the problem instance.
     */
    public enum RoadType {
        HIGHWAY,
        EXPRESS,
        COUNTRY
    }

    /**
     * Base type for all locations in the road network.
     */
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

        /**
         * Computes Euclidean distance to another location.
         *
         * @param other target location
         * @return distance between this and other
         */
        public double distanceTo(Location other) {
            double dx = this.x - other.x;
            double dy = this.y - other.y;
            return Math.sqrt(dx * dx + dy * dy);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            Location other = (Location) obj;
            return Double.compare(other.x, x) == 0
                    && Double.compare(other.y, y) == 0
                    && Objects.equals(name, other.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(getClass(), name, x, y);
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "{name='" + name + "', x=" + x + ", y=" + y + "}";
        }
    }

    /**
     * City location with population.
     */
    public static final class City extends Location {
        private final int population;

        public City(String name, double x, double y, int population) {
            super(name, x, y);
            this.population = population;
        }

        public int getPopulation() {
            return population;
        }

        @Override
        public String toString() {
            return super.toString().replace("}", ", population=" + population + "}");
        }
    }

    /**
     * Airport location with number of terminals.
     */
    public static final class Airport extends Location {
        private final int terminals;

        public Airport(String name, double x, double y, int terminals) {
            super(name, x, y);
            this.terminals = terminals;
        }

        public int getTerminals() {
            return terminals;
        }

        @Override
        public String toString() {
            return super.toString().replace("}", ", terminals=" + terminals + "}");
        }
    }

    /**
     * Gas station location with gas price.
     */
    public static final class GasStation extends Location {
        private final double gasPrice;

        public GasStation(String name, double x, double y, double gasPrice) {
            super(name, x, y);
            this.gasPrice = gasPrice;
        }

        public double getGasPrice() {
            return gasPrice;
        }

        @Override
        public String toString() {
            return super.toString().replace("}", ", gasPrice=" + gasPrice + "}");
        }
    }

    /**
     * Road connecting two locations.
     */
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

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            Road other = (Road) obj;
            return Double.compare(other.length, length) == 0
                    && Double.compare(other.speedLimit, speedLimit) == 0
                    && type == other.type
                    && Objects.equals(from, other.from)
                    && Objects.equals(to, other.to);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, from, to, length, speedLimit);
        }

        @Override
        public String toString() {
            return "Road{type=" + type + ", from=" + from.getName() + ", to=" + to.getName()
                    + ", length=" + length + ", speedLimit=" + speedLimit + "}";
        }
    }

    /**
     * Describes a problem instance with locations and roads.
     */
    public static final class ProblemInstance {
        private final List<Location> locations = new ArrayList<>();
        private final List<Road> roads = new ArrayList<>();

        /**
         * Adds a location if it does not already exist.
         *
         * @param location location to add
         * @return true if added, false if duplicate
         */
        public boolean addLocation(Location location) {
            if (locations.contains(location)) {
                return false;
            }
            locations.add(location);
            return true;
        }

        /**
         * Adds a road if it does not already exist.
         *
         * @param road road to add
         * @return true if added, false if duplicate
         */
        public boolean addRoad(Road road) {
            if (roads.contains(road)) {
                return false;
            }
            roads.add(road);
            return true;
        }

        public List<Location> getLocations() {
            return new ArrayList<>(locations);
        }

        public List<Road> getRoads() {
            return new ArrayList<>(roads);
        }

        /**
         * Validates that all roads connect existing locations and constraints hold.
         *
         * @return true if the instance is valid
         */
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

        /**
         * Checks if there is a path between two locations using the given roads.
         *
         * @param start start location
         * @param end end location
         * @return true if a path exists
         */
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
