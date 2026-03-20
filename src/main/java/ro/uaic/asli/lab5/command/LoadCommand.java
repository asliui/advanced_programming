package ro.uaic.asli.lab5.command;

import ro.uaic.asli.lab5.exception.CatalogException;
import ro.uaic.asli.lab5.repository.CatalogRepository;

import java.nio.file.Path;

public final class LoadCommand implements Command {
    private final CatalogRepository repository;
    private final Path path;

    public LoadCommand(CatalogRepository repository, Path path) {
        this.repository = repository;
        this.path = path;
    }

    @Override
    public void execute() throws CatalogException {
        repository.load(path);
    }
}
