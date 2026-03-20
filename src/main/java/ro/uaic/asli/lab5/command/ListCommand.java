package ro.uaic.asli.lab5.command;

import ro.uaic.asli.lab5.exception.CatalogException;
import ro.uaic.asli.lab5.repository.CatalogRepository;

public final class ListCommand implements Command {
    private final CatalogRepository repository;

    public ListCommand(CatalogRepository repository) {
        this.repository = repository;
    }

    @Override
    public void execute() throws CatalogException {
        repository.listAll().forEach(System.out::println);
    }
}
