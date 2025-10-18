package com.example.taskify.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.taskify.domain.user.User;
import com.example.taskify.domain.user.UserRole;
import com.example.taskify.domain.user.UserStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class UserMapperTest {

  private final UserMapper mapper = Mappers.getMapper(UserMapper.class);

  @Test
  void toResponseFlattensOptionals() {
    User user =
        User.rehydrate(
            UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
            "demo",
            "demo@example.com",
            "Demo User",
            UserRole.TEAM_MEMBER,
            UserStatus.ACTIVE,
            Instant.parse("2024-01-03T00:00:00Z"),
            Instant.parse("2024-01-01T00:00:00Z"),
            Instant.parse("2024-01-02T00:00:00Z"));

    UserResponse response = mapper.toResponse(user);

    assertThat(response.displayName()).isEqualTo("Demo User");
    assertThat(response.lastLoginAt()).isEqualTo(Instant.parse("2024-01-03T00:00:00Z"));
  }

  @Test
  void toResponsesConvertsCollection() {
    User user =
        User.rehydrate(
            UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"),
            "other",
            "other@example.com",
            null,
            UserRole.TEAM_LEAD,
            UserStatus.INVITED,
            null,
            Instant.parse("2024-02-01T00:00:00Z"),
            Instant.parse("2024-02-02T00:00:00Z"));

    List<UserResponse> responses = mapper.toResponses(List.of(user));

    assertThat(responses).hasSize(1);
    assertThat(responses.get(0).displayName()).isNull();
  }
}
