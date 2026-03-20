package ro.uaic.asli.lab5.exception;

public class InvalidCatalogDataException extends CatalogException {
    public InvalidCatalogDataException(String message) {
        super(message);
    }

    public InvalidCatalogDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
