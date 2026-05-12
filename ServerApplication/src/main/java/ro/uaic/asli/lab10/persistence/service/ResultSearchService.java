package ro.uaic.asli.lab10.persistence.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.uaic.asli.lab10.persistence.entity.ResultEntity;
import ro.uaic.asli.lab10.persistence.repository.ResultRepository;
import ro.uaic.asli.lab10.persistence.search.ResultSearchCriteria;
import ro.uaic.asli.lab10.persistence.search.ResultSpecification;

import java.util.List;

@Service
public class ResultSearchService {

    private static final Logger LOG = LoggerFactory.getLogger(ResultSearchService.class);

    private final ResultRepository resultRepository;

    public ResultSearchService(ResultRepository resultRepository) {
        this.resultRepository = resultRepository;
    }

    @Transactional(readOnly = true)
    public List<ResultEntity> searchResults(ResultSearchCriteria criteria) {
        long startNs = System.nanoTime();
        try {
            return resultRepository.findAll(ResultSpecification.from(criteria));
        } catch (RuntimeException ex) {
            LOG.error("searchResults failed criteria={}", criteria, ex);
            throw ex;
        } finally {
            long ms = (System.nanoTime() - startNs) / 1_000_000L;
            LOG.info("service timing | searchResults | {} ms", ms);
        }
    }
}
