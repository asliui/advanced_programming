package ro.uaic.asli.lab7.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ro.uaic.asli.lab7.entity.ActorEntity;
import ro.uaic.asli.lab7.entity.GenreEntity;
import ro.uaic.asli.lab7.entity.MovieEntity;
import ro.uaic.asli.lab7.repository.ActorRepository;
import ro.uaic.asli.lab7.repository.GenreRepository;
import ro.uaic.asli.lab7.repository.MovieRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Seeds H2 with sample genres, actors, and movies for demos (unrelated-movies solver, Postman).
 */
@Component
public class Lab7DataInitializer implements ApplicationRunner {

    private final GenreRepository genreRepository;
    private final ActorRepository actorRepository;
    private final MovieRepository movieRepository;

    public Lab7DataInitializer(
            GenreRepository genreRepository,
            ActorRepository actorRepository,
            MovieRepository movieRepository
    ) {
        this.genreRepository = genreRepository;
        this.actorRepository = actorRepository;
        this.movieRepository = movieRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (genreRepository.count() > 0) {
            return;
        }

        GenreEntity drama = saveGenre("Drama");
        GenreEntity scifi = saveGenre("Sci-Fi");
        GenreEntity action = saveGenre("Action");

        ActorEntity a1 = saveActor("Alice Alpha");
        ActorEntity a2 = saveActor("Bob Beta");
        ActorEntity a3 = saveActor("Carol Gamma");
        ActorEntity a4 = saveActor("Dan Delta");
        ActorEntity a5 = saveActor("Eve Epsilon");

        // M1 and M2 share Bob Beta -> cannot both be in unrelated set
        MovieEntity m1 = movie("Inception Day", LocalDate.of(2020, 1, 1), 120, new BigDecimal("8.5"), drama, Set.of(a1, a2));
        MovieEntity m2 = movie("Dark Matter", LocalDate.of(2021, 2, 2), 95, new BigDecimal("7.2"), scifi, Set.of(a2, a3));
        MovieEntity m3 = movie("Quiet Road", LocalDate.of(2019, 3, 3), 110, new BigDecimal("6.9"), drama, Set.of(a4));
        MovieEntity m4 = movie("Fast Lane", LocalDate.of(2022, 4, 4), 88, new BigDecimal("7.8"), action, Set.of(a5));

        movieRepository.save(m1);
        movieRepository.save(m2);
        movieRepository.save(m3);
        movieRepository.save(m4);
    }

    private GenreEntity saveGenre(String name) {
        GenreEntity g = new GenreEntity();
        g.setName(name);
        return genreRepository.save(g);
    }

    private ActorEntity saveActor(String name) {
        ActorEntity a = new ActorEntity();
        a.setName(name);
        return actorRepository.save(a);
    }

    private static MovieEntity movie(
            String title,
            LocalDate release,
            int duration,
            BigDecimal score,
            GenreEntity genre,
            Set<ActorEntity> actors
    ) {
        MovieEntity m = new MovieEntity();
        m.setTitle(title);
        m.setReleaseDate(release);
        m.setDuration(duration);
        m.setScore(score);
        m.setGenre(genre);
        m.setActors(new HashSet<>(actors));
        return m;
    }
}
