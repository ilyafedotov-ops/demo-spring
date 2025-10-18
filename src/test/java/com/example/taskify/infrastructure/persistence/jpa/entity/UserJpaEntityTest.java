package com.example.taskify.infrastructure.persistence.jpa.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.taskify.domain.user.UserRole;
import com.example.taskify.domain.user.UserStatus;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UserJpaEntityTest {

  @Test
  void gettersAndSettersExposeJpaState() {
    UUID userId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    Instant createdAt = Instant.parse("2024-01-01T00:00:00Z");
    Instant updatedAt = Instant.parse("2024-02-01T00:00:00Z");
    UUID createdBy = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    UUID updatedBy = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");

    UserJpaEntity entity = new UserJpaEntity();
    entity.setId(userId);
    entity.setUsername("demo");
    entity.setEmail("demo@example.com");
    entity.setPasswordHash("secret");
    entity.setDisplayName("Demo User");
    entity.setRole(UserRole.TEAM_LEAD);
    entity.setStatus(UserStatus.ACTIVE);
    entity.setLastLoginAt(Instant.parse("2024-01-15T00:00:00Z"));
    entity.setCreatedAt(createdAt);
    entity.setCreatedBy(createdBy);
    entity.setUpdatedAt(updatedAt);
    entity.setUpdatedBy(updatedBy);

    assertThat(entity.getId()).isEqualTo(userId);
    assertThat(entity.getUsername()).isEqualTo("demo");
    assertThat(entity.getEmail()).isEqualTo("demo@example.com");
    assertThat(entity.getPasswordHash()).isEqualTo("secret");
    assertThat(entity.getDisplayName()).isEqualTo("Demo User");
    assertThat(entity.getRole()).isEqualTo(UserRole.TEAM_LEAD);
    assertThat(entity.getStatus()).isEqualTo(UserStatus.ACTIVE);
    assertThat(entity.getLastLoginAt()).isEqualTo(Instant.parse("2024-01-15T00:00:00Z"));
    assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
    assertThat(entity.getCreatedBy()).isEqualTo(createdBy);
    assertThat(entity.getUpdatedAt()).isEqualTo(updatedAt);
    assertThat(entity.getUpdatedBy()).isEqualTo(updatedBy);
  }
}
