package ro.uaic.asli.lab5.model;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public final class BibliographicResource {
    private final String id;
    private final String title;
    private final String location;
    private final int year;
    private final String authors;
    private final String description;
    private final Set<String> concepts;

    public BibliographicResource(
            String id,
            String title,
            String location,
            int year,
            String authors,
            String description,
            Set<String> concepts) {
        this.id = Objects.requireNonNull(id);
        this.title = Objects.requireNonNull(title);
        this.location = Objects.requireNonNull(location);
        this.year = year;
        this.authors = Objects.requireNonNull(authors);
        this.description = description == null ? "" : description;
        this.concepts = concepts == null ? Set.of() : new LinkedHashSet<>(concepts);
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getLocation() {
        return location;
    }

    public int getYear() {
        return year;
    }

    public String getAuthors() {
        return authors;
    }

    public String getDescription() {
        return description;
    }

    public Set<String> getConcepts() {
        return Set.copyOf(concepts);
    }

    @Override
    public String toString() {
        return "BibliographicResource{id='%s', title='%s', year=%d, location='%s'}"
                .formatted(id, title, year, location);
    }
}
