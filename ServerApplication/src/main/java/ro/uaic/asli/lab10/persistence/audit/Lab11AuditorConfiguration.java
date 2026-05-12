package ro.uaic.asli.lab10.persistence.audit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

@Configuration
public class Lab11AuditorConfiguration {

    @Bean
    public AuditorAware<String> lab11AuditorAware() {
        return () -> Optional.ofNullable(Lab11AuditContextHolder.currentPrincipalOrNull())
                .filter(s -> !s.isBlank())
                .or(() -> Optional.of("system"));
    }
}
