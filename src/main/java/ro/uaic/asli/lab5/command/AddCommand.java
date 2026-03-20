package ro.uaic.asli.lab5.command;

import ro.uaic.asli.lab5.exception.CatalogException;
import ro.uaic.asli.lab5.model.BibliographicResource;
import ro.uaic.asli.lab5.repository.CatalogRepository;

public final class AddCommand implements Command {
    private final CatalogRepository repository;
    private final BibliographicResource resource;

    public AddCommand(CatalogRepository repository, BibliographicResource resource) {
        this.repository = repository;
        this.resource = resource;
    }

    @Override
    public void execute() throws CatalogException {
        repository.add(resource);
    }
}
