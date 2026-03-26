package ro.uaic.asli.lab6;

import ro.uaic.asli.lab6.dao.ActorDAO;
import ro.uaic.asli.lab6.dao.GenreDAO;
import ro.uaic.asli.lab6.dao.MovieDAO;
import ro.uaic.asli.lab6.database.FlywayMigrator;
import ro.uaic.asli.lab6.model.Actor;
import ro.uaic.asli.lab6.model.Genre;
import ro.uaic.asli.lab6.model.Movie;
import ro.uaic.asli.lab6.service.MovieReportService;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

public final class HomeworkApp {
    public static void main(String[] args) {
        System.out.println("--- LAB 6 HOMEWORK (JDBC + Pool + HTML) ---");

        FlywayMigrator.migrate();

        GenreDAO genreDAO = new GenreDAO();
        ActorDAO actorDAO = new ActorDAO();
        MovieDAO movieDAO = new MovieDAO();
        MovieReportService reportService = new MovieReportService();

        // Seed sample data only if movies table is empty.
        List<Movie> existing = movieDAO.findAll();
        if (existing.isEmpty()) {
            Genre sciFi = genreDAO.create("Sci-Fi");
            Genre drama = genreDAO.create("Drama");

            Actor keanu = actorDAO.create("Keanu Reeves");
            Actor laurence = actorDAO.create("Laurence Fishburne");
            Actor al = actorDAO.create("Al Pacino");
            Actor mare = actorDAO.create("Mare Winningham");

            Movie matrix = new Movie(
                    "The Matrix",
                    LocalDate.parse("1999-03-31"),
                    136,
                    new BigDecimal("8.7"),
                    sciFi
            );
            movieDAO.create(matrix);
            movieDAO.addActorToMovie(matrix.getId(), keanu.getId());
            movieDAO.addActorToMovie(matrix.getId(), laurence.getId());

            Movie scent = new Movie(
                    "Scent of a Woman",
                    LocalDate.parse("1992-12-23"),
                    157,
                    new BigDecimal("8.0"),
                    drama
            );
            movieDAO.create(scent);
            movieDAO.addActorToMovie(scent.getId(), al.getId());
            movieDAO.addActorToMovie(scent.getId(), mare.getId());
        }

        Path output = Path.of("target/lab6-movies-report.html");
        reportService.createAndOpenHtmlReport(output);
        System.out.println("Report generated at: " + output.toAbsolutePath());
    }
}

