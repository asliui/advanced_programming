package ro.uaic.asli.lab6.database;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Simple singleton for JDBC connection.
 * <p>
 * Configure via environment variables:
 * - LAB6_DB_URL
 * - LAB6_DB_USER
 * - LAB6_DB_PASSWORD
 */
public final class DatabaseConnection {
    private static final DatabaseConnection INSTANCE = new DatabaseConnection();

    private final HikariDataSource dataSource;

    private DatabaseConnection() {
        String url = envOrDefault("LAB6_DB_URL", "jdbc:postgresql://localhost:5432/lab6_movies");
        String user = envOrDefault("LAB6_DB_USER", "postgres");
        String password = envOrDefault("LAB6_DB_PASSWORD", "1234");

        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl(url);
        cfg.setUsername(user);
        cfg.setPassword(password);
        cfg.setMaximumPoolSize(Integer.parseInt(envOrDefault("LAB6_DB_MAX_POOL", "10")));
        cfg.setPoolName(envOrDefault("LAB6_DB_POOL_NAME", "lab6-pool"));

        this.dataSource = new HikariDataSource(cfg);

        // Fail fast: verify the pool can provide a connection.
        try (Connection c = dataSource.getConnection()) {
            if (c == null) {
                throw new RuntimeException("Failed to obtain JDBC connection from HikariCP.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database connection failed: " + e.getMessage(), e);
        }
    }

    public static DatabaseConnection getInstance() {
        return INSTANCE;
    }

    public Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Could not get connection from pool: " + e.getMessage(), e);
        }
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    private static String envOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        return value == null ? defaultValue : value;
    }
}

