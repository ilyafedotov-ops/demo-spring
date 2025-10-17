package com.example.taskify.config.properties;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.Assert;

@ConfigurationProperties(prefix = "taskify.datasource")
public record TaskifyDataSourceProperties(String schema, Pool pool) {

  public TaskifyDataSourceProperties(String schema, Pool pool) {
    Assert.hasText(schema, "taskify.datasource.schema must not be blank");
    this.schema = schema;
    this.pool = pool == null ? new Pool(10, 2, Duration.ofMinutes(30)) : pool;
  }

  public record Pool(int maxSize, int minIdle, Duration maxLifetime) {
    public Pool {
      Assert.isTrue(maxSize > 0, "taskify.datasource.pool.max-size must be greater than zero");
      Assert.isTrue(
          minIdle >= 0, "taskify.datasource.pool.min-idle must be greater than or equal to zero");
      Assert.notNull(maxLifetime, "taskify.datasource.pool.max-lifetime must not be null");
    }
  }
}
