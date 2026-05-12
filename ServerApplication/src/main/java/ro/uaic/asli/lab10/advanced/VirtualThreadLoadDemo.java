package ro.uaic.asli.lab10.advanced;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p><b>Optional advanced demo (+1 context).</b> Shows why <b>virtual threads</b> help when many clients connect
 * concurrently: each connection only blocks on I/O; mapping one lightweight task per socket avoids holding many
 * OS thread stacks.</p>
 *
 * <p><b>How to use (safe defaults):</b></p>
 * <ol>
 *   <li>Start the server (compulsory or homework/advanced) on the same port, e.g.
 *       {@code mvn -q compile exec:java@advanced} in {@code ServerApplication/}.</li>
 *   <li>From {@code ServerApplication/} run:
 *       {@code mvn -q compile exec:java@load-demo -Dload.port=5555 -Dload.clients=400}</li>
 * </ol>
 *
 * <p>Each virtual client opens a TCP socket, sends one line, reads one server line, then closes. No busy loops.
 * Cap is enforced so this stays a lab-scale demo, not a DDoS tool.</p>
 */
public final class VirtualThreadLoadDemo {

    private static final int MAX_CLIENTS = 5_000;

    private VirtualThreadLoadDemo() {
    }

    public static void main(String[] args) throws Exception {
        if (Runtime.version().feature() < 21) {
            System.err.println("This demo uses virtual threads and requires Java 21+.");
            return;
        }

        int port = Integer.getInteger("load.port", 5555);
        int clients = Integer.getInteger("load.clients", 300);
        if (args.length >= 1) {
            port = Integer.parseInt(args[0]);
        }
        if (args.length >= 2) {
            clients = Integer.parseInt(args[1]);
        }
        clients = Math.max(1, Math.min(clients, MAX_CLIENTS));
        final int portFinal = port;
        final int clientsFinal = clients;

        System.out.println("VirtualThreadLoadDemo: connecting " + clientsFinal + " clients to 127.0.0.1:" + portFinal);
        System.out.println("Ensure Lab 10 server is already listening on that port.");

        AtomicInteger ok = new AtomicInteger();
        AtomicInteger fail = new AtomicInteger();

        long t0 = System.nanoTime();
        try (ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < clientsFinal; i++) {
                int id = i;
                pool.submit(() -> oneShotClient(portFinal, id, ok, fail));
            }
            pool.shutdown();
            if (!pool.awaitTermination(5, TimeUnit.MINUTES)) {
                System.err.println("Timed out waiting for clients (partial results below).");
                pool.shutdownNow();
            }
        }
        long ms = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - t0);

        System.out.println("Finished in " + ms + " ms. success=" + ok.get() + " fail=" + fail.get());
        System.out.println("Compare with a bounded platform thread pool when many sockets block on read/write.");
    }

    private static void oneShotClient(int port, int id, AtomicInteger ok, AtomicInteger fail) {
        try (Socket s = new Socket("127.0.0.1", port)) {
            s.setTcpNoDelay(true);
            PrintWriter out = new PrintWriter(s.getOutputStream(), true, StandardCharsets.UTF_8);
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
            out.println("loadtest-" + id);
            String line = in.readLine();
            if (line != null) {
                ok.incrementAndGet();
            } else {
                fail.incrementAndGet();
            }
        } catch (Exception e) {
            fail.incrementAndGet();
        }
    }
}
