package ro.uaic.asli.lab10.persistence.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.uaic.asli.lab10.model.Player;
import ro.uaic.asli.lab10.model.Question;
import ro.uaic.asli.lab10.persistence.entity.GameEntity;
import ro.uaic.asli.lab10.persistence.entity.QuestionEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Facade used by {@link ro.uaic.asli.lab10.game.HomeworkGameSession} so gameplay code does not touch repositories.
 */
@Service
public class QuizGamePersistenceService {

    private final PlayerPersistenceService playerPersistenceService;
    private final QuestionPersistenceService questionPersistenceService;
    private final GamePersistenceService gamePersistenceService;
    private final ResultPersistenceService resultPersistenceService;
    private final CachePerformanceProbe cachePerformanceProbe;

    public QuizGamePersistenceService(
            PlayerPersistenceService playerPersistenceService,
            QuestionPersistenceService questionPersistenceService,
            GamePersistenceService gamePersistenceService,
            ResultPersistenceService resultPersistenceService,
            CachePerformanceProbe cachePerformanceProbe
    ) {
        this.playerPersistenceService = playerPersistenceService;
        this.questionPersistenceService = questionPersistenceService;
        this.gamePersistenceService = gamePersistenceService;
        this.resultPersistenceService = resultPersistenceService;
        this.cachePerformanceProbe = cachePerformanceProbe;
    }

    @Transactional
    public void registerParticipant(String playerName) {
        playerPersistenceService.ensurePlayer(playerName);
    }

    @Transactional
    public MatchHandle beginMatch(List<Question> orderedQuestions) {
        List<QuestionEntity> persisted = new ArrayList<>();
        for (Question q : orderedQuestions) {
            persisted.add(questionPersistenceService.upsertFromQuizQuestion(q));
        }
        String code = "G-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase(Locale.ROOT);
        GameEntity game = gamePersistenceService.createInProgressGame(code, persisted);
        return new MatchHandle(game.getId(), code);
    }

    @Transactional
    public void completeMatch(MatchHandle handle, List<Player> players, int totalRounds) {
        resultPersistenceService.replaceFinalResultsForGame(handle.gameId(), players, totalRounds);
        if (Boolean.getBoolean("quiz.cacheBench")) {
            cachePerformanceProbe.logQuestionEntityReadColdVsWarm(1);
        }
    }

    public record MatchHandle(long gameId, String gameCode) {
    }
}
