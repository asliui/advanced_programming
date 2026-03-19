package ro.uaic.asli.lab2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;

public class Lab2Advanced {
    public enum RoadType {
        HIGHWAY,
        EXPRESS,
        COUNTRY
    }

    public static class Location {
        private final String name;
        private final double x;
        private final double y;

        public Location(String name, double x, double y) {
            this.name = name;
            this.x = x;
            this.y = y;
        }

        public String getName() {
            return name;
        }

        public double distanceTo(Location other) {
            double dx = this.x - other.x;
            double dy = this.y - other.y;
            return Math.sqrt(dx * dx + dy * dy);
        }
    }

    public static class Road {
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

        public double travelTime() {
            return length / speedLimit;
        }
    }

    public static class RouteResult {
        private final List<Location> path;
        private final double totalCost;

        public RouteResult(List<Location> path, double totalCost) {
            this.path = path;
            this.totalCost = totalCost;
        }

        public List<Location> getPath() {
            return path;
        }

        public double getTotalCost() {
            return totalCost;
        }
    }

    public static class BestRouteSolver {
        private final Map<Location, List<Road>> adjacency = new HashMap<>();

        public void addLocation(Location location) {
            adjacency.putIfAbsent(location, new ArrayList<>());
        }

        public void addRoad(Road road) {
            addLocation(road.getFrom());
            addLocation(road.getTo());
            adjacency.get(road.getFrom()).add(road);
        }

        public RouteResult shortestRoute(Location start, Location end) {
            return dijkstra(start, end, true);
        }

        public RouteResult fastestRoute(Location start, Location end) {
            return dijkstra(start, end, false);
        }

        private RouteResult dijkstra(Location start, Location end, boolean byLength) {
            Map<Location, Double> dist = new HashMap<>();
            Map<Location, Location> prev = new HashMap<>();
            PriorityQueue<Node> pq = new PriorityQueue<>();

            for (Location loc : adjacency.keySet()) {
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
                for (Road road : adjacency.get(cur.location)) {
                    double weight = byLength ? road.getLength() : road.travelTime();
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
        runPerformanceTest(2000, 8000);
    }

    private static void runPerformanceTest(int locationsCount, int roadsCount) {
        Random random = new Random(42);
        List<Location> locations = new ArrayList<>();
        for (int i = 0; i < locationsCount; i++) {
            locations.add(new Location("L" + i, random.nextDouble() * 1000, random.nextDouble() * 1000));
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
            RoadType type = RoadType.values()[random.nextInt(RoadType.values().length)];
            solver.addRoad(new Road(type, from, to, length, speed));
        }

        Location start = locations.get(0);
        Location end = locations.get(locationsCount - 1);

        long t1 = System.nanoTime();
        RouteResult shortest = solver.shortestRoute(start, end);
        long t2 = System.nanoTime();
        RouteResult fastest = solver.fastestRoute(start, end);
        long t3 = System.nanoTime();

        System.out.println("Performance test:");
        System.out.println("Shortest route cost: " + shortest.getTotalCost());
        System.out.println("Fastest route cost: " + fastest.getTotalCost());
        System.out.println("Shortest time ms: " + ((t2 - t1) / 1_000_000.0));
        System.out.println("Fastest time ms: " + ((t3 - t2) / 1_000_000.0));
    }
}
