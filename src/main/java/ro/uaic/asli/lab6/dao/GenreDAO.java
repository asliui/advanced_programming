package ro.uaic.asli.lab6.dao;

import ro.uaic.asli.lab6.database.DatabaseConnection;
import ro.uaic.asli.lab6.model.Genre;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class GenreDAO {
    public GenreDAO() {
    }

    /**
     * Creates a genre (by unique name) and returns it.
     * Uses PostgreSQL-specific UPSERT semantics.
     */
    public Genre create(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Genre name must not be blank.");
        }
        name = name.strip();

        Genre existing = findByName(name);
        if (existing != null) {
            return existing;
        }

        String sql = "INSERT INTO genres(name) VALUES (?) RETURNING id, name";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Genre(rs.getInt("id"), rs.getString("name"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while creating genre: " + e.getMessage(), e);
        }

        // INSERT .. RETURNING should return one row; fallback to search.
        return findByName(name);
    }

    public Genre findById(int id) {
        String sql = "SELECT id, name FROM genres WHERE id = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Genre(rs.getInt("id"), rs.getString("name"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while finding genre by id: " + e.getMessage(), e);
        }

        return null;
    }

    public Genre findByName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Genre name must not be blank.");
        }
        name = name.strip();

        String sql = "SELECT id, name FROM genres WHERE name = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Genre(rs.getInt("id"), rs.getString("name"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while finding genre by name: " + e.getMessage(), e);
        }

        return null;
    }
}

