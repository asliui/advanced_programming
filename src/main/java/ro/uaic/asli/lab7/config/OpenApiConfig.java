package ro.uaic.asli.lab7.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"lab7-homework", "lab7-advanced"})
public class OpenApiConfig {

    @Bean
    public OpenAPI lab7OpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Lab 7 Movie API")
                        .description("REST services for movies. Homework: CRUD, Swagger. Advanced: Choco unrelated query + JWT.")
                        .version("1.0"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}
