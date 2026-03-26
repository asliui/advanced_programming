package ro.uaic.asli.lab6.service;

import ro.uaic.asli.lab6.dao.MovieDAO;
import ro.uaic.asli.lab6.dao.MovieListDAO;
import ro.uaic.asli.lab6.model.Actor;
import ro.uaic.asli.lab6.model.Movie;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Partitions movies into independent sets (lists) where movies share no actors within a list.
 *
 * This is graph coloring where:
 * - each movie is a vertex
 * - an edge exists if two movies share an actor
 * - each color class is a list
 */
public final class MovieListPartitionService {
    private final MovieDAO movieDAO = new MovieDAO();
    private final MovieListDAO movieListDAO = new MovieListDAO();

    public void partitionAndPersist() {
        List<Movie> movies = movieDAO.findAll();
        if (movies.isEmpty()) {
            throw new IllegalStateException("No movies found in database.");
        }

        Map<Integer, Set<Integer>> adjacency = buildAdjacencyByCommonActor(movies);
        Map<Integer, Integer> coloring = dsaturColoring(adjacency);

        coloring = reduceColorCount(adjacency, coloring);

        // Post-process: balance list sizes (independence is preserved by constraint checks).
        coloring = balanceColorClasses(adjacency, coloring);

        Map<Integer, List<Integer>> colorToMovies = buildColorToMovies(coloring);
        persist(colorToMovies);

        int lists = 1 + colorToMovies.keySet().stream().mapToInt(Integer::intValue).max().orElse(0);
        System.out.println("Partitioned into " + lists + " movie list(s). Sizes: "
                + sizesSummary(colorToMovies));
    }

    private static Map<Integer, Set<Integer>> buildAdjacencyByCommonActor(List<Movie> movies) {
        Map<Integer, Set<Integer>> adjacency = new HashMap<>();
        for (Movie m : movies) {
            adjacency.put(m.getId(), new HashSet<>());
        }

        // For each actor, all movies featuring that actor become pairwise adjacent.
        Map<Integer, List<Integer>> actorToMovieIds = new HashMap<>();
        for (Movie m : movies) {
            for (Actor a : m.getActors()) {
                actorToMovieIds.computeIfAbsent(a.getId(), ignored -> new ArrayList<>())
                        .add(m.getId());
            }
        }

        for (List<Integer> movieIds : actorToMovieIds.values()) {
            for (int i = 0; i < movieIds.size(); i++) {
                int u = movieIds.get(i);
                for (int j = i + 1; j < movieIds.size(); j++) {
                    int v = movieIds.get(j);
                    adjacency.get(u).add(v);
                    adjacency.get(v).add(u);
                }
            }
        }

        return adjacency;
    }

    /**
     * DSATUR heuristic for graph coloring: tends to keep the number of colors small.
     */
    private static Map<Integer, Integer> dsaturColoring(Map<Integer, Set<Integer>> adjacency) {
        Map<Integer, Integer> colorByVertex = new HashMap<>();

        // Precompute degrees for tie-breaking.
        Map<Integer, Integer> degree = new HashMap<>();
        for (var e : adjacency.entrySet()) {
            degree.put(e.getKey(), e.getValue().size());
        }

        while (colorByVertex.size() < adjacency.size()) {
            int nextVertex = selectNextVertex(adjacency, degree, colorByVertex);
            Set<Integer> forbidden = new HashSet<>();
            for (int nb : adjacency.get(nextVertex)) {
                Integer c = colorByVertex.get(nb);
                if (c != null) forbidden.add(c);
            }

            int color = 0;
            while (forbidden.contains(color)) {
                color++;
            }
            colorByVertex.put(nextVertex, color);
        }

        return colorByVertex;
    }

    /**
     * Heuristic post-processing that attempts to reduce the number of colors (lists)
     * by re-coloring with an upper bound on the allowed color count.
     */
    private static Map<Integer, Integer> reduceColorCount(
            Map<Integer, Set<Integer>> adjacency,
            Map<Integer, Integer> currentColoring
    ) {
        int currentK = 1 + currentColoring.values().stream().mapToInt(Integer::intValue).max().orElse(0);
        if (currentK <= 1) {
            return currentColoring;
        }

        // Order: higher degree first for better chance to fit into fewer colors.
        List<Integer> vertices = new ArrayList<>(adjacency.keySet());
        vertices.sort((a, b) -> Integer.compare(adjacency.get(b).size(), adjacency.get(a).size()));

        Map<Integer, Integer> best = currentColoring;
        for (int k = currentK - 1; k >= 1; k--) {
            Map<Integer, Integer> limited = tryGreedyColorLimit(adjacency, vertices, k);
            if (limited != null) {
                best = limited;
            } else {
                break; // can't go lower with this greedy attempt
            }
        }

        return best;
    }

    private static Map<Integer, Integer> tryGreedyColorLimit(
            Map<Integer, Set<Integer>> adjacency,
            List<Integer> verticesOrder,
            int kLimit
    ) {
        Map<Integer, Integer> colorByVertex = new HashMap<>();

        for (int v : verticesOrder) {
            Set<Integer> forbidden = new HashSet<>();
            for (int nb : adjacency.get(v)) {
                Integer c = colorByVertex.get(nb);
                if (c != null) forbidden.add(c);
            }

            int chosen = -1;
            for (int c = 0; c < kLimit; c++) {
                if (!forbidden.contains(c)) {
                    chosen = c;
                    break;
                }
            }

            if (chosen == -1) {
                return null;
            }

            colorByVertex.put(v, chosen);
        }

        return colorByVertex;
    }

    private static int selectNextVertex(
            Map<Integer, Set<Integer>> adjacency,
            Map<Integer, Integer> degree,
            Map<Integer, Integer> colorByVertex
    ) {
        int best = -1;
        int bestSat = -1;
        int bestDeg = -1;

        for (int v : adjacency.keySet()) {
            if (colorByVertex.containsKey(v)) continue;

            Set<Integer> neighborColors = new HashSet<>();
            for (int nb : adjacency.get(v)) {
                Integer c = colorByVertex.get(nb);
                if (c != null) neighborColors.add(c);
            }
            int sat = neighborColors.size();
            int deg = degree.getOrDefault(v, 0);

            if (sat > bestSat || (sat == bestSat && deg > bestDeg)) {
                best = v;
                bestSat = sat;
                bestDeg = deg;
            }
        }
        return best;
    }

    private static Map<Integer, Integer> balanceColorClasses(
            Map<Integer, Set<Integer>> adjacency,
            Map<Integer, Integer> coloring
    ) {
        // Build color -> vertices sets
        Map<Integer, Set<Integer>> colorVertices = new HashMap<>();
        int maxColor = coloring.values().stream().mapToInt(Integer::intValue).max().orElse(0);
        for (int c = 0; c <= maxColor; c++) {
            colorVertices.put(c, new HashSet<>());
        }
        for (var e : coloring.entrySet()) {
            colorVertices.get(e.getValue()).add(e.getKey());
        }

        int n = adjacency.size();
        int k = maxColor + 1;
        int maxSize = (n + k - 1) / k; // ceil

        boolean changed;
        do {
            changed = false;

            for (int c = 0; c <= maxColor; c++) {
                while (colorVertices.get(c).size() > maxSize) {
                    Integer overflowVertex = pickOverflowVertex(adjacency, c, colorVertices);
                    if (overflowVertex == null) break;

                    Integer destinationColor = pickDestinationColor(adjacency, overflowVertex, c, colorVertices, maxSize);
                    if (destinationColor == null) break;

                    // Move vertex c -> destinationColor (still independent due to constraint check).
                    colorVertices.get(c).remove(overflowVertex);
                    colorVertices.get(destinationColor).add(overflowVertex);
                    coloring.put(overflowVertex, destinationColor);
                    changed = true;
                }
            }
        } while (changed);

        return coloring;
    }

    private static Integer pickOverflowVertex(
            Map<Integer, Set<Integer>> adjacency,
            int overflowColor,
            Map<Integer, Set<Integer>> colorVertices
    ) {
        // Prefer vertices with fewer possible destinations (greedy).
        Integer best = null;
        int bestFlex = Integer.MAX_VALUE;

        for (int v : colorVertices.get(overflowColor)) {
            int flex = countCompatibleColors(adjacency, v, overflowColor, colorVertices);
            if (flex < bestFlex) {
                bestFlex = flex;
                best = v;
            }
        }
        return best;
    }

    private static int countCompatibleColors(
            Map<Integer, Set<Integer>> adjacency,
            int v,
            int currentColor,
            Map<Integer, Set<Integer>> colorVertices
    ) {
        int count = 0;
        for (var e : colorVertices.entrySet()) {
            int c = e.getKey();
            if (c == currentColor) continue;
            if (isCompatible(adjacency, v, e.getValue())) {
                count++;
            }
        }
        return count;
    }

    private static Integer pickDestinationColor(
            Map<Integer, Set<Integer>> adjacency,
            int v,
            int currentColor,
            Map<Integer, Set<Integer>> colorVertices,
            int maxSize
    ) {
        int bestColor = -1;
        int bestSize = Integer.MAX_VALUE;

        for (var e : colorVertices.entrySet()) {
            int destColor = e.getKey();
            if (destColor == currentColor) continue;
            if (e.getValue().size() >= maxSize) continue;

            if (isCompatible(adjacency, v, e.getValue())) {
                int size = e.getValue().size();
                if (size < bestSize) {
                    bestSize = size;
                    bestColor = destColor;
                }
            }
        }
        return bestColor == -1 ? null : bestColor;
    }

    private static boolean isCompatible(Map<Integer, Set<Integer>> adjacency, int v, Set<Integer> destVertices) {
        for (int dest : destVertices) {
            if (adjacency.get(v).contains(dest)) {
                return false;
            }
        }
        return true;
    }

    private Map<Integer, List<Integer>> buildColorToMovies(Map<Integer, Integer> coloring) {
        Map<Integer, List<Integer>> colorToMovies = new HashMap<>();
        int maxColor = coloring.values().stream().mapToInt(Integer::intValue).max().orElse(0);
        for (int c = 0; c <= maxColor; c++) {
            colorToMovies.put(c, new ArrayList<>());
        }
        for (var e : coloring.entrySet()) {
            colorToMovies.get(e.getValue()).add(e.getKey());
        }
        return colorToMovies;
    }

    private String sizesSummary(Map<Integer, List<Integer>> colorToMovies) {
        List<String> parts = new ArrayList<>();
        for (int c : colorToMovies.keySet()) {
            parts.add("List " + (c + 1) + "=" + colorToMovies.get(c).size());
        }
        return String.join(", ", parts);
    }

    private void persist(Map<Integer, List<Integer>> colorToMovies) {
        movieListDAO.clearAll();

        // Create movie list rows and mapping rows.
        int maxColor = colorToMovies.keySet().stream().mapToInt(Integer::intValue).max().orElse(0);
        for (int c = 0; c <= maxColor; c++) {
            String name = "List " + (c + 1);
            int listId = movieListDAO.create(name, java.time.Instant.now());
            for (int movieId : colorToMovies.get(c)) {
                movieListDAO.addMovieToList(listId, movieId);
            }
        }
    }
}

