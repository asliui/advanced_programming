package ro.uaic.asli.lab10.server;

import ro.uaic.asli.lab10.game.HomeworkGameSession;
import ro.uaic.asli.lab10.game.Lab10Session;
import ro.uaic.asli.lab10.game.Lab10SessionFactory;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Accepts TCP clients and dispatches each connection to the {@link ClientThread} worker.
 * Session behaviour (compulsory echo vs homework quiz vs advanced networking) comes from {@link Lab10SessionFactory}.
 */
public final class GameServer {

    private final int port;
    private final Path questionsOverride;
    private final Path knowledgeBaseOverride;

    private final AtomicBoolean accepting = new AtomicBoolean(true);
    private final AtomicBoolean shutdownInitiated = new AtomicBoolean(false);

    private final ExecutorService networkExecutor;
    private final ExecutorService gameDirector;
    private final AtomicInteger threadCounter = new AtomicInteger(1);

    private final List<ClientConnection> clients = new CopyOnWriteArrayList<>();
    private final Lab10Session session;

    private volatile ServerSocket serverSocket;

    /** Homework defaults: bounded thread pool + full {@link HomeworkGameSession}. */
    public GameServer(int port, Path questionsOverride, Path knowledgeBaseOverride) {
        this(port, questionsOverride, knowledgeBaseOverride, null, HomeworkGameSession::new);
    }

    public GameServer(int port, Path questionsOverride, Path knowledgeBaseOverride, ExecutorService networkExecutorOverride) {
        this(port, questionsOverride, knowledgeBaseOverride, networkExecutorOverride, HomeworkGameSession::new);
    }

    public GameServer(
            int port,
            Path questionsOverride,
            Path knowledgeBaseOverride,
            ExecutorService networkExecutorOverride,
            Lab10SessionFactory sessionFactory) {
        this.port = port;
        this.questionsOverride = questionsOverride;
        this.knowledgeBaseOverride = knowledgeBaseOverride;

        int cores = Math.max(2, Runtime.getRuntime().availableProcessors());
        if (networkExecutorOverride != null) {
            this.networkExecutor = networkExecutorOverride;
        } else {
            RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();
            this.networkExecutor = new ThreadPoolExecutor(
                    cores,
                    cores * 4,
                    60L,
                    TimeUnit.SECONDS,
                    new LinkedBlockingQueue<>(512),
                    newThreadFactory(),
                    handler
            );
        }
        this.gameDirector = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "quiz-game-director");
            t.setDaemon(true);
            return t;
        });

        this.session = sessionFactory.create(this, questionsOverride, knowledgeBaseOverride);
    }

    private ThreadFactory newThreadFactory() {
        return runnable -> {
            Thread t = new Thread(runnable, "quiz-worker-" + threadCounter.getAndIncrement());
            t.setDaemon(true);
            return t;
        };
    }

    public Lab10Session getSession() {
        return session;
    }

    public ExecutorService getNetworkExecutor() {
        return networkExecutor;
    }

    public ExecutorService getGameDirector() {
        return gameDirector;
    }

    public void registerClient(ClientConnection connection) {
        clients.add(connection);
    }

    public void unregisterClient(ClientConnection connection) {
        clients.remove(connection);
    }

    public List<ClientConnection> getClientsSnapshot() {
        return List.copyOf(clients);
    }

    public boolean isAcceptingConnections() {
        return accepting.get() && !shutdownInitiated.get();
    }

    public boolean isShutdownInitiated() {
        return shutdownInitiated.get();
    }

    public void start() throws IOException {
        try {
            serverSocket = new ServerSocket(port);
        } catch (BindException e) {
            System.err.println("Lab 10: cannot bind to port " + port + " (" + e.getMessage() + ").");
            System.err.println("Usually another Java server is still running on this port (e.g. an older Lab10CompulsoryApp).");
            System.err.println("Stop it with Ctrl+C in that terminal, or: netstat -ano | findstr :" + port);
            System.err.println("Or use another port: mvn ... -Dquiz.port=5556  (and match the client: -Dclient.port=5556)");
            throw e;
        }
        System.out.println("Lab 10 server listening on port " + port);
        System.out.println("Connect with: ClientApplication module -> mvn exec:java@client (class ro.uaic.asli.lab10.client.GameClient).");
        while (!shutdownInitiated.get()) {
            Socket socket;
            try {
                socket = serverSocket.accept();
            } catch (IOException e) {
                if (shutdownInitiated.get()) {
                    break;
                }
                throw e;
            }
            if (!isAcceptingConnections()) {
                try {
                    socket.close();
                } catch (IOException ignored) {
                }
                continue;
            }
            ClientConnection connection = new ClientConnection(socket);
            registerClient(connection);
            networkExecutor.execute(new ClientThread(this, connection));
        }
        shutdownGracefully();
    }

    public void stopAcceptingNewConnections() {
        accepting.set(false);
    }

    public void initiateShutdownFromCommand() {
        if (!shutdownInitiated.compareAndSet(false, true)) {
            return;
        }
        stopAcceptingNewConnections();
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException ignored) {
        }
        session.onServerShutdown();
    }

    public void shutdownGracefully() {
        initiateShutdownFromCommand();
        session.broadcastLine("SERVER|Shutting down. Goodbye.");
        for (ClientConnection c : clients) {
            c.closeQuietly();
        }
        clients.clear();
        networkExecutor.shutdown();
        gameDirector.shutdownNow();
        try {
            if (!networkExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                networkExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            networkExecutor.shutdownNow();
        }
    }
}
