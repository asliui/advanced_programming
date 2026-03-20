package ro.uaic.asli.lab5.exception;

public class ResourceNotFoundException extends CatalogException {
    public ResourceNotFoundException(String id) {
        super("Resource with id '%s' was not found.".formatted(id));
    }
}
