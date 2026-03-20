package ro.uaic.asli.lab5.command;

import ro.uaic.asli.lab5.exception.CatalogException;
import ro.uaic.asli.lab5.repository.CatalogRepository;
import ro.uaic.asli.lab5.repository.ResourceViewer;

public final class ViewCommand implements Command {
    private final CatalogRepository repository;
    private final ResourceViewer viewer;
    private final String resourceId;

    public ViewCommand(CatalogRepository repository, ResourceViewer viewer, String resourceId) {
        this.repository = repository;
        this.viewer = viewer;
        this.resourceId = resourceId;
    }

    @Override
    public void execute() throws CatalogException {
        viewer.view(repository.getById(resourceId));
    }
}
