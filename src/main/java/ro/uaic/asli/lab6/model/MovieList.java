package ro.uaic.asli.lab6.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public final class MovieList {
    private int id;
    private String name;
    private Instant createdAt;
    private final List<Movie> movies = new ArrayList<>();

    public MovieList() {
    }

    public MovieList(int id, String name, Instant createdAt) {
        this.id = id;
        this.name = Objects.requireNonNull(name);
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public MovieList(String name, Instant createdAt) {
        this(0, name, createdAt);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public List<Movie> getMovies() {
        return List.copyOf(movies);
    }

    public void setMovies(Collection<Movie> newMovies) {
        movies.clear();
        if (newMovies != null) {
            movies.addAll(newMovies);
        }
    }

    public void addMovie(Movie movie) {
        if (movie != null) {
            movies.add(movie);
        }
    }
}

