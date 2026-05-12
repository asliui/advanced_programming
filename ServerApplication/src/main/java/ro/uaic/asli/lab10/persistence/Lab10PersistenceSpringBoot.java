package ro.uaic.asli.lab10.persistence;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Non-web Spring Boot entry used to bootstrap JPA (Lab 11) for the TCP server.
 */
@SpringBootApplication(scanBasePackages = "ro.uaic.asli.lab10.persistence")
@EnableJpaAuditing(auditorAwareRef = "lab11AuditorAware")
public class Lab10PersistenceSpringBoot {
}
