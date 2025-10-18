package com.example.taskify.infrastructure.persistence.jpa.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.taskify.application.port.out.UserDirectory;
import com.example.taskify.domain.user.UserRole;
import com.example.taskify.domain.user.UserStatus;
import com.example.taskify.infrastructure.persistence.jpa.entity.UserJpaEntity;
import com.example.taskify.infrastructure.persistence.jpa.repository.UserJpaRepository;
import com.example.taskify.support.PostgresIntegrationTest;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
  DatabaseUserDirectory.class,
  com.example.taskify.config.TaskifyPropertiesConfiguration.class,
  com.example.taskify.config.PersistenceConfig.class
})
class DatabaseUserDirectoryIntegrationTest extends PostgresIntegrationTest {

  @Autowired private UserJpaRepository userJpaRepository;
  @Autowired private UserDirectory userDirectory;

  @Test
  void findsUserSummary() {
    Instant now = Instant.now();
    UUID userId = UUID.randomUUID();

    UserJpaEntity entity = new UserJpaEntity();
    entity.setId(userId);
    entity.setUsername("member");
    entity.setEmail("member@example.com");
    entity.setPasswordHash("hashed");
    entity.setDisplayName("Member");
    entity.setRole(UserRole.TEAM_MEMBER);
    entity.setStatus(UserStatus.ACTIVE);
    entity.setLastLoginAt(now);
    entity.setCreatedAt(now);
    entity.setCreatedBy(userId);
    entity.setUpdatedAt(now);
    entity.setUpdatedBy(userId);
    userJpaRepository.save(entity);

    assertThat(userDirectory.existsById(userId)).isTrue();
    assertThat(userDirectory.findSummary(userId))
        .isPresent()
        .get()
        .extracting(UserDirectory.UserSummary::username)
        .isEqualTo("member");
  }
}
