package com.example.taskify.config.openapi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  private static final String ACTOR_HEADER_SCHEME = "ActorHeader";

  @Bean
  public OpenAPI taskifyOpenApi() {
    return new OpenAPI()
        .info(
            new Info()
                .title("Taskify API")
                .version("0.1.0")
                .description(
                    """
                    Taskify exposes REST endpoints for task management, including tasks, projects, tags, comments, users, and activity feeds.
                    All endpoints require the `X-Actor-Id` header which is modeled below as an API key security scheme.
                    """)
                .license(new License().name("MIT").url("https://opensource.org/licenses/MIT")))
        .components(
            new Components()
                .addSecuritySchemes(
                    ACTOR_HEADER_SCHEME,
                    new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .name("X-Actor-Id")
                        .description(
                            "UUID of the acting user. Required on every write/read endpoint until JWT integration is completed.")))
        .addSecurityItem(new SecurityRequirement().addList(ACTOR_HEADER_SCHEME));
  }
}
