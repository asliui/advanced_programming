package ro.uaic.asli.lab5.algorithm;

import ro.uaic.asli.lab5.model.BibliographicResource;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class SetCoverSolver {
    /**
     * Greedy set cover: repeatedly pick the resource that covers
     * the largest number of uncovered concepts.
     */
    public List<BibliographicResource> greedyCover(Set<String> concepts, List<BibliographicResource> resources) {
        Set<String> uncovered = new LinkedHashSet<>(concepts);
        List<BibliographicResource> chosen = new ArrayList<>();
        List<BibliographicResource> pool = new ArrayList<>(resources);

        while (!uncovered.isEmpty()) {
            BibliographicResource best = null;
            int bestGain = 0;
            for (BibliographicResource resource : pool) {
                int gain = (int) resource.getConcepts().stream().filter(uncovered::contains).count();
                if (gain > bestGain) {
                    bestGain = gain;
                    best = resource;
                }
            }
            if (best == null || bestGain == 0) {
                break;
            }
            chosen.add(best);
            uncovered.removeAll(best.getConcepts());
            pool.remove(best);
        }
        return chosen;
    }
}
