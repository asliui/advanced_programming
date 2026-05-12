package ro.uaic.asli.lab10.persistence.service;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.uaic.asli.lab10.model.Player;
import ro.uaic.asli.lab10.persistence.entity.GameEntity;
import ro.uaic.asli.lab10.persistence.entity.GameStatus;
import ro.uaic.asli.lab10.persistence.entity.PlayerEntity;
import ro.uaic.asli.lab10.persistence.entity.ResultEntity;
import ro.uaic.asli.lab10.persistence.repository.GameRepository;
import ro.uaic.asli.lab10.persistence.repository.PlayerRepository;

import java.time.Instant;
import java.util.List;

@Service
public class ResultPersistenceService {

    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;
    private final EntityManager entityManager;

    public ResultPersistenceService(
            GameRepository gameRepository,
            PlayerRepository playerRepository,
            EntityManager entityManager
    ) {
        this.gameRepository = gameRepository;
        this.playerRepository = playerRepository;
        this.entityManager = entityManager;
    }

    @Transactional
    public void replaceFinalResultsForGame(long gameId, List<Player> playersInMatch, int totalRounds) {
        GameEntity game = gameRepository.findById(gameId).orElseThrow(() -> new IllegalStateException("Missing game id=" + gameId));
        Instant finishedAt = Instant.now();

        for (Player p : playersInMatch) {
            PlayerEntity player = playerRepository.findByName(p.getName())
                    .orElseThrow(() -> new IllegalStateException("Missing player row for name=" + p.getName()));

            int correct = p.getScore();
            int wrong = Math.max(0, totalRounds - correct);
            int scorePoints = correct;

            ResultEntity result = new ResultEntity(scorePoints, correct, wrong, finishedAt, player, game);
            game.getResults().add(result);
        }

        gameRepository.save(game);
        entityManager.flush();

        gameRepository.updateStatusAndEndedAtById(gameId, GameStatus.FINISHED, finishedAt);
    }
}
