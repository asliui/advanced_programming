package ro.uaic.asli.lab10.game;

import ro.uaic.asli.lab10.command.CommandParser;
import ro.uaic.asli.lab10.server.ClientConnection;
import ro.uaic.asli.lab10.server.GameServer;

import java.nio.file.Path;

/**
 * Minimal Lab 10 compulsory tier: TCP echo of unknown commands and graceful {@code stop}.
 */
public final class CompulsoryGameSession implements Lab10Session {

    private final GameServer server;
    private volatile boolean serverShutdown;

    public CompulsoryGameSession(GameServer server, Path ignoredQuestions, Path ignoredKnowledgeBase) {
        this.server = server;
    }

    @Override
    public synchronized void onServerShutdown() {
        serverShutdown = true;
        notifyAll();
    }

    @Override
    public void onClientDisconnected(ClientConnection connection) {
        // No player registry in compulsory mode.
    }

    @Override
    public void broadcastLine(String message) {
        for (String line : message.split("\n")) {
            if (line.isEmpty()) {
                continue;
            }
            for (ClientConnection c : server.getClientsSnapshot()) {
                c.sendLine(line);
            }
        }
    }

    @Override
    public synchronized void handleIncomingLine(ClientConnection connection, String rawLine) {
        if (serverShutdown) {
            return;
        }
        String line = rawLine == null ? "" : rawLine.trim();
        if (line.isEmpty()) {
            return;
        }
        String verb = CommandParser.verb(line);
        if ("stop".equals(verb)) {
            connection.sendLine("Server stopped");
            server.initiateShutdownFromCommand();
            return;
        }
        connection.sendLine("Server received the request: " + line);
    }
}
