package ro.uaic.asli.lab3;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@interface Test {
}

final class Assert {
    static void assertTrue(boolean condition) {
        if (!condition) {
            throw new AssertionError("Expected condition to be true");
        }
    }

    static void assertEquals(int expected, int actual) {
        if (expected != actual) {
            throw new AssertionError("Expected " + expected + " but was " + actual);
        }
    }
}

public class Lab3AdvancedTest {

    private Lab3Homework.SocialNetwork buildSampleNetwork() {
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

    @Test
    public void testFindCutVertices() {
        Lab3Homework.SocialNetwork network = buildSampleNetwork();
        Lab3Advanced.NetworkGraph graph = Lab3Advanced.fromSocialNetwork(network);

        Set<Lab3Homework.Profile> cuts = Lab3Advanced.findCutVertices(graph);

        boolean hasBob = cuts.stream().anyMatch(p -> "Bob".equals(p.getName()));
        boolean hasClara = cuts.stream().anyMatch(p -> "Clara".equals(p.getName()));

        Assert.assertTrue(hasBob);
        Assert.assertTrue(hasClara);
    }

    @Test
    public void testFindBlocksWithoutCutVertices() {
        Lab3Homework.SocialNetwork network = buildSampleNetwork();
        Lab3Advanced.NetworkGraph graph = Lab3Advanced.fromSocialNetwork(network);

        List<Lab3Advanced.Block> blocks = Lab3Advanced.findBlocksWithoutCutVertices(graph);
        Assert.assertTrue(blocks.size() >= 2);

        int totalVertices = blocks.stream()
                .mapToInt(b -> b.getProfiles().size())
                .sum();

        Set<Lab3Homework.Profile> cuts = Lab3Advanced.findCutVertices(graph);
        int expected = graph.getNodes().size() - cuts.size();

        Assert.assertEquals(expected, totalVertices);
    }
}
