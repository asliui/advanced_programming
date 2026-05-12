package ro.uaic.asli.lab10.game;

import ro.uaic.asli.lab10.server.GameServer;

import java.nio.file.Path;

/**
 * Builds a {@link Lab10Session} once the owning {@link GameServer} exists (breaks the circular dependency).
 */
@FunctionalInterface
public interface Lab10SessionFactory {

    Lab10Session create(GameServer server, Path questionsOverride, Path knowledgeBaseOverride);
}
