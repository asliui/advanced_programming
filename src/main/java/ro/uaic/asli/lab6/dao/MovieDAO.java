package ro.uaic.asli.lab6.dao;

import ro.uaic.asli.lab6.database.DatabaseConnection;
import ro.uaic.asli.lab6.model.Actor;
import ro.uaic.asli.lab6.model.Genre;
import ro.uaic.asli.lab6.model.Movie;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class MovieDAO {
    public MovieDAO() {
    }

    public Movie create(Movie movie) {
        if (movie == null) {
            throw new IllegalArgumentException("Movie must not be null.");
        }
        if (movie.getGenre() == null || movie.getGenre().getId() == 0) {
            throw new IllegalArgumentException("Movie.genre must exist (genreId must be set).");
        }

        // Idempotency: avoid duplicate movies by natural key.
        Movie existing = findByTitleAndReleaseDate(movie.getTitle(), movie.getReleaseDate());
        if (existing != null) {
            return existing;
        }

        String sql = """
                INSERT INTO movies(title, release_date, duration, score, genre_id)
                VALUES (?, ?, ?, ?, ?)
                RETURNING id
                """;

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, movie.getTitle());
            stmt.setDate(2, Date.valueOf(movie.getReleaseDate()));
            stmt.setInt(3, movie.getDuration());
            stmt.setBigDecimal(4, movie.getScore());
            stmt.setInt(5, movie.getGenre().getId());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id");
                    movie.setId(id);
                    return movie;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while creating movie: " + e.getMessage(), e);
        }

        throw new RuntimeException("Could not create movie (no generated key returned).");
    }

    public Movie findByTitleAndReleaseDate(String title, LocalDate releaseDate) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Movie title must not be blank.");
        }
        if (releaseDate == null) {
            throw new IllegalArgumentException("Movie releaseDate must not be null.");
        }

        String sql = """
                SELECT
                    m.id, m.title, m.release_date, m.duration, m.score,
                    g.id AS genre_id, g.name AS genre_name
                FROM movies m
                JOIN genres g ON g.id = m.genre_id
                WHERE m.title = ? AND m.release_date = ?
                """;

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, title.strip());
            stmt.setDate(2, Date.valueOf(releaseDate));

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                int movieId = rs.getInt("id");
                String foundTitle = rs.getString("title");
                LocalDate foundReleaseDate = rs.getDate("release_date").toLocalDate();
                int duration = rs.getInt("duration");
                BigDecimal score = rs.getBigDecimal("score");
                Genre genre = new Genre(rs.getInt("genre_id"), rs.getString("genre_name"));

                Movie movie = new Movie(movieId, foundTitle, foundReleaseDate, duration, score, genre);
                movie.setActors(findActorsForMovie(connection, movieId));
                return movie;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while finding movie by title/releaseDate: " + e.getMessage(), e);
        }
    }

    public Movie findById(int id) {
        String sql = """
                SELECT
                    m.id, m.title, m.release_date, m.duration, m.score,
                    g.id AS genre_id, g.name AS genre_name
                FROM movies m
                JOIN genres g ON g.id = m.genre_id
                WHERE m.id = ?
                """;

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                int movieId = rs.getInt("id");
                String title = rs.getString("title");
                LocalDate releaseDate = rs.getDate("release_date").toLocalDate();
                int duration = rs.getInt("duration");
                BigDecimal score = rs.getBigDecimal("score");

                Genre genre = new Genre(rs.getInt("genre_id"), rs.getString("genre_name"));

                Movie movie = new Movie(movieId, title, releaseDate, duration, score, genre);
                movie.setActors(findActorsForMovie(connection, movieId));
                return movie;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while finding movie by id: " + e.getMessage(), e);
        }
    }

    public List<Movie> findAll() {
        String sql = """
                SELECT
                    m.id, m.title, m.release_date, m.duration, m.score,
                    g.id AS genre_id, g.name AS genre_name
                FROM movies m
                JOIN genres g ON g.id = m.genre_id
                ORDER BY m.id
                """;

        List<Movie> movies = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                int movieId = rs.getInt("id");
                String title = rs.getString("title");
                LocalDate releaseDate = rs.getDate("release_date").toLocalDate();
                int duration = rs.getInt("duration");
                BigDecimal score = rs.getBigDecimal("score");

                Genre genre = new Genre(rs.getInt("genre_id"), rs.getString("genre_name"));
                Movie movie = new Movie(movieId, title, releaseDate, duration, score, genre);
                movie.setActors(findActorsForMovie(connection, movieId));
                movies.add(movie);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while listing movies: " + e.getMessage(), e);
        }

        return movies;
    }

    public void addActorToMovie(int movieId, int actorId) {
        String sql = """
                INSERT INTO movie_actors(movie_id, actor_id)
                VALUES (?, ?)
                ON CONFLICT (movie_id, actor_id) DO NOTHING
                """;

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, movieId);
            stmt.setInt(2, actorId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error while linking actor to movie: " + e.getMessage(), e);
        }
    }

    private List<Actor> findActorsForMovie(Connection connection, int movieId) throws SQLException {
        String sql = """
                SELECT a.id, a.name
                FROM actors a
                JOIN movie_actors ma ON ma.actor_id = a.id
                WHERE ma.movie_id = ?
                ORDER BY a.name
                """;

        List<Actor> actors = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, movieId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    actors.add(new Actor(rs.getInt("id"), rs.getString("name")));
                }
            }
        }
        return actors;
    }
}

