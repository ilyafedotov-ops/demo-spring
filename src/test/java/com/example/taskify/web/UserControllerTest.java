package com.example.taskify.web;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.taskify.application.service.UserQueryService;
import com.example.taskify.config.security.SecurityConfig;
import com.example.taskify.domain.user.User;
import com.example.taskify.domain.user.UserRole;
import com.example.taskify.domain.user.UserStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, ActorResolver.class, ApiErrorHandler.class})
class UserControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private UserQueryService userQueryService;
  @MockBean private UserMapper userMapper;

  private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000111");

  @Test
  void listUsersReturnsResponses() throws Exception {
    User domain = sampleUser();
    UserResponse response = sampleResponse();

    when(userQueryService.listUsers()).thenReturn(List.of(domain));
    when(userMapper.toResponses(List.of(domain))).thenReturn(List.of(response));

    mockMvc
        .perform(authenticated(get("/api/users")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(USER_ID.toString()))
        .andExpect(jsonPath("$[0].username").value("demo"));
  }

  @Test
  void getUserReturnsResponse() throws Exception {
    User domain = sampleUser();
    UserResponse response = sampleResponse();

    when(userQueryService.getUser(USER_ID)).thenReturn(domain);
    when(userMapper.toResponse(domain)).thenReturn(response);

    mockMvc
        .perform(authenticated(get("/api/users/{userId}", USER_ID)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(USER_ID.toString()));

    verify(userQueryService).getUser(USER_ID);
  }

  @Test
  void getUserReturnsNotFoundWhenMissing() throws Exception {
    doThrow(new IllegalArgumentException("User not found")).when(userQueryService).getUser(USER_ID);

    mockMvc
        .perform(authenticated(get("/api/users/{userId}", USER_ID)))
        .andExpect(status().isNotFound());
  }

  private MockHttpServletRequestBuilder authenticated(MockHttpServletRequestBuilder builder) {
    return builder.header("X-Actor-Id", UUID.randomUUID().toString());
  }

  private User sampleUser() {
    return User.rehydrate(
        USER_ID,
        "demo",
        "demo@example.com",
        "Demo User",
        UserRole.TEAM_MEMBER,
        UserStatus.ACTIVE,
        Instant.parse("2025-01-01T00:00:00Z"),
        Instant.parse("2025-01-01T00:00:00Z"),
        Instant.parse("2025-01-02T00:00:00Z"));
  }

  private UserResponse sampleResponse() {
    return new UserResponse(
        USER_ID,
        "demo",
        "demo@example.com",
        "Demo User",
        UserRole.TEAM_MEMBER,
        UserStatus.ACTIVE,
        Instant.parse("2025-01-01T00:00:00Z"),
        Instant.parse("2025-01-01T00:00:00Z"),
        Instant.parse("2025-01-02T00:00:00Z"));
  }
}
