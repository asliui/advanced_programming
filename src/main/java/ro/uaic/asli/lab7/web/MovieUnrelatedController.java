package ro.uaic.asli.lab7.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ro.uaic.asli.lab7.dto.MovieResponse;
import ro.uaic.asli.lab7.exception.BadRequestException;
import ro.uaic.asli.lab7.service.UnrelatedMoviesConstraintService;

import java.util.List;

/**
 * Advanced: unrelated movies via Choco-Solver (count &gt; threshold).
 */
@RestController
@RequestMapping("/api/movies")
@Profile("lab7-advanced")
@Tag(name = "Movies", description = "Advanced: unrelated movie set")
public class MovieUnrelatedController {

    private final UnrelatedMoviesConstraintService unrelatedMoviesConstraintService;

    public MovieUnrelatedController(UnrelatedMoviesConstraintService unrelatedMoviesConstraintService) {
        this.unrelatedMoviesConstraintService = unrelatedMoviesConstraintService;
    }

    @GetMapping("/unrelated")
    @Operation(
            summary = "Find unrelated movies (no shared actors) with size strictly greater than threshold",
            description = "Uses Choco-Solver: selected movies must be pairwise unrelated; selected count must be > threshold."
    )
    @ApiResponse(responseCode = "200", description = "A feasible set was found")
    @ApiResponse(responseCode = "404", description = "No feasible set or not enough movies")
    public List<MovieResponse> unrelatedMovies(
            @Parameter(description = "Result list size must be strictly greater than this value")
            @RequestParam(value = "threshold", required = false) Integer threshold,
            @Parameter(description = "Alias for threshold (same semantics)")
            @RequestParam(value = "minCount", required = false) Integer minCount
    ) {
        if (threshold != null && minCount != null && !threshold.equals(minCount)) {
            throw new BadRequestException("Use either threshold or minCount with the same value (strict > semantics).");
        }
        int t = threshold != null ? threshold : (minCount != null ? minCount : 0);
        return unrelatedMoviesConstraintService.findUnrelatedMovies(t);
    }
}
