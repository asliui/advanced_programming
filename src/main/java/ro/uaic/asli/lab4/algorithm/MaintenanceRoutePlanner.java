package ro.uaic.asli.lab4.algorithm;

import ro.uaic.asli.lab4.model.Intersection;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;

public final class MaintenanceRoutePlanner {

    /**
     * 2-approximation for metric TSP:
     * 1) build MST on complete graph induced by Euclidean distances;
     * 2) preorder traversal of MST;
     * 3) shortcut repeated vertices and close cycle.
     */
    public MaintenanceRoute twoApproximateRoute(Set<Intersection> intersections) {
        if (intersections.isEmpty()) {
            return new MaintenanceRoute(List.of(), 0.0);
        }
        if (intersections.size() == 1) {
            Intersection only = intersections.iterator().next();
            return new MaintenanceRoute(List.of(only, only), 0.0);
        }

        Map<Intersection, List<Intersection>> mstAdj = buildMstAdjacency(intersections);
        Intersection start = intersections.iterator().next();
        List<Intersection> preorder = new ArrayList<>();
        dfs(start, null, mstAdj, preorder);

        List<Intersection> cycle = preorder.stream().distinct().collect(Collectors.toCollection(ArrayList::new));
        cycle.add(cycle.get(0));

        double total = 0.0;
        for (int i = 0; i < cycle.size() - 1; i++) {
            total += cycle.get(i).distanceTo(cycle.get(i + 1));
        }
        return new MaintenanceRoute(cycle, total);
    }

    private void dfs(Intersection node, Intersection parent, Map<Intersection, List<Intersection>> adj, List<Intersection> out) {
        out.add(node);
        for (Intersection next : adj.getOrDefault(node, List.of())) {
            if (!next.equals(parent)) {
                dfs(next, node, adj, out);
            }
        }
    }

    private Map<Intersection, List<Intersection>> buildMstAdjacency(Set<Intersection> nodes) {
        List<Intersection> list = new ArrayList<>(nodes);
        Map<Intersection, List<Intersection>> adj = new LinkedHashMap<>();
        nodes.forEach(n -> adj.put(n, new ArrayList<>()));

        Intersection seed = list.get(0);
        Map<Intersection, Double> best = new LinkedHashMap<>();
        Map<Intersection, Intersection> parent = new LinkedHashMap<>();
        Set<Intersection> inTree = new java.util.HashSet<>();
        nodes.forEach(n -> best.put(n, Double.POSITIVE_INFINITY));
        best.put(seed, 0.0);

        PriorityQueue<Intersection> pq = new PriorityQueue<>(Comparator.comparingDouble(best::get));
        pq.add(seed);

        while (!pq.isEmpty()) {
            Intersection cur = pq.poll();
            if (inTree.contains(cur)) {
                continue;
            }
            inTree.add(cur);
            Intersection p = parent.get(cur);
            if (p != null) {
                adj.get(cur).add(p);
                adj.get(p).add(cur);
            }
            for (Intersection candidate : nodes) {
                if (candidate.equals(cur) || inTree.contains(candidate)) {
                    continue;
                }
                double w = cur.distanceTo(candidate);
                if (w < best.get(candidate)) {
                    best.put(candidate, w);
                    parent.put(candidate, cur);
                    pq.add(candidate);
                }
            }
        }
        return adj;
    }
}
