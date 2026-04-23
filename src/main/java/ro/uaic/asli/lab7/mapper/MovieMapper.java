package ro.uaic.asli.lab7.mapper;

import org.springframework.stereotype.Component;
import ro.uaic.asli.lab7.dto.MovieResponse;
import ro.uaic.asli.lab7.entity.ActorEntity;
import ro.uaic.asli.lab7.entity.MovieEntity;

import java.util.Comparator;
import java.util.List;

@Component
public class MovieMapper {

    public MovieResponse toResponse(MovieEntity entity) {
        if (entity == null) {
            return null;
        }
        MovieResponse r = new MovieResponse();
        r.setId(entity.getId());
        r.setTitle(entity.getTitle());
        r.setReleaseDate(entity.getReleaseDate());
        r.setDuration(entity.getDuration());
        r.setScore(entity.getScore());
        if (entity.getGenre() != null) {
            r.setGenreId(entity.getGenre().getId());
            r.setGenreName(entity.getGenre().getName());
        }
        List<Long> actorIds = entity.getActors().stream()
                .map(ActorEntity::getId)
                .sorted()
                .toList();
        List<String> actorNames = entity.getActors().stream()
                .sorted(Comparator.comparing(ActorEntity::getName))
                .map(ActorEntity::getName)
                .toList();
        r.setActorIds(actorIds);
        r.setActorNames(actorNames);
        return r;
    }
}
