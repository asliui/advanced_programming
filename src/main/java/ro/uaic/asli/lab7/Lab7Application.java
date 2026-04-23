package ro.uaic.asli.lab7;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Default entry: full Lab 7 (Advanced profile). For tiered runs use
 * {@link Lab7CompulsoryApp}, {@link Lab7HomeworkApp}, or {@link Lab7AdvancedApp}.
 */
@SpringBootApplication(scanBasePackages = "ro.uaic.asli.lab7")
public class Lab7Application {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(Lab7Application.class);
        app.setAdditionalProfiles("lab7-advanced");
        app.run(args);
    }
}
