package ro.uaic.asli.lab5;


import java.nio.file.Path;

import ro.uaic.asli.lab5.command.ListCommand;
import ro.uaic.asli.lab5.command.LoadCommand;
import ro.uaic.asli.lab5.command.ReportCommand;
import ro.uaic.asli.lab5.command.ViewCommand;
import ro.uaic.asli.lab5.exception.CatalogException;
import ro.uaic.asli.lab5.repository.CatalogRepository;
import ro.uaic.asli.lab5.repository.ResourceViewer;


public final class HomeworkApp {
    public static void main(String[] args) {
        var repository = new CatalogRepository();
        var viewer = new ResourceViewer();

        try {
            

            new LoadCommand(repository, Path.of("src/main/resources/lab5-sample.txt")).execute();
            System.out.println("Loaded resources: " + repository.listAll().size());

            new ListCommand(repository).execute();
            new ReportCommand(repository, Path.of("target/lab5-report.html")).execute();
            new ViewCommand(repository, viewer, "java25-local").execute();
        } catch (CatalogException e) {
            System.err.println("Homework flow failed: " + e.getMessage());
        }
    }
}
