package ro.uaic.asli.lab5.repository;

import ro.uaic.asli.lab5.exception.DuplicateResourceException;
import ro.uaic.asli.lab5.exception.InvalidCatalogDataException;
import ro.uaic.asli.lab5.exception.ResourceNotFoundException;
import ro.uaic.asli.lab5.model.BibliographicResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class CatalogRepository {
    private final Map<String, BibliographicResource> resources = new LinkedHashMap<>();

    public void add(BibliographicResource resource) throws DuplicateResourceException {
        if (resources.containsKey(resource.getId())) {
            throw new DuplicateResourceException(resource.getId());
        }
        resources.put(resource.getId(), resource);
    }

    public BibliographicResource getById(String id) throws ResourceNotFoundException {
        var resource = resources.get(id);
        if (resource == null) {
            throw new ResourceNotFoundException(id);
        }
        return resource;
    }

    public List<BibliographicResource> listAll() {
        return new ArrayList<>(resources.values());
    }

    /**
     * Loads a simple line-based format:
     * id|title|location|year|authors|description|concept1,concept2
     */
    public void load(Path path) throws InvalidCatalogDataException {
        List<String> lines;
        try {
            lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new InvalidCatalogDataException("Cannot read catalog file: " + path, e);
        }

        int lineNo = 0;
        for (String line : lines) {
            lineNo++;
            if (line.isBlank() || line.stripLeading().startsWith("#")) {
                continue;
            }
            String[] tokens = line.split("\\|", -1);
            if (tokens.length < 6) {
                throw new InvalidCatalogDataException("Invalid line at " + lineNo + ": " + line);
            }
            try {
                Set<String> concepts = tokens.length > 6 && !tokens[6].isBlank()
                        ? Set.of(tokens[6].split(","))
                        : Set.of();
                add(new BibliographicResource(
                        tokens[0].trim(),
                        tokens[1].trim(),
                        tokens[2].trim(),
                        Integer.parseInt(tokens[3].trim()),
                        tokens[4].trim(),
                        tokens[5].trim(),
                        concepts
                ));
            } catch (DuplicateResourceException | IllegalArgumentException ex) {
                throw new InvalidCatalogDataException("Invalid line at " + lineNo + ": " + line, ex);
            }
        }
    }
}
