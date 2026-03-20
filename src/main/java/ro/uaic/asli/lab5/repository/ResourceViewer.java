package ro.uaic.asli.lab5.repository;

import ro.uaic.asli.lab5.exception.CatalogException;
import ro.uaic.asli.lab5.model.BibliographicResource;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ResourceViewer {
    public void view(BibliographicResource resource) throws CatalogException {
        if (!Desktop.isDesktopSupported()) {
            throw new CatalogException("Desktop API is not supported on this platform.");
        }
        Desktop desktop = Desktop.getDesktop();
        String location = resource.getLocation();

        try {
            if (location.startsWith("http://") || location.startsWith("https://")) {
                desktop.browse(URI.create(location));
                return;
            }
            Path path = Path.of(location);
            if (!Files.exists(path)) {
                throw new CatalogException("Local file does not exist: " + location);
            }
            desktop.open(new File(location));
        } catch (IOException | IllegalArgumentException | SecurityException e) {
            throw new CatalogException("Cannot open resource '%s' at '%s'".formatted(resource.getId(), location), e);
        }
    }
}
