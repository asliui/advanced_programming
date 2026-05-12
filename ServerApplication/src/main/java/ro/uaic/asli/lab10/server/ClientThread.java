package ro.uaic.asli.lab10.server;

import java.io.IOException;

/**
 * One runnable task per connected client. Delegates lines to the active {@link ro.uaic.asli.lab10.game.Lab10Session}.
 */
public final class ClientThread implements Runnable {

    private final GameServer server;
    private final ClientConnection connection;

    public ClientThread(GameServer server, ClientConnection connection) {
        this.server = server;
        this.connection = connection;
    }

    @Override
    public void run() {
        try {
            String line;
            while (!server.isShutdownInitiated()
                    && (line = connection.getIn().readLine()) != null) {
                server.getSession().handleIncomingLine(connection, line);
            }
        } catch (IOException e) {
            if (!server.isShutdownInitiated()) {
                System.err.println("Client disconnected: " + e.getMessage());
            }
        } finally {
            server.unregisterClient(connection);
            server.getSession().onClientDisconnected(connection);
            connection.closeQuietly();
        }
    }
}
