package com.example.taskify.domain.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UserTest {
  private static final UUID USER_ID = UUID.fromString("aaaaaaaa-1111-2222-3333-bbbbbbbbbbbb");
  private static final Instant NOW = Instant.parse("2025-01-01T00:00:00Z");

  @Test
  void registerInitialisesDefaults() {
    User user =
        User.register(USER_ID, "  jdoe ", "jdoe@example.com", UserRole.TEAM_MEMBER, () -> NOW);

    assertThat(user.getId()).isEqualTo(USER_ID);
    assertThat(user.getUsername()).isEqualTo("jdoe");
    assertThat(user.getEmail()).isEqualTo("jdoe@example.com");
    assertThat(user.getRole()).isEqualTo(UserRole.TEAM_MEMBER);
    assertThat(user.getStatus()).isEqualTo(UserStatus.INVITED);
    assertThat(user.getCreatedAt()).isEqualTo(NOW);
  }

  @Test
  void changeRoleUpdatesRoleAndTimestamp() {
    User user = User.register(USER_ID, "jdoe", "jdoe@example.com", UserRole.TEAM_MEMBER, () -> NOW);
    Instant later = NOW.plusSeconds(30);

    user.changeRole(UserRole.TEAM_LEAD, () -> later);

    assertThat(user.getRole()).isEqualTo(UserRole.TEAM_LEAD);
    assertThat(user.getUpdatedAt()).isEqualTo(later);
  }

  @Test
  void invalidEmailRejected() {
    assertThatThrownBy(
            () -> User.register(USER_ID, "jdoe", "invalid-email", UserRole.TEAM_MEMBER, () -> NOW))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Invalid email address");
  }

  @Test
  void activateTransitionsToActiveAndIsIdempotent() {
    User user = User.register(USER_ID, "jdoe", "jdoe@example.com", UserRole.TEAM_MEMBER, () -> NOW);
    Instant activatedAt = NOW.plusSeconds(10);

    user.activate(() -> activatedAt);
    user.activate(() -> activatedAt.plusSeconds(5));

    assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
    assertThat(user.getUpdatedAt()).isEqualTo(activatedAt);
  }

  @Test
  void updateProfileTrimsValuesAndUpdatesTimestamp() {
    User user = User.register(USER_ID, "jdoe", "jdoe@example.com", UserRole.TEAM_MEMBER, () -> NOW);
    Instant later = NOW.plusSeconds(20);

    user.updateProfile("  newname  ", "Display", () -> later);

    assertThat(user.getUsername()).isEqualTo("newname");
    assertThat(user.getDisplayName()).contains("Display");
    assertThat(user.getUpdatedAt()).isEqualTo(later);
  }

  @Test
  void recordSuccessfulLoginUpdatesAuditFields() {
    User user = User.register(USER_ID, "jdoe", "jdoe@example.com", UserRole.TEAM_MEMBER, () -> NOW);
    Instant loginAt = NOW.plusSeconds(30);

    user.recordSuccessfulLogin(() -> loginAt);

    assertThat(user.getLastLoginAt()).contains(loginAt);
    assertThat(user.getUpdatedAt()).isEqualTo(loginAt);
  }

  @Test
  void lockAndDisableUpdateStatus() {
    User user = User.register(USER_ID, "jdoe", "jdoe@example.com", UserRole.TEAM_MEMBER, () -> NOW);
    Instant later = NOW.plusSeconds(40);

    user.lock(() -> later);
    assertThat(user.getStatus()).isEqualTo(UserStatus.LOCKED);

    Instant disabledAt = NOW.plusSeconds(60);
    user.disable(() -> disabledAt);
    assertThat(user.getStatus()).isEqualTo(UserStatus.DISABLED);
    assertThat(user.getUpdatedAt()).isEqualTo(disabledAt);
  }
}
