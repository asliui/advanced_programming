import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;

public class lab2 {
    public static class Location {
        private String name;
        private String type;
        private double x;
        private double y;

        public Location(String name, String type, double x, double y) {
            this.name = name;
            this.type = type;
            this.x = x;
            this.y = y;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public double getX() {
            return x;
        }

        public void setX(double x) {
            this.x = x;
        }

        public double getY() {
            return y;
        }

        public void setY(double y) {
            this.y = y;
        }

        public double distanceTo(Location other) {
            double dx = this.x - other.x;
            double dy = this.y - other.y;
            return Math.sqrt(dx * dx + dy * dy);
        }

        @Override
        public String toString() {
            return "Location{name='" + name + "', type='" + type + "', x=" + x + ", y=" + y + "}";
        }
    }

    public static class Road {
        private String type;
        private Location from;
        private Location to;
        private double length;
        private double speedLimit;

        public Road(String type, Location from, Location to, double length, double speedLimit) {
            double minLength = from.distanceTo(to);
            if (length < minLength) {
                throw new IllegalArgumentException("Road length cannot be less than Euclidean distance.");
            }
            if (speedLimit <= 0) {
                throw new IllegalArgumentException("Speed limit must be positive.");
            }
            this.type = type;
            this.from = from;
            this.to = to;
            this.length = length;
            this.speedLimit = speedLimit;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Location getFrom() {
            return from;
        }

        public void setFrom(Location from) {
            this.from = from;
        }

        public Location getTo() {
            return to;
        }

        public void setTo(Location to) {
            this.to = to;
        }

        public double getLength() {
            return length;
        }

        public void setLength(double length) {
            if (length < from.distanceTo(to)) {
                throw new IllegalArgumentException("Road length cannot be less than Euclidean distance.");
            }
            this.length = length;
        }

        public double getSpeedLimit() {
            return speedLimit;
        }

        public void setSpeedLimit(double speedLimit) {
            if (speedLimit <= 0) {
                throw new IllegalArgumentException("Speed limit must be positive.");
            }
            this.speedLimit = speedLimit;
        }

        public double travelTime() {
            return length / speedLimit;
        }

        @Override
        public String toString() {
            return "Road{type='" + type + "', from=" + from.getName() + ", to=" + to.getName()
                    + ", length=" + length + ", speedLimit=" + speedLimit + "}";
        }
    }

    public static class RouteResult {
        private final List<Location> path;
        private final double totalWeight;

        public RouteResult(List<Location> path, double totalWeight) {
            this.path = path;
            this.totalWeight = totalWeight;
        }

        public List<Location> getPath() {
            return path;
        }

        public double getTotalWeight() {
            return totalWeight;
        }

        @Override
        public String toString() {
            return "RouteResult{path=" + path + ", totalWeight=" + totalWeight + "}";
        }
    }

    public static class BestRouteSolver {
        private final Map<Location, List<Road>> graph = new HashMap<>();

        public void addLocation(Location location) {
            graph.putIfAbsent(location, new ArrayList<>());
        }

        public void addRoad(Road road) {
            addLocation(road.getFrom());
            addLocation(road.getTo());
            graph.get(road.getFrom()).add(road);
        }

        public RouteResult shortestRoute(Location start, Location end) {
            return dijkstra(start, end, true);
        }

        public RouteResult fastestRoute(Location start, Location end) {
            return dijkstra(start, end, false);
        }

        private RouteResult dijkstra(Location start, Location end, boolean useLength) {
            Map<Location, Double> dist = new HashMap<>();
            Map<Location, Location> prev = new HashMap<>();
            PriorityQueue<Node> pq = new PriorityQueue<>();

            for (Location loc : graph.keySet()) {
                dist.put(loc, Double.POSITIVE_INFINITY);
            }
            dist.put(start, 0.0);
            pq.add(new Node(start, 0.0));

            while (!pq.isEmpty()) {
                Node cur = pq.poll();
                if (cur.distance > dist.get(cur.location)) {
                    continue;
                }
                if (cur.location.equals(end)) {
                    break;
                }
                for (Road road : graph.get(cur.location)) {
                    double weight = useLength ? road.getLength() : road.travelTime();
                    double newDist = dist.get(cur.location) + weight;
                    if (newDist < dist.get(road.getTo())) {
                        dist.put(road.getTo(), newDist);
                        prev.put(road.getTo(), cur.location);
                        pq.add(new Node(road.getTo(), newDist));
                    }
                }
            }

            if (!dist.containsKey(end) || dist.get(end).isInfinite()) {
                return new RouteResult(new ArrayList<>(), Double.POSITIVE_INFINITY);
            }

            List<Location> path = new ArrayList<>();
            Location step = end;
            while (step != null) {
                path.add(0, step);
                step = prev.get(step);
            }
            return new RouteResult(path, dist.get(end));
        }

        private static class Node implements Comparable<Node> {
            private final Location location;
            private final double distance;

            private Node(Location location, double distance) {
                this.location = location;
                this.distance = distance;
            }

            @Override
            public int compareTo(Node other) {
                return Double.compare(this.distance, other.distance);
            }
        }
    }

    public static void main(String[] args) {
        Location a = new Location("CityA", "city", 0, 0);
        Location b = new Location("CityB", "city", 3, 4);
        Location c = new Location("AirportC", "airport", 10, 1);

        Road r1 = new Road("highway", a, b, 5.0, 100);
        Road r2 = new Road("express", b, c, 8.5, 120);
        Road r3 = new Road("country", a, c, 11.0, 60);

        System.out.println(a);
        System.out.println(b);
        System.out.println(c);
        System.out.println(r1);
        System.out.println(r2);
        System.out.println(r3);

        BestRouteSolver solver = new BestRouteSolver();
        solver.addRoad(r1);
        solver.addRoad(r2);
        solver.addRoad(r3);

        RouteResult shortest = solver.shortestRoute(a, c);
        RouteResult fastest = solver.fastestRoute(a, c);
        System.out.println("Shortest route: " + shortest);
        System.out.println("Fastest route: " + fastest);

        runPerformanceTest();
    }

    private static void runPerformanceTest() {
        int locationsCount = 2000;
        int roadsCount = 8000;
        Random random = new Random(42);

        List<Location> locations = new ArrayList<>();
        for (int i = 0; i < locationsCount; i++) {
            locations.add(new Location("L" + i, "random", random.nextDouble() * 1000, random.nextDouble() * 1000));
        }

        BestRouteSolver solver = new BestRouteSolver();
        for (Location loc : locations) {
            solver.addLocation(loc);
        }

        for (int i = 0; i < roadsCount; i++) {
            Location from = locations.get(random.nextInt(locationsCount));
            Location to = locations.get(random.nextInt(locationsCount));
            if (from == to) {
                i--;
                continue;
            }
            double baseLength = from.distanceTo(to);
            double length = baseLength + random.nextDouble() * 20.0;
            double speed = 40 + random.nextDouble() * 100;
            solver.addRoad(new Road("random", from, to, length, speed));
        }

        Location start = locations.get(0);
        Location end = locations.get(locationsCount - 1);

        Runtime runtime = Runtime.getRuntime();
        runtime.gc();
        long beforeUsed = runtime.totalMemory() - runtime.freeMemory();

        long t1 = System.nanoTime();
        RouteResult shortest = solver.shortestRoute(start, end);
        long t2 = System.nanoTime();
        RouteResult fastest = solver.fastestRoute(start, end);
        long t3 = System.nanoTime();

        long afterUsed = runtime.totalMemory() - runtime.freeMemory();

        System.out.println("Performance test:");
        System.out.println("Shortest route distance: " + shortest.getTotalWeight());
        System.out.println("Fastest route time: " + fastest.getTotalWeight());
        System.out.println("Shortest time ms: " + ((t2 - t1) / 1_000_000.0));
        System.out.println("Fastest time ms: " + ((t3 - t2) / 1_000_000.0));
        System.out.println("Approx memory used bytes: " + (afterUsed - beforeUsed));
    }
}
