package ro.uaic.asli.lab10.persistence.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ro.uaic.asli.lab10.persistence.repository.QuestionRepository;

/**
 * Prints and logs a simple cold vs warm read comparison for cached {@link ro.uaic.asli.lab10.persistence.entity.QuestionEntity} rows.
 */
@Service
public class CachePerformanceProbe {

    private static final Logger LOG = LoggerFactory.getLogger("lab11.jpql");

    private final QuestionRepository questionRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public CachePerformanceProbe(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    public void logQuestionEntityReadColdVsWarm(int sourceQuestionId) {
        long coldNs = readOnceNanos(sourceQuestionId);
        long warmNs = readOnceNanos(sourceQuestionId);
        String msg = "cacheBench sourceQuestionId=%d firstReadNs=%d secondReadNs=%d"
                .formatted(sourceQuestionId, coldNs, warmNs);
        System.out.println(msg);
        LOG.info(msg);
    }

    private long readOnceNanos(int sourceQuestionId) {
        entityManager.clear();
        long start = System.nanoTime();
        questionRepository.findBySourceQuestionId(sourceQuestionId).orElse(null);
        return System.nanoTime() - start;
    }
}
