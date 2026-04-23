package ro.uaic.asli.lab7;

import org.springframework.boot.SpringApplication;

/**
 * Lab 7 Compulsory (1p): Spring Boot + {@code GET /api/movies} only (no Swagger, no JWT).
 */
public final class Lab7CompulsoryApp {

    private Lab7CompulsoryApp() {
    }
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(Lab7Application.class);
        app.setAdditionalProfiles("lab7-compulsory");
        app.run(args);
    }
}
