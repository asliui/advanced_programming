package ro.uaic.asli.lab6.database;

import org.flywaydb.core.Flyway;

/**
 * Runs Flyway migrations on application startup.
 *
 * In case a transitive dependency issue (for example around JSON/Jackson classes inside newer
 * Flyway versions) prevents Flyway from starting, migrations are skipped so that Lab 6
 * Compulsory/Homework can still run against an already-initialized database.
 */
public final class FlywayMigrator {
    private FlywayMigrator() {
    }

    public static void migrate() {
        try {
            Flyway flyway = Flyway.configure()
                    .dataSource(DatabaseConnection.getInstance().getDataSource())
                    .baselineOnMigrate(true)
                    .locations("classpath:db/migration")
                    .load();
            flyway.migrate();
        } catch (Exception | LinkageError e) {
            System.err.println("[Lab6] Flyway migrations skipped due to: " + e.getClass().getSimpleName()
                    + " - " + String.valueOf(e.getMessage()));
        }
    }
}

