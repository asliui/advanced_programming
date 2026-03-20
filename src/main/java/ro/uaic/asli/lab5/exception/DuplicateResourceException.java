package ro.uaic.asli.lab5.exception;

public class DuplicateResourceException extends CatalogException {
    public DuplicateResourceException(String id) {
        super("Resource with id '%s' already exists.".formatted(id));
    }
}
