package ro.uaic.asli.lab10.game;

import ro.uaic.asli.lab10.server.ClientConnection;

/**
 * Per-connection command handling for Lab 10 (compulsory echo server vs full homework quiz).
 */
public interface Lab10Session {

    void handleIncomingLine(ClientConnection connection, String rawLine);

    void onClientDisconnected(ClientConnection connection);

    void onServerShutdown();

    void broadcastLine(String message);
}
