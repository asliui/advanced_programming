package ro.uaic.asli.lab6.service;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import ro.uaic.asli.lab6.dao.ActorDAO;
import ro.uaic.asli.lab6.dao.GenreDAO;
import ro.uaic.asli.lab6.dao.MovieDAO;
import ro.uaic.asli.lab6.model.Actor;
import ro.uaic.asli.lab6.model.Genre;
import ro.uaic.asli.lab6.model.Movie;

import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Imports movies from a CSV file with header:
 * title,release_date,duration,score,genre,actors
 *
 * actors format: actor1;actor2;actor3 (semicolon-separated).
 */
public final class CsvMovieImportService {
    private final GenreDAO genreDAO = new GenreDAO();
    private final ActorDAO actorDAO = new ActorDAO();
    private final MovieDAO movieDAO = new MovieDAO();

    public void importFromCsv(Path csvPath) {
        if (csvPath == null) {
            throw new IllegalArgumentException("csvPath must not be null.");
        }
        if (!Files.exists(csvPath)) {
            throw new IllegalArgumentException("CSV file does not exist: " + csvPath);
        }

        try (Reader fileReader = Files.newBufferedReader(csvPath, StandardCharsets.UTF_8);
             CSVReader reader = new CSVReaderBuilder(fileReader).build()) {

            // header
            String[] header = reader.readNext();
            if (header == null) {
                return;
            }

            String[] row;
            while ((row = reader.readNext()) != null) {
                if (row.length < 6) {
                    continue; // skip malformed/blank lines
                }

                String title = row[0].trim();
                LocalDate releaseDate = LocalDate.parse(row[1].trim());
                int duration = Integer.parseInt(row[2].trim());
                BigDecimal score = new BigDecimal(row[3].trim());
                String genreName = row[4].trim();

                Genre genre = genreDAO.create(genreName);
                Movie movie = new Movie(title, releaseDate, duration, score, genre);
                Movie created = movieDAO.create(movie);

                // actors: semicolon-separated names inside the CSV cell
                List<String> actorNames = splitActors(row[5]);
                for (String actorName : actorNames) {
                    Actor actor = actorDAO.create(actorName);
                    movieDAO.addActorToMovie(created.getId(), actor.getId());
                }
            }
        } catch (IOException | CsvValidationException e) {
            throw new RuntimeException("Could not read CSV: " + csvPath, e);
        }
    }

    private static List<String> splitActors(String cell) {
        if (cell == null || cell.isBlank()) {
            return List.of();
        }
        String[] tokens = cell.split(";");
        List<String> out = new ArrayList<>(tokens.length);
        for (String t : tokens) {
            String name = t.trim();
            if (!name.isBlank()) {
                out.add(name);
            }
        }
        return out;
    }
}

