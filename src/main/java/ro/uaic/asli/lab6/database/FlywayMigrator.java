package ro.uaic.asli.lab6.database;

import org.flywaydb.core.Flyway;

/**
 * Runs Flyway migrations on application startup.
 */
public final class FlywayMigrator {
    private FlywayMigrator() {
    }

    public static void migrate() {
        Flyway flyway = Flyway.configure()
                .dataSource(DatabaseConnection.getInstance().getDataSource())
                .baselineOnMigrate(true)
                .locations("classpath:db/migration")
                .load();
        flyway.migrate();
    }
}

