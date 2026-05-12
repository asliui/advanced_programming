package ro.uaic.asli.lab10.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ro.uaic.asli.lab10.model.Player;
import ro.uaic.asli.lab10.model.Question;
import ro.uaic.asli.lab10.persistence.entity.GameStatus;
import ro.uaic.asli.lab10.persistence.entity.PlayerEntity;
import ro.uaic.asli.lab10.persistence.entity.QuestionEntity;
import ro.uaic.asli.lab10.persistence.entity.ResultEntity;
import ro.uaic.asli.lab10.persistence.repository.GameRepository;
import ro.uaic.asli.lab10.persistence.repository.PlayerRepository;
import ro.uaic.asli.lab10.persistence.repository.QuestionRepository;
import ro.uaic.asli.lab10.persistence.repository.ResultRepository;
import ro.uaic.asli.lab10.persistence.search.ResultSearchCriteria;
import ro.uaic.asli.lab10.persistence.service.GamePersistenceService;
import ro.uaic.asli.lab10.persistence.service.PlayerPersistenceService;
import ro.uaic.asli.lab10.persistence.service.QuestionPersistenceService;
import ro.uaic.asli.lab10.persistence.service.QuizGamePersistenceService;
import ro.uaic.asli.lab10.persistence.service.ResultPersistenceService;
import ro.uaic.asli.lab10.persistence.service.ResultSearchService;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = Lab10PersistenceSpringBoot.class)
@ActiveProfiles("test")
@Transactional
class Lab11PersistenceIT {

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private ResultRepository resultRepository;

    @Autowired
    private PlayerPersistenceService playerPersistenceService;

    @Autowired
    private QuestionPersistenceService questionPersistenceService;

    @Autowired
    private GamePersistenceService gamePersistenceService;

    @Autowired
    private ResultPersistenceService resultPersistenceService;

    @Autowired
    private ResultSearchService resultSearchService;

    @Autowired
    private QuizGamePersistenceService quizGamePersistenceService;

    @Test
    void playerRoundTrip() {
        PlayerEntity p = playerPersistenceService.ensurePlayer("Alice");
        assertThat(p.getId()).isNotNull();
        assertThat(playerRepository.findByName("Alice")).isPresent();
    }

    @Test
    void questionUpsertIdempotent() {
        Question q = new Question(1, "Q?", List.of("a", "b", "c", "d"), 'A');
        QuestionEntity first = questionPersistenceService.upsertFromQuizQuestion(q);
        QuestionEntity second = questionPersistenceService.upsertFromQuizQuestion(q);
        assertThat(second.getId()).isEqualTo(first.getId());
        assertThat(questionRepository.findBySourceQuestionId(1)).isPresent();
    }

    @Test
    void jpqlPrefixQueryWorks() {
        PlayerEntity alice = playerRepository.save(new PlayerEntity("AliceJPQL"));
        QuestionEntity q = questionRepository.save(new QuestionEntity(99, "t", "a", "b", "c", "d", "A"));
        var game = gamePersistenceService.createInProgressGame("G-JPQL-1", List.of(q));
        resultPersistenceService.replaceFinalResultsForGame(
                game.getId(),
                List.of(Player.human("AliceJPQL")),
                5
        );

        List<ResultEntity> rows = resultRepository.findByPlayerNamePrefixAndMinScore("Alice", 0);
        assertThat(rows).isNotEmpty();
        assertThat(rows.get(0).getPlayer().getName()).isEqualTo("AliceJPQL");
    }

    @Test
    void modifyingQueryUpdatesGameStatus() {
        QuestionEntity q = questionRepository.save(new QuestionEntity(42, "t", "a", "b", "c", "d", "B"));
        var game = gamePersistenceService.createInProgressGame("G-MOD-1", List.of(q));
        assertThat(game.getStatus()).isEqualTo(GameStatus.IN_PROGRESS);

        Instant end = Instant.now();
        int updated = gameRepository.updateStatusAndEndedAtById(game.getId(), GameStatus.FINISHED, end);
        assertThat(updated).isEqualTo(1);

        assertThat(gameRepository.findById(game.getId()))
                .hasValueSatisfying(g -> assertThat(g.getStatus()).isEqualTo(GameStatus.FINISHED));
    }

    @Test
    void dynamicSearchCombinesOptionalFilters() {
        PlayerEntity p = playerRepository.save(new PlayerEntity("DynPlayer"));
        QuestionEntity q = questionRepository.save(new QuestionEntity(7, "t", "a", "b", "c", "d", "C"));
        var game = gamePersistenceService.createInProgressGame("G-DYN-1", List.of(q));
        resultPersistenceService.replaceFinalResultsForGame(game.getId(), List.of(Player.human("DynPlayer")), 10);

        ResultSearchCriteria all = new ResultSearchCriteria();
        assertThat(resultSearchService.searchResults(all)).isNotEmpty();

        ResultSearchCriteria byCode = new ResultSearchCriteria();
        byCode.setGameCode("G-DYN-1");
        assertThat(resultSearchService.searchResults(byCode)).hasSize(1);

        ResultSearchCriteria byScore = new ResultSearchCriteria();
        byScore.setMinScore(1);
        byScore.setMaxScore(20);
        assertThat(resultSearchService.searchResults(byScore)).isNotEmpty();
    }

    @Test
    void quizFacadeCreatesGameAndResults() {
        quizGamePersistenceService.registerParticipant("P1");
        quizGamePersistenceService.registerParticipant("P2");

        List<Question> deck = List.of(
                new Question(1, "One", List.of("a", "b", "c", "d"), 'A'),
                new Question(2, "Two", List.of("a", "b", "c", "d"), 'B')
        );

        QuizGamePersistenceService.MatchHandle handle = quizGamePersistenceService.beginMatch(deck);
        assertThat(handle.gameId()).isNotNull();

        Player p1 = Player.human("P1");
        p1.addResult(true, 10);
        p1.addResult(false, 10);
        Player p2 = Player.human("P2");
        p2.addResult(true, 5);
        p2.addResult(true, 5);

        quizGamePersistenceService.completeMatch(handle, List.of(p1, p2), deck.size());

        assertThat(resultRepository.findAll()).isNotEmpty();
        assertThat(gameRepository.findById(handle.gameId())).hasValueSatisfying(g -> assertThat(g.getStatus()).isEqualTo(GameStatus.FINISHED));
    }
}
