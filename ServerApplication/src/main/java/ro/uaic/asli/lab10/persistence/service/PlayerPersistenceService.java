package ro.uaic.asli.lab10.persistence.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.uaic.asli.lab10.persistence.entity.PlayerEntity;
import ro.uaic.asli.lab10.persistence.repository.PlayerRepository;

@Service
public class PlayerPersistenceService {

    private static final Logger LOG = LoggerFactory.getLogger(PlayerPersistenceService.class);

    private final PlayerRepository playerRepository;

    public PlayerPersistenceService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @Transactional
    public PlayerEntity ensurePlayer(String exactName) {
        long start = System.nanoTime();
        try {
            return playerRepository.findByName(exactName).orElseGet(() -> playerRepository.save(new PlayerEntity(exactName)));
        } catch (RuntimeException ex) {
            LOG.error("ensurePlayer failed for name={}", exactName, ex);
            throw ex;
        } finally {
            long ms = (System.nanoTime() - start) / 1_000_000L;
            LOG.info("service timing | ensurePlayer | {} ms | name={}", ms, exactName);
        }
    }
}
