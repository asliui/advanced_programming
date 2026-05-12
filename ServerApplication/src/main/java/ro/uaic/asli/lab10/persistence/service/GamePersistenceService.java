package ro.uaic.asli.lab10.persistence.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.uaic.asli.lab10.persistence.entity.GameEntity;
import ro.uaic.asli.lab10.persistence.entity.GameStatus;
import ro.uaic.asli.lab10.persistence.entity.QuestionEntity;
import ro.uaic.asli.lab10.persistence.repository.GameRepository;

import java.time.Instant;
import java.util.Collection;

@Service
public class GamePersistenceService {

    private final GameRepository gameRepository;

    public GamePersistenceService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    @Transactional
    public GameEntity createInProgressGame(String gameCode, Collection<QuestionEntity> orderedQuestions) {
        GameEntity game = new GameEntity(gameCode, GameStatus.IN_PROGRESS);
        game.setStartedAt(Instant.now());
        for (QuestionEntity q : orderedQuestions) {
            game.getQuestions().add(q);
        }
        return gameRepository.save(game);
    }
}
