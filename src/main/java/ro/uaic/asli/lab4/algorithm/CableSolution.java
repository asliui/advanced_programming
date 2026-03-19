package ro.uaic.asli.lab4.algorithm;

import ro.uaic.asli.lab4.model.Street;

import java.util.Comparator;
import java.util.List;

public record CableSolution(List<Street> streets, double totalCost) {
    public static final Comparator<CableSolution> BY_COST = Comparator.comparingDouble(CableSolution::totalCost);
}
