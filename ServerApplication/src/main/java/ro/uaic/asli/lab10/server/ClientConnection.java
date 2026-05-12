package ro.uaic.asli.lab10.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Wraps one client {@link Socket}: character streams + optional player name after {@code join}.
 */
public final class ClientConnection {

    private final Socket socket;
    private final BufferedReader in;
    private final PrintWriter out;

    private volatile String playerName;

    public ClientConnection(Socket socket) throws IOException {
        this.socket = socket;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        this.out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
    }

    public Socket getSocket() {
        return socket;
    }

    public BufferedReader getIn() {
        return in;
    }

    public PrintWriter getOut() {
        return out;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public void sendLine(String line) {
        out.println(line);
    }

    public void closeQuietly() {
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }
}
