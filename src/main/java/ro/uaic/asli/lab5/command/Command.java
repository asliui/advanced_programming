package ro.uaic.asli.lab5.command;

import ro.uaic.asli.lab5.exception.CatalogException;

public interface Command {
    void execute() throws CatalogException;
}
