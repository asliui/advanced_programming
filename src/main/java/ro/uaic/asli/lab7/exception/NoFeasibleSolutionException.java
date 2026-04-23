package ro.uaic.asli.lab7.exception;

/**
 * Thrown when the constraint solver finds no set of movies satisfying unrelated + min size.
 */
public class NoFeasibleSolutionException extends RuntimeException {

    public NoFeasibleSolutionException(String message) {
        super(message);
    }
}
