package com.example.taskify.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class ActorResolverTest {

  private final ActorResolver resolver = new ActorResolver();

  @Mock private Authentication authentication;

  @Test
  void requireActorReturnsUuidWhenAuthenticated() {
    UUID actorId = UUID.fromString("00000000-0000-0000-0000-000000000001");
    when(authentication.isAuthenticated()).thenReturn(true);
    when(authentication.getName()).thenReturn(actorId.toString());

    UUID result = resolver.requireActor(authentication);

    assertThat(result).isEqualTo(actorId);
  }

  @Test
  void requireActorThrowsWhenAuthenticationMissing() {
    assertThatThrownBy(() -> resolver.requireActor(null))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            ex ->
                assertThat(((ResponseStatusException) ex).getStatusCode())
                    .isEqualTo(HttpStatus.UNAUTHORIZED));
  }

  @Test
  void requireActorThrowsWhenNameNotUuid() {
    when(authentication.isAuthenticated()).thenReturn(true);
    when(authentication.getName()).thenReturn("not-a-uuid");

    assertThatThrownBy(() -> resolver.requireActor(authentication))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            ex ->
                assertThat(((ResponseStatusException) ex).getStatusCode())
                    .isEqualTo(HttpStatus.UNAUTHORIZED));
  }
}
