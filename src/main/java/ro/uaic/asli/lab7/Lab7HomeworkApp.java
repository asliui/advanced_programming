package ro.uaic.asli.lab7;

import org.springframework.boot.SpringApplication;

/**
 * Lab 7 Homework (2p): movie + actor CRUD, global exception handler, Swagger, client;
 * no JWT and no unrelated-movies solver.
 */
public final class Lab7HomeworkApp {

    private Lab7HomeworkApp() {
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(Lab7Application.class);
        app.setAdditionalProfiles("lab7-homework");
        app.run(args);
    }
}
