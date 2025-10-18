package com.example.taskify.config.audit;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class SecurityAuditorAwareTest {

  private final SecurityAuditorAware auditorAware = new SecurityAuditorAware();

  @AfterEach
  void clearContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void returnsEmptyWhenAuthenticationMissing() {
    Optional<UUID> result = auditorAware.getCurrentAuditor();

    assertThat(result).isEmpty();
  }

  @Test
  void returnsEmptyWhenNotAuthenticated() {
    TestingAuthenticationToken authentication =
        new TestingAuthenticationToken("ignored", "credentials");
    authentication.setAuthenticated(false);
    SecurityContextHolder.getContext().setAuthentication(authentication);

    Optional<UUID> result = auditorAware.getCurrentAuditor();

    assertThat(result).isEmpty();
  }

  @Test
  void returnsEmptyWhenPrincipalNotUuid() {
    TestingAuthenticationToken authentication =
        new TestingAuthenticationToken("not-a-uuid", "credentials");
    SecurityContextHolder.getContext().setAuthentication(authentication);

    Optional<UUID> result = auditorAware.getCurrentAuditor();

    assertThat(result).isEmpty();
  }

  @Test
  void returnsUuidWhenAuthenticationValid() {
    UUID actorId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    TestingAuthenticationToken authentication =
        new TestingAuthenticationToken(actorId.toString(), "credentials");
    authentication.setAuthenticated(true);
    SecurityContextHolder.getContext().setAuthentication(authentication);

    Optional<UUID> result = auditorAware.getCurrentAuditor();

    assertThat(result).contains(actorId);
  }
}
