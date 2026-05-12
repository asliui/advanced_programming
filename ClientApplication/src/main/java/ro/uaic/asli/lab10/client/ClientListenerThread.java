package ro.uaic.asli.lab10.client;

import ro.uaic.asli.lab10.client.ui.MessageFormatter;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Continuously reads server lines and prints them (real-time feedback while typing is handled elsewhere).
 */
public final class ClientListenerThread extends Thread {

    private final BufferedReader socketIn;
    private final AtomicBoolean running;
    private final MessageFormatter formatter = new MessageFormatter();

    public ClientListenerThread(BufferedReader socketIn, AtomicBoolean running) {
        super("quiz-client-listener");
        this.socketIn = socketIn;
        this.running = running;
    }

    @Override
    public void run() {
        try {
            String line;
            while (running.get() && (line = socketIn.readLine()) != null) {
                System.out.println(formatter.format(line));
                if ("Server stopped".equals(line)) {
                    running.set(false);
                    break;
                }
            }
        } catch (IOException e) {
            if (running.get()) {
                System.err.println("Disconnected: " + e.getMessage());
            }
        } finally {
            running.set(false);
        }
    }
}
