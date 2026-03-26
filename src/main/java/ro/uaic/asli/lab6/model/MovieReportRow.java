package ro.uaic.asli.lab6.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class MovieReportRow {
    private final int movieId;
    private final String title;
    private final LocalDate releaseDate;
    private final int duration;
    private final BigDecimal score;
    private final String genreName;
    private final String actors;

    public MovieReportRow(
            int movieId,
            String title,
            LocalDate releaseDate,
            int duration,
            BigDecimal score,
            String genreName,
            String actors
    ) {
        this.movieId = movieId;
        this.title = title;
        this.releaseDate = releaseDate;
        this.duration = duration;
        this.score = score;
        this.genreName = genreName;
        this.actors = actors;
    }

    public int getMovieId() {
        return movieId;
    }

    public String getTitle() {
        return title;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public int getDuration() {
        return duration;
    }

    public BigDecimal getScore() {
        return score;
    }

    public String getGenreName() {
        return genreName;
    }

    public String getActors() {
        return actors;
    }
}

