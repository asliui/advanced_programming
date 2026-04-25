package ro.uaic.asli.lab7.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ro.uaic.asli.lab7.dto.MovieRequest;
import ro.uaic.asli.lab7.dto.MovieResponse;
import ro.uaic.asli.lab7.dto.ScorePatchRequest;
import ro.uaic.asli.lab7.service.MovieService;

import java.net.URI;

/**
 * Homework+: movie CRUD. Advanced: JWT required at runtime for these methods (homework profile allows all without token).
 */
@RestController
@RequestMapping("/api/movies")
@Profile({"lab7-homework", "lab7-advanced"})
@Tag(name = "Movies", description = "Movie write operations")
public class MovieCommandController {

    private final MovieService movieService;

    public MovieCommandController(MovieService movieService) {
        this.movieService = movieService;
    }

    @PostMapping
    @Operation(summary = "Create a movie")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "201", description = "Created")
    @ApiResponse(responseCode = "400", description = "Bad request")
    public ResponseEntity<MovieResponse> createMovie(@Valid @RequestBody MovieRequest request) {
        MovieResponse created = movieService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id:\\d+}")
    @Operation(summary = "Replace movie properties")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "404", description = "Not found")
    public MovieResponse updateMovie(@PathVariable Integer id, @Valid @RequestBody MovieRequest request) {
        return movieService.update(id, request);
    }

    @PatchMapping("/{id:\\d+}/score")
    @Operation(summary = "Update only the movie score")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "404", description = "Not found")
    public MovieResponse patchScore(@PathVariable Integer id, @Valid @RequestBody ScorePatchRequest scorePatch) {
        return movieService.updateScore(id, scorePatch.getScore());
    }

    @DeleteMapping("/{id:\\d+}")
    @Operation(summary = "Delete a movie")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "204", description = "No content")
    @ApiResponse(responseCode = "404", description = "Not found")
    public ResponseEntity<Void> deleteMovie(@PathVariable Integer id) {
        movieService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
