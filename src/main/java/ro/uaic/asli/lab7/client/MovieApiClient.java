package ro.uaic.asli.lab7.client;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Simple console client that calls the Lab 7 REST API (login, CRUD, unrelated query).
 * Run the Spring Boot app first, then run this main with the same JVM:
 * {@code java -cp ... ro.uaic.asli.lab7.client.MovieApiClient}
 */
public final class MovieApiClient {

    /** Override with env {@code LAB7_CLIENT_BASE} (e.g. http://localhost:8081 for Compulsory). */
    private static final String BASE = resolveBase();

    private static String resolveBase() {
        String env = System.getenv("LAB7_CLIENT_BASE");
        if (env != null && !env.isBlank()) {
            return env.trim().replaceAll("/+$", "");
        }
        return "http://localhost:8080";
    }

    private MovieApiClient() {
    }

    public static void main(String[] args) {
        RestTemplate rest = new RestTemplate();
        Scanner scanner = new Scanner(System.in);

        System.out.println("Lab 7 Movie API client. Default user: labuser / labpass");
        System.out.print("JWT token (empty = login first): ");
        String token = scanner.nextLine().trim();

        if (token.isEmpty()) {
            System.out.print("Username [labuser]: ");
            String user = scanner.nextLine().trim();
            if (user.isEmpty()) {
                user = "labuser";
            }
            System.out.print("Password [labpass]: ");
            String pass = scanner.nextLine().trim();
            if (pass.isEmpty()) {
                pass = "labpass";
            }
            try {
                token = login(rest, user, pass);
                System.out.println("Token obtained (first 40 chars): " + token.substring(0, Math.min(40, token.length())) + "...");
            } catch (RuntimeException e) {
                System.out.println("Login skipped (e.g. Lab7 Homework has no /api/auth/login). Writes work without JWT. " + e.getMessage());
                token = "";
            }
        }

        while (true) {
            System.out.println();
            System.out.println("1) GET all movies");
            System.out.println("2) GET unrelated movies (liste boyutu > threshold)");
            System.out.println("3) POST new movie (JWT if Advanced profile)");
            System.out.println("4) PUT update movie (JWT if Advanced)");
            System.out.println("5) PATCH score (JWT if Advanced)");
            System.out.println("6) DELETE movie (JWT if Advanced)");
            System.out.println("7) GET all actors");
            System.out.println("8) POST new actor (JWT if Advanced)");
            System.out.println("9) PUT actor (JWT if Advanced)");
            System.out.println("10) DELETE actor (JWT if Advanced)");
            System.out.println("0) Exit");
            System.out.print("Choice: ");
            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> listMovies(rest);
                    case "2" -> {
                        System.out.print("threshold (liste boyutu bundan > olmalı): ");
                        int t = Integer.parseInt(scanner.nextLine().trim());
                        unrelated(rest, t);
                    }
                    case "3" -> createMovie(rest, token, scanner);
                    case "4" -> updateMovie(rest, token, scanner);
                    case "5" -> patchScore(rest, token, scanner);
                    case "6" -> deleteMovie(rest, token, scanner);
                    case "7" -> listActors(rest);
                    case "8" -> createActor(rest, token, scanner);
                    case "9" -> updateActor(rest, token, scanner);
                    case "10" -> deleteActor(rest, token, scanner);
                    case "0" -> {
                        return;
                    }
                    default -> System.out.println("Unknown option.");
                }
            } catch (RuntimeException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private static void addBearerIfPresent(HttpHeaders headers, String token) {
        if (token != null && !token.isEmpty()) {
            headers.setBearerAuth(token);
        }
    }

    private static String login(RestTemplate rest, String username, String password) {
        Map<String, String> body = new HashMap<>();
        body.put("username", username);
        body.put("password", password);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<Map<String, Object>> resp = rest.exchange(
                BASE + "/api/auth/login",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<>() {
                }
        );
        Map<String, Object> map = resp.getBody();
        if (map == null || !map.containsKey("token")) {
            throw new IllegalStateException("Login failed");
        }
        return String.valueOf(map.get("token"));
    }

    private static void listMovies(RestTemplate rest) {
        ResponseEntity<List<Map<String, Object>>> resp = rest.exchange(
                BASE + "/api/movies",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );
        System.out.println(resp.getBody());
    }

    private static void unrelated(RestTemplate rest, int threshold) {
        ResponseEntity<List<Map<String, Object>>> resp = rest.exchange(
                BASE + "/api/movies/unrelated?threshold=" + threshold,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );
        System.out.println(resp.getBody());
    }

    private static void listActors(RestTemplate rest) {
        ResponseEntity<List<Map<String, Object>>> resp = rest.exchange(
                BASE + "/api/actors",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );
        System.out.println(resp.getBody());
    }

    private static void createActor(RestTemplate rest, String token, Scanner scanner) {
        System.out.print("actor name: ");
        String name = scanner.nextLine();
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        addBearerIfPresent(headers, token);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<Map<String, Object>> resp = rest.exchange(
                BASE + "/api/actors",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<>() {
                }
        );
        System.out.println("Created: " + resp.getBody());
    }

    private static void updateActor(RestTemplate rest, String token, Scanner scanner) {
        System.out.print("actor id: ");
        long id = Long.parseLong(scanner.nextLine().trim());
        System.out.print("new name: ");
        String name = scanner.nextLine();
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        addBearerIfPresent(headers, token);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<Map<String, Object>> resp = rest.exchange(
                BASE + "/api/actors/" + id,
                HttpMethod.PUT,
                entity,
                new ParameterizedTypeReference<>() {
                }
        );
        System.out.println("Updated: " + resp.getBody());
    }

    private static void deleteActor(RestTemplate rest, String token, Scanner scanner) {
        System.out.print("actor id: ");
        long id = Long.parseLong(scanner.nextLine().trim());
        HttpHeaders headers = new HttpHeaders();
        addBearerIfPresent(headers, token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        rest.exchange(
                BASE + "/api/actors/" + id,
                HttpMethod.DELETE,
                entity,
                Void.class
        );
        System.out.println("Deleted (204).");
    }

    private static void createMovie(RestTemplate rest, String token, Scanner scanner) {
        System.out.print("title: ");
        String title = scanner.nextLine();
        System.out.print("releaseDate (yyyy-MM-dd): ");
        LocalDate date = LocalDate.parse(scanner.nextLine().trim());
        System.out.print("duration (minutes): ");
        int duration = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("score (0-10): ");
        BigDecimal score = new BigDecimal(scanner.nextLine().trim());
        System.out.print("genreId (1=Drama,2=Sci-Fi,3=Action in seed): ");
        long genreId = Long.parseLong(scanner.nextLine().trim());

        Map<String, Object> body = new HashMap<>();
        body.put("title", title);
        body.put("releaseDate", date.toString());
        body.put("duration", duration);
        body.put("score", score);
        body.put("genreId", genreId);
        body.put("actorIds", List.of());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        addBearerIfPresent(headers, token);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<Map<String, Object>> resp = rest.exchange(
                BASE + "/api/movies",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<>() {
                }
        );
        System.out.println("Created: " + resp.getBody());
    }

    private static void updateMovie(RestTemplate rest, String token, Scanner scanner) {
        System.out.print("movie id: ");
        long id = Long.parseLong(scanner.nextLine().trim());
        System.out.print("title: ");
        String title = scanner.nextLine();
        System.out.print("releaseDate (yyyy-MM-dd): ");
        LocalDate date = LocalDate.parse(scanner.nextLine().trim());
        System.out.print("duration: ");
        int duration = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("score: ");
        BigDecimal score = new BigDecimal(scanner.nextLine().trim());
        System.out.print("genreId: ");
        long genreId = Long.parseLong(scanner.nextLine().trim());

        Map<String, Object> body = new HashMap<>();
        body.put("title", title);
        body.put("releaseDate", date.toString());
        body.put("duration", duration);
        body.put("score", score);
        body.put("genreId", genreId);
        body.put("actorIds", List.of());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        addBearerIfPresent(headers, token);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<Map<String, Object>> resp = rest.exchange(
                BASE + "/api/movies/" + id,
                HttpMethod.PUT,
                entity,
                new ParameterizedTypeReference<>() {
                }
        );
        System.out.println("Updated: " + resp.getBody());
    }

    private static void patchScore(RestTemplate rest, String token, Scanner scanner) {
        System.out.print("movie id: ");
        long id = Long.parseLong(scanner.nextLine().trim());
        System.out.print("new score: ");
        BigDecimal score = new BigDecimal(scanner.nextLine().trim());

        Map<String, Object> body = Map.of("score", score);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        addBearerIfPresent(headers, token);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<Map<String, Object>> resp = rest.exchange(
                BASE + "/api/movies/" + id + "/score",
                HttpMethod.PATCH,
                entity,
                new ParameterizedTypeReference<>() {
                }
        );
        System.out.println("Patched: " + resp.getBody());
    }

    private static void deleteMovie(RestTemplate rest, String token, Scanner scanner) {
        System.out.print("movie id: ");
        long id = Long.parseLong(scanner.nextLine().trim());
        HttpHeaders headers = new HttpHeaders();
        addBearerIfPresent(headers, token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        rest.exchange(
                BASE + "/api/movies/" + id,
                HttpMethod.DELETE,
                entity,
                Void.class
        );
        System.out.println("Deleted (204).");
    }
}
