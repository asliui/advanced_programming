package ro.uaic.asli.lab5.exception;

public class CatalogException extends Exception {
    public CatalogException(String message) {
        super(message);
    }

    public CatalogException(String message, Throwable cause) {
        super(message, cause);
    }
}
