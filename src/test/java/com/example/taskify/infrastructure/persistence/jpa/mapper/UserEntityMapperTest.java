package com.example.taskify.infrastructure.persistence.jpa.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.taskify.domain.user.UserRole;
import com.example.taskify.domain.user.UserStatus;
import com.example.taskify.infrastructure.persistence.jpa.entity.UserJpaEntity;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UserEntityMapperTest {

  private final UserEntityMapper mapper = new UserEntityMapper();

  @Test
  void toDomainCopiesAllRelevantFields() {
    UUID userId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    Instant createdAt = Instant.parse("2024-01-01T00:00:00Z");
    Instant updatedAt = Instant.parse("2024-01-02T00:00:00Z");
    Instant lastLoginAt = Instant.parse("2024-01-03T00:00:00Z");

    UserJpaEntity entity = new UserJpaEntity();
    entity.setId(userId);
    entity.setUsername("demo");
    entity.setEmail("demo@example.com");
    entity.setDisplayName("Demo");
    entity.setPasswordHash("secret");
    entity.setRole(UserRole.TEAM_MEMBER);
    entity.setStatus(UserStatus.ACTIVE);
    entity.setLastLoginAt(lastLoginAt);
    entity.setCreatedAt(createdAt);
    entity.setUpdatedAt(updatedAt);

    var domain = mapper.toDomain(entity);

    assertThat(domain.getId()).isEqualTo(userId);
    assertThat(domain.getUsername()).isEqualTo("demo");
    assertThat(domain.getEmail()).isEqualTo("demo@example.com");
    assertThat(domain.getDisplayName()).contains("Demo");
    assertThat(domain.getRole()).isEqualTo(UserRole.TEAM_MEMBER);
    assertThat(domain.getStatus()).isEqualTo(UserStatus.ACTIVE);
    assertThat(domain.getLastLoginAt()).contains(lastLoginAt);
    assertThat(domain.getCreatedAt()).isEqualTo(createdAt);
    assertThat(domain.getUpdatedAt()).isEqualTo(updatedAt);
  }
}
