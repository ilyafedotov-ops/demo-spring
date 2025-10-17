package com.example.taskify.config.properties;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.taskify.config.TaskifyPropertiesConfiguration;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class TaskifyDataSourcePropertiesTest {

  private final ApplicationContextRunner contextRunner =
      new ApplicationContextRunner()
          .withUserConfiguration(TaskifyPropertiesConfiguration.class)
          .withPropertyValues(
              "taskify.datasource.schema=test_schema",
              "taskify.datasource.pool.max-size=20",
              "taskify.datasource.pool.min-idle=5",
              "taskify.datasource.pool.max-lifetime=PT10M");

  @Test
  void bindsPropertiesWithCustomValues() {
    contextRunner.run(
        context -> {
          TaskifyDataSourceProperties properties =
              context.getBean(TaskifyDataSourceProperties.class);

          assertThat(properties.schema()).isEqualTo("test_schema");
          assertThat(properties.pool().maxSize()).isEqualTo(20);
          assertThat(properties.pool().minIdle()).isEqualTo(5);
          assertThat(properties.pool().maxLifetime()).isEqualTo(Duration.ofMinutes(10));
        });
  }

  @Test
  void appliesDefaultsWhenPoolNotDeclared() {
    new ApplicationContextRunner()
        .withUserConfiguration(TaskifyPropertiesConfiguration.class)
        .withPropertyValues("taskify.datasource.schema=another_schema")
        .run(
            context -> {
              TaskifyDataSourceProperties properties =
                  context.getBean(TaskifyDataSourceProperties.class);

              assertThat(properties.schema()).isEqualTo("another_schema");
              assertThat(properties.pool().maxSize()).isEqualTo(10);
              assertThat(properties.pool().minIdle()).isEqualTo(2);
              assertThat(properties.pool().maxLifetime()).isEqualTo(Duration.ofMinutes(30));
            });
  }
}
