package ro.uaic.asli.lab10.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * TCP quiz client entry point: starts {@link ClientInputThread} and {@link ClientListenerThread}.
 */
public final class GameClient {

    public static void main(String[] args) throws Exception {
        String host = System.getProperty("client.host", "localhost");
        int port = Integer.getInteger("client.port", 5555);

        Socket socket = new Socket(host, port);
        socket.setTcpNoDelay(true);

        BufferedReader socketIn = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        PrintWriter socketOut = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
        BufferedReader keyboard = new BufferedReader(
                new InputStreamReader(System.in, StandardCharsets.UTF_8));

        AtomicBoolean running = new AtomicBoolean(true);

        ClientListenerThread listener = new ClientListenerThread(socketIn, running);
        ClientInputThread input = new ClientInputThread(keyboard, socketOut, socket, running);

        listener.start();
        input.start();

        input.join();
        running.set(false);
        try {
            socket.shutdownOutput();
        } catch (Exception ignored) {
        }
        socket.close();
        listener.join(2000);
    }
}
