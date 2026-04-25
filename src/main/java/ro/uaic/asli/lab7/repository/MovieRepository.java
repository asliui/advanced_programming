package ro.uaic.asli.lab7.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ro.uaic.asli.lab7.entity.MovieEntity;

import java.util.List;
import java.util.Optional;

public interface MovieRepository extends JpaRepository<MovieEntity, Integer> {

    @Query("SELECT DISTINCT m FROM MovieEntity m LEFT JOIN FETCH m.actors JOIN FETCH m.genre")
    List<MovieEntity> findAllWithActorsAndGenre();

    @Query("SELECT DISTINCT m FROM MovieEntity m LEFT JOIN FETCH m.actors JOIN FETCH m.genre WHERE m.id = :id")
    Optional<MovieEntity> findByIdWithActorsAndGenre(@Param("id") Integer id);

    @Query("SELECT DISTINCT m FROM MovieEntity m JOIN m.actors a WHERE a.id = :actorId")
    List<MovieEntity> findMoviesByActorId(@Param("actorId") Integer actorId);
}
