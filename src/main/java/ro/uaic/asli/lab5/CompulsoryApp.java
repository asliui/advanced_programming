package ro.uaic.asli.lab5;

import java.util.Set;

import ro.uaic.asli.lab5.exception.CatalogException;
import ro.uaic.asli.lab5.model.BibliographicResource;
import ro.uaic.asli.lab5.repository.CatalogRepository;

public final class CompulsoryApp {
    public static void main(String[] args) {
        var repository = new CatalogRepository();
        try {
            repository.add(new BibliographicResource(
                    "knuth67",
                    "The Art of Computer Programming",
                    "https://en.wikipedia.org/wiki/The_Art_of_Computer_Programming",
                    1967,
                    "Donald E. Knuth",
                    "Classic reference for algorithms.",
                    Set.of("algorithms", "analysis")));
            repository.add(new BibliographicResource(
                    "jvm25",
                    "The Java Virtual Machine Specification",
                    "https://docs.oracle.com/javase/specs/jvms/se25/html/index.html",
                    2025,
                    "Tim Lindholm & others",
                    "Official JVM specification.",
                    Set.of("java", "jvm", "spec")));

            System.out.println("Catalog created with " + repository.listAll().size() + " entries.");
            for (var resource : repository.listAll()) {
                System.out.println();
                System.out.println("ID: " + resource.getId());
                System.out.println("Title: " + resource.getTitle());
                System.out.println("Location: " + resource.getLocation());
                System.out.println("Year: " + resource.getYear());
                System.out.println("Authors: " + resource.getAuthors());
                System.out.println("Description: " + resource.getDescription());
                System.out.println("Concepts: " + String.join(", ", resource.getConcepts()));
            }
        } catch (CatalogException e) {
            System.err.println("Compulsory flow failed: " + e.getMessage());
        }
    }
}
