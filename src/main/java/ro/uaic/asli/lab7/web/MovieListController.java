package ro.uaic.asli.lab7.web;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import ro.uaic.asli.lab7.dto.MovieResponse;
import ro.uaic.asli.lab7.service.MovieService;

/**
 * Compulsory: GET list of movies only.
 */
@RestController
@RequestMapping("/api/movies")
@Profile({"lab7-compulsory", "lab7-homework", "lab7-advanced"})
@Tag(name = "Movies", description = "Movie list (all lab tiers)")
public class MovieListController {

    private final MovieService movieService;

    public MovieListController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping
    @Operation(summary = "List all movies")
    @ApiResponse(responseCode = "200", description = "OK")
    public List<MovieResponse> listMovies() {
        return movieService.findAll();
    }
}
