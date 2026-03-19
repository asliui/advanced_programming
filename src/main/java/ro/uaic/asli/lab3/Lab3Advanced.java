package ro.uaic.asli.lab3;

import java.time.LocalDate;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Advanced tasks for the social network:
 * - determine cut vertices (articulation points) that ensure connectivity;
 * - identify maximal parts of the network without such vertices (blocks).
 */
public class Lab3Advanced {

    /**
     * Simple wrapper around the social graph used only in this class.
     * Nodes are {@link Lab3Homework.Profile} instances and edges are undirected
     * connections derived from relationships.
     */
    public static final class NetworkGraph {
        private final List<Lab3Homework.Profile> nodes;
        private final Map<Lab3Homework.Profile, List<Lab3Homework.Profile>> adjacency;

        private NetworkGraph(List<Lab3Homework.Profile> nodes,
                             Map<Lab3Homework.Profile, List<Lab3Homework.Profile>> adjacency) {
            this.nodes = nodes;
            this.adjacency = adjacency;
        }

        public List<Lab3Homework.Profile> getNodes() {
            return nodes;
        }

        public Map<Lab3Homework.Profile, List<Lab3Homework.Profile>> getAdjacency() {
            return adjacency;
        }
    }

    /**
     * Builds an undirected graph representation from a {@link Lab3Homework.SocialNetwork} instance.
     * Two profiles are connected if at least one of them has a relationship entry to the other.
     */
    public static NetworkGraph fromSocialNetwork(Lab3Homework.SocialNetwork network) {
        List<Lab3Homework.Profile> profiles = new ArrayList<>(network.getProfilesOrderedByImportance());
        Map<Lab3Homework.Profile, List<Lab3Homework.Profile>> adj = new HashMap<>();
        for (Lab3Homework.Profile profile : profiles) {
            adj.put(profile, new ArrayList<>());
        }

        for (Lab3Homework.Profile a : profiles) {
            for (Lab3Homework.Profile b : profiles) {
                if (a == b) {
                    continue;
                }
                if (hasRelation(a, b) || hasRelation(b, a)) {
                    addUndirectedEdge(adj, a, b);
                }
            }
        }
        return new NetworkGraph(profiles, adj);
    }

    private static boolean hasRelation(Lab3Homework.Profile from,
                                       Lab3Homework.Profile to) {
        if (from instanceof Lab3Homework.Person person) {
            return person.getRelationships().containsKey(to);
        }
        if (from instanceof Lab3Homework.Company company) {
            return company.getRelationships().containsKey(to);
        }
        return false;
    }

    private static void addUndirectedEdge(Map<Lab3Homework.Profile, List<Lab3Homework.Profile>> adj,
                                          Lab3Homework.Profile a,
                                          Lab3Homework.Profile b) {
        adj.get(a).add(b);
        adj.get(b).add(a);
    }

    /**
     * Finds all articulation points (cut vertices) in the given graph using
     * a depth-first search with discovery/low times (Tarjan algorithm).
     */
    public static Set<Lab3Homework.Profile> findCutVertices(NetworkGraph graph) {
        List<Lab3Homework.Profile> nodes = graph.getNodes();
        Map<Lab3Homework.Profile, List<Lab3Homework.Profile>> adj = graph.getAdjacency();

        Map<Lab3Homework.Profile, Integer> disc = new HashMap<>();
        Map<Lab3Homework.Profile, Integer> low = new HashMap<>();
        Map<Lab3Homework.Profile, Lab3Homework.Profile> parent = new HashMap<>();
        Set<Lab3Homework.Profile> visited = new HashSet<>();
        Set<Lab3Homework.Profile> articulation = new HashSet<>();

        int[] time = {0};

        for (Lab3Homework.Profile v : nodes) {
            if (!visited.contains(v)) {
                dfsArticulation(v, adj, visited, disc, low, parent, articulation, time);
            }
        }
        return articulation;
    }

    private static void dfsArticulation(
            Lab3Homework.Profile u,
            Map<Lab3Homework.Profile, List<Lab3Homework.Profile>> adj,
            Set<Lab3Homework.Profile> visited,
            Map<Lab3Homework.Profile, Integer> disc,
            Map<Lab3Homework.Profile, Integer> low,
            Map<Lab3Homework.Profile, Lab3Homework.Profile> parent,
            Set<Lab3Homework.Profile> articulation,
            int[] time) {

        visited.add(u);
        disc.put(u, time[0]);
        low.put(u, time[0]);
        time[0]++;

        int children = 0;

        for (Lab3Homework.Profile v : adj.getOrDefault(u, List.of())) {
            if (!visited.contains(v)) {
                children++;
                parent.put(v, u);
                dfsArticulation(v, adj, visited, disc, low, parent, articulation, time);

                low.put(u, Math.min(low.get(u), low.get(v)));

                if (parent.get(u) == null && children > 1) {
                    articulation.add(u);
                }
                if (parent.get(u) != null && low.get(v) >= disc.get(u)) {
                    articulation.add(u);
                }
            } else if (!Objects.equals(v, parent.get(u))) {
                low.put(u, Math.min(low.get(u), disc.get(v)));
            }
        }
    }

    /**
     * Represents a maximal subgraph (block) that does not contain any cut vertex
     * internal to it. We identify them as the connected components that remain
     * after removing all global cut vertices from the graph.
     */
    public static final class Block {
        private final Set<Lab3Homework.Profile> profiles;

        public Block(Set<Lab3Homework.Profile> profiles) {
            this.profiles = profiles;
        }

        public Set<Lab3Homework.Profile> getProfiles() {
            return profiles;
        }

        @Override
        public String toString() {
            return "Block{" + profiles + '}';
        }
    }

    /**
     * Identifies maximal parts of the network that, taken separately,
     * do not have any of the global cut vertices.
     */
    public static List<Block> findBlocksWithoutCutVertices(NetworkGraph graph) {
        Set<Lab3Homework.Profile> cutVertices = findCutVertices(graph);
        Map<Lab3Homework.Profile, List<Lab3Homework.Profile>> adj = graph.getAdjacency();

        Set<Lab3Homework.Profile> visited = new HashSet<>();
        List<Block> blocks = new ArrayList<>();

        for (Lab3Homework.Profile start : graph.getNodes()) {
            if (cutVertices.contains(start) || visited.contains(start)) {
                continue;
            }
            Set<Lab3Homework.Profile> component = new HashSet<>();
            Deque<Lab3Homework.Profile> stack = new ArrayDeque<>();
            stack.push(start);
            visited.add(start);

            while (!stack.isEmpty()) {
                Lab3Homework.Profile u = stack.pop();
                component.add(u);
                for (Lab3Homework.Profile v : adj.getOrDefault(u, List.of())) {
                    if (!cutVertices.contains(v) && !visited.contains(v)) {
                        visited.add(v);
                        stack.push(v);
                    }
                }
            }

            if (!component.isEmpty()) {
                blocks.add(new Block(component));
            }
        }

        return blocks;
    }

    private static Lab3Homework.SocialNetwork buildDemoNetwork() {
        Lab3Homework.Programmer alice = new Lab3Homework.Programmer("Alice", LocalDate.of(1995, 3, 10), "Java");
        Lab3Homework.Programmer bob = new Lab3Homework.Programmer("Bob", LocalDate.of(1990, 7, 21), "Python");
        Lab3Homework.Designer clara = new Lab3Homework.Designer("Clara", LocalDate.of(1992, 11, 5), "Figma");
        Lab3Homework.Designer dave = new Lab3Homework.Designer("Dave", LocalDate.of(1991, 5, 12), "Sketch");

        Lab3Homework.Company techCorp = new Lab3Homework.Company("TechCorp", "Software");
        Lab3Homework.Company designStudio = new Lab3Homework.Company("DesignStudio", "Creative");

        alice.addRelationship(bob, Lab3Homework.RelationType.FRIEND);
        bob.addRelationship(alice, Lab3Homework.RelationType.FRIEND);

        bob.addRelationship(clara, Lab3Homework.RelationType.COLLEAGUE);
        clara.addRelationship(bob, Lab3Homework.RelationType.COLLEAGUE);

        clara.addRelationship(dave, Lab3Homework.RelationType.FRIEND);
        dave.addRelationship(clara, Lab3Homework.RelationType.FRIEND);

        alice.addRelationship(techCorp, Lab3Homework.RelationType.EMPLOYER);
        techCorp.addRelationship(alice, Lab3Homework.RelationType.MANAGER);

        dave.addRelationship(designStudio, Lab3Homework.RelationType.EMPLOYER);
        designStudio.addRelationship(dave, Lab3Homework.RelationType.MANAGER);

        Lab3Homework.SocialNetwork network = new Lab3Homework.SocialNetwork();
        network.addProfile(alice);
        network.addProfile(bob);
        network.addProfile(clara);
        network.addProfile(dave);
        network.addProfile(techCorp);
        network.addProfile(designStudio);

        return network;
    }

    public static void main(String[] args) {
        Lab3Homework.SocialNetwork network = buildDemoNetwork();
        NetworkGraph graph = fromSocialNetwork(network);

        Set<Lab3Homework.Profile> cuts = findCutVertices(graph);
        System.out.println("Cut vertices (profiles whose removal disconnects the network):");
        for (Lab3Homework.Profile p : cuts) {
            System.out.println(" - " + p.getName());
        }

        System.out.println();
        System.out.println("Blocks without cut vertices:");
        List<Block> blocks = findBlocksWithoutCutVertices(graph);
        int index = 1;
        for (Block block : blocks) {
            System.out.print("Block " + index++ + ": ");
            boolean first = true;
            for (Lab3Homework.Profile p : block.getProfiles()) {
                if (!first) {
                    System.out.print(", ");
                }
                System.out.print(p.getName());
                first = false;
            }
            System.out.println();
        }
    }
}
