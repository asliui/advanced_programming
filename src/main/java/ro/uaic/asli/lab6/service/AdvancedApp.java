package ro.uaic.asli.lab6.service;

import ro.uaic.asli.lab6.database.FlywayMigrator;

import java.nio.file.Path;

public final class AdvancedApp {
    public static void main(String[] args) {
        System.out.println("--- LAB 6 ADVANCED (CSV import + partition + movie lists) ---");

        FlywayMigrator.migrate();

        String csv = args.length >= 1 ? args[0] : "src/main/resources/lab6-movies-import-sample.csv";
        Path csvPath = Path.of(csv);

        new CsvMovieImportService().importFromCsv(csvPath);

        new MovieListPartitionService().partitionAndPersist();
    }
}

