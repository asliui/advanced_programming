package ro.uaic.asli.lab4.algorithm;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.jgrapht.alg.spanning.KruskalMinimumSpanningTree;
import org.jgrapht.graph.SimpleWeightedGraph;

import ro.uaic.asli.lab4.City;
import ro.uaic.asli.lab4.model.Intersection;
import ro.uaic.asli.lab4.model.Street;

public final class MinimumCablePlanner {
    private final Random random = new Random(42);

    public CableSolution minimumCostSolution(City city) {
        SimpleWeightedGraph<Intersection, Street> graph = buildSimpleGraph(city);
        KruskalMinimumSpanningTree<Intersection, Street> mst = new KruskalMinimumSpanningTree<>(graph);
        List<Street> streets = new ArrayList<>(mst.getSpanningTree().getEdges());
        double cost = streets.stream().mapToDouble(Street::getLength).sum();
        streets.sort(Street::compareTo);
        return new CableSolution(streets, cost);
    }

    public List<CableSolution> bestSolutions(City city, int maxSolutions) {
        if (maxSolutions <= 0) {
            return List.of();
        }

        Map<String, CableSolution> unique = new LinkedHashMap<>();
        CableSolution optimal = minimumCostSolution(city);
        unique.put(signature(optimal.streets()), optimal);

        int attempts = Math.max(40, maxSolutions * 25);
        for (int i = 0; i < attempts && unique.size() < maxSolutions; i++) {
            var maybe = randomizedKruskal(city);
            if (maybe != null) {
                unique.putIfAbsent(signature(maybe.streets()), maybe);
            }
        }

        return unique.values().stream()
                .sorted(CableSolution.BY_COST)
                .limit(maxSolutions)
                .toList();
    }

    private CableSolution randomizedKruskal(City city) {
        List<Street> sorted = city.getStreets().stream().sorted().collect(Collectors.toCollection(ArrayList::new));
        perturbByGroups(sorted);

        DisjointSet dsu = new DisjointSet(city.getIntersections());
        List<Street> chosen = new ArrayList<>();

        for (Street street : sorted) {
            if (dsu.union(street.getFirst(), street.getSecond())) {
                chosen.add(street);
                if (chosen.size() == city.getIntersections().size() - 1) {
                    double cost = chosen.stream().mapToDouble(Street::getLength).sum();
                    List<Street> output = chosen.stream().sorted().toList();
                    return new CableSolution(output, cost);
                }
            }
        }
        return null;
    }

    private void perturbByGroups(List<Street> streets) {
        int i = 0;
        while (i < streets.size()) {
            int j = i + 1;
            while (j < streets.size()
                    && Double.compare(streets.get(i).getLength(), streets.get(j).getLength()) == 0) {
                j++;
            }
            if (j - i > 1) {
                for (int k = i; k < j; k++) {
                    int swapWith = i + random.nextInt(j - i);
                    Street tmp = streets.get(k);
                    streets.set(k, streets.get(swapWith));
                    streets.set(swapWith, tmp);
                }
            }
            i = j;
        }
    }

    private String signature(List<Street> streets) {
        return streets.stream()
                .map(s -> s.getName() + ":" + s.getFirst().name() + ":" + s.getSecond().name() + ":" + s.getLength())
                .sorted()
                .collect(Collectors.joining("|"));
    }

    private static SimpleWeightedGraph<Intersection, Street> buildSimpleGraph(City city) {
        SimpleWeightedGraph<Intersection, Street> graph = new SimpleWeightedGraph<>(Street.class);
        city.getIntersections().forEach(graph::addVertex);
        city.getStreets().forEach(street -> {
            if (!graph.containsEdge(street.getFirst(), street.getSecond())) {
                graph.addEdge(street.getFirst(), street.getSecond(), street);
                graph.setEdgeWeight(street, street.getLength());
            }
        });
        return graph;
    }

    private static final class DisjointSet {
        private final Map<Intersection, Intersection> parent = new LinkedHashMap<>();
        private final Map<Intersection, Integer> rank = new LinkedHashMap<>();

        private DisjointSet(Set<Intersection> nodes) {
            nodes.forEach(n -> {
                parent.put(n, n);
                rank.put(n, 0);
            });
        }

        private Intersection find(Intersection x) {
            Intersection p = parent.get(x);
            if (!Objects.equals(p, x)) {
                parent.put(x, find(p));
            }
            return parent.get(x);
        }

        private boolean union(Intersection a, Intersection b) {
            Intersection rootA = find(a);
            Intersection rootB = find(b);
            if (Objects.equals(rootA, rootB)) {
                return false;
            }
            int rankA = rank.get(rootA);
            int rankB = rank.get(rootB);
            if (rankA < rankB) {
                parent.put(rootA, rootB);
            } else if (rankA > rankB) {
                parent.put(rootB, rootA);
            } else {
                parent.put(rootB, rootA);
                rank.put(rootA, rankA + 1);
            }
            return true;
        }
    }
}
