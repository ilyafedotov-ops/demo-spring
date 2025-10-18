package com.example.taskify.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

import com.example.taskify.application.port.out.UserRepository;
import com.example.taskify.domain.user.User;
import com.example.taskify.domain.user.UserRole;
import com.example.taskify.domain.user.UserStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserQueryServiceTest {

  @Mock private UserRepository userRepository;

  private UserQueryService service;

  @BeforeEach
  void setUp() {
    service = new UserQueryService(userRepository);
  }

  @Test
  void getUserReturnsWhenRepositoryMatches() {
    UUID userId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    User user = sampleUser(userId);
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    User result = service.getUser(userId);

    assertThat(result).isSameAs(user);
  }

  @Test
  void getUserThrowsWhenRepositoryEmpty() {
    UUID userId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> service.getUser(userId))
        .withMessage("User not found");
  }

  @Test
  void listUsersDelegatesToRepository() {
    User first = sampleUser(UUID.fromString("11111111-1111-1111-1111-111111111111"));
    User second = sampleUser(UUID.fromString("22222222-2222-2222-2222-222222222222"));
    when(userRepository.findAll()).thenReturn(List.of(first, second));

    List<User> result = service.listUsers();

    assertThat(result).containsExactly(first, second);
  }

  private static User sampleUser(UUID id) {
    Instant created = Instant.parse("2024-01-01T00:00:00Z");
    Instant updated = Instant.parse("2024-01-02T00:00:00Z");
    return User.rehydrate(
        id,
        "demo",
        "demo@example.com",
        "Demo User",
        UserRole.TEAM_LEAD,
        UserStatus.ACTIVE,
        created,
        created,
        updated);
  }
}
