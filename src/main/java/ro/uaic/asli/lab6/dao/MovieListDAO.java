package ro.uaic.asli.lab6.dao;

import ro.uaic.asli.lab6.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;

public final class MovieListDAO {
    public void clearAll() {
        String sql = "DELETE FROM movie_lists";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error while clearing movie lists: " + e.getMessage(), e);
        }
    }

    public int create(String name, Instant createdAt) {
        String sql = """
                INSERT INTO movie_lists(name, created_at)
                VALUES (?, ?)
                RETURNING id
                """;

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setTimestamp(2, Timestamp.from(createdAt));
            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while creating movie list: " + e.getMessage(), e);
        }

        throw new RuntimeException("Could not create movie list (no generated key returned).");
    }

    public void addMovieToList(int listId, int movieId) {
        String sql = """
                INSERT INTO movie_list_movies(list_id, movie_id)
                VALUES (?, ?)
                ON CONFLICT (list_id, movie_id) DO NOTHING
                """;

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, listId);
            stmt.setInt(2, movieId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error while adding movie to list: " + e.getMessage(), e);
        }
    }
}

