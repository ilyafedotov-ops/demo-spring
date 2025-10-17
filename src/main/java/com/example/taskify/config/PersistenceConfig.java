package com.example.taskify.config;

import com.example.taskify.config.audit.SecurityAuditorAware;
import com.example.taskify.config.properties.TaskifyDataSourceProperties;
import java.time.Clock;
import java.util.UUID;
import org.hibernate.cfg.AvailableSettings;
import org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy;
import org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider", modifyOnCreate = false)
public class PersistenceConfig {

  @Bean
  public AuditorAware<UUID> auditorProvider() {
    return new SecurityAuditorAware();
  }

  @Bean
  public HibernatePropertiesCustomizer taskifyHibernatePropertiesCustomizer(
      TaskifyDataSourceProperties properties) {
    return hibernateProperties -> {
      hibernateProperties.put(AvailableSettings.DEFAULT_SCHEMA, properties.schema());
      hibernateProperties.put(
          AvailableSettings.PHYSICAL_NAMING_STRATEGY, SpringPhysicalNamingStrategy.class.getName());
      hibernateProperties.put(
          AvailableSettings.IMPLICIT_NAMING_STRATEGY, SpringImplicitNamingStrategy.class.getName());
    };
  }

  @Bean
  public Clock systemClock() {
    return Clock.systemUTC();
  }
}
