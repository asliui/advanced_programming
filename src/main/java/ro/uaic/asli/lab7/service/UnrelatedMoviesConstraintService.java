package ro.uaic.asli.lab7.service;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.uaic.asli.lab7.dto.MovieResponse;
import ro.uaic.asli.lab7.entity.ActorEntity;
import ro.uaic.asli.lab7.entity.MovieEntity;
import ro.uaic.asli.lab7.exception.BadRequestException;
import ro.uaic.asli.lab7.exception.NoFeasibleSolutionException;
import ro.uaic.asli.lab7.mapper.MovieMapper;
import ro.uaic.asli.lab7.repository.MovieRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Finds a set of movies that are pairwise unrelated (share no actors), with size &gt; threshold,
 * using Choco-Solver. "Unrelated" matches lab 6: an edge exists if two movies share an actor.
 */
@Service
@Profile("lab7-advanced")
public class UnrelatedMoviesConstraintService {

    private final MovieRepository movieRepository;
    private final MovieMapper movieMapper;

    public UnrelatedMoviesConstraintService(MovieRepository movieRepository, MovieMapper movieMapper) {
        this.movieRepository = movieRepository;
        this.movieMapper = movieMapper;
    }

    /**
     * @param threshold list size must be strictly greater than this value (lab advanced: count &gt; parameter)
     */
    @Transactional(readOnly = true)
    public List<MovieResponse> findUnrelatedMovies(int threshold) {
        if (threshold < 0) {
            throw new BadRequestException("threshold must be non-negative");
        }

        List<MovieEntity> movies = movieRepository.findAllWithActorsAndGenre();
        if (movies.size() <= threshold) {
            throw new NoFeasibleSolutionException(
                    "Not enough movies in database: need more than " + threshold
                            + " movies (have " + movies.size() + ")"
            );
        }

        int n = movies.size();
        boolean[][] conflict = buildConflictMatrix(movies);

        Model model = new Model("unrelatedMovies");
        BoolVar[] x = model.boolVarArray("pick", n);

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (conflict[i][j]) {
                    IntVar[] pair = new IntVar[]{x[i], x[j]};
                    model.post(model.scalar(pair, new int[]{1, 1}, "<=", 1));
                }
            }
        }

        model.post(model.sum(x, ">", threshold));

        Solver solver = model.getSolver();
        boolean found = solver.solve();

        if (!found) {
            throw new NoFeasibleSolutionException(
                    "No set of pairwise unrelated movies with size strictly greater than " + threshold + " exists."
            );
        }

        List<MovieResponse> result = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            if (x[i].getValue() == 1) {
                result.add(movieMapper.toResponse(movies.get(i)));
            }
        }
        return result;
    }

    /**
     * Two movies conflict (share an actor) iff they cannot both be selected.
     */
    private static boolean[][] buildConflictMatrix(List<MovieEntity> movies) {
        int n = movies.size();
        boolean[][] conflict = new boolean[n][n];

        Map<Integer, List<Integer>> actorToMovieIndices = new HashMap<>();
        for (int i = 0; i < n; i++) {
            for (ActorEntity a : movies.get(i).getActors()) {
                actorToMovieIndices.computeIfAbsent(a.getId(), ignored -> new ArrayList<>()).add(i);
            }
        }

        for (List<Integer> indices : actorToMovieIndices.values()) {
            for (int ii = 0; ii < indices.size(); ii++) {
                int u = indices.get(ii);
                for (int jj = ii + 1; jj < indices.size(); jj++) {
                    int v = indices.get(jj);
                    conflict[u][v] = true;
                    conflict[v][u] = true;
                }
            }
        }

        return conflict;
    }
}
