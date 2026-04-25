package ro.uaic.asli.lab7.service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ro.uaic.asli.lab7.dto.MovieRequest;
import ro.uaic.asli.lab7.dto.MovieResponse;
import ro.uaic.asli.lab7.entity.ActorEntity;
import ro.uaic.asli.lab7.entity.GenreEntity;
import ro.uaic.asli.lab7.entity.MovieEntity;
import ro.uaic.asli.lab7.exception.BadRequestException;
import ro.uaic.asli.lab7.exception.ResourceNotFoundException;
import ro.uaic.asli.lab7.mapper.MovieMapper;
import ro.uaic.asli.lab7.repository.ActorRepository;
import ro.uaic.asli.lab7.repository.GenreRepository;
import ro.uaic.asli.lab7.repository.MovieRepository;

@Service
public class MovieService {

    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;
    private final ActorRepository actorRepository;
    private final MovieMapper movieMapper;

    public MovieService(
            MovieRepository movieRepository,
            GenreRepository genreRepository,
            ActorRepository actorRepository,
            MovieMapper movieMapper
    ) {
        this.movieRepository = movieRepository;
        this.genreRepository = genreRepository;
        this.actorRepository = actorRepository;
        this.movieMapper = movieMapper;
    }

    @Transactional(readOnly = true)
    public List<MovieResponse> findAll() {
        return movieRepository.findAllWithActorsAndGenre().stream()
                .map(movieMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public MovieResponse findById(Integer id) {
        return movieRepository.findByIdWithActorsAndGenre(id)
                .map(movieMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + id));
    }

    @Transactional
    public MovieResponse create(MovieRequest request) { //POST
        GenreEntity genre = genreRepository.findById(request.getGenreId())
                .orElseThrow(() -> new BadRequestException("Genre not found with id: " + request.getGenreId()));

        MovieEntity movie = new MovieEntity();
        applyRequest(movie, request, genre);
        movie.setActors(resolveActors(request.getActorIds()));

        MovieEntity saved = movieRepository.save(movie);
        return movieRepository.findByIdWithActorsAndGenre(saved.getId())
                .map(movieMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found after create"));
    }

    @Transactional
    public MovieResponse update(Integer id, MovieRequest request) { //PUT
        MovieEntity movie = movieRepository.findByIdWithActorsAndGenre(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + id));
        GenreEntity genre = genreRepository.findById(request.getGenreId())
                .orElseThrow(() -> new BadRequestException("Genre not found with id: " + request.getGenreId()));

        applyRequest(movie, request, genre);
        movie.setActors(resolveActors(request.getActorIds()));

        movieRepository.save(movie);
        return movieRepository.findByIdWithActorsAndGenre(id)
                .map(movieMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + id));
    }

    @Transactional
    public MovieResponse updateScore(Integer id, BigDecimal score) { //PATCH
        MovieEntity movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + id));
        movie.setScore(score);
        movieRepository.save(movie);
        return movieRepository.findByIdWithActorsAndGenre(id)
                .map(movieMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + id));
    }

    @Transactional
    public void delete(Integer id) {
        if (!movieRepository.existsById(id)) {
            throw new ResourceNotFoundException("Movie not found with id: " + id);
        }
        movieRepository.deleteById(id);
    }

    private static void applyRequest(MovieEntity movie, MovieRequest request, GenreEntity genre) {
        movie.setTitle(request.getTitle().strip());
        movie.setReleaseDate(request.getReleaseDate());
        movie.setDuration(request.getDuration());
        movie.setScore(request.getScore());
        movie.setGenre(genre);
    }

    private Set<ActorEntity> resolveActors(List<Integer> actorIds) {
        if (actorIds == null || actorIds.isEmpty()) {
            return new HashSet<>();
        }
        Set<ActorEntity> actors = new HashSet<>();
        for (Integer aid : actorIds) {
            ActorEntity a = actorRepository.findById(aid)
                    .orElseThrow(() -> new BadRequestException("Actor not found with id: " + aid));
            actors.add(a);
        }
        return actors;
    }
}
