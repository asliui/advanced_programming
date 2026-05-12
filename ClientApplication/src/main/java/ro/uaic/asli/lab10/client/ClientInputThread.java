package ro.uaic.asli.lab10.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Reads keyboard lines and sends them to the server (non-blocking UX: separate from incoming messages).
 */
public final class ClientInputThread extends Thread {

    private final BufferedReader keyboard;
    private final PrintWriter socketOut;
    private final Socket socket;
    private final AtomicBoolean running;

    public ClientInputThread(BufferedReader keyboard, PrintWriter socketOut, Socket socket, AtomicBoolean running) {
        super("quiz-client-input");
        this.keyboard = keyboard;
        this.socketOut = socketOut;
        this.socket = socket;
        this.running = running;
    }

    @Override
    public void run() {
        try {
            String line;
            while (running.get()) {
                System.out.print("quiz> ");
                System.out.flush();
                line = keyboard.readLine();
                if (line == null) {
                    break;
                }
                String trimmed = line.trim();
                if (trimmed.equalsIgnoreCase("exit")) {
                    break;
                }
                socketOut.println(trimmed);
                if (socket.isClosed()) {
                    break;
                }
            }
        } catch (IOException e) {
            if (running.get()) {
                System.err.println("Input error: " + e.getMessage());
            }
        } finally {
            running.set(false);
        }
    }
}
