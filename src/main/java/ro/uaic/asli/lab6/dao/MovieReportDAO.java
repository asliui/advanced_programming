package ro.uaic.asli.lab6.dao;

import ro.uaic.asli.lab6.database.DatabaseConnection;
import ro.uaic.asli.lab6.model.MovieReportRow;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class MovieReportDAO {
    public List<MovieReportRow> findAll() {
        String sql = """
                SELECT
                    movie_id,
                    title,
                    release_date,
                    duration,
                    score,
                    genre,
                    actors
                FROM v_movie_report
                ORDER BY movie_id
                """;

        List<MovieReportRow> rows = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                int movieId = rs.getInt("movie_id");
                String title = rs.getString("title");
                LocalDate releaseDate = rs.getDate("release_date").toLocalDate();
                int duration = rs.getInt("duration");
                BigDecimal score = rs.getBigDecimal("score");
                String genre = rs.getString("genre");
                String actors = rs.getString("actors");

                rows.add(new MovieReportRow(movieId, title, releaseDate, duration, score, genre, actors));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while reading movie report from view: " + e.getMessage(), e);
        }

        return rows;
    }
}

