package ro.uaic.asli.lab7.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.uaic.asli.lab7.dto.MovieResponse;
import ro.uaic.asli.lab7.service.MovieService;

/**
 * Homework+: single movie by id.
 */
@RestController
@RequestMapping("/api/movies")
@Profile({"lab7-homework", "lab7-advanced"})
@Tag(name = "Movies", description = "Movie by id")
public class MovieDetailController {

    private final MovieService movieService;

    public MovieDetailController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping("/{id:\\d+}")
    @Operation(summary = "Get movie by id")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "404", description = "Not found")
    public MovieResponse getMovie(@PathVariable Long id) {
        return movieService.findById(id);
    }
}
