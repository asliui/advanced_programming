package ro.uaic.asli.lab10.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.uaic.asli.lab10.persistence.entity.QuestionEntity;

import java.util.Optional;

public interface QuestionRepository extends JpaRepository<QuestionEntity, Long> {

    Optional<QuestionEntity> findBySourceQuestionId(int sourceQuestionId);
}
