package ro.uaic.asli.lab10.persistence.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.uaic.asli.lab10.model.Question;
import ro.uaic.asli.lab10.persistence.entity.QuestionEntity;
import ro.uaic.asli.lab10.persistence.repository.QuestionRepository;

import java.util.List;

@Service
public class QuestionPersistenceService {

    private final QuestionRepository questionRepository;

    public QuestionPersistenceService(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    @Transactional
    public QuestionEntity upsertFromQuizQuestion(Question question) {
        return questionRepository.findBySourceQuestionId(question.getId()).orElseGet(() -> {
            List<String> opts = question.getOptions();
            QuestionEntity entity = new QuestionEntity(
                    question.getId(),
                    question.getText(),
                    opts.get(0),
                    opts.get(1),
                    opts.get(2),
                    opts.get(3),
                    String.valueOf(Character.toUpperCase(question.getCorrectOption()))
            );
            return questionRepository.save(entity);
        });
    }
}
