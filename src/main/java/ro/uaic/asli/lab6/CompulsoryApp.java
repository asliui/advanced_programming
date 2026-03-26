package ro.uaic.asli.lab6;

import ro.uaic.asli.lab6.dao.GenreDAO;
import ro.uaic.asli.lab6.database.FlywayMigrator;
import ro.uaic.asli.lab6.model.Genre;

public final class CompulsoryApp {
    public static void main(String[] args) {
        System.out.println("--- LAB 6 COMPULSORY (JDBC) ---");

        FlywayMigrator.migrate();
        GenreDAO genreDAO = new GenreDAO();

        try {
            Genre sciFi = genreDAO.create("Sci-Fi");
            System.out.println("Created/exists: " + sciFi);

            Genre byId = genreDAO.findById(sciFi.getId());
            System.out.println("findById: " + byId);

            Genre drama = genreDAO.create("Drama");
            System.out.println("Created/exists: " + drama);

            Genre byName = genreDAO.findByName("Drama");
            System.out.println("findByName: " + byName);
        } catch (RuntimeException e) {
            System.err.println("Lab 6 flow failed: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }
}

