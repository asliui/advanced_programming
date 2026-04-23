package ro.uaic.asli.lab7.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.core.env.Environment;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Lab 7: after the app is fully ready, opens useful URLs in the default browser.
 * Uses OS-specific commands (Windows {@code start}) because {@link Desktop#browse} often fails under IDE/headless JVMs.
 */
@Component
@Profile({"lab7-compulsory", "lab7-homework", "lab7-advanced"})
public class OpenMoviesApiInBrowserRunner {

    @Value("${server.port:8081}")
    private int serverPort;

    private final Environment environment;

    public OpenMoviesApiInBrowserRunner(Environment environment) {
        this.environment = environment;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        List<String> urls = buildUrlsToOpen();

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "lab7-open-movies-url");
            t.setDaemon(true);
            return t;
        });

        long initialDelayMs = 400;
        long betweenOpensMs = 250;
        for (int i = 0; i < urls.size(); i++) {
            String url = urls.get(i);
            long delayMs = initialDelayMs + (betweenOpensMs * i);
            scheduler.schedule(() -> openUrl(url), delayMs, TimeUnit.MILLISECONDS);
        }
        scheduler.schedule(scheduler::shutdown, initialDelayMs + (betweenOpensMs * urls.size()) + 500, TimeUnit.MILLISECONDS);
    }

    private List<String> buildUrlsToOpen() {
        String base = "http://localhost:" + serverPort;
        List<String> urls = new ArrayList<>();

        // Always useful
        urls.add(base + "/api/movies");

        // Homework+ features
        if (isActive("lab7-homework") || isActive("lab7-advanced")) {
            urls.add(base + "/api/actors");
            urls.add(base + "/swagger-ui.html");
        }

        return urls;
    }

    private boolean isActive(String profile) {
        return Arrays.asList(environment.getActiveProfiles()).contains(profile);
    }

    private static void openUrl(String url) {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        try {
            if (os.contains("win")) {
                new ProcessBuilder("cmd", "/c", "start", "", url).start();
                return;
            }
            if (os.contains("mac")) {
                new ProcessBuilder("open", url).start();
                return;
            }
            new ProcessBuilder("xdg-open", url).start();
        } catch (IOException ignored) {
            tryDesktopBrowse(url);
        }
    }

    private static void tryDesktopBrowse(String url) {
        try {
            if (!java.awt.GraphicsEnvironment.isHeadless()
                    && Desktop.isDesktopSupported()
                    && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            }
        } catch (java.net.URISyntaxException | IOException ignored) {
        }
    }
}
