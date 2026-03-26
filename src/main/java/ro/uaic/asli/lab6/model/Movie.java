package ro.uaic.asli.lab6.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public final class Movie {
    private int id;
    private String title;
    private LocalDate releaseDate;
    private int duration;
    private BigDecimal score;
    private Genre genre;
    private final List<Actor> actors = new ArrayList<>();

    public Movie() {
    }

    public Movie(int id, String title, LocalDate releaseDate, int duration, BigDecimal score, Genre genre) {
        this.id = id;
        this.title = Objects.requireNonNull(title);
        this.releaseDate = Objects.requireNonNull(releaseDate);
        this.duration = duration;
        this.score = Objects.requireNonNull(score);
        this.genre = Objects.requireNonNull(genre);
    }

    public Movie(String title, LocalDate releaseDate, int duration, BigDecimal score, Genre genre) {
        this(0, title, releaseDate, duration, score, genre);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public Genre getGenre() {
        return genre;
    }

    public void setGenre(Genre genre) {
        this.genre = genre;
    }

    public List<Actor> getActors() {
        return List.copyOf(actors);
    }

    public void setActors(Collection<Actor> newActors) {
        actors.clear();
        if (newActors != null) {
            actors.addAll(newActors);
        }
    }

    public void addActor(Actor actor) {
        if (actor != null) {
            actors.add(actor);
        }
    }

    @Override
    public String toString() {
        return "Movie{id=" + id + ", title='" + title + "', genre=" + (genre != null ? genre.getName() : "null") + "}";
    }
}

