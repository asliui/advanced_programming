package ro.uaic.asli.lab7.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.uaic.asli.lab7.entity.GenreEntity;

public interface GenreRepository extends JpaRepository<GenreEntity, Long> {
}
