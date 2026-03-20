package ro.uaic.asli.lab5.algorithm;

import net.datafaker.Faker;
import ro.uaic.asli.lab5.model.BibliographicResource;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.IntStream;

public final class RandomInstanceGenerator {
    private final Faker faker = new Faker();
    private final Random random = new Random(42);

    public Set<String> conceptUniverse(int size) {
        return IntStream.range(0, size)
                .mapToObj(i -> "concept-" + i)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

    public List<BibliographicResource> resources(int count, Set<String> universe, int minConcepts, int maxConcepts) {
        List<String> concepts = new ArrayList<>(universe);
        List<BibliographicResource> out = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int take = random.nextInt(minConcepts, maxConcepts + 1);
            Set<String> subset = new LinkedHashSet<>();
            while (subset.size() < take) {
                subset.add(concepts.get(random.nextInt(concepts.size())));
            }
            out.add(new BibliographicResource(
                    "res-" + i,
                    faker.book().title(),
                    "https://example.org/resource/" + i,
                    random.nextInt(1980, 2026),
                    faker.book().author(),
                    faker.lorem().sentence(8),
                    subset
            ));
        }
        return out;
    }
}
