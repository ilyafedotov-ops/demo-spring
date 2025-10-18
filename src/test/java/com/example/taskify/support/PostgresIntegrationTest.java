package com.example.taskify.support;

import java.util.Map;
import org.flywaydb.core.Flyway;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public abstract class PostgresIntegrationTest {

  @Container
  private static final PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>("postgres:16.4")
          .withDatabaseName("taskify")
          .withUsername("taskify")
          .withPassword("taskify");

  @DynamicPropertySource
  static void overrideProperties(DynamicPropertyRegistry registry) {
    ensureDatabaseReady();
    registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES::getUsername);
    registry.add("spring.datasource.password", POSTGRES::getPassword);
    registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    registry.add("taskify.datasource.schema", () -> "public");
    registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
    registry.add("spring.flyway.placeholders.schema", () -> "public");
    registry.add("spring.flyway.default-schema", () -> "public");
  }

  private static synchronized void ensureDatabaseReady() {
    if (!POSTGRES.isRunning()) {
      POSTGRES.start();
    }
    Flyway.configure()
        .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
        .schemas("public")
        .placeholders(Map.of("schema", "public"))
        .load()
        .migrate();
  }
}
