package ro.uaic.asli.lab6.dao;

import ro.uaic.asli.lab6.database.DatabaseConnection;
import ro.uaic.asli.lab6.model.Actor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class ActorDAO {
    public ActorDAO() {
    }

    public Actor create(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Actor name must not be blank.");
        }
        name = name.strip();

        Actor existing = findByName(name);
        if (existing != null) {
            return existing;
        }

        String sql = "INSERT INTO actors(name) VALUES (?) RETURNING id, name";

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Actor(rs.getInt("id"), rs.getString("name"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while creating actor: " + e.getMessage(), e);
        }

        return findByName(name);
    }

    public Actor findById(int id) {
        String sql = "SELECT id, name FROM actors WHERE id = ?";

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Actor(rs.getInt("id"), rs.getString("name"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while finding actor by id: " + e.getMessage(), e);
        }

        return null;
    }

    public Actor findByName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Actor name must not be blank.");
        }
        name = name.strip();

        String sql = "SELECT id, name FROM actors WHERE name = ?";

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Actor(rs.getInt("id"), rs.getString("name"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while finding actor by name: " + e.getMessage(), e);
        }

        return null;
    }

    public List<Actor> findAll() {
        String sql = "SELECT id, name FROM actors ORDER BY id";
        List<Actor> actors = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                actors.add(new Actor(rs.getInt("id"), rs.getString("name")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while listing actors: " + e.getMessage(), e);
        }

        return actors;
    }
}

