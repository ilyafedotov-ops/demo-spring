package com.example.taskify.infrastructure.persistence.jpa.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.taskify.application.port.out.UserRepository;
import com.example.taskify.domain.user.User;
import com.example.taskify.domain.user.UserRole;
import com.example.taskify.domain.user.UserStatus;
import com.example.taskify.infrastructure.persistence.jpa.entity.UserJpaEntity;
import com.example.taskify.infrastructure.persistence.jpa.mapper.UserEntityMapper;
import com.example.taskify.infrastructure.persistence.jpa.repository.UserJpaRepository;
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
class UserRepositoryAdapterTest {

  @Mock private UserJpaRepository userJpaRepository;
  @Mock private UserEntityMapper userEntityMapper;

  private UserRepository adapter;

  @BeforeEach
  void setUp() {
    adapter = new UserRepositoryAdapter(userJpaRepository, userEntityMapper);
  }

  @Test
  void findByIdMapsEntityToDomain() {
    UUID userId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    UserJpaEntity entity = new UserJpaEntity();
    entity.setId(userId);
    entity.setUsername("demo");
    entity.setEmail("demo@example.com");
    entity.setDisplayName("Demo");
    entity.setRole(UserRole.TEAM_LEAD);
    entity.setStatus(UserStatus.ACTIVE);
    entity.setCreatedAt(Instant.parse("2024-01-01T00:00:00Z"));
    entity.setUpdatedAt(Instant.parse("2024-01-02T00:00:00Z"));
    User mapped =
        User.rehydrate(
            userId,
            "demo",
            "demo@example.com",
            "Demo",
            UserRole.TEAM_LEAD,
            UserStatus.ACTIVE,
            Instant.parse("2024-01-03T00:00:00Z"),
            Instant.parse("2024-01-01T00:00:00Z"),
            Instant.parse("2024-01-02T00:00:00Z"));

    when(userJpaRepository.findById(userId)).thenReturn(Optional.of(entity));
    when(userEntityMapper.toDomain(entity)).thenReturn(mapped);

    Optional<User> result = adapter.findById(userId);

    assertThat(result).contains(mapped);
    verify(userJpaRepository).findById(userId);
    verify(userEntityMapper).toDomain(entity);
  }

  @Test
  void findByIdReturnsEmptyWhenRepositoryMisses() {
    UUID userId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    when(userJpaRepository.findById(userId)).thenReturn(Optional.empty());

    Optional<User> result = adapter.findById(userId);

    assertThat(result).isEmpty();
  }

  @Test
  void findAllMapsEachEntityToDomain() {
    UserJpaEntity first = new UserJpaEntity();
    first.setId(UUID.fromString("11111111-1111-1111-1111-111111111111"));
    first.setUsername("first");
    first.setEmail("first@example.com");
    first.setRole(UserRole.TEAM_LEAD);
    first.setStatus(UserStatus.ACTIVE);
    first.setCreatedAt(Instant.parse("2024-01-01T00:00:00Z"));
    first.setUpdatedAt(Instant.parse("2024-01-02T00:00:00Z"));

    UserJpaEntity second = new UserJpaEntity();
    second.setId(UUID.fromString("22222222-2222-2222-2222-222222222222"));
    second.setUsername("second");
    second.setEmail("second@example.com");
    second.setRole(UserRole.TEAM_MEMBER);
    second.setStatus(UserStatus.INVITED);
    second.setCreatedAt(Instant.parse("2024-01-03T00:00:00Z"));
    second.setUpdatedAt(Instant.parse("2024-01-04T00:00:00Z"));

    User firstDomain =
        User.rehydrate(
            first.getId(),
            first.getUsername(),
            first.getEmail(),
            null,
            UserRole.TEAM_LEAD,
            UserStatus.ACTIVE,
            null,
            first.getCreatedAt(),
            first.getUpdatedAt());
    User secondDomain =
        User.rehydrate(
            second.getId(),
            second.getUsername(),
            second.getEmail(),
            null,
            UserRole.TEAM_MEMBER,
            UserStatus.INVITED,
            null,
            second.getCreatedAt(),
            second.getUpdatedAt());

    when(userJpaRepository.findAll()).thenReturn(List.of(first, second));
    when(userEntityMapper.toDomain(first)).thenReturn(firstDomain);
    when(userEntityMapper.toDomain(second)).thenReturn(secondDomain);

    List<User> result = adapter.findAll();

    assertThat(result).containsExactly(firstDomain, secondDomain);
  }
}
